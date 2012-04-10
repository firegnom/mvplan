/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mvplan.dive.printer;

import java.util.ArrayList;
import mvplan.dive.Profile;
import mvplan.gas.Gas;

/**
 *
 * @author Maciej Kaniewski
 */
public abstract class ProfilePrinter <T> {
    public ProfilePrinter(Profile p , T data ,ArrayList<Gas> knownGases){
    }
    public abstract T print();
    
    
}
