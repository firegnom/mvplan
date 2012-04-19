/*
 * Profile.java
 *
 * Conducts dive based on inputSegments, knownGases, and an existing model.
 * Iterates through dive segments updating the ZHL16B. When all dive segments are
 * processed then calls ascend(0.0) to return to the surface.
 *
 * The ZHL16B can be either null in which case a new model is created, or can be
 * an existing model with tissue loadings.
 *
 * Gas switching is done on the final ascent if OC deco or bailout is specified. See setDecoGas()
 *
 * Outputs profile to an ArrayList of dive segments - outputSegments. 
 *
 * @author Guy Wittig
 * @version 4 March 2005, 08:57
 *
 *   This program is part of MV-Plan
 *   Copywrite 2006 Guy Wittig
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   The GNU General Public License can be read at http://www.gnu.org/licenses/licenses.html
 */

package mvplan.dive;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import mvplan.gas.Gas;
import mvplan.main.MvplanInstance;
import mvplan.model.*;
import mvplan.prefs.Prefs;
import mvplan.segments.SegmentAscDec;
import mvplan.segments.SegmentDive;
import mvplan.segments.SegmentDeco;
import mvplan.segments.SegmentAbstract;

// TODO -   Force all stops may generate excursions that are not possible. Should be removed or
//          more complex ascent process used.

public class Profile
{
    private final int debug = MvplanInstance.getMvplan().getDebug();
    private Prefs prefs = MvplanInstance.getMvplan().getPrefs();
    
    private ArrayList<SegmentAbstract> inputSegments;    // Stores input dive segment objects passed from GUI and enabled
    private ArrayList<SegmentAbstract> outputSegments;   // Stores output segments produced by theis class
    private ArrayList<Gas> gases;                        // Stores dive gas objects passed from GUI and enabled
    private int currentGasIndex;                         // Points to current gas
    private double currentDepth;    // Currend dive depth (msw/fsw)        
    //private double pAmb;            // Ambient pressure (msw/fsw)
    //private double fHe,fN2,fO2;     // Current gas fractions
    private Gas currentGas;         // Current gas
    private AbstractModel model;            // Buhlmann model for this dive
    private double runTime;         // Runtime
    private double ppO2;            // CCR ppO2 or zero for OC
    private boolean closedCircuit;  // Flag to store closed circuit / open circuit state
    private boolean inFinalAscent;  // Flag for final ascent
    private boolean isRepetativeDive;   // For repetative dives
    private int surfaceInterval;        // For surface interval
    private String metaData;            // Description of the dive for model persistance
    
    // Return flags
    public static final int SUCCESS=0;      
    public static final int CEILING_VIOLATION=1;
    public static final int NOTHING_TO_PROCESS=2;
    public static final int PROCESSING_ERROR=3;
    public static final int INFINITE_DECO=4;

    /** Constructor for objects of class Profile */
    public Profile(ArrayList <SegmentAbstract> knownSegments, ArrayList <Gas> knownGases, AbstractModel m)
    {
        Gas g;                  // Used for constructing arraylists of gases and segments   
        SegmentAbstract s;      // as above
        Class<? extends AbstractModel> modelClass;



        inputSegments = new ArrayList <SegmentAbstract>();
        outputSegments = new ArrayList<SegmentAbstract>();
        gases = new ArrayList<Gas>();
        // Is this a new model or a repetative dive ?
        if (m==null) {  
            // Initialise new model.
            isRepetativeDive=false;
            // TODO Define which concrete model to use
             try {                
                if (debug>0) System.out.println("Loading model class:"+prefs.getModelClass());
                modelClass = Class.forName(prefs.getModelClass()).asSubclass(AbstractModel.class);
                model = modelClass.newInstance();
            } catch (Exception ex) {
                model = new ZHL16B();
                if (debug>0) System.out.println("Model instantiation exception for: "+ex.getMessage());
            }

            //model = new ZHL16B();
            model.initModel();
            metaData="";
        } else {
            // ZHL16B exists
            model=m; 
            // TODO - Resources
            metaData = model.getMetaData()+" *PLUS* ";
            isRepetativeDive=true;
            // Reset the Gradient Factors
            model.initGradient();             
        }

        // Construct list of dive segments from known segments
        Iterator<SegmentAbstract> is=knownSegments.iterator();
        while(is.hasNext()) {
            s=is.next();
            // Add enabled dive segments only to input segments
            if (s.getEnable().booleanValue()==true) 
                inputSegments.add(s);
        }
        // construct list of dive gases from known gases
        Iterator<Gas> ig=knownGases.iterator();
        while(ig.hasNext()) {
            g=ig.next();
            g.setVolume(0.0);   // Reset the gas volume to zero
            if (g.getEnable()==true) {
                // Add enabled gases only to gas arraylist
                if(debug >1) System.out.println("Adding gas "+g);
                gases.add(g);            
            }
        }
    }
    
