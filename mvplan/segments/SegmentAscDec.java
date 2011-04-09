/*
 * SegmentAscDec.java
 *
 * Class of segment for describing ascent/descent dive segments. Specialisation of AbstractSegment
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

import mvplan.main.Mvplan;
import java.text.MessageFormat;
import mvplan.gas.Gas;

public class SegmentAscDec extends SegmentAbstract
{
    /** Ascent (+ve)/ Descent (-ve) rate in msw/sec */
    private double rate;    // Ascent (+ve)/ Descent (-ve) rate in msw/sec

    /**
     * Constructor for objects of class SegmentAscDec
     * @param gas Gas object for this segment
     * @param startDepth Starting depth of segment in m (ft)
     * @param endDepth Ending depth of segment in m (ft)
     * @param rate Rate of change of depth in m/min (ft/min)
     * @param setpoint Setpoint for segment, or 0.0 for open circuit
     */
    public SegmentAscDec(double startDepth, double endDepth, double rate, Gas gas, double setpoint)
    {       
            // Init super fields
            super();
            super.depth=endDepth;
            super.gas=gas;
            super.setpoint=setpoint;
            // Init this class fields
            this.rate=rate;
            super.time = (endDepth-startDepth)/rate;
            if (startDepth < endDepth) 
                    super.type=super.DESCENT;
            else
                    super.type=super.ASCENT;
    }

    /** Override gasUsed() to determine the gas used in this segment  
     *  @return Gas Used in litres (cuft)
     */
    public double gasUsed()
    {
        if(setpoint>0.0) return(0.0);
        
        double p;   // pressure
        double d;   // depth
        double startDepth;
        
        startDepth=depth - rate*time;
        // Calculate average depth
        d=startDepth+(depth-startDepth)/2.0;
        p=(d+Mvplan.prefs.getPAmb())/Mvplan.prefs.getPConversion();    // Convert to pressure (atm);     
        return( p * time * Mvplan.prefs.getDiveRMV());
    }

    /** Gets ascent rate for segment
     * @return Ascent Rate in m/min (ft/min)
     */
    public double   getRate()   { return rate; }

    /** Override toString to return String representation of AscDecSegment
     * @return String representation of AscDEcSegment
     */
    public String toStringLong()
    {   
        String s;
        int timeMins,timeSeconds;
        timeMins=(int)time;
        timeSeconds = (int)((time - (double)timeMins)*60.0);
        
        if (super.type == super.ASCENT)  s="ASC "; else s="DESC";
        
        return String.format("%1$4s:%2$3.0f"+Mvplan.prefs.getDepthShortString()+" for %3$02d:%4$02d [%5$3.0f] on %6$s, SP: %7$3.1f, END:%8$3.0f"+Mvplan.prefs.getDepthShortString(),
              s, depth, timeMins, timeSeconds ,  runTime,  gas.toString(), setpoint, getEnd());

    }

}
