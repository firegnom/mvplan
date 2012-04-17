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

package mvplan.prefs;


/**This is interface for saving Mv-plan preferences
 *
 * @author Maciej Kaniewski
 */
public interface PrefsDAO {
    
   
    /**
     * Persists Prefs object 
     */
    public void savePrefs(Prefs p) throws PrefsException;
    
    /**
     * Read Prefs object 
     */
    public Prefs loadPrefs() throws PrefsException;
    
}
