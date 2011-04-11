/*
 * PrefsDAO.java
 * 
 *   Provides Data Access to Prefs objects. Persists Prefs object to XML using the
 *   XMLEncoder objects.
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
package mvplan.prefs;

import java.io.*;
import java.beans.*;

/**
 *
 * @author Guy
 */
public class PrefsDAO {
    
   /*
    * Creates a new instance of PrefsDAO 
    */
    public PrefsDAO() {
    }
    
    /*
     * Persists Prefs object to XML 
     */
    public void setPrefs(Prefs p, String fileName) throws FileNotFoundException, SecurityException {
        XMLEncoder encoder = new XMLEncoder(
            new BufferedOutputStream(
            new FileOutputStream(fileName)) );
        encoder.writeObject(p);
        encoder.close();    
    }
    
    /*
     * Read Prefs object from XML file
     */
    public Prefs getPrefs(String fileName){
        try{
            XMLDecoder decoder = new XMLDecoder(
                new BufferedInputStream(
                new FileInputStream(fileName)));
            Prefs p = (Prefs)decoder.readObject();
            decoder.close();  
            p.validatePrefs();      // Check that all is within bounds
            return p;
        } catch (Exception ex) {
            System.out.println("Error reading prefs: "+ex);
            return null;
        }        
    }
    
}
