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

import mvplan.dive.printer.*;
import mvplan.main.Mvplan;
import mvplan.dive.TableGeneratorModel;
import mvplan.segments.SegmentAbstract;

import javax.swing.JTextArea;
import mvplan.main.MvplanInstance;
        
public class JTextAreaTablePrinter extends TablePrinter<JTextArea> {
    
    private JTextArea textArea;
    private TableGeneratorModel tm;
    private boolean showStopTime = Mvplan.prefs.isShowStopTime();  
     
    
    /** Creates a new instance of TablePrinter */
    public JTextAreaTablePrinter(TableGeneratorModel tm, JTextArea textArea) {
        super(tm, textArea); 
        this.tm=tm;
        this.textArea=textArea;             
                   
    }

    
    
    
    /** Prints table to textArea */
    public JTextArea print() {
        StringBuffer ret = new StringBuffer();
        TablePrinter p = new TextTablePrinter(tm, ret);
        textArea.append(p.print().toString());
        return textArea;
        
    }
}
