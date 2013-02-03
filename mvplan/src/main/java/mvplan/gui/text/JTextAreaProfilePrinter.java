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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;

import mvplan.dive.Profile;
import mvplan.dive.printer.ProfilePrinter;
import mvplan.dive.printer.TextProfilePrinter;
import mvplan.gas.Gas;
//import java.util.MissingResourceException;

public class JTextAreaProfilePrinter extends ProfilePrinter <JTextArea> {
   
    private JTextArea textArea;
    private Profile profile;
    //private boolean showStopTime = Mvplan.prefs.isShowStopTime();
    private List<Gas> knownGases;
    StringBuffer result = new StringBuffer();
    
     /** Creates a new instance of ProfileTextAreaPrinter */
    public JTextAreaProfilePrinter(Profile p, JTextArea text, List<Gas> knownGases) {
        super(p, text, knownGases);
        this.profile=p;
        this.textArea=text;
        this.knownGases = knownGases;
    }
    /* 
     * Prints the dive table
     */
    public JTextArea print() {
        TextProfilePrinter p = new TextProfilePrinter(profile, result, knownGases);
        textArea.append( p.print().toString());
        return textArea;
    }

}