    // Accessor methods for profile 
    public ArrayList<SegmentAbstract> getProfile()   { return outputSegments; }
    public ArrayList<Gas> getGases()     { return gases; }
    public AbstractModel getModel()         { return model; }
    public boolean getIsRepetitiveDive()    {return isRepetativeDive;}
    public int getSurfaceInterval()         { return surfaceInterval; }

    /* 
     * Surface Interval
     * Conducts a surface interval by performing a constant depth calculation on air at zero meters.
     *
     */
    public int doSurfaceInterval(int time)
    {
        try {
            model.constDepth(0.0,time,0.0,0.79,0.0);      // Do constant depth of zero on air
        } catch (ModelStateException e) {
            return PROCESSING_ERROR;
        }
        // Note that pAmb is applied in the model so changes in altitude will be handled
        // TODO - this holding and passing back of the SI is not very elegant
        surfaceInterval=time;
        return SUCCESS;
    }
    
    /** isDiveSegments(): Returns true if there are loaded dive segments, else false means there is nothing to process
     *  @return true if there are dive segments to process
     */
    public boolean isDiveSegments(){
        if (inputSegments.size() == 0) return false;
        return true;
    }
    
    /** Process the dive */
    public int doDive()
    {
        int returnCode;
        SegmentAbstract s;
        SegmentDive sd;
        double t;
        double deltaDepth;
        boolean runtimeFlag=prefs.getRuntimeFlag();      // Used to decide if segment represents runtime or segtime
    
        // Check that there are segments to process
        if (!isDiveSegments()) 
            return NOTHING_TO_PROCESS;
        
        // Set initial state
        s = (SegmentAbstract)inputSegments.get(0);  // Set the first segment and set initial gas from it
        currentGas = s.getGas();
        Collections.sort(gases);    // Sort gases based on MOD, the Gas natural order
        currentDepth=0.0;                               
        ppO2=s.getSetpoint();       // Set initial ppO2 based on first segment
        //Determine if we are Open or Closed circuit
        if (ppO2==0.0)
            closedCircuit=false;
        else
            closedCircuit=true;
        inFinalAscent=false;    // Flag used to work out when all segments are complete and are in flnal ascent  
        
        // Process segment list of user defined segments through model
        Iterator<SegmentAbstract> it=inputSegments.iterator();        
        while(it.hasNext()) {
            s=(SegmentAbstract)it.next();   // Get segment
            if (debug >1) System.out.println("Processing: "+s);
            if(s.getType() == SegmentAbstract.CONST) {      // Should be constant depth segments only
                sd=(SegmentDive)s;      
                deltaDepth=sd.getDepth()-currentDepth;  // Has depth changed ?
                // Ascend or descend to dive segment, using existing gas and ppO2 settings
                if(deltaDepth>0.0) {  // Segment causes a descent
                    try {
                        model.ascDec(currentDepth,sd.getDepth(),prefs.getDescentRate(),currentGas.getFHe(),currentGas.getFN2(),ppO2);
                    } catch (ModelStateException e) { 
                        return PROCESSING_ERROR;  
                    }
                    // Add segment to output segments
                    outputSegments.add(new SegmentAscDec(currentDepth,sd.getDepth(),prefs.getDescentRate(),currentGas,ppO2));
                    runTime+=deltaDepth/prefs.getDescentRate();

                } else if (deltaDepth < 0.0) { // Segment causes an ascent. 
                    // Call ascend() to process this as it can require decompression
                    ascend( sd.getDepth());
                } 
                                                                
                // Now at desired depth so process dive segments.                 
                currentDepth=sd.getDepth();     // Reset current depth
                ppO2=sd.getSetpoint();          // Set ppO2
                currentGas=s.getGas();          // Set gas used
                // Process segment. 
                if (sd.getTime() > 0) { // Only do this if it is not a waypoint.
                    // Interpret first segment time as runtime or segment time depending on runtimeFlag
                    if (runtimeFlag) {
                        runtimeFlag=false;  // Do this once only. Make segment == runtime
                        try {
                            model.constDepth(sd.getDepth(),sd.getTime()-runTime,currentGas.getFHe(),currentGas.getFN2(),ppO2);
                        } catch (ModelStateException e){ 
                            return PROCESSING_ERROR;  
                        }
                        // Add segment to output segments
                        outputSegments.add(new SegmentDive(sd.getDepth(),sd.getTime()-runTime,currentGas,ppO2));
                        runTime=sd.getTime();   // Reset runTime to segment end time    
                        // Update metadata
                        // TODO - resources
                        metaData=metaData+"Dive to "+sd.getDepth()+" for "+sd.getTime();
                    } else {    // Segtime is segtime
                        try {
                            model.constDepth(sd.getDepth(),sd.getTime(),currentGas.getFHe(),currentGas.getFN2(),ppO2);
                        } catch (ModelStateException e) { 
                            return PROCESSING_ERROR; 
                        }
                        // Add segment to output segments
                        outputSegments.add(new SegmentDive(sd.getDepth(),sd.getTime(),currentGas,ppO2));
                        runTime+=sd.getTime();  // update runtime
                    }
                } else {  // Process waypoint
                    // Add waypoint to output segments
                    outputSegments.add(new SegmentDive(sd.getDepth(),sd.getTime(),currentGas,ppO2));
                }
            }
        }

        // Processed all specified segments, now get back to surface
        inFinalAscent=true; // Enables automatic gas selection in ascend() method
        // Call ascend to move to the surface
        returnCode=ascend(0.0);
        // Was there an error ?
        if(returnCode != SUCCESS)
            return returnCode;
        // Calculate runtimes and update the segments
        Iterator<SegmentAbstract> it2=outputSegments.iterator();
        t=0;
        while(it2.hasNext()) {
            s=(SegmentAbstract)it2.next();      
            t+=s.getTime();          // Set segment runtime
            s.setRunTime(t);
        }
        // Write metadata into the model
        model.setMetaData(metaData);    
        return SUCCESS;
    }


