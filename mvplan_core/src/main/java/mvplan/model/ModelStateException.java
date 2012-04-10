/**
 * ModelStateException.java 
 *
 * Indicates that the model is in an un-stable state.
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

package mvplan.model;

public class ModelStateException extends Exception {
    /**
     * Empty Constructor.
     */
    public ModelStateException() {
        super();
    }
    /**
     * Constructor wih String message
     * @param desc Message
     */
    public ModelStateException(String desc) {
        super(desc);
    }
    
}

