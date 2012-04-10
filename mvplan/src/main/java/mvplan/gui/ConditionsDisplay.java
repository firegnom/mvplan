/*
 * TermsDisplay.java
 *
 * Displays terms and conditions into a JTextArea. T&C's are obtained from
 * a resource bundle mvplan/resources/conditions.
 *
 * @author Guy Wittig
 * @version 13-May-2007
 *
 *   This program is part of MV-Plan
 *   Copyright 2005-2007 Guy Wittig
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

package mvplan.gui;

import java.util.ResourceBundle;
import javax.swing.*;
import mvplan.main.Mvplan;

/**
 *
 * @author Guy Wittig
 */
public class ConditionsDisplay {
    
    private JTextArea text;
    private ResourceBundle stringResource;
    
    /** Creates a new instance of TermsDisplay */
    public ConditionsDisplay(JTextArea t) {
            try {
                if (Mvplan.preferredLocale != null )
                    stringResource = ResourceBundle.getBundle("mvplan/resources/conditions", Mvplan.preferredLocale ); 
                else
                stringResource = ResourceBundle.getBundle("mvplan/resources/conditions");                
            } catch (java.util.MissingResourceException e) {
                System.err.println("Mvplan: critical error, missing conditions resources.");
                System.exit(0);  
            }                         
        display(t);
    }
    
    public void display(JTextArea t){
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.append(getResource("mvplan.conditions.heading.text")+"\n\n");      
        t.append(getResource("mvplan.conditions.text"));
        // Reposition back to top
        t.setCaretPosition(0);       
    }
    
    /* Provide safe access to resources */
    public  String getResource (String key) {
        try {
           return stringResource.getString(key);        
        } catch (java.util.MissingResourceException e) {
            System.err.println("Mvplan: critical error, missing conditions resources.");
            System.exit(0); 
            return "";
        }
    }
    
}
