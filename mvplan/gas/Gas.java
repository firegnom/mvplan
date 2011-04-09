/*
 * Gas.java
 *
 *  Represents a breathing gas. Maintains fractions of O2, He and N2, 
 *  provides MOD and accumulates volume used in a dive.
 *
  * @author Guy Wittig
 *  @version 17-Dec-2005
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

package mvplan.gas;

import java.io.*;
import java.text.MessageFormat;
import mvplan.main.Mvplan;


public class Gas implements Comparable, Serializable, Cloneable
{
    private double fHe,fO2; // Gas fractions
    private double mod;     // Maximum operating depth 
    private double volume;  // Accumulate gas volume
    private boolean enable; // Gas is enabled for use
    
    /** Constructor for Gas objects. Fractions must add to <= 1.0. Remainder assumed Nitrogen.
     *  If constructed with erroneous data is set up as air.
     *
     *  @param fHe Fraction of helium (0.0 - 1.0)
     *  @param fO2 Fraction of Oxygen (0.0 - 1.0)
     *  @param mod Maximum Operating depth in m (ft)
     */    
    public Gas(double fHe, double fO2, double mod)
    {   
        setGas(fHe,fO2,mod);
        enable=true; //Boolean.TRUE;
        this.volume=0.0;
    }
    /** Empty constructor for bean interface */
    public Gas() {            
    }
    
    /* 
     * Method to validate a field for limits only 
     */
    public static boolean validate(String field, double value) {
        if(field.equals("fHe") || field.equals("fO2"))
            return ( value >= 0.0 && value <=1.0);
        if(field.equals("mod"))
            // Need to hard code nominal max value due to potential of prefs not being fully set up when this is called
            return (value >=0.0 && value <= 900.0);
        return false;        
    }
    
    /* 
     * Method to validate all inputs (fO2, fHe and MOD)
     */
    public static boolean validate(double fHe, double fO2, double mod) {
        boolean passed=true;
        // Check individual fields for bounds   
        passed = (passed && validate("fHe",fHe));
        passed = (passed && validate("fO2",fO2));
        passed = (passed && validate("mod",mod));
        if(!passed) return false;
        // Check combined fractions
        passed = (passed && (fHe+fO2)<=1.0);
        if(!passed) return false;
        
        // Check MOD for sensible value   
        if(fO2 == 0.0 && mod == 0.0)    // Leave empty gases alone to allow construction
            return passed;
        if(Mvplan.prefs != null) {   // Need to check that prefs exists. We can get to this point during the initilisation of the prefs object
            double d = ((mod+Mvplan.prefs.getPConversion())/Mvplan.prefs.getPConversion()*fO2);
            passed = (d <= Mvplan.prefs.getMaxMOD()+0.05);  // Tolerance of 0.05 to prevent unneccessary failure due to rounding
        }
            
        return passed;        
    }        
    /* 
     * Method to get a maximum MOD based on O2 fraction
     */
    public static double getMaxMod(double o) {
        return (Mvplan.prefs.getMaxMOD()/o * Mvplan.prefs.getPConversion())-Mvplan.prefs.getPConversion();
    }
    /* 
     * Method to get a MOD based on O2 fraction and maximum ppO2
     */
    public static double getMod(double fO2, double ppO2) {
        return (ppO2/fO2 * Mvplan.prefs.getPConversion())-Mvplan.prefs.getPConversion();
    }
    /* 
     * Method to get a ppO2 based on O2 fraction and MOD
     */
    public static double getppO2(double f, double m) {
        return ( (m+Mvplan.prefs.getPConversion())*f/Mvplan.prefs.getPConversion());
    }
    
    /**
     * Override the clone method to make it public
     *  @return Cloned Gas object
     *  @throws CloneNotSupportedException Never thrown and required for Cloneable interface
     */
    public Object clone () {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("Never happens");
        }
    }

    /** Used to implement the Comparable interface. To compare gases
      *  based on their mod (Maximum Operating Depth).
     *  @param  Object (Gas) to compare to
     *  @return Integer, Mod of compared Gas - Mod of this gas
      */
    public int compareTo(Object o)
    {
        double m;
        Gas g;
        g = (Gas)o;
        m=g.getMod();
        return (int)(m-mod);
    }

    // Accessors
    public double getFHe() { return fHe; }
    public double getFO2() { return fO2; }
    public double getFN2()  { 
        double n=1.0-fHe-fO2;
        if (n<0.0001)
            return 0.0;
        else
            return n;
    }
    public double getMod() { return mod; }
    public double getVolume() {return volume;}        
    public boolean getEnable() { return enable; }
    
    // Mutators - used for Bean serialization ONLY
    public void setFHe(double f)    {fHe=f;}
    public void setFO2(double f)    {fO2=f;}
    public void setMod(double m)    {mod=m;}    
    public void setEnable(boolean state) { enable = state;}
    public void setVolume(double vol) { volume=vol;}

    /* setGas() - sets gas fractions
     * or creates Air by default
     */
    public void setGas(double fHe,double fO2, double mod)
    {
       if( validate(fHe, fO2, mod)) {
            this.fHe=fHe;
            this.fO2=fO2;
            this.mod=mod;
        } else  {           // Set it up for air
            this.fHe=0.;
            this.fO2=.21;
            this.mod=66.;
        }
    }
    
    /* 
     * Construct a human readable name for this gas and override Object.toString method
     */
    public String toString()
    {  
        String name;
        String composition;
        if (fHe==0.0)
            composition = String.valueOf((int)Math.round(fO2*100.0));
        else
            composition  = ( (int)Math.round(fO2*100.0) +"/" + (int)Math.round(fHe*100.0));
        if(fHe > 0.0) {
            if ((fHe + fO2) == 1.0)
                name = Mvplan.getResource("mvplan.gas.Gas.heliox")+" " + composition;
            else
                name = Mvplan.getResource("mvplan.gas.Gas.trimix")+" "+ composition;
        }
        else if (fO2==0.21) name = Mvplan.getResource("mvplan.gas.Gas.air");
        else if (fO2==1.0) name = Mvplan.getResource("mvplan.gas.Gas.oxygen");
        else name = Mvplan.getResource("mvplan.gas.Gas.nitrox")+" "+composition;
        return(name);       
    }
    
    /*
     * Make short name for tables
     */
    public String getShortName()
    {
        if (fHe==0.0) {
            Object[] obs={new Integer((int)Math.round(fO2*100.0))};
            if(fO2==1.0) 
                return MessageFormat.format("  {0,number,000}",obs);
            else 
                return MessageFormat.format("   {0,number,00}",obs);
        } else {
            Object[] obs={new Integer((int)Math.round(fO2*100.0)),new Integer((int)Math.round(fHe*100.0))};
            return MessageFormat.format("{0,number,00}/{1,number,00}",obs);
        }
    }

}