    /** 
     * Ascend to target depth, decompressing if necessary. 
     * If inFinalAscent then gradient factors start changing, and automatic gas selection is made. 
     */
    public int ascend(double target)
    {
        //boolean surfacing=false;      // Flag to indicate we are headed for final deco to surface
        //boolean openCircuit=true;     // Flag to indicate that we are OC or bailing out
        boolean inDecoCycle=false;      // Flag to track if we are in a deco cycle
        boolean inAscentCycle=false;    // Flag to track if we are in a free ascent cycle as opposed to a deco cycle
        boolean forceDecoStop=false;    // Flag for forcing every deco stop. // TODO - ALWAYS TRUE IN LOGIC
        double stopTime;                // Used for deco stop time
        double decoStopTime=0;          // Accumulates deco stop time
        double startDepth;              // Holds depth at start of ascent segment
        double maxMV=0;                 // Holds maximum mvgradient at each stop
        double nextStopDepth;           // Next proposed stop depth
        int control=0;                  // Stores controlling compartment at new depth
        //double ceiling;                 // Used to store ceiling
        Gas tempGas;        
        SegmentDeco decoSegment;        // Use for adding deco segments
        
        if (debug > 1) System.out.println("\nASCEND: started ascent to: "+target);
        if (debug > 1) System.out.println("RT: "+runTime+" ppO2: "+ppO2);        
        /*
         * Set up some initial stuff:
         *      Are we surfacing
         *      Are we open circuit deco == bailing out     
         */
            
        if(inFinalAscent && (prefs.getOcDeco()))  {      // Switch to Open circuit deco  
            currentGasIndex=-1;
            setDecoGas(currentDepth);   // Or pick a better gas. Also sets OC mode
        }
        
        if(currentDepth < target)               // Going backwards !
            return PROCESSING_ERROR;
        
        // Set initial stop to be the next integral stop depth
        if ((currentDepth%(int)prefs.getStopDepthIncrement()) > 0)   // Are we on a stop depth already ?
            // If not, go to next stop depth
            nextStopDepth= (int)(currentDepth /  prefs.getStopDepthIncrement()) * (int)prefs.getStopDepthIncrement();
        else
            nextStopDepth = (int)(currentDepth - prefs.getStopDepthIncrement());           
           
        // Check in case we are overshooting or hit last stop or any of the other bizzar combinations ...
        if((nextStopDepth < target) || (currentDepth < prefs.getLastStopDepth()))
            nextStopDepth=target;
        else if(currentDepth==prefs.getLastStopDepth()) 
            nextStopDepth=target;
        else if(nextStopDepth < prefs.getLastStopDepth()) 
            nextStopDepth=prefs.getLastStopDepth();
                
        startDepth=currentDepth;    // Initialise ascent segment start depth
        inAscentCycle=true;         // Start in free ascent
        
        // Initialise gradient factor for next (in this case first) stop depth
        model.getGradient().setGfAtDepth(nextStopDepth);

        // Remember maxM-Value and controlling compartment
        maxMV=model.mValue(currentDepth);
        control = model.controlCompartment();
        
        if (debug > 1) System.out.println("Initial stop depth: "+nextStopDepth);        
        if (debug > 1) System.out.println(" ... ceiling is now:"+model.ceiling());
        if (debug > 1) System.out.println(" ... set m-value gradient for: "+nextStopDepth);
        
        while (currentDepth > target) {              
            // Can we move to the proposed next stop depth ?
            while (forceDecoStop || (nextStopDepth < model.ceiling())) {
                // Need to decompress .... enter decompression loop
                if (debug > 1) System.out.println(" ... entering decompression loop ...");
                inDecoCycle=true;
                forceDecoStop=false;    // Only used for first entry into deco stop
                if(inAscentCycle) {     // Finalise last ascent cycle as we are now decompressing
                    if(startDepth > currentDepth)   // Did we ascend at all ?
                        // Add ascent segment 
                        outputSegments.add(new SegmentAscDec(startDepth,currentDepth,prefs.getAscentRate(),currentGas,ppO2));  
                    inAscentCycle=false;
                    // TODO - start depth is not re-initialised after first use
                }

                // set m-value gradient under the following conditions:
                //      if not in multilevel mode, then set it as soon as we do a decompression cycle
                //      otherwise wait until we are finally surfacing before setting it
                if ((!prefs.getGfMultilevelMode() || inFinalAscent) && !model.getGradient().isGfSet()) { 
                    model.getGradient().setGfSlopeAtDepth(currentDepth);
                    if (debug > 1) System.out.println(" ... m-Value gradient slope set at: "+currentDepth+" GF is:"+ model.getGradient().getGf());                      
                    model.getGradient().setGfAtDepth(nextStopDepth);
                    if (debug > 1) System.out.println(" ... set m-value gradient for: "+nextStopDepth+" to:"+model.getGradient().getGf());                                        
                }
                
                // Round up runtime to integral number of minutes - only first time through on this cycle
                if( decoStopTime==0 && (runTime%prefs.getStopTimeIncrement() > 0))  // Is this not an integral time
                    stopTime=(int)(runTime/prefs.getStopTimeIncrement())*prefs.getStopTimeIncrement() +
                            prefs.getStopTimeIncrement()-runTime;
                else 
                    stopTime=prefs.getStopTimeIncrement();
                
                // Sanity check the rounding
                if(stopTime==0) stopTime=prefs.getStopTimeIncrement();
                
                if (debug > 1) System.out.println(" ... decompressing at depth: "+
                        currentDepth+" for next depth :"+nextStopDepth+" Stop: "+stopTime+" ppO2: "+ppO2);   
                // Execute stop
                try {
                    model.constDepth(currentDepth,stopTime,currentGas.getFHe(),currentGas.getFN2(),ppO2);
                } catch (ModelStateException e) { return PROCESSING_ERROR; }
                decoStopTime+=stopTime;                
                if (debug > 1) System.out.println(" ... ceiling is now:"+model.ceiling());
                // Sanity check decoStopTime for infinite loop
                if(decoStopTime > 5000) {
                    if (debug > 0) System.err.println("Infinite loop on deco stop at "+currentDepth);
                    return INFINITE_DECO;
                }
            } 
            // Finished decompression loop 
            if(inDecoCycle) {
                // Finalise last deco cycle ...
                runTime+=decoStopTime;                
                if(prefs.getForceAllStops())     // Reset for next depth
                    forceDecoStop=true;                 // ALWAYS TRUE AT THIS POINT
                    
                // write deco segment
                decoSegment = new SegmentDeco(currentDepth,decoStopTime,currentGas,ppO2);
                decoSegment.setMvMax(maxMV);
                decoSegment.setGfUsed(model.getGradient().getGf());                
                decoSegment.setControlCompartment(control);
                outputSegments.add(decoSegment);
                if (debug > 1) System.out.println(decoSegment);
                inDecoCycle=false;    
                decoStopTime=0;            
            } else if (inAscentCycle) {  
                // Did not decompress, just ascend
                // TODO - if we enable this code always (remove else if) then model will ascend between deco stops, but ... this causes collateral damage to runtim calculations
                try {
                    model.ascDec(currentDepth,(double)nextStopDepth,prefs.getAscentRate(),currentGas.getFHe(),currentGas.getFN2(),ppO2);
                } catch (ModelStateException e) { return PROCESSING_ERROR; }
                runTime+=(currentDepth-nextStopDepth)/(-1.0*prefs.getAscentRate());
                // TODO - Issue here is that this ascent time is not accounted for in any segments unless it was in an ascent cycle            
            } 
            
            // Moved up to next depth ...
            if (debug > 1) System.out.println("Now at next stop depth: "+nextStopDepth+" runtime: "+runTime);     

            currentDepth=nextStopDepth;
            maxMV=model.mValue(currentDepth);
            control = model.controlCompartment();
  
            // Check and switch deco gases
            tempGas=currentGas;                     // Remember this in case we switch
            if (setDecoGas(currentDepth) == true) { // If true we have changed gases
                if (inAscentCycle) {                // To switch gases during ascent need to force a waypoint
                    if(debug > 1) ;
                    if (debug > 1) System.out.println(" ... forcing waypoint for gas switch");
                    outputSegments.add(new SegmentAscDec(startDepth,currentDepth,prefs.getAscentRate(),tempGas,ppO2));  
                    startDepth=currentDepth;
                }
            }
            
            // Set next rounded stop depth
            nextStopDepth = (int)currentDepth - (int)prefs.getStopDepthIncrement(); 
            
            // Check in case we are overshooting or hit last stop
            if((nextStopDepth < target) || (currentDepth < prefs.getLastStopDepth()))
                nextStopDepth=target;
            else if(currentDepth==prefs.getLastStopDepth()) 
                nextStopDepth=target;
            else if(nextStopDepth < prefs.getLastStopDepth())
                nextStopDepth=prefs.getLastStopDepth();
            
            if(model.getGradient().isGfSet()) {   // Update GF for next stop                         
                model.getGradient().setGfAtDepth(nextStopDepth);
                if (debug > 1) System.out.println(" ... set m-value gradient for: "+nextStopDepth+" to:"+model.getGradient().getGf());                 
            }  
        }  
        // Are we still in an ascent segment ?
        if (inAscentCycle) {
            outputSegments.add(new SegmentAscDec(startDepth,currentDepth,prefs.getAscentRate(),currentGas,ppO2));                  
        }
        if(debug > 1) model.printModel();        
        return SUCCESS;        
    } // ascend

