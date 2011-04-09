/**
 * AbstractModel.java <br/>
 *
 * Base class for dive models.<br/>
 * Composed of a tissue array of Compartment[]<br/>
 * Has an OxTox and Gradient object <br/>
 * Can throw a ModelStateException propagated from a Compartment if pressures or time is out of bounds.<br/>
 *
 * Model can be initialised from scratch or may be rebuilt from a saved Model via the ModelDAO class. <br/>
 * Models are initialised by initModel() if they are new models, or<br/>
 * validated by validateModel() if they are rebuild from a saved model.<br/>
 *
 * The model is capable of ascending or descending via ascDec() using the ascDec() method of Compartment,<br/>
 * or accounting for a constant depth using the constDepth() method of Compartment.<br/>
 *
 *   @author Guy Wittig
 *   @version 17-Apr-2010
 *
 *   This program is part of MV-Plan
 *   Copywrite 2010 Guy Wittig
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
import java.io.Serializable;
import mvplan.main.Mvplan;


/**
 *
 * @author guy
 */
public abstract class AbstractModel implements Serializable {

    Compartment[] tissues;          // Buhlmann tissues == array of compartments
    Gradient gradient;              // Gradient factor object
    OxTox oxTox;                    // Oxygen toxicity model
    String metaData;                // Stores information about where the model was created
    int units;                      // Metric or imperial units
    String modelName;        // Contains model name


     /** Return value - ZHL16BModel validated correctly */
    public static int MODEL_VALIDATION_SUCCESS=0;
    /** Return value - ZHL16BModel failed to validate correctly */
    public static int MODEL_VALIDATION_FAILED=-1;
    /** Indicates metric units used for model - msw */
    public static int METRIC=0;
    /** Indicates imperial units used for model - fsw */
    public static int IMPERIAL=1;


    public abstract void initModel();
    public abstract int validateModel();
    public abstract int controlCompartment();
    public abstract double ceiling();
    public abstract double mValue(double depth);
    public abstract void constDepth(double depth, double segTime, double fHe, double fN2, double pO2) throws ModelStateException;
    public abstract void ascDec(double start, double finish, double rate, double fHe, double fN2,double pO2 ) throws ModelStateException;
    public abstract void printModel();


     /**
     * Initialises the model's gradient factor object
     */
    public void initGradient() {
        gradient = new Gradient(Mvplan.prefs.getGfLow(),Mvplan.prefs.getGfHigh());        // Default Gradient factors
    }

    /**
     * Initialise OxTox model
     */
    public void initOxTox() {
        oxTox = new OxTox();
        oxTox.initOxTox();
    }

    /****************** Accessor and mutator bean methods ****************/
    /**
     * Gets Gradient object for this model
     * @return return Gradient object
     */
    public Gradient getGradient()           { return gradient; }
    /**
     * Sets Gradient object for this model
     * @param g Gradient object
     */
    public void setGradient(Gradient g)     { gradient = g; }
    /**
     * Gets OxTox object for this model
     * @return OxTox object
     */
    public OxTox getOxTox()                 { return oxTox; }
    /**
     * Sets OxTox object for this model
     * @param o OxTox Object
     */
    public void setOxTox(OxTox o)           { oxTox = o; }
    /**
     * Gets tissues as Array of Compartment[] for this model
     * @return tissues - Array of Compartment[]
     */
    public Compartment[] getTissues()       { return tissues; }
    /**
     * Sets tissues array of Compartment[]
     * @param t Tissue array of Compartment
     */
    public void setTissues(Compartment[] t) { tissues=t; }
    /**
     * Gets metadata string for this model
     * @return Metadata String
     */
    public String getMetaData()             { return metaData; }
    /**
     * Sets metadata String for this model
     * @param s Metadata String
     */
    public void setMetaData(String s)       { metaData=s;}

    /**
     * Gets units for this model: METRIC or IMPERIAL
     * @return units: METRIC or IMPERIAL
     */
    public int getUnits() {
        return units;
    }      

    /**
     * Sets units for this model: METRIC or IMPERIAL
     * @param units Sets units: METRIC or IMPERIAL
     */
    public void setUnits(int units) {
        this.units = units;
    }

    /**
     * Gets model name
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Sets model name
     */
    public void setModelName(String n) {
        modelName=n;
    }       

}
