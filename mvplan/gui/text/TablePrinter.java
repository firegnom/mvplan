/*
 * TablePrinter.java
 *
 * Prints text version of multi profile table onto the text area
 * Requires a TableGeneratorModel and the JTextArea for output
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

import mvplan.main.Mvplan;
import mvplan.dive.TableGeneratorModel;
import mvplan.segments.SegmentAbstract;

import javax.swing.JTextArea;
        
public class TablePrinter {
    
    private JTextArea textArea;
    private TableGeneratorModel tm;
    private boolean showStopTime = Mvplan.prefs.isShowStopTime();  
    
    /** Creates a new instance of TablePrinter */
    public TablePrinter(TableGeneratorModel tm, JTextArea textArea) {
        // Store locally
        this.tm=tm;
        this.textArea=textArea;             
                   
    }
    
    /** Prints table to textArea */
    public void doPrintTable() {
        String separator;
        int i,j;
        SegmentAbstract s,sl;
        // Get segment array
        SegmentAbstract [][] segmentArray = tm.getSegmentArray();   
        if(segmentArray==null) return;
        
        // Determine number of profiles, segments, longest profile, row at which ascent starts
        int numProfiles = tm.getNumProfiles();
        int numSegments = tm.getNumSegments();
        int longestProfile = tm.getLongestprofile();
        //int ascentRow = tm.getAscentRow();
        
        // Prepare strings
        String disclaimer = Mvplan.getResource("mvplan.disclaimer.text");        
        String runChar = Mvplan.getResource("mvplan.run.char");
        String stopChar = Mvplan.getResource("mvplan.stop.char");        
        String gasString = Mvplan.getResource("mvplan.gas.shortText");
        String spString = Mvplan.getResource("mvplan.sp.shortText");        
        
        // Create heading
        textArea.append(Mvplan.appName+'\n');
        textArea.append(Mvplan.getResource("mvplan.gui.text.ProfilePrinter.settings.text")+"="+(int)Math.round(Mvplan.prefs.getGfLow()*100.)+"-"+(int)Math.round(Mvplan.prefs.getGfHigh()*100.));
        textArea.append(" "+ Mvplan.getResource("mvplan.gui.text.ProfilePrinter.multilevel.text")+"="+Mvplan.prefs.getGfMultilevelMode());
        textArea.append(" "+tm.getModelName());
        textArea.append("\n");
        printAltitude();
        // Create separator line        
        separator="";
        for (i=0;i<numProfiles;i++) 
            separator = showStopTime ? separator+"--------": separator+"-----";            
        separator=separator+"-----------------\n";
        
        // Create table heading row
        String result = "   "+Mvplan.prefs.getDepthShortString()+" ";
        for (i=0;i<numProfiles;i++) 
            //result = showStopTime ? result+"  S   R ": result+"   R "; 
            result = showStopTime ? result+"  "+stopChar+"   "+runChar+" ": result+"   "+runChar+" ";
        result=result+"  "+spString+"    "+gasString+"\n"+separator;
        
        for (j=0;j<=numSegments-1;j++){    // For all rows
            // Get depth of this series of segments from longest column
            sl=segmentArray[longestProfile][j];  
            if(sl==null) {
                if(Mvplan.DEBUG>0) System.err.println("MultiProfile: null segment at profile:"+longestProfile+" row:"+j);
                return;
            }  
            if ((sl.getDepth()-(int)sl.getDepth())>0) // Do we have non-integer depth ?      
                result = result+ String.format(" %1$3.1f ", sl.getDepth() );   
            else 
                result=result+ String.format(" %1$3.0f ", sl.getDepth() );  
            
            for (i=0;i<=numProfiles-1;i++){     // For each profile        
                s=segmentArray[i][j];           // Get segment
                sl=segmentArray[longestProfile][j];
                if(s == null)                   // Some can be null
                    result = showStopTime ? result + "        " : result + "     ";
                else                
                    result= showStopTime ? result+String.format(" %1$2.0f %2$3.0f ", s.getTime(), s.getRunTime()) : result+String.format(" %1$3.0f ", s.getRunTime());
            }  
            // Get gas details from longest profile so as to avoid reading gas details from null segments
            result=result+ String.format(" %1$3.1f  %2$5s ", sl.getSetpoint(),sl.getGas().getShortName()); 

            result = result + "\n";
            /* if(j==ascentRow-1)
                result = result + separator; */
        }
        result = result + '\n';            
        
        // Copy string to textArea
        textArea.append(result);
        
        // Check oxygen limits
        if (tm.getMaxPO2() > Mvplan.prefs.getMaxPO2()) {
            textArea.append(Mvplan.getResource("mvplan.gui.text.tablePrinter.maxPp02.text")+" "+ ((int)Math.round(tm.getMaxPO2()*100)/100.0)+" "+
                    Mvplan.getResource("mvplan.gui.text.tablePrinter.cnsEstimated.text")+'\n');
        }
        textArea.append(Mvplan.getResource("mvplan.gui.text.tablePrinter.oxTox.text")+" "+(int)Math.round(tm.getMaxCNS()*100.)+"%"+'\n');
        textArea.append(disclaimer+'\n'); 
    }
    
    /* Print altitude message */
    private void printAltitude() {
        // Is this an altitude dive ?
        if(Mvplan.prefs.getAltitude()>0.0) {
            textArea.append(String.format("%1$s %2$4.0f%3$s (%4$2.1f%3$ssw) %5$s\n",
                    Mvplan.getResource("mvplan.gui.text.altitude.text"),
                    Mvplan.prefs.getAltitude(), 
                    Mvplan.prefs.getDepthShortString(),
                    Mvplan.prefs.getPAmb(),
                    Mvplan.getResource("mvplan.gui.text.altitudeCalibration.text")));                        
        }        
    }    
}
