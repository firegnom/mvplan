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


package mvplan.dive.printer;

import mvplan.dive.TableGeneratorModel;

        
public abstract class TablePrinter <T>{
    
   
    public TablePrinter(TableGeneratorModel tm, T textArea) {
       
    }
    
    
    public abstract T print() ;
}
    