/*
 * SegmentDive.java
 *
 *   Describes a dive segment, a specialisation of AbstractSegment
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
import mvplan.prefs.Prefs;

public class SegmentDive extends SegmentAbstract 
{

    /** Constructor for objects of class SegmentDive
     * @param depth Depth of segment
     * @param time Time of segment
     * @param gas Gas object for segment
     * @param setpoint Setpoint for segment, or 0.0 for open circuit
     */
    public SegmentDive(double depth, double time, Gas gas, double setpoint )
    {
        // Initialise super fields
        super();
        super.depth=depth;
        super.gas=gas;
        super.setpoint=setpoint;
        super.type=CONST;
        super.time=time;
    }

    /** Empty constructor for DiveSegment */
    public SegmentDive() { 
        super();
        // Make sure it has at least air as a gas
        super.gas=new Gas(0.0,0.21,66.0);
    }

    /** Override gasUsed() to determine the gas used in this segment 
     *  @return gasUsed in litres (cuft)
     */
    public double gasUsed()
    {
    	Prefs prefs = MvplanInstance.getPrefs();
		boolean ocMode = prefs.isOcMode();
        if(setpoint>0.0 && !ocMode) return(0.0);
        double p;   // pressure
        p=(depth+prefs.getPAmb())/prefs.getPConversion();    // Convert to pressure (atm);
        return( p * time * prefs.getDiveRMV());
    }


    /** Override toString to return text value 
     * @return String representation of Dive Segment
     */
    public String toStringLong()
    {
        int timeMins,timeSeconds;
        timeMins=(int)time;
        timeSeconds = (int)((time - (double)timeMins)*60.0);

        return String.format("DIVE:%1$3.0f"+MvplanInstance.getMvplan().getPrefs().getDepthShortString()+" for %2$02d:%3$02d [%4$3.0f] on %5$s, SP: %6$3.1f, END:%7$3.0f"+MvplanInstance.getMvplan().getPrefs().getDepthShortString(),
                    depth,  timeMins,  timeSeconds,  runTime,  gas.toString(), setpoint, getEnd());
        
    }

	
}

