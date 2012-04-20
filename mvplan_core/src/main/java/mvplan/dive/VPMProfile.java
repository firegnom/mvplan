/*
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

package mvplan.dive;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import mvplan.gas.Gas;
import mvplan.main.MvplanInstance;
import mvplan.model.*;
import mvplan.prefs.Prefs;
import mvplan.segments.SegmentAscDec;
import mvplan.segments.SegmentDive;
import mvplan.segments.SegmentDeco;
import mvplan.segments.SegmentAbstract;

public class VPMProfile
{
    private Prefs prefs = MvplanInstance.getMvplan().getPrefs();
    
    public VPMProfile(ArrayList <SegmentAbstract> knownSegments, ArrayList <Gas> knownGases, AbstractModel m)
    {}
    public int doDive(){ return 0; }

}



