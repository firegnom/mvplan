/*
 *   PressureConverter.java
 *
 *   Used to convert between Pressure and altitude in meters.
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

package mvplan.util;

/**
 *
 * @author Guy Wittig
 */
public class PressureConverter {
    
    /** Creates a new instance of pressureConverter */
    public PressureConverter() {
    }
    
    /**
     * Converts altitude in meters to pressure in msw
     * @return Pressure in msw or 0.0 if out of bounds.
     * @param altitude Altitude in meters
     */
    public static double altitudeToPressure(double altitude) {
        if (altitude==0.0)
            return 10.0;
        else if (altitude>0.0)
            return Math.pow( ((44330.8-altitude)/4946.54), 5.25588)/10131.0;        
        else
            return 0.0;
    }
}
