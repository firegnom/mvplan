//********************************************************************************
//
//    DiveTools - CNS, OTU and gass managment calculations
//
//    Copyright (C) 2006-2008  Jurij Zelic - vpm.open@gmail.com
//
//    This program is free software; you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation; either version 2 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program; if not, write to the Free Software
//    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
//********************************************************************************
//    Revision history:
//    Jun.  2006: J. Zelic - First Version
//    Nov.  2006: J. Zelic - 1.0.1 fixed bug "ascent rate higher than 10m/min + 
//                oxigen deco returnes error"
//    Dec.  2006: J. Zelic - 1.1.0 added depth2press() 
//    Feb.  2007: J. Zelic - 1.2.0 ICD warning 
//    Apr.  2007: J. Zelic - 1.3.1 Corrected setMetric methode factors
//                                 
//********************************************************************************
package mvplan.util;

public class DiveTools
{

private double depthToPressFactor=10.1972; // 10.1972 if meters 33.45 , if feets

//*****************************************
// Class Constructor: DiveTools
// Input:    /
// Output:   /
//*****************************************
public DiveTools()
{
}

//*****************************************
// Method:   setMetric
// Input:    true if metric false if imperiall
// Output:   /
//*****************************************
public void setMetric(boolean metric)
{
    if (metric)
        depthToPressFactor=10.1972;
    else
        depthToPressFactor=33.45;
}

//*****************************************
// Method:   getSegmentOTU
// Input:    segment data
// Output:   OTU on this segment
//*****************************************
public double getSegmentOTU(double startDepth, double endDepth, double fO2First ,double fO2Second, 
                                double ascentRate, double descenRate, double segmentTime)
{
    // Calculates OTU on a given segment
    //Segment is a accent/decent and then constant depth

    double rate;
    double otu=0;

    //*******************************
    // imput parameter testing
    //*******************************
    if ((ascentRate>-1) || (descenRate<1) || (startDepth<0) || (endDepth<0) ||
         (segmentTime<0) || (fO2First>1) || (fO2First<0)|| (fO2Second>1) || (fO2Second<0))
        return -1;

    if (startDepth>endDepth)
        rate=ascentRate;
    else if (startDepth<endDepth)
        rate=descenRate;
    else
        rate=1; // does not mater, but it maust be value you can devide with

    //*************************************
    // ascent/descent part of the segment
    //*************************************
    double firstSegmentTime = (endDepth-startDepth)/rate;
    if (firstSegmentTime>0)
    {
        double startPres = startDepth/depthToPressFactor + 1;
        double endPres = endDepth/depthToPressFactor + 1;
        double maxPres = Math.max(startPres, endPres);
        double minPres = Math.min(startPres, endPres);
        double maxPO2 = maxPres * fO2First;
        double minPO2 = minPres * fO2First;
        if (maxPO2 > 0.5)
        {
            double lowPO2 = Math.max(0.5 , minPO2);
            double o2Time = firstSegmentTime*(maxPO2 - lowPO2)/(maxPO2 - minPO2);
            otu = 3.0/11.0*o2Time/(maxPO2-lowPO2)*
                  Math.pow((maxPO2-0.5)/0.5 , (11.0/6.0)) - Math.pow((lowPO2-0.5)/0.5 , (11.0/6.0));
            if (otu<0) otu=0;
        }
    }

    //*************************************
    // constant depth part of the segment
    //*************************************
    double secondSegmentTime=segmentTime-firstSegmentTime;
    double segmentPres=endDepth/depthToPressFactor + 1;
    double segmentPO2 = segmentPres * fO2Second;
    if ((segmentPO2>0.5) && (secondSegmentTime>0))
        otu += secondSegmentTime*Math.pow(0.5/(segmentPO2-0.5) , (-5.0/6.0));
    
    return otu;
}

//*****************************************
// Method:   getSegmentCNS
// Input:    segment data
// Output:   CNS on this segment
//*****************************************
public double getSegmentCNS(double startDepth, double endDepth, double fO2First,double fO2Second, 
                                double ascentRate, double descenRate, double segmentTime)
{
    // Calculates CNS on a given segment
    //Segment is a accent/decent and then constant depth

    double rate;
    double cns=0;
    int i;
    final int noSegments=10;
    final double pO2lo[]= {0.5,  0.6,  0.7,  0.8,  0.9, 1.1, 1.5, 1.6, 1.7, 1.8};
    final double pO2hi[]= {0.6,  0.7,  0.8,  0.9,  1.1, 1.5, 1.6, 1.7, 1.8, 2.00};
    final double limSLP[]={-1800,-1500,-1200,-900,-600,-300,-750,-280, -72,  -44};
    final double limINT[]={ 1800, 1620, 1410,1171, 900, 570,1245, 493,139.4, 89 };

    //*******************************
    // imput parameter testing
    //*******************************
    if ((ascentRate>-1) || (descenRate<1) || (startDepth<0) || (endDepth<0) || 
         (segmentTime<0) || (fO2First>1) || (fO2First<0)|| (fO2Second>1) || (fO2Second<0))
        return -1;

    if (startDepth>endDepth)
        rate=ascentRate;
    else if (startDepth<endDepth)
        rate=descenRate;
    else
        rate=1; // does not mater, but it maust be value you can devide with

    //*************************************
    // ascent/descent part of the segment
    //*************************************
    double firstSegmentTime = (endDepth-startDepth)/rate;
    if (firstSegmentTime>0)
    {
        double startPres = startDepth/depthToPressFactor + 1;
        double endPres = endDepth/depthToPressFactor + 1;
        double maxPres = Math.max(startPres, endPres);
        double minPres = Math.min(startPres, endPres);
        double maxPO2 = maxPres * fO2First;
        double minPO2 = minPres * fO2First;
        if (maxPO2>2.0)
            return 5*segmentTime;

        if (maxPO2>0.5)
        {
            double lowPO2 = Math.max(0.5 , minPO2);
            double o2Time = firstSegmentTime*(maxPO2 - minPO2)/(maxPO2 - lowPO2);

            double oTime[]=new double[noSegments];
            double pO2o[]=new double[noSegments];
            double pO2f[]=new double[noSegments];
            double segpO2[]=new double[noSegments];

            for(i=0;i<noSegments;i++)
            {
                if((maxPO2 > pO2lo[i]) && (lowPO2 <= pO2hi[i]))
                {

                    if(startDepth > endDepth)
                    {
                        pO2o[i] = Math.min(maxPO2,pO2hi[i]);
                        pO2f[i] = Math.max(lowPO2,pO2lo[i]);
                    } else
                    {
                        pO2o[i] = Math.max(lowPO2,pO2lo[i]);
                        pO2f[i] = Math.min(maxPO2,pO2hi[i]);
                    }
 
                    segpO2[i] = pO2f[i] - pO2o[i];
                    if (maxPO2!=lowPO2)
                        oTime[i] = firstSegmentTime*Math.abs(segpO2[i])/(maxPO2 - lowPO2);
                    else
                        oTime[i] = 0.0;
                }
                else
                {
                     oTime[i] = 0.0;
                }
            } // for

            for(i=0;i<noSegments;i++)
            {
                double tlimi, mk;
                if(oTime[i] > 0.0)
                {
                    tlimi = limSLP[i] * pO2o[i] + limINT[i];
                    mk = limSLP[i] * (segpO2[i]/oTime[i]);
                    cns+= 1.0/mk * (Math.log(Math.abs(tlimi + mk*oTime[i]))-
                        Math.log(Math.abs(tlimi)));
                }
            } // for
        } // if (maxPO2>0.5)
    }

    //*************************************
    // constant depth part of the segment
    //*************************************
    double secondSegmentTime=segmentTime-firstSegmentTime;
    if (secondSegmentTime>0)
    {
        double segmentPres=endDepth/depthToPressFactor + 1;
        double segmentPO2 = segmentPres * fO2Second;
        double tlim=0;

        if (segmentPO2>2.0)
            return (cns+5*secondSegmentTime);

        if (segmentPO2>0.5)
        {
            for(i=0;i<noSegments;i++)
            {
                if((segmentPO2 > pO2lo[i]) && ( segmentPO2 <= pO2hi[i]))
                {
                    tlim = limSLP[i]*segmentPO2 + limINT[i];
                    break;
                }
            } // for
            if (tlim>0)
                cns+= secondSegmentTime/tlim;
            else
                return -1;

        } // if (maxPO2>0.5)
    }
    
    return cns;
}

//*****************************************
// Method:   getSegmentGas
// Input:    segment data
// Output:   gas needed on this segment
//*****************************************
public double getSegmentGas(double startDepth, double endDepth, double rmv,
                                double ascentRate, double descenRate, double segmentTime, long segment)
{
	  // segment==1 calculate gas only on accent/descent part of a segment
	  // segment==2 calculate gas only on constant depth part of a segment
	  // segment==3 calculate gas on both parts of a segment
    // Calculates gas needed on a given segment
    //Segment is a accent/decent and then constant depth

    double rate;
    double gas=0;

    //*******************************
    // imput parameter testing
    //*******************************
    if ((ascentRate>-1) || (descenRate<1) || (startDepth<0) || (endDepth<0) || (segmentTime<0) )
        return -1;

    if (startDepth>endDepth)
        rate=ascentRate;
    else if (startDepth<endDepth)
        rate=descenRate;
    else
        rate=1; // does not mater, but it maust be value you can devide with

    // if not enough time
    if ( (startDepth != endDepth)  &&  (((endDepth-startDepth)/rate) > segmentTime) )
        return -1;

    //*************************************
    // ascent/descent part of the segment
    //*************************************
    double firstSegmentTime = (endDepth-startDepth)/rate;
    if (firstSegmentTime>0)
    {
        double startPres = startDepth/depthToPressFactor + 1;
        double endPres = endDepth/depthToPressFactor + 1;
        if ((segment & 1)==1)
            gas=(startPres+endPres)/2*rmv*firstSegmentTime;
    }

    //*************************************
    // constant depth part of the segment
    //*************************************
    double secondSegmentTime=segmentTime-firstSegmentTime;
    double segmentPres=endDepth/depthToPressFactor + 1;
    if ((segment & 2)==2)
        gas+=segmentPres*rmv*secondSegmentTime;
    
    return gas;
}

//*****************************************
// Method:   depth2press
// Input:    depth
// Output:   preasure at depth/bar
//*****************************************
public double depth2press(double depth)
{
	  return (depth/depthToPressFactor + 1);
}

}// end of class

   