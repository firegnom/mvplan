/*
 * DoublCellEditor.java
 *
 * Used for inputting double values in JTables.
 *
 * @author Guy Wittig
 * @version 6-Jul-2005
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

package mvplan.gui;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;


/**
 *
 * @author Guy
 */
public class DoubleCellEditor extends DefaultCellEditor {
    double min,max;
    
    /** Creates a new instance of FloatCellEditor */    
    public DoubleCellEditor(JTextField tf, double min, double max){
        super(tf);
        setLimits(min,max);
        setClickCountToStart(1);
    }
    
    /** Sets min and max limits */
    public void setLimits( double min, double max) {
        this.min=min;
        this.max=max;        
    }
            
    //Override to ensure that the value remains a double.
    @Override
    public Object getCellEditorValue() {
        JTextField tf = (JTextField)getComponent();
        String str = tf.getText();
        Double d;
        // Try to parse double
        try {
            d = new Double(str);
            if(d<min) d=min;
            else if (d>max) d=max;
        } catch (NumberFormatException ex) {            
            d= new Double(0.0);
        }
        return d;
    }    
  
}
