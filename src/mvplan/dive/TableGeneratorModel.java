/*
 * TableGeneratorModel.java
 *
 *  Class for creation and management of multi-profile dives. 
 *  Takes knownSegments, knownGases and an array of dive time modifiers. Creates
 *  a series of dives and executes them, storing the results in a 2D datastructure.
 *
 *  Time modifiers change the segment time of the "controlling segment". (Nothing to do with deco controlling segments).
 *
  * @author Guy Wittig
 *  @version 04-Mar-2005
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

import mvplan.main.*;
import mvplan.segments.SegmentAbstract;
import mvplan.gas.Gas;

import java.util.ArrayList;
import java.util.Iterator;


public class TableGeneratorModel {
    Profile [] multiProfile;        // Holds collection of dive profiles created
    private ArrayList<SegmentAbstract> knownSegments;   // Input dive segments and gases
    private ArrayList<Gas> knownGases;
    private int[] modifiers;        // Holds the segment time modifiers. These alter the controlling segment
    private double maxCNS;          // Records maximum oxygen numbers only
    private double maxPO2;
    private int controlSegmentIndex;     // Points to controlling segment (or -1 == none)        
    
    private SegmentAbstract [][] segmentArray;    // Will store results
    private int numProfiles;                      // Number of profiles
    private int longestProfile;                   // Index of longest profile
    private int rows;                             // Number of rows (== segments) in longest profile
    private int ascentRow;                        // Row (== segment) of the start of ascent
    
    /**
     * TableGeneratorModel() - Creates a new instance of TableGeneratorModel 
     *                 Initialises by cloning known segments and gases from those passed
     * @param s Arrayist of known segments
     * @param g ArrayList of known gases
     * @param modifiers int[] of profile time modifiers
     */
    public TableGeneratorModel(ArrayList s, ArrayList g, int[] modifiers) {
        // Create new clean ArrayLists
        knownSegments = new ArrayList();
        knownGases = new ArrayList();
        int i;      
        
        // Initialise modifiers - gets REFERENCE to array
        this.modifiers= modifiers;
        
        // Copy and clean known segments and gases
        Iterator is=s.iterator();
        while(is.hasNext()){
            SegmentAbstract segment = (SegmentAbstract)is.next();
            if(segment.getEnable().booleanValue()==true)                // Only copy enabled segments
                knownSegments.add((SegmentAbstract)segment.clone());    // Clone them to avoid conflicts
        }
        Iterator ig=g.iterator();
        while(ig.hasNext()){
            Gas gas = (Gas)ig.next();
            if(gas.getEnable() == true)             // Only copy enabled gases
                knownGases.add((Gas)gas.clone());   // Clone them to avoid conflicts
        }   
        
        // Determine proposed control segment (-1 == none)
        controlSegmentIndex=-1;
        if (knownSegments.size()>0) {
              // Iterate BACK through dive segments and return last one that is NOT a waypoint
            for (i=knownSegments.size()-1; i>=0; i--){
                if(((SegmentAbstract)knownSegments.get(i)).getTime() > 0.0f ) {
                    controlSegmentIndex = i;
                    break;
                }
            }           
        }                                  
    }
    
    /**
     * getModel - gets the dive model used
     */
    public String getModelName() {
        if(multiProfile.length>0)
            return multiProfile[0].getModel().getModelName();
        else
            return Mvplan.prefs.getModelClassName();
    }


    /** getSegmentArray - gets the 2D array of output segments
     *  @return SegmentAbstract [][] segmentArray
     */
    public SegmentAbstract [][] getSegmentArray(){
        return segmentArray;        
    }
    
    /** getAscentRow - gets row (or  Segment) that is the first of the ascent segments
     *  @return int AscentRow
     */
    public int getAscentRow() {
        return ascentRow;
    }
    
    /** getNumProfiles - gets number of dive profiles in SegmentArray
     *  @return int numProfiles
     */
    public int getNumProfiles() {
        return numProfiles;        
    }
    
    /** getNumSegments - gets number of dive segments in SegmentArray
     *  @return int numSegments
     */
    public int getNumSegments() {
        return rows;
    }
    
    /** getLongestProfile - gets the longest/deepest/most complete profile so as to determine depth
     *  @return int longestProfile
     */
    public int getLongestprofile() {
        return longestProfile;
    }
    
    /**
     * double getMaxCNS() - returns maximum CNS encountered on these profiles
     * @return maxCNS
     */
    public double getMaxCNS() {
        return maxCNS;        
    }
    
    /** 
     * double getMaxPpO2() - returns maximum ppO2 encountered on these profiles
     * @return maxPpO2
     */
    public double getMaxPO2() {
        return maxPO2;
    }
    
     /**     
     * Gets the controlling segment, i.e. the segment that the modifiers will be used on.
     * @return Controlling Segment or -1 of none are known
     */
    public int getControlSegmentIndex() {
        return controlSegmentIndex;
    }
     /**     
     * Gets the controlling segment, i.e. the segment that the modifiers will be used on.
     * @return Controlling Segment or -null of none are known
     */    
    public Object getControlSegment() {
        if(controlSegmentIndex>=0) 
            return knownSegments.get(controlSegmentIndex);
        else
            return null;
    }
    
    /**
     * Sets controlSegmentIndex. May be used in the future if the GUI modifies it.
     * @param i int i - controlling segment to which to apply modifiers
     */
    public void setControlSegmentIndex(int i){
        if(i>=0 & i<=knownSegments.size())
            controlSegmentIndex=i;
    }
    
    /**
     * Sets controlSegment. May be used in the future if the GUI modifies it.
     * @param i int i - controlling segment to which to apply modifiers
     */
    public void setControlSegmentIndex(Object obj){
        controlSegmentIndex = knownSegments.indexOf(obj);
    }
        
    /**
     * void doMultiDiveProfile() - Execute the multi-dive profile.
     * @return returnCodes as defined in Profile()
     */
    public int doMultiDive() {
        ArrayList a;        // Working arraylist
        SegmentAbstract s;  // Working segment

        int i,j;            // Counters
        int returnCode;     // Saves return code from dive profile

        // If there is no controlling segment then can't proceed.
        if(controlSegmentIndex <0)   return Profile.NOTHING_TO_PROCESS;
                
        // Determine how many profiles to create        
        numProfiles=modifiers.length;
        // Potentially shorten if  zero modifiers passed
        for (i=1;i<modifiers.length;i++) {
            if(modifiers[i]==0) {
                numProfiles=i;
                break;
            }
        }
        
        if(Mvplan.DEBUG>0) System.out.println("Creating "+numProfiles+" profiles.");
            
        // Create profile array
        multiProfile = new Profile [numProfiles];
               
        // Initialise maximum Oxygen numbers
        maxPO2=0.0;
        maxCNS=0.0;
        
        // Process the  profiles 
        for(i=0;i<=numProfiles-1;i++) {     // For each profile ...
            // Clone from knownSegments into a new ArrayList
            a=new ArrayList();            
            Iterator is=knownSegments.iterator();
            while(is.hasNext()) {
                s=(SegmentAbstract)is.next();
                a.add(s.clone());   // Need to clone s !
            }
            // Adjust LAST segment for modified time
            s = (SegmentAbstract)a.get(controlSegmentIndex);
            double time = s.getTime();
            time = time+modifiers[i];
            s.setTime(time);
            a.set(controlSegmentIndex, s);
            // Put new profile into array
            multiProfile[i]=new Profile(a,knownGases,null);
            // Conduct the dive on this profile         
            returnCode = multiProfile[i].doDive(); 
            if (returnCode != Profile.SUCCESS) {
                // Houston, we have a problem !
                if(Mvplan.DEBUG>0) System.err.println("MultiProfile: error conducting dive. Return code:"+returnCode);
                return returnCode;
            }   
            // Check conditions of dive and save maximums
            if (multiProfile[i].getModel().getOxTox().getMaxOx() > maxPO2)
                maxPO2=multiProfile[i].getModel().getOxTox().getMaxOx();
            if (multiProfile[i].getModel().getOxTox().getCns() > maxCNS)
                maxCNS = multiProfile[i].getModel().getOxTox().getCns();
                        
            // Strip ascent and waypoint segments as we don't use them in tables 
            a=multiProfile[i].getProfile(); // get output profile
            j=0;
            while(j<a.size()-1) {   
                s= (SegmentAbstract)a.get(j);
                if(s.getType() == SegmentAbstract.ASCENT || (s.getType() == SegmentAbstract.CONST && s.getTime()==0.0 ))                         
                        a.remove(j); 
                else 
                    j++;
            }            
        }      
                
        // Process results into a neat data structure
        
        // Work out the number of rows by looking for the longest profile.
        rows=0;
        longestProfile=0;
        for (i=0;i<=numProfiles-1;i++){
            if (multiProfile[i].getProfile().size() >rows) {
                rows = multiProfile[i].getProfile().size();
                longestProfile=i;
            }
        }    

        // Determine last row for dive plan, remainder is deco        
        ascentRow=knownSegments.size()+1;        
        // Sequence through longest profile and look for first deco segment        
        for (i=0; i<multiProfile[longestProfile].getProfile().size()-1; i++){
            if( ((SegmentAbstract)multiProfile[longestProfile].getProfile().get(i)).getType()== SegmentAbstract.DECO ) {
                ascentRow=i;
                break;
            }                        
        }
                
        if(Mvplan.DEBUG>0) System.out.println("Row count: "+rows+" in column "+longestProfile+" deco starts row "+ascentRow+'\n');
       
        // Adjust dive profiles if shorter than longestProfile by inserting null deep segments
        for( i=0;i<=numProfiles-1;i++) {
            // Determine how many rows short
            int n = rows-multiProfile[i].getProfile().size();         // Note +1 assumes descent segment
            while(n>0){               
                multiProfile[i].getProfile().add(ascentRow,null);    // Add empty 
                n--;
            }                        
        }
        
        // Transfer results into a 2D array of segments (actually by REFERENCE)
        segmentArray = new SegmentAbstract [numProfiles][rows];        
        for(i=0; i<=numProfiles-1;i++){ // For each profile
            for(j=0;j<=rows-1;j++) {     // For each segment (row)
                s=(SegmentAbstract)multiProfile[i].getProfile().get(j);
                segmentArray [i][j] = s;
            }
        }
                       
        return Profile.SUCCESS;
    }
    
    /**
     * getModifiers - gets time modifier array
     * @return int [] modifiers - integer array of time modifiers
     */
    public int[] getModifiers() {
        return modifiers;
    }

    /**
     * setModifiers - sets time modifier array
     * @param modifiers int [] of time modifiers
     */
    public void setModifiers(int[] modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * getKnownSegments - gets the KnownSegments ArrayList
     * @return (SegmentAbstract)ArrayList KnownSegments
     */
    public ArrayList getKnownSegments() {
        return knownSegments;
    }    
}
