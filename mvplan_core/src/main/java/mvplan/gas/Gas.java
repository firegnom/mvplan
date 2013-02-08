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
import java.util.UUID;

import mvplan.main.MvplanInstance;
import mvplan.prefs.Prefs;
import mvplan.util.GasUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class Gas.
 */
public class Gas implements Comparable<Gas>, Serializable, Cloneable
{
    
    /** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The unique id of the gas. */
	private UUID id;
	
	/** Gas fractions. */
	private double fHe,fO2;
	
	/** Maximum operating depth. */
    private double mod;     
    
    /** Accumulate gas volume. */
    private double volume;  
    
    /** Gas is enabled for use. */
    private boolean enable; 
    
    /** Constructor for Gas objects. Fractions must add to <= 1.0. Remainder assumed Nitrogen.
     *  If constructed with erroneous data is set up as air.
     *
     *  @param fHe Fraction of helium (0.0 - 1.0)
     *  @param fO2 Fraction of Oxygen (0.0 - 1.0)
     *  @param mod Maximum Operating depth in m (ft)
     */    
    public Gas(double fHe, double fO2, double mod)
    {   
    	id = UUID.randomUUID();
        setGas(fHe,fO2,mod);
        enable=true; 
        this.volume=0.0;
    }
    
    
    /** Constructor for Gas objects. Fractions must add to <= 1.0. Remainder assumed Nitrogen.
     *  If constructed with erroneous data is set up as air.
     *  <br><br>
     *  Mod is set to be maximum mod for specified fO2 
     *
     *  @param fHe Fraction of helium (0.0 - 1.0)
     *  @param fO2 Fraction of Oxygen (0.0 - 1.0)
     */    
    public Gas(double fHe, double fO2) {
		this(fHe, fO2, GasUtils.getMaxMod(fO2));
	}
	
	/**
	 * Override the clone method to make it public.
	 *
	 * @return Cloned Gas object
	 */
    @Override
    public Object clone () {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("Never happens");
        }
    }

    /**
     * Used to implement the Comparable interface. To compare gases
     * based on their mod (Maximum Operating Depth).
     *
     * @param g the g
     * @return Integer, Mod of compared Gas - Mod of this gas
     */
    public int compareTo(Gas g)
    {
    	
        double m;
        m=g.getMod();
        return (int)(m-mod);
    }

    // Accessors
    /**
     * Gets the f he.
     *
     * @return the f he
     */
    public double getFHe() { return fHe; }
    
    /**
     * Gets the f o2.
     *
     * @return the f o2
     */
    public double getFO2() { return fO2; }
    
    /**
     * Gets the f n2.
     *
     * @return the f n2
     */
    public double getFN2()  { 
        double n=1.0-fHe-fO2;
        if (n<0.0001)
            return 0.0;
        else
            return n;
    }
    
    /**
     * Gets the mod.
     *
     * @return the mod
     */
    public double getMod() { return mod; }
    
    /**
     * Gets the volume.
     *
     * @return the volume
     */
    public double getVolume() {return volume;}        
    
    /**
     * Gets the enable.
     *
     * @return the enable
     */
    public boolean getEnable() { return enable; }
    
    // Mutators - used for Bean serialization ONLY
    /**
     * Sets the f he.
     *
     * @param f the new f he
     */
    public void setFHe(double f)    {fHe=f;}
    
    /**
     * Sets the f o2.
     *
     * @param f the new f o2
     */
    public void setFO2(double f)    {fO2=f;}
    
    /**
     * Sets the mod.
     *
     * @param m the new mod
     */
    public void setMod(double m)    {mod=m;}    
    
    /**
     * Sets the enable.
     *
     * @param state the new enable
     */
    public void setEnable(boolean state) { enable = state;}
    
    /**
     * Sets the volume.
     *
     * @param vol the new volume
     */
    public void setVolume(double vol) { volume=vol;}

    /**
     * setGas() - sets gas fractions
     * or creates Air by default.
     *
     * @param fHe the f he
     * @param fO2 the f o2
     * @param mod the mod
     */
    public void setGas(double fHe,double fO2, double mod)
    {
       if( GasUtils.validate(fHe, fO2, mod)) {
            this.fHe=fHe;
            this.fO2=fO2;
            this.mod=mod;
        } else  {           // Set it up for air
            this.fHe=0.;
            this.fO2=.21;
            this.mod=66.;
        }
    }
    @Override
    public boolean equals(Object obj) {
    	if (obj instanceof Gas){
    		return id.equals(((Gas) obj).id);
    	}
    	return false;
    }
    
    /* 
     * Construct a human readable name for this gas and override Object.toString method
     */
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
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
                name = MvplanInstance.getMvplan().getResource("mvplan.gas.Gas.heliox")+" " + composition;
            else
                name = MvplanInstance.getMvplan().getResource("mvplan.gas.Gas.trimix")+" "+ composition;
        }
        else if (fO2==0.21) name = MvplanInstance.getMvplan().getResource("mvplan.gas.Gas.air");
        else if (fO2==1.0) name = MvplanInstance.getMvplan().getResource("mvplan.gas.Gas.oxygen");
        else name = MvplanInstance.getMvplan().getResource("mvplan.gas.Gas.nitrox")+" "+composition;
        return(name);       
    }
    
    /**
     * Make short name for tables.
     *
     * @return the short name
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

	/**
	 * Gets the unique id for the gas.
	 *
	 * @return the id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Sets the unique id.
	 *
	 * @param id the new id
	 */
	public void setId(UUID id) {
		this.id = id;
	}

}