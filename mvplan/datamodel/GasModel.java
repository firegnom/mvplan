/*
 * GasModel.java
 *
 * Provides AbstractTableModel for the gas table on the Main Window (MainFrame)
 *
 *   @author Guy Wittig
 *   @version 18-Jun-2006
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

package mvplan.datamodel;

import javax.swing.table.*;
import java.util.ArrayList;
import java.util.MissingResourceException;
import mvplan.gas.Gas;
import mvplan.main.Mvplan;


public class GasModel extends AbstractTableModel 
{
	String [] headings;
	ArrayList<Gas> gasList;	
	public static int COLUMN_COUNT=3;

	public GasModel(ArrayList gasList) {
        this.gasList=gasList;
       // Set up headings          
        headings = new String[] {
            Mvplan.getResource("mvplan.gui.MainFrame.gasTable.name.text"),
            Mvplan.getResource("mvplan.gui.MainFrame.gasTable.mod.text"),
            Mvplan.getResource("mvplan.gui.MainFrame.gasTable.enable.text")  };                                          
	}
        
        // Accessors
	public ArrayList<Gas> getGasList()      { return gasList;}
	public int getRowCount()                { return gasList.size();}
	public int getColumnCount()             { return COLUMN_COUNT; }
	public String getColumnName(int col)    { return headings[col]; }
	public Gas getGas(int row)              { return (Gas)gasList.get(row);}

        // Remove row
	public void removeRow(int row)
	{
		gasList.remove(row);
		// Fire event
		fireTableDataChanged();
	}
        
     /*
     * Get class for column
     */
	public Class getColumnClass(int col) { 
		Gas g=(Gas)gasList.get(0);
		switch (col) {
			case 0:	return g.toString().getClass();
			case 1: return new Double(g.getMod()).getClass();
			case 2: return new Boolean(g.getEnable()).getClass();
			default: return null;		

		}
	}
        
    /*
     * Check if cell is editable. Only column 2 is (enable)
     */        
    public boolean isCellEditable(int row, int col) { 
                    if (col==2) 
                        return true; 
                    else 
                        return false;
    }

     /*
     * Set value at cell. Only col 2 is editable
     */
    public void setValueAt(Object obj, int row, int col)
    {
            Gas g=(Gas)gasList.get(row);
            switch (col) {
                case 2: Boolean b = (Boolean)obj; 
                        g.setEnable(b.booleanValue());
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
            Gas g=(Gas)gasList.get(row);
            switch (col) {
                    case 0:	return g.toString();
                    case 1: return new Double(g.getMod());
                    case 2: return new Boolean(g.getEnable());
                    default: return null;		

            }
    }
}
