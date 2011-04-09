/*
 * SegmentAbstract.java
 *
 * Abstract Segment Class. Defines basic attributes and functions of all dive segments
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

package mvplan.segments;

import java.io.*;
import mvplan.gas.Gas;
import mvplan.main.Mvplan;

public abstract class SegmentAbstract implements Serializable, Cloneable
{
      double depth=0.0;     // Depth of segment (typically end)
      double setpoint=0.0;  // For CCR (=0 for Open Circuit)
      Gas gas;          // Gas used
      double time=0.0;      // Segment time
      double runTime=0.0;   // Runtime in profile
      Boolean enable=true;   // Is this segment enabled (i.e. used)
      int type=CONST;         // type of segment, as per below

     // Note dependencies SOMEWHERE on this ordering !!!
     /** Segment is Constant depth */
     public static final int CONST =1;      
     /** Segment is Ascent */
     public static final int ASCENT=2;  
     /** Segment is Descent */
     public static final int DESCENT=0;
     /** Segment is Deco */
     public static final int DECO=4;
     /** Segment is Waypoint */
     public static final int WAYPOINT=3;
     /** Segment is Surface */
     public static final int SURFACE=5;

    /**
     * Constructor for class Segment
     */
    public SegmentAbstract()
    {
        enable = Boolean.TRUE;
    }

    /**
     * Override the clone method due to the need for deep cloning
     * @return AbstractSegment object
     */
    public Object clone(){
        try {
            SegmentAbstract copy = (SegmentAbstract)super.clone();  // Shallow clone
            copy.gas = (Gas)gas.clone();
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new Error("Never happens");
        }
    }

    /** Gets Equivalent Narcosis Depth (END) in msw (fsw)
     *  @return Equivalent Narcosis Depth (END) in msw (fsw)
    */
    public double getEnd() {        
        double pAbsolute = depth + Mvplan.prefs.getPAmb();  // msw (fsw)
        double fN2 = gas.getFN2();
        double fHe = gas.getFHe(); 
        double pInert;
        double ppN2Inspired;
        // Set inspired gas fractions.
        if(setpoint > 0.0) {                     
            // Rebreather mode
            // Determine pInert by subtracting absolute oxygen pressure (msw), or force to zero if no inert fraction
            pInert = ((fHe+fN2)>0.0) ? pAbsolute - setpoint * Mvplan.prefs.getPConversion() : 0.0;        
            ppN2Inspired = pInert>0.0 ? (pInert * fN2)/(fHe+fN2) : 0.0;                                       
        } else 
            // Open circuit mode
            ppN2Inspired = (pAbsolute )*fN2;                                            
        double end = ppN2Inspired /0.79 - Mvplan.prefs.getPAmb();
        return end > 0.0 ? end : 0.0;       // Only return positive numbers.
    }
    
    /************* ACCESSORS AND MUTATORS *****************/
    
    /**
     * Gets segment type (CONST, ASCENT, DESCENT, DECO, WAYPOINT, SURFACE)
     * @return segment type
     */
    public int      getType()   { return type; }
    /**
     * Sets segment type
     * @param i Segment type (CONST, ASCENT, DESCENT, DECO, WAYPOINT, SURFACE)
     */
    public void     setType(int i)  { type = i; }
    /**
     * Gets depth of segment
     * @return Depth of segment
     */
    public double   getDepth()  { return depth;}
    /**
     * Gets time of segment
     * @return Time of segment
     */
    public double   getTime()   { return time; }
    /**
     * Gets Gas object used on this segment
     * @return Gas object
     */
    public Gas      getGas()    { return gas; }
    /**
     * Gets setpoint used on this segment (or 0.0 for open circuit)
     * @return Setpoint
     */
    public double   getSetpoint() { return setpoint; }
    /**
     * Gets runTime of this segment
     * @return RunTime of segment.
     */
    public double   getRunTime() { return runTime; }
    /**
     * Returns whether this segment is enabled. Typically used for input segments to dive profile.
     * @return Enabled
     */
    public Boolean  getEnable() { return enable; }
    /**
     * Enables or disables this segment
     * @param state Enabled (<CODE>true</CODE> or <CODE>false</CODE>)
     */
    public void setEnable(Boolean state) { enable = state;}
    /**
     * Sets depth of segment
     * @param d Depth of segment
     */
    public void setDepth(double d) { depth=d;}
    /**
     * Sets time of segment
     * @param t Time of segment
     */
    public void setTime(double t) { time=t;}
    /**
     * Sets runTime of segment
     * @param t RunTime of segment
     */
    public void setRunTime(double t) { runTime = t; }
    /**
     * Sets setPoint of segment
     * @param sp SetPoint of segment
     */
    public void setSetpoint(double sp) { setpoint=sp;}
    /**
     * Sets gas of segment
     * @param g Gas of segment
     */
    public void setGas(Gas g) { gas=g; }
    /**
     * Gets segment type as short symbolic string
     * @return Symbolic string
     */
    public String getTypeString() {
        switch(type) {
            case CONST:
                return "-";
            case ASCENT:
                return "^";
            case DESCENT:
                return "v";
            case DECO:
                return "~";
            case WAYPOINT:
                return ".";
            default:
                return " ";
        }
    }

     /** toString to return String representation of AscDecSegment
     * @return String representation of AscDEcSegment
     */
    public String toString()
    {   
        int timeMins,timeSeconds;
        timeMins=(int)time;
        timeSeconds = (int)((time - (double)timeMins)*60.0);
                        
        return String.format("%1$3.0f"+Mvplan.prefs.getDepthShortString()+" - %2$02d:%3$02dmin, %4$s @%5$3.1f",
              depth, timeMins, timeSeconds ,  gas.toString(), setpoint);

    }
    
    /**
     * Gets string representation of segment
     * @return String representation of segment
     */
    public abstract String toStringLong();    
    /**
     * Gets volume of gas used in litres (cuft)
     * @return Volume of gas used
     */
    public abstract double gasUsed();


}
