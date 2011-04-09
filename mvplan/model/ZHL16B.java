/**
 * ZHL16B.java <br/>
 *
 * Represents a Buhlmann model.<br/>
 * Composed of a tissue array of Compartment[]<br/>
 * Has an OxTox and Gradient object <br/>
 * Can throw a ModelStateException propagated from a Compartment if pressures or time is out of bounds.<br/>
 * 
 * ZHL16B can be initialised from scratch or may be rebuilt from a saved ZHL16B via the ModelDAO class. <br/>
 * Models are initialised by initModel() if they are new models, or<br/>
 * validated by validateModel() if they are rebuild from a saved model.<br/> 
 * 
 * The model is capable of ascending or descending via ascDec() using the ascDec() method of Compartment,<br/>
 * or accounting for a constant depth using the constDepth() method of Compartment.<br/>
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

import java.io.Serializable;

public class ZHL16B extends AbstractZHL16Model implements Serializable
{
    
    
    /**
     * Constructor for objects of class ZHL16B
     */
    @Override
    public void initModel() {
        super.initModel();
        modelName="ZHL16B";
    }

    /**
     * Initialise time constants in Buhlmann tissue array
     */
    protected void setTimeConstants(){

        if(units == METRIC) {
            // public Compartment(double hHe,double hN2,double aHe,double bHe,double aN2,double bN2)
            // This is for Buhlmann ZHL-16B with the 1b halftimes
            // a = intercept at zero ambient pressure
            // b = reciprocal of slope of m-value line
            // public Compartment                  (hHe,    hN2,    aHe,    bHe,    aN2,    bN2
            tissues[0].setCompartmentTimeConstants(1.88,    5.0,    16.189, 0.4770, 11.696, 0.5578);         
            tissues[1].setCompartmentTimeConstants(3.02,    8.0,    13.83,  0.5747, 10.0,   0.6514);
            tissues[2].setCompartmentTimeConstants(4.72,    12.5,   11.919, 0.6527, 8.618,  0.7222);
            tissues[3].setCompartmentTimeConstants(6.99,    18.5,   10.458, 0.7223, 7.562,  0.7825);
            tissues[4].setCompartmentTimeConstants(10.21,   27.0,   9.220,  0.7582, 6.667,  0.8126);
            tissues[5].setCompartmentTimeConstants(14.48,   38.3,   8.205,  0.7957, 5.60,   0.8434);
            tissues[6].setCompartmentTimeConstants(20.53,   54.3,   7.305,  0.8279, 4.947,  0.8693);
            tissues[7].setCompartmentTimeConstants(29.11,   77.0,   6.502,  0.8553, 4.5,    0.8910);
            tissues[8].setCompartmentTimeConstants(41.20,   109.0,  5.950,  0.8757, 4.187,  0.9092);
            tissues[9].setCompartmentTimeConstants(55.19,   146.0,  5.545,  0.8903, 3.798,  0.9222);
            tissues[10].setCompartmentTimeConstants(70.69,  187.0,  5.333,  0.8997, 3.497,  0.9319);
            tissues[11].setCompartmentTimeConstants(90.34,  239.0,  5.189,  0.9073, 3.223,  0.9403);
            tissues[12].setCompartmentTimeConstants(115.29, 305.0,  5.181,  0.9122, 2.850,  0.9477);
            tissues[13].setCompartmentTimeConstants(147.42, 390.0,  5.176,  0.9171, 2.737,  0.9544);
            tissues[14].setCompartmentTimeConstants(188.24, 498.0,  5.172,  0.9217, 2.523,  0.9602);
            tissues[15].setCompartmentTimeConstants(240.03, 635.0,  5.119,  0.9267, 2.327,  0.9653);
        } else if(units == IMPERIAL) {            
            // public Compartment                  (hHe,    hN2,    aHe,    bHe,    aN2,    bN2
            tissues[0].setCompartmentTimeConstants(1.88,    5.0,    52.73,  0.4770, 38.09,  0.5578); 
            tissues[1].setCompartmentTimeConstants(3.02,    8.0,    45.04,  0.5747, 32.57,  0.6514);
            tissues[2].setCompartmentTimeConstants(4.72,    12.5,   38.82,  0.6527, 28.07,  0.7222);
            tissues[3].setCompartmentTimeConstants(6.99,    18.5,   34.06,  0.7223, 24.63,  0.7825);            
            tissues[4].setCompartmentTimeConstants(10.21,   27.0,   30.03,  0.7582, 21.71,  0.8126);
            tissues[5].setCompartmentTimeConstants(14.48,   38.3,   26.72,  0.7957, 18.24,  0.8434);
            tissues[6].setCompartmentTimeConstants(20.53,   54.3,   23.79,  0.8279, 16.11,  0.8693);
            tissues[7].setCompartmentTimeConstants(29.11,   77.0,   21.18,  0.8553, 14.66,  0.8910);
            tissues[8].setCompartmentTimeConstants(41.20,   109.0,  19.38,  0.8757, 13.64,  0.9092);
            tissues[9].setCompartmentTimeConstants(55.19,   146.0,  18.06,  0.8903, 12.37,  0.9222);
            tissues[10].setCompartmentTimeConstants(70.69,  187.0,  17.37,  0.8997, 11.39,  0.9319);
            tissues[11].setCompartmentTimeConstants(90.34,  239.0,  16.90,  0.9073, 10.50,  0.9403);
            tissues[12].setCompartmentTimeConstants(115.29, 305.0,  16.87,  0.9122, 9.280,  0.9477);
            tissues[13].setCompartmentTimeConstants(147.42, 390.0,  16.86,  0.9171, 8.910,  0.9544);
            tissues[14].setCompartmentTimeConstants(188.24, 498.0,  16.84,  0.9217, 8.220,  0.9602);
            tissues[15].setCompartmentTimeConstants(240.03, 635.0,  16.67,  0.9267, 7.580,  0.9653);            
        }
    }
    
    
}
