/**
 *  Gradient.java
 *
 *  Defines a Gradient Factor object. 
 *  A GF Object maintains a low and high setting and is able to determine
 *  a GF for any depth between its initialisation depth (see setGfAtDepth())
 *  and the surface.
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
import java.io.*;

public class Gradient implements Serializable
{
    // instance variables
    private double gfHigh,gfLow;    // GF high and low settings
    private double gf;              // Current GF
    private double gfSlope;         // Slope of the linear equation
    private boolean gfSet;        // Indicates that gf Slope has been initialised

    /** Empty constructor for Bean compliance
     */
    public Gradient() {             
    }    
    
    /**
     * Constructor for objects of class Gradient
     * @param gfLow Low GF. 0.0 to 1.0
     * @param gfHigh High GF. 0.0 to 1.0
     */
    public Gradient(double gfLow, double gfHigh)
    {
        this.gfHigh=gfHigh;         
        this.gfLow=gfLow;
        gfSlope=1.0;
        gf = gfLow;
        gfSet=false;
    }

    /**
     * Returns current GF with bounds checking. If GF < GLLow, returns GFLow.
     * @return Current GF
     */
    public double getGradientFactor()
    {
            if (gf >= gfLow) return gf;
            else return gfLow;
    }
    

    /**
     * Sets the gf for a given depth. Must be called after setGfSlope()
     * has initialised slope
     * @param depth Current Depth msw (fsw)
     */
    public void setGfAtDepth(double depth)
    {
        if ((gfSlope < 1.0) && (depth >= 0.0)) 
            gf=(depth*gfSlope) + gfHigh;
            
    }
    
    /**
     * Set gf Slope at specified depth. Typically called once to initialise the GF slope.
     * @param depth Depth msw (fsw) 
     */
    public void setGfSlopeAtDepth(double depth)
    {
        if (depth > 0) {
            gfSlope = (gfHigh -gfLow)/(0.0-depth);      
            gfSet=true;
        }
    }    
    
    /************** Accessor and mutator methods for Bean compliance **************/
    
    /**
     * Gets GF High setting. Typical (0.0-1.0)
     * @return GF High
     */
    public double getGfHigh()           { return gfHigh; }
    /**
     * Sets GF High
     * @param d GF High. Tyically 0.0 to 1.0
     */
    public void setGfHigh(double d)     { gfHigh=d; }
    /**
     * Gets GF High setting. Typical (0.0-1.0)
     * @return GF Low
     */
    public double getGfLow()            { return gfLow; }
    /**
     * Sets GF Low setting. Typical (0.0-1.0)
     * @param d GF Low
     */
    public void setGfLow(double d)      { gfLow=d; }
    /**
     * Gets current GF. Typical GFLow < GF < GFHigh
     * @return Current GF
     */
    public double getGf()               { return gf; }
    /**
     * Sets Current GF. Required for Bean compliance.
     * @param d Current GF
     */
    public void setGf(double d)         { gf=d; }
    /**
     * Gets GF Slope (i.e. slope of the linear equation)
     * @return GF Slope
     */
    public double getGfSope()           { return gfSlope; }
    /**
     * Sets GF Slope (Required for Bean Compliance)
     * @param d GF Slope
     */
    public void setGfSlope(double d)    { gfSlope=d; }
    /**
     * Returns <CODE>true</CODE> if GF is set
     * @return If GF is set
     */
    public boolean isGfSet()         { return gfSet; }
    /**
     * Sets GFIsSet flag
     * @param b GF is Set
     */
    public void setGfSet(boolean b)  { gfSet=b; }
    


}
