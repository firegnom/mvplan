/*
 * OxTox.java
 *
 * Models oxygen toxicity 
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

public class OxTox {
    private double cns;
    private double otu;
    private double maxOx;
    
    
    /** Creates a new instance of OxTox */
    public OxTox() {
    }
    /** Initialise OxTox model */
    public void initOxTox(){
        cns=0.0;
        otu=0.0;
        maxOx=0.0;
    }
    /**
     * Adds oxygen load into model. Uses NOAA lookup table to add percentage based on time and ppO2.
     * @param pO2 Partial pressure of oxygen in atm (not msw!)
     * @param time Time of segment in minutes
     */
    public void addO2( double time, double pO2){
        //assert(time >0.0 && pO2 >= 0.0 && pO2 <= 5.0);
        double d;
        double exposure;   // == mins for cns==100%
        // Calculate OTU using formula OTU= T * (0.5/(pO2-0.5))^-(5/6)
        if(pO2 >0.5) {     // Only accumulate OTUs for ppO2 >= 0.5 atm
            d = time * Math.pow((0.5/(pO2-0.5)),-0.833333); 
            otu+=d;
        }
        // CNS Calculations
        if (pO2>1.8)        exposure=1; // Need a better figure here
        else if (pO2>1.7)   exposure=4;
        else if (pO2>1.6 )  exposure=12;     
        else if (pO2>1.5)  exposure=45;
        else if (pO2>1.4)  exposure=120;
        else if (pO2>1.3)  exposure=150;
        else if (pO2>1.2)  exposure=180;
        else if (pO2>1.1)  exposure=210;
        else if (pO2>1.0)  exposure=240;
        else if (pO2>0.9)  exposure=300;
        else if (pO2>0.8)  exposure=360;        
        else if (pO2>0.7)  exposure=450;
        else if (pO2>0.6)  exposure=570;
        else if (pO2>0.5)  exposure=720;
        else exposure=0;
        if (exposure>0)
            cns+=time/exposure;
        if (pO2 > maxOx) maxOx=pO2;
        //System.out.println("OxTox: add "+time+" min on "+pO2+". OTU="+(int)otu+" CNS="+(int)(cns*100.0)+'\n');        
    }
    /**
     * Removes oxygen load from model during surface intervals
     * @param time Time of segment in minutes
     */
    public void removeO2(double time) {
       if (time >= 1440.0) {    // 24 hrs
           otu=0.0;
       }
       double cnsOld=cns;
       // Decay cns with a halftime of 90mins
       cns=cns * Math.exp(-time*0.693147/90.0);
       //System.out.println("CNS decayed for"+time+". Was ="+cnsOld+", now="+cns);
   } 
    
    /**
     * Gets the maximum ppO2 ecountered
     * @return Maximum ppO2
     */
    public double getMaxOx()    { return maxOx; }
    /**
     * Gets current CNS
     * @return Current CNS
     */
    public double getCns()  { return cns; }
    /**
     * Gets current OTU
     * @return Current OTU
     */
    public double getOtu()  { return otu; }    
    
    /********** ACCESSORS AND MUTATORS FOR BEAN COMPLIANCE ***************/    
    
    /**
     * Sets current CNS (for Bean Compliance)
     * @param d Current CNS
     */
    public void setCns(double d)    { cns=d; }
    /**
     * Sets current OTU (for Bean Compliance)
     * @param d Current OTU
     */
    public void setOtu(double d)    { otu=d; }
    /**
     * Sets Max ppO2 (for Bean Compliance)
     * @param d Max ppO2
     */
    public void setMaxOx(double d)  { maxOx=d; }
}
