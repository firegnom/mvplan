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

package mvplan.dive.printer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mvplan.dive.Profile;
import mvplan.gas.Gas;
import mvplan.main.IMvplan;
import mvplan.main.MvplanInstance;
import mvplan.segments.SegmentAbstract;
//import java.util.MissingResourceException;

public class TextProfilePrinter extends ProfilePrinter <StringBuffer>{
    private IMvplan mvplan = MvplanInstance.getMvplan();
   
    private StringBuffer textArea;
    private Profile profile;
    //private boolean showStopTime = MvplanInstance.getPrefs().isShowStopTime();
    private String disclaimer;
    private List<Gas> knownGases;
    
    /** Creates a new instance of ProfilePrinter */
    public TextProfilePrinter(Profile p, StringBuffer text, List<Gas> knownGases) {
        super(p, text, knownGases);
        this.profile=p;
        this.textArea=text;
        this.knownGases = knownGases;
        disclaimer = mvplan.getResource("mvplan.disclaimer.text");            
    }
    /** Creates a new instance of ProfilePrinter */
    public TextProfilePrinter(Profile p, StringBuffer text) {
        super(p, text, p.getGases());
        this.profile=p;
        this.textArea=text;
        this.knownGases = p.getGases();
        disclaimer = mvplan.getResource("mvplan.disclaimer.text");            
    }
    /* 
     * Prints the dive table
     */
    public StringBuffer print() {
        if(MvplanInstance.getPrefs().getOutputStyle()==MvplanInstance.getPrefs().BRIEF)
            doPrintShortTable();
        else
            doPrintExtendedTable();
        return textArea;
    }

