/**
 * Compartment.java
 *
 * Defines a single Buhlmann compartment.
 * 
 *   @author Guy Wittig
 *   @version 1-Jun-2009
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

import mvplan.main.*;
import java.io.*;
import mvplan.prefs.Prefs;


public class Compartment implements Serializable
{
    private double kHe,kN2;         // Time constants - calculated from halftimes
    private double aHe,bHe,aN2,bN2; // A and b co-efficients
    private double ppHe, ppN2;            // partial pressure
    static final double LOG2=0.69315;           // ln(2)
    private double factorComp, factorDecomp;    // COnservatism factors

    /**
     * Constructor for Compartments. Initialises partial pressures to zero.
     */    
    public Compartment() {  
        // Initialise compartment pressures to zero
        ppHe=0.0d;
        ppN2=0.0d;
        factorComp=Prefs.getPrefs().getFactorComp();
        factorDecomp=Prefs.getPrefs().getFactorDecomp();
    }
        
    /**
     * Sets compartment's time constants
     * @param hHe Halftime, helium
     * @param hN2 Halftime, Nitrogen
     * @param aHe a coefficient, Helium
     * @param bHe b coefficient, Helium
     * @param aN2 a coefficient, Nitrogen
     * @param bN2 b coefficient, Nitrogen
     */ 
    public void setCompartmentTimeConstants(double hHe,double hN2,double aHe,double bHe,double aN2,double bN2)
    {
        kHe=LOG2/hHe;   // Time constants
        kN2=LOG2/hN2;
        this.aHe=aHe;   // Co-efficients
        this.bHe=bHe;
        this.aN2=aN2;
        this.bN2=bN2;       
    }
    
    
    /**
     * Sets partial pressures of He and N2 - in msw (fsw)
     * @param ppHe Partial pressure of Helium
     * @param ppN2 Partial pressure of Nitrogen
     * @throws mvplan.model.ModelStateException Throws ModelStateException if partial pressures are < 0.0 or time < 0.0
     */
    public void setpp(double ppHe, double ppN2) throws ModelStateException
    {
        if(ppHe<0.0 || ppN2 <0.0) {
            if((Mvplan.DEBUG > 0)) System.out.println("ERROR: setpp() is throwing exception, ppHe="+ppHe+" ppN2="+ppN2);        
            throw new ModelStateException("Error in argument: Compartment.setpp()");
        }
        else {
            this.ppHe = ppHe;
            this.ppN2 = ppN2;
        }
    }

    /**
     * Constant depth calculations. Uses instananeous equation: P = Po + (Pi - Po)(1-e^-kt)
     * Updated to use conservatism factors on Compression and Decompression
     * @param ppHeInspired Partiap pressure of inspired helium
     * @param ppN2Inspired Partial pressure of inspired Nitrogen
     * @param segTime Segment time in minutes
     * @throws mvplan.model.ModelStateException Throws ModelStateException if partial pressures are < 0.0 or time < 0.0
     */
    public void constDepth(double ppHeInspired, double ppN2Inspired, double segTime) throws ModelStateException
    {
       double deltaHe, deltaN2;
       
       if(ppHeInspired<0.0 || ppN2Inspired <0.0 || segTime <0.0){
            if((Mvplan.DEBUG > 0)) System.out.println("ERROR: constDepth() is throwing exception, ppHe="+ppHeInspired+" ppN2="+ppN2Inspired);  
            throw new ModelStateException("Error in argument: Compartment.constDepth()");
       }

       // Calculate change in pp
       deltaHe= ((ppHeInspired-ppHe) * (1-Math.exp(-kHe*segTime)));
       deltaN2= ((ppN2Inspired-ppN2) * (1-Math.exp(-kN2*segTime)));
       // Apply conservatism factors
       deltaHe = (deltaHe > 0.0d ) ? deltaHe * factorComp : deltaHe * factorDecomp;
       deltaN2 = (deltaN2 > 0.0d ) ? deltaN2 * factorComp : deltaN2 * factorDecomp;

       // Apply to compartment
       ppHe = ppHe + deltaHe;
       ppN2 = ppN2 + deltaN2;
    
    }

    /**
     * Ascend or descend calculation
     * UsesEquation: P=Pio+R(t -1/k)-[Pio-Po-(R/k)]e^-kt
     * @param ppHeInspired Partiap pressure of inspired helium
     * @param ppN2Inspired Partial pressure of inspired Nitrogen
     * @param rateHe Rate of change of ppHe
     * @param rateN2 Rate of change of ppHe
     * @param segTime Segment time in minutes
     * @throws mvplan.model.ModelStateException Throws ModelStateException if partial pressures are < 0.0 or time < 0.0
     */
    public void ascDec(double ppHeInspired, double ppN2Inspired, double rateHe, double rateN2, double segTime)
        throws ModelStateException
    {        
        if(ppHeInspired<0.0 || ppN2Inspired<0.0 || segTime < 0.0) {
            if((Mvplan.DEBUG > 0)) System.out.println("ERROR: ascDec() is throwing exception, ppHe="+ppHeInspired+" ppN2="+ppN2Inspired);  
            throw new ModelStateException("Error in argument: Compartment.ascDec()");
        }

        ppHe = ppHeInspired + rateHe * (segTime - (1.0/kHe)) - (ppHeInspired - ppHe - (rateHe/kHe)) * Math.exp(-kHe*segTime);
        ppN2 = ppN2Inspired + rateN2 * (segTime - (1.0/kN2)) - (ppN2Inspired - ppN2 - (rateN2/kN2)) * Math.exp(-kN2*segTime);
    	
    }
 
     /**
     * Gets M-Value for given ambient pressure uning the Buhlmann equation
     * Pm = Pa/b +a         where: Pm = M-Value pressure,
      *                             Pa = ambinet pressure
      *                             a,b co-efficients
      * Not used for decompression but for display of M-value limit line
      * Note that this does not factor gradient factors.
      *
     * @param p = Pressure, ambient, absolute in msw (fws)
     * @return Maximum tolerated pressure in mws (fws)
     */
    public double getMvalueAt(double p)
    {
        double aHeN2,bHeN2;
        double pHeN2;

        pHeN2 = ppHe + ppN2;    // Sum partial pressures
        // Calculate adjusted a, b coefficients based on those of He and N2
        aHeN2 = ((aHe * ppHe) + (aN2 * ppN2)) / pHeN2;
        bHeN2 = ((bHe * ppHe) + (bN2 * ppN2)) / pHeN2;
        
        return ( p / bHeN2 + aHeN2);
    }
   
    /**
     * Gets Tolerated Absolute Pressure for the compartment
     * @param gf = gradient factor, 0.1 to 1.0, typical 0.2 - 0.95
     * @return Maximum tolerated pressure in mws (fws)
     */
    public double getMaxAmb(double gf)
    {
        double aHeN2,bHeN2;
        double pHeN2;

        pHeN2 = ppHe + ppN2;    // Sum partial pressures
        // Calculate adjusted a, b coefficients based on those of He and N2
        aHeN2 = ((aHe * ppHe) + (aN2 * ppN2)) / pHeN2;
        bHeN2 = ((bHe * ppHe) + (bN2 * ppN2)) / pHeN2;
        
        return (pHeN2 - aHeN2*gf)/(gf/bHeN2-gf+1.0);
    }

    /**
     * Gets M-Value for a compartment, given an ambient pressure
     * @param pAmb Ambient pressure
     * @return M-Value
     */
    public double getMV(double pAmb)
    {
        double aHeN2,bHeN2;
        double pHeN2;
        pHeN2 = ppHe + ppN2;    // Sum partial pressures
        // Calculate adjusted a, b coefficients based on those of He and N2
        aHeN2 = ((aHe * ppHe) + (aN2 * ppN2)) / pHeN2;
        bHeN2 = ((bHe * ppHe) + (bN2 * ppN2)) / pHeN2;

        return pHeN2 / (pAmb/bHeN2 + aHeN2);
    }
    
    /************************* ACCESSORS AND MUTATORS *****************/
    /* This is required for bean compliance so as to allow serialisation 
     * of Compartments to XML
     */

    /**
     * Gets partial pressure of Helium of the compartment in msw (fsw)
     * @return Partial Pressure of Helium in msw (fsw)
     */
    public double getPpHe()         { return ppHe; }
    /**
     * Gets Partial pressure of Nitrogen for the compartment in msw (fsw)
     * @return Partial pressure of Nitrogen in msw (fsw)
     */
    public double getPpN2()         { return ppN2; }
    /**
     * Sets partial pressure of Helium of the compartment in msw (fsw)
     * @param p Partial pressure of Helium in msw (fsw)
     */
    public void setPpHe(double p)   { ppHe=p; }
    /**
     * Sets partial pressure of Nitrogen of the compartment in msw (fsw)
     * @param p Partial pressure of Nitrogen in msw (fsw)
     */
    public void setPpN2(double p)   { ppN2=p; }
    /**
     * Gets time constant K for Helium
     * @return Time constant K
     */
    public double getKHe()          { return kHe; }
    /**
     * Gets time constant K for Nitrogen
     * @return Gets time constant K for Helium
     */
    public double getKN2()          { return kN2; }
    /**
     * Sets time constant K for Helium
     * @param k Time constant K
     */
    public void setKHe(double k)    { kHe=k; }
    /**
     * Sets time constant K for Nitrogen
     * @param k Time constant K
     */
    public void setKN2(double k)    {kN2=k; }
    /**
     * Gets Buhlmann A factor for Helium
     * @return Buhlmann A factor
     */
    public double getAHe()          { return aHe; }
    /**
     * Gets Buhlmann B factor for Helium
     * @return Buhlmann B factor
     */
    public double getBHe()          { return bHe; }
    /**
     * Gets Buhlmann A factor for Nitrogen
     * @return Buhlmann A factor
     */
    public double getAN2()          { return aN2; }
    /**
     * Gets Buhlmann B factor for Nitrogen
     * @return Buhlmann B factor
     */
    public double getBN2()          { return bN2; }
    /**
     * Sets Buhlmann A factor for Helium
     * @param d Buhlmann A factor
     */
    public void setAHe(double d)    { aHe=d; }
    /**
     * Sets Buhlmann B factor for Helium
     * @param d Buhlmann B factor
     */
    public void setBHe(double d)    { bHe=d; }
    /**
     * Sets Buhlmann A factor for Nitrogen
     * @param d Buhlmann A factor
     */
    public void setAN2(double d)    { aN2=d; }
    /**
     * Sets Buhlmann B factor for Nitrogen
     * @param d Buhlmann B factor
     */
    public void setBN2(double d)    { bN2=d; }         
           
}
