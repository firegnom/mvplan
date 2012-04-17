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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import mvplan.gas.Gas;
import mvplan.segments.SegmentDive;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * 
 * @author Maciej Kaniewski
 */
public class PrefsXStreamDAO implements PrefsDAO {
	private final String fileName;
	private final XStream x;

	/**
	 * Creates a new instance of PrefsXStreamDAO
	 */
	public PrefsXStreamDAO(String filename) {
		this.fileName = filename;
		x = new XStream(new DomDriver());
		x.alias("Preferences", Prefs.class);
		x.alias("Gas", Gas.class);
		x.alias("SegmentDive", SegmentDive.class);
	}

	/**
	* Save Preferences to XML file using XStream library, file is defined in constructor 
	*/
	public void savePrefs(Prefs p) throws PrefsException {
		if (fileName == null) {
			throw new PrefsException("Could not save preferences");
		}
		try {
			x.toXML(p, new BufferedOutputStream(new FileOutputStream(fileName)));
		} catch (FileNotFoundException e) {
			throw new PrefsException("Could not save preferences", e);
		}
	}
	
	/**
	* Load Preferences  from XML file using XStream library file is defined in constructor 
	*/
	public Prefs loadPrefs() throws PrefsException {
		if (fileName == null) {
			throw new PrefsException("Could not save preferences");
		}
		try {
			Prefs p = (Prefs) x.fromXML(new BufferedInputStream(
					new FileInputStream(fileName)));
			p.validatePrefs(); // Check that all is within bounds
			return p;

		} catch (FileNotFoundException e) {
			throw new PrefsException("Could not save preferences", e);
		}
		catch (RuntimeException e) {
			throw new PrefsException("Could not save preferences", e);
		}
		
	}

}