    /** 
     * Prints an extended dive table to the textArea 
     */
    private void doPrintExtendedTable() {        
        
        if(profile.getIsRepetitiveDive()) {
            // Print repetitive dive heading
            textArea.append('\n'+mvplan.getResource("mvplan.gui.text.ProfilePrinter.repetitiveDive.text")+"\n\n"+mvplan.getAppName()+'\n'+
                    mvplan.getResource("mvplan.gui.text.ProfilePrinter.surfaceInterval.text")+profile.getSurfaceInterval()+" "+
                    mvplan.getResource("mvplan.minutes.shortText")+'\n');
        } else
            textArea.append(mvplan.getAppName()+'\n');

        // Print settings heading
        textArea.append(mvplan.getResource("mvplan.gui.text.ProfilePrinter.settings.text")+"="+
                        (int)Math.round(MvplanInstance.getPrefs().getGfLow()*100.)+"-"+(int)Math.round(MvplanInstance.getPrefs().getGfHigh()*100.));
        textArea.append(" "+ mvplan.getResource("mvplan.gui.text.ProfilePrinter.factors.text") + MvplanInstance.getPrefs().getFactorComp() + "/"+ MvplanInstance.getPrefs().getFactorDecomp());
        if (MvplanInstance.getPrefs().getGfMultilevelMode())
            textArea.append(" "+mvplan.getResource("mvplan.gui.text.ProfilePrinter.multilevel.text"));
        textArea.append(" "+mvplan.getResource("mvplan.gui.text.ProfilePrinter.pph2o.text")+"="+
                        MvplanInstance.getPrefs().getPH2O()+" "+MvplanInstance.getPrefs().getDepthShortString()+
                        mvplan.getResource("mvplan.gui.text.ProfilePrinter.seaWater.shortText"));
        textArea.append(" "+profile.getModel().getModelName());
        textArea.append("\n");
        printAltitude();
        
        textArea.append("========================================================="+'\n');
        for(SegmentAbstract s : profile.getProfile()) {
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
        List<Gas> gases=knownGases; //profile.getGases();
        String volumeUnits = MvplanInstance.getPrefs().getVolumeShortString();
        
        // Gas usage heading
        textArea.append('\n'+mvplan.getResource("mvplan.gui.text.ProfilePrinter.gasEstimate.text")+" ="+
                        MvplanInstance.getPrefs().getDiveRMV()+", "+mvplan.getResource("mvplan.gui.text.ProfilePrinter.decoRmv.text")+
                        " ="+MvplanInstance.getPrefs().getDecoRMV()+volumeUnits+"/"+ mvplan.getResource("mvplan.minutes.shortText") + '\n');
        for (Gas g : gases){
            if(g.getVolume()> 0.0d)
                textArea.append(g+" : "+ roundDouble(1, g.getVolume())+volumeUnits+'\n');
        }
        textArea.append(mvplan.getResource("mvplan.gui.text.ProfilePrinter.oxygenToxcicity.text")+" "+
                        (int)profile.getModel().getOxTox().getOtu()+ " "+mvplan.getResource("mvplan.gui.text.ProfilePrinter.cns.text")+
                        ": "+(int)(profile.getModel().getOxTox().getCns()*100.)+"%"+'\n');
        if (profile.getModel().getOxTox().getMaxOx() > MvplanInstance.getPrefs().getMaxPO2() )
            textArea.append(mvplan.getResource("mvplan.gui.text.ProfilePrinter.warningPpO2.text")+": "+ ((int)(profile.getModel().getOxTox().getMaxOx()*100)/100.0)+
                    " "+mvplan.getResource("mvplan.gui.text.ProfilePrinter.atmCnsEstimate.text")+'\n');
        textArea.append(disclaimer+'\n');        
    }
    
    /** 
     * Prints a short text dive table to the textArea 
     */
    private void doPrintShortTable() {              
        
        
        int segTimeMins,segTimeSeconds;

        if(profile.getIsRepetitiveDive()) {
            // Print repetitive dive heading
            textArea.append('\n'+mvplan.getResource("mvplan.gui.text.ProfilePrinter.repetitiveDive.text")+'\n'+'\n'+mvplan.getAppName()+'\n'+
                    mvplan.getResource("mvplan.gui.text.ProfilePrinter.surfaceInterval.text")+profile.getSurfaceInterval()+" "+
                    mvplan.getResource("mvplan.minutes.shortText")+'\n');
        } else
            textArea.append(mvplan.getAppName()+'\n');;

        // Print settings heading
        textArea.append(mvplan.getResource("mvplan.gui.text.ProfilePrinter.settings.text")+"="+
                        (int)Math.round(MvplanInstance.getPrefs().getGfLow()*100.)+"-"+(int)Math.round(MvplanInstance.getPrefs().getGfHigh()*100.));
        if( MvplanInstance.getPrefs().isUsingFactors())
            textArea.append(" "+ mvplan.getResource("mvplan.gui.text.ProfilePrinter.factors.text") + MvplanInstance.getPrefs().getFactorComp() + "/"+ MvplanInstance.getPrefs().getFactorDecomp());
        if (MvplanInstance.getPrefs().getGfMultilevelMode())
            textArea.append(" "+mvplan.getResource("mvplan.gui.text.ProfilePrinter.multilevel.text"));
        textArea.append(" "+profile.getModel().getModelName());
        textArea.append("\n");        
        printAltitude();        
        boolean ocMode = profile.getPrefs().isOcMode();
        if (ocMode){
        	textArea.append("    "+MvplanInstance.getPrefs().getDepthShortString()+"   "+mvplan.getResource("mvplan.gui.text.ProfilePrinter.heading.textNoSP")+'\n');
        }else{
        	textArea.append("    "+MvplanInstance.getPrefs().getDepthShortString()+"   "+mvplan.getResource("mvplan.gui.text.ProfilePrinter.heading.text")+'\n');
        }
        if (ocMode){
        textArea.append("========================"+'\n');
        //              "- 120  00:00  000  88/88
        }else{
        textArea.append("=============================="+'\n');
        //              "- 120  00:00  000  88/88  1.30
        }  

        for (SegmentAbstract s: profile.getProfile()){
			segTimeMins = (int) s.getTime();
			segTimeSeconds = (int) ((s.getTime() - (double) segTimeMins) * 60.0);

			if ((s.getDepth() - (int) s.getDepth()) > 0) { // Do we have
															// non-integer depth
															// ?
				if (ocMode) {
					textArea.append(String.format(
							"%1$s  %2$03.1f  %3$02d:%4$02d  %5$03.0f  %6$5s\n",
							s.getTypeString(), s.getDepth(), segTimeMins,
							segTimeSeconds, s.getRunTime(), s.getGas()
									.getShortName()));
				} else {
					textArea.append(String
							.format("%1$s  %2$03.1f  %3$02d:%4$02d  %5$03.0f  %6$5s  %7$3.1f\n",
									s.getTypeString(), s.getDepth(),
									segTimeMins, segTimeSeconds,
									s.getRunTime(), s.getGas().getShortName(),
									s.getSetpoint()));
				}
			} else {
				if (ocMode) {
					textArea.append(String
							.format("%1$s  %2$03.0f  %3$02d:%4$02d  %5$03.0f  %6$5s\n",
									s.getTypeString(), s.getDepth(),
									segTimeMins, segTimeSeconds,
									s.getRunTime(), s.getGas().getShortName()));
				} else {
					textArea.append(String
							.format("%1$s  %2$03.0f  %3$02d:%4$02d  %5$03.0f  %6$5s  %7$3.1f\n",
									s.getTypeString(), s.getDepth(),
									segTimeMins, segTimeSeconds,
									s.getRunTime(), s.getGas().getShortName(),
									s.getSetpoint()));
				}
			}
		}
        doGasUsage();      
    }
    
    /* 
     * Print altitude message 
     */
    private void printAltitude() {
        // Is this an altitude dive ?
        if(MvplanInstance.getPrefs().getAltitude()>0.0) {
            textArea.append(String.format("%1$s %2$4.0f%6$s (%4$1.2f%3$s) %5$s\n",
                    mvplan.getResource("mvplan.gui.text.altitude.text"),
                    MvplanInstance.getPrefs().getAltitude(), 
                    mvplan.getResource("mvplan.bar.text"),
                    MvplanInstance.getPrefs().getPAmb()/MvplanInstance.getPrefs().getPConversion(),
                    mvplan.getResource("mvplan.gui.text.altitudeCalibration.text"),
                    MvplanInstance.getPrefs().getDepthShortString() ) );                        
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
