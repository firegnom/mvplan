/*
 * SegmentDeco.java
 *
 *  Describes a Deco Segment, a specialisation of AbstractSegment
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

import mvplan.gas.Gas;
import mvplan.main.MvplanInstance;

/**
 * Segment - describes a Deco Segment, a specialisation of AbstractSegment
 * 
 * @author Guy Wittig 
 * @version 17-Dec-2005
 */
public class SegmentDeco extends SegmentAbstract
{
    /** Maximum M-Value Gradient encountered in this segment */
    private double mvMax;
    /** Gradient Factor used in this segment */
    private double gfUsed;
    /** Controlling compartment of this segment */
    private int controlCompartment;

    /** Constructor for objects of class SegmentDeco 
     * @param depth Depth of segment
     * @param time Time of segment
     * @param gas Gas object for segment
     * @param setpoint Setpoint for segment, or 0.0 for open circuit
     */
    public SegmentDeco(double depth, double time, Gas gas, double setpoint )
    {
        super();
        super.depth=depth;
        super.gas=gas;
        super.setpoint=setpoint;
        super.type=super.DECO;
        super.time=time;
        mvMax=0;
        gfUsed=0;
        controlCompartment=-1;
    }
    
    /** Override gasUsed() to determine the gas used in this segment 
     *  @return Gas Used in litres (cuft)
     */
    public double gasUsed()
    {
        if(setpoint>0.0) return(0.0);   // No gas used for closed circuit        
        double p;   // pressure
        p=(depth+MvplanInstance.getMvplan().getPrefs().getPAmb())/MvplanInstance.getMvplan().getPrefs().getPConversion();    // Convert to pressure (atm);
        return( p * time * MvplanInstance.getMvplan().getPrefs().getDecoRMV());
    }


    /** Override toString to return String representation of DecoSegment
     * @return String representation of DecoSegment
     */
    public String toStringLong()
    {
        int timeMins,timeSeconds;
        timeMins=(int)time;
        timeSeconds = (int)((time - (double)timeMins)*60.0);
        
        return String.format("DECO:%1$3.0f"+MvplanInstance.getMvplan().getPrefs().getDepthShortString()+" for %2$02d:%3$02d [%4$3.0f] on %5$s, SP: %6$3.1f, END:%7$3.0f"+MvplanInstance.getMvplan().getPrefs().getDepthShortString()+" M-Value: %8$02.0f%% [%9$02d], GF: %10$02.0f%%",
                    depth,  timeMins,  timeSeconds,  runTime,  gas.toString(), setpoint, getEnd(), mvMax*100, controlCompartment, gfUsed*100);
        
    }
    
    /**************** Accessor and mutator methods ****************/
    /** Sets controlling compartment - required for Bean interface 
     *  @param c Compartment number
     */
    public void setControlCompartment(int c) {controlCompartment=c;}
    /** Sets Gradient factor used - required for Bean Interface
     *  @param gf GradientFactor used
     */
    public void setGfUsed(double gf) {gfUsed=gf;}
    /** Sets Maximum M-Value Gradient for compartment - required for Bean interface
     *  @param mv M-Value Gradient
     */
    public void setMvMax(double mv) {mvMax=mv;}    

}