    // Estimate gas consumption for all output segments and set this into the respective gas objects
    public void doGasCalcs()
    {
        SegmentAbstract s;
        Iterator<SegmentAbstract> it=outputSegments.iterator();
        
        while(it.hasNext()) {
            s=(SegmentAbstract)it.next();
            s.getGas().setVolume( s.getGas().getVolume()+s.gasUsed());
            if(debug > 1) System.out.println(" ... gas used: "+s.getGas()+" - "+(int)s.gasUsed()+" ("+(int)s.getGas().getVolume()+")");
        }     
    }
    
    /**
     * Select appropriate deco gas for the depth specified
     * Returns true if a gas switch occured
    */
    private boolean setDecoGas(double depth) 
    {
        Gas g;
        boolean finished=false;
        boolean gasSwitch=false;

        if(debug > 1) System.out.println("Evaluating deco gas at "+depth); 
        // Check to see if we should be changing gases at all ... if so just return doing nothing
        if(!inFinalAscent)               return false;   // Not ascending yet so no gas switching
        if (!prefs.getOcDeco())   return false;   // No OC deco so no bailout
        if(gases.size() ==0 )            return false ;  // No gases to change to
        
        // If this is the first time that this method is called we need to change to Open Circuit bailout
        if (closedCircuit) {
            closedCircuit=false;
            ppO2=0.0;
            currentGas= (Gas)gases.get(0);  // Select the first gas in the list based on MOD
            currentGasIndex=0;     
         }
         
        // Check and switch deco gases
        while(!finished & (currentGasIndex+1<gases.size())) {        // Is there another gas to switch to anyway ?
            g=(Gas)gases.get(currentGasIndex+1);    // Look at next gas then
            if (g.getMod() >= depth) {              // Check MOD, Can move to this gas ?
                currentGasIndex+=1;                 // Yes !
                currentGas=g;  
                gasSwitch=true;
                if(debug > 1) System.out.println(" ... changing gas to "+currentGas);                    
            } else
                finished=true;     // Look no more
        }                    
        return gasSwitch;
    }
}



