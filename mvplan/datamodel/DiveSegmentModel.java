/*
 * DiveSegmentModel.java
 *
 * Provides AbstractTableModel for the dive segment table on the Main Window (MainFrame)
 *
 * @author Guy Wittig
 * @version 04-Mar-2005
 *
 *   This program is part of MV-Plan
*   copyright 2006 Guy Wittig
*
*   This program is free software; you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation; either version 2 of the License, or
*   (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of 
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    The GNU General Public License can be read at http://www.gnu.org/licenses/licenses.html*
 */

package mvplan.datamodel;

import javax.swing.table.*;
import java.util.ArrayList;
import mvplan.gas.Gas;
import mvplan.segments.SegmentAbstract;
import mvplan.main.*;

public class DiveSegmentModel extends AbstractTableModel
{
    String [] headings;
    ArrayList knownSegments;    
    public static int COLUMN_COUNT=5;

    public DiveSegmentModel(ArrayList knownSegments)
    {
        this.knownSegments=knownSegments;
        
        // Create headings from resource file
        // e.g. headings = new String[] {"Depth", "Time","Gas","SP","Enable"};
        headings = new String[] {
            Mvplan.getResource("mvplan.gui.MainFrame.diveTable.depth.text"),
            Mvplan.getResource("mvplan.gui.MainFrame.diveTable.time.text"),
            Mvplan.getResource("mvplan.gui.MainFrame.diveTable.gas.text"),
            Mvplan.getResource("mvplan.gui.MainFrame.diveTable.setpoint.text"),                    
            Mvplan.getResource("mvplan.gui.MainFrame.diveTable.enable.text")  };                                 
    }

    // Accessors
    public ArrayList<SegmentAbstract> getKnownSegments()    { return knownSegments;}
    public int getRowCount()                                { return knownSegments.size();}
    public int getColumnCount()                             { return COLUMN_COUNT; }
    public String getColumnName(int col)                    { return headings[col]; }
    public SegmentAbstract getSegment(int row)              { return (SegmentAbstract)knownSegments.get(row);}


    /* 
     * Move row up 
     */
    public void moveRowUp(int row)
    {   
        // Check bounds
        if(row > 0 && row <= (knownSegments.size()-1)) {
            SegmentAbstract s=(SegmentAbstract)knownSegments.get(row-1);
            knownSegments.set(row-1,knownSegments.get(row));
            knownSegments.set(row,s);
        }
        // Fire event
        fireTableDataChanged();
    }

    /* 
     * Move row down 
     */
    public void moveRowDown(int row)
    {   
        // Check bounds
        if(row >= 0 && row < (knownSegments.size()-1)) {
            SegmentAbstract s=(SegmentAbstract)knownSegments.get(row+1);
            knownSegments.set(row+1,knownSegments.get(row));
            knownSegments.set(row,s);
        }
        // Fire event
        fireTableDataChanged();
    }

    /*
     * Remove row from model
     */
    public void removeRow(int row)
    {
        knownSegments.remove(row);
        // Fire event
        fireTableDataChanged();
    }

    /*
     * Get class for column
     */
    public Class getColumnClass(int col) { 
        SegmentAbstract s=(SegmentAbstract)knownSegments.get(0);
        switch (col) {
            case 0: return new Double(s.getDepth()).getClass();
            case 1: return new Double(s.getTime()).getClass();
            case 2: // Ensure we don't have a null gas
                    if(s.getGas()==null) s.setGas(new Gas(0.0, 0.21, 66.0));   
                    return s.getGas().getClass();
            case 3: return new Double(s.getSetpoint()).getClass();
            case 4: return s.getEnable().getClass();
            default: return null;       
        }
    }
    
    /*
     * Check if cell is editable
     */
    public boolean isCellEditable(int row, int col)
    { 
            // All are editable
            return true;
    }

    /*
     * Set value at cell
     */
    public void setValueAt(Object obj, int row, int col)
    {
        // New JVM appears to miss combo box selections so check for nulls
        if(obj == null) {
            //System.out.println("DiveSegmentModel.setValueAt(): null argument");
            return;
        }
        // Get dive segment related to this row
        SegmentAbstract s=(SegmentAbstract)knownSegments.get(row);      
        double d;
        String str;        
        // Switch by column. If entries are out of bounds then set them to zero.
        switch (col) {
            case 0: // Depth
                    d=((Double)obj).doubleValue();
                    if(d<0.0 || d>Mvplan.prefs.getMaxDepth()) d=0.0;
                    s.setDepth(d);
                    break;                     
            case 1: // Time
                    d=((Double)obj).doubleValue();
                    if(d<0.0 || d>Mvplan.prefs.getMaxSegmentTime()) d=0.0;
                    s.setTime(d);
                    break;
            case 2: // Gas
                    s.setGas((Gas)obj);
                    break;
            case 3: // Setpoint
                    d=((Double)obj).doubleValue();
                    if (d<0.0 || d>Mvplan.prefs.getMaxSetpoint()) d=0.0;
                    s.setSetpoint(d);
                    break;  
            case 4: // Enable
                    s.setEnable((Boolean)obj);
                    break;
            default:
                    break;
        }
    }
    
    /*
     * Get value at cell
     */
    public Object getValueAt(int row, int col)
    {
        // Get dive segment related to this row 
        SegmentAbstract s=(SegmentAbstract)knownSegments.get(row);
        
        s.setType(SegmentAbstract.CONST);   // Fix for previous bug         
        // Switch by column
        switch (col) {
            case 0: // Depth
                    return new Double(s.getDepth());
            case 1: // Time
                    return new Double(s.getTime());
            case 2: // Gas
                    return s.getGas().toString();
            case 3: // Setpoint
                    return new Double(s.getSetpoint());
            case 4: // Enable
                    return s.getEnable();
            default: return null;
        }
    }
}
