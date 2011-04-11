/*
 * ProfilePrinter.java
 *
 * Prints a text single dive table onto the Text Area
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

package mvplan.gui.text;

import java.math.BigDecimal;
import mvplan.main.Mvplan;
import mvplan.segments.SegmentAbstract;
import mvplan.dive.Profile;
import mvplan.gas.Gas;
import mvplan.prefs.Prefs;

import javax.swing.JTextArea;
import java.util.ArrayList;
import java.util.Iterator;
//import java.util.MissingResourceException;

public class ProfilePrinter {
   
    private JTextArea textArea;
    private Profile profile;
    //private boolean showStopTime = Mvplan.prefs.isShowStopTime();
    private String disclaimer;
    private ArrayList knownGases;
    
    /** Creates a new instance of ProfilePrinter */
    public ProfilePrinter(Profile p, JTextArea text, ArrayList knownGases) {
        this.profile=p;
        this.textArea=text;
        this.knownGases = knownGases;
        disclaimer = Mvplan.getResource("mvplan.disclaimer.text");            
    }
    /* 
     * Prints the dive table
     */
    public void doPrintTable() {
        if(Mvplan.prefs.getOutputStyle()==Prefs.BRIEF)
            doPrintShortTable();
        else
            doPrintExtendedTable();
    }

    /** 
     * Prints an extended dive table to the textArea 
     */
    private void doPrintExtendedTable() {        
        ArrayList segments;
        SegmentAbstract s;
        
        if(profile.getIsRepetitiveDive()) {
            // Print repetitive dive heading
            textArea.append('\n'+Mvplan.getResource("mvplan.gui.text.ProfilePrinter.repetitiveDive.text")+'\n'+'\n'+Mvplan.appName+'\n'+
                    Mvplan.getResource("mvplan.gui.text.ProfilePrinter.surfaceInterval.text")+profile.getSurfaceInterval()+" "+
                    Mvplan.getResource("mvplan.minutes.shortText")+'\n');
        } else
            textArea.append(Mvplan.appName+'\n');

        // Print settings heading
        textArea.append(Mvplan.getResource("mvplan.gui.text.ProfilePrinter.settings.text")+"="+
                        (int)Math.round(Mvplan.prefs.getGfLow()*100.)+"-"+(int)Math.round(Mvplan.prefs.getGfHigh()*100.));
        textArea.append(" "+ Mvplan.getResource("mvplan.gui.text.ProfilePrinter.factors.text") + Prefs.getPrefs().getFactorComp() + "/"+ Prefs.getPrefs().getFactorDecomp());
        if (Mvplan.prefs.getGfMultilevelMode())
            textArea.append(" "+Mvplan.getResource("mvplan.gui.text.ProfilePrinter.multilevel.text"));
        textArea.append(" "+Mvplan.getResource("mvplan.gui.text.ProfilePrinter.pph2o.text")+"="+
                        Mvplan.prefs.getPH2O()+" "+Mvplan.prefs.getDepthShortString()+
                        Mvplan.getResource("mvplan.gui.text.ProfilePrinter.seaWater.shortText"));
        textArea.append(" "+profile.getModel().getModelName());
        textArea.append("\n");
        printAltitude();
        
        textArea.append("========================================================="+'\n');
        segments = profile.getProfile();      
        Iterator i = segments.iterator();
        while(i.hasNext()) {
            s=(SegmentAbstract)i.next();
            textArea.append(s.toStringLong()+'\n');
        }
        doGasUsage();        
    }
    
    /* 
     * Prints gas usage table 
     */
    private void doGasUsage() {
        //TODO - use String formatter for these so as to display localisations properly        
        // Display gas usage
        // GW - Modified Mar-2009 to display all knaown gases with volumes > 0 so as to pick up open circuit bottom gas
        ArrayList gases=knownGases; //profile.getGases();
        Iterator i2 = gases.iterator();  
        String volumeUnits = Mvplan.prefs.getVolumeShortString();
        
        // Gas usage heading
        textArea.append('\n'+Mvplan.getResource("mvplan.gui.text.ProfilePrinter.gasEstimate.text")+" ="+
                        Mvplan.prefs.getDiveRMV()+", "+Mvplan.getResource("mvplan.gui.text.ProfilePrinter.decoRmv.text")+
                        " ="+Mvplan.prefs.getDecoRMV()+volumeUnits+"/"+ Mvplan.getResource("mvplan.minutes.shortText") + '\n');
        while(i2.hasNext()) {
            Gas g=(Gas)i2.next();
            if(g.getVolume()> 0.0d)
                textArea.append(g+" : "+ roundDouble(1, g.getVolume())+volumeUnits+'\n');
        }
        textArea.append(Mvplan.getResource("mvplan.gui.text.ProfilePrinter.oxygenToxcicity.text")+" "+
                        (int)profile.getModel().getOxTox().getOtu()+ " "+Mvplan.getResource("mvplan.gui.text.ProfilePrinter.cns.text")+
                        ": "+(int)(profile.getModel().getOxTox().getCns()*100.)+"%"+'\n');
        if (profile.getModel().getOxTox().getMaxOx() > Mvplan.prefs.getMaxPO2() )
            textArea.append(Mvplan.getResource("mvplan.gui.text.ProfilePrinter.warningPpO2.text")+": "+ ((int)(profile.getModel().getOxTox().getMaxOx()*100)/100.0)+
                    " "+Mvplan.getResource("mvplan.gui.text.ProfilePrinter.atmCnsEstimate.text")+'\n');
        textArea.append(disclaimer+'\n');        
    }
    
    /** 
     * Prints a short text dive table to the textArea 
     */
    private void doPrintShortTable() {              
        ArrayList segments;
        SegmentAbstract s;
        
        int segTimeMins,segTimeSeconds;

        if(profile.getIsRepetitiveDive()) {
            // Print repetitive dive heading
            textArea.append('\n'+Mvplan.getResource("mvplan.gui.text.ProfilePrinter.repetitiveDive.text")+'\n'+'\n'+Mvplan.appName+'\n'+
                    Mvplan.getResource("mvplan.gui.text.ProfilePrinter.surfaceInterval.text")+profile.getSurfaceInterval()+" "+
                    Mvplan.getResource("mvplan.minutes.shortText")+'\n');
        } else
            textArea.append(Mvplan.appName+'\n');;

        // Print settings heading
        textArea.append(Mvplan.getResource("mvplan.gui.text.ProfilePrinter.settings.text")+"="+
                        (int)Math.round(Mvplan.prefs.getGfLow()*100.)+"-"+(int)Math.round(Mvplan.prefs.getGfHigh()*100.));
        if( Prefs.getPrefs().isUsingFactors())
            textArea.append(" "+ Mvplan.getResource("mvplan.gui.text.ProfilePrinter.factors.text") + Prefs.getPrefs().getFactorComp() + "/"+ Prefs.getPrefs().getFactorDecomp());
        if (Mvplan.prefs.getGfMultilevelMode())
            textArea.append(" "+Mvplan.getResource("mvplan.gui.text.ProfilePrinter.multilevel.text"));
        textArea.append(" "+profile.getModel().getModelName());
        textArea.append("\n");        
        printAltitude();        
        textArea.append("    "+Mvplan.prefs.getDepthShortString()+"   "+Mvplan.getResource("mvplan.gui.text.ProfilePrinter.heading.text")+'\n');
        textArea.append("=============================="+'\n');
        //              "- 120  00:00  000  88/88  1.30  
        segments = profile.getProfile();      
        Iterator i = segments.iterator();
        while(i.hasNext()) {
            s=(SegmentAbstract)i.next();
            segTimeMins=(int)s.getTime();
            segTimeSeconds = (int)((s.getTime() - (double)segTimeMins)*60.0);

            if ((s.getDepth()-(int)s.getDepth())>0) // Do we have non-integer depth ?
                
                textArea.append(String.format("%1$s  %2$03.1f  %3$02d:%4$02d  %5$03.0f  %6$5s  %7$3.1f\n",
                            s.getTypeString(),s.getDepth(), segTimeMins,segTimeSeconds,s.getRunTime(),s.getGas().getShortName() ,s.getSetpoint() ));              
            else     
                textArea.append(String.format("%1$s  %2$03.0f  %3$02d:%4$02d  %5$03.0f  %6$5s  %7$3.1f\n",
                            s.getTypeString(),s.getDepth(), segTimeMins,segTimeSeconds,s.getRunTime(),s.getGas().getShortName() ,s.getSetpoint() ));
        }        
        doGasUsage();      
    }
    
    /* 
     * Print altitude message 
     */
    private void printAltitude() {
        // Is this an altitude dive ?
        if(Mvplan.prefs.getAltitude()>0.0) {
            textArea.append(String.format("%1$s %2$4.0f%6$s (%4$1.2f%3$s) %5$s\n",
                    Mvplan.getResource("mvplan.gui.text.altitude.text"),
                    Mvplan.prefs.getAltitude(), 
                    Mvplan.getResource("mvplan.bar.text"),
                    Mvplan.prefs.getPAmb()/Mvplan.prefs.getPConversion(),
                    Mvplan.getResource("mvplan.gui.text.altitudeCalibration.text"),
                    Mvplan.prefs.getDepthShortString() ) );                        
        }        
    }
    
     /* 
     * Rounds double values
     */
    private double roundDouble(int precision, double d){        
        int decimalPlace = precision;
        BigDecimal bd = new BigDecimal(d);
        bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();                
    }
}
