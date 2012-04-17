/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mvplan.main;

import mvplan.prefs.Prefs;
import mvplan.util.Version;

/**
 * 
 * @author Maciej Kaniewski
 */
public class MvplanInstance {
	public static final String NAME = "MV-Plan";    // Application name
	public static final int MAJOR_VERSION = 1; // Application version codes
	public static final int MINOR_VERSION = 6;
	public static final int PATCH_VERSION = 0;
	public static final String VERSION_STATUS = "BETA"; // Application status
	public static final String BUILD_DATE = "10-04-2012"; // Application
															// release date
	public static Version mvplanVersion;    // App version. See Version Class    
	private static IMvplan mvplan;
	
	static{
		 mvplanVersion=new Version(MAJOR_VERSION,MINOR_VERSION,PATCH_VERSION,VERSION_STATUS,BUILD_DATE);
	}

	public static Prefs getPrefs() {
		if (mvplan != null) {
			return mvplan.getPrefs();
		}
		return null;
	}
	public static void setPrefs(Prefs p) {
		if (mvplan != null) {
			mvplan.setPrefs(p);
		}
	}

	public static IMvplan getMvplan() {
		return mvplan;
	}

	public static void setMvplan(IMvplan m) {
		mvplan = m;
		mvplan.init();
	}

	public static Version getVersion() {
		return mvplanVersion;
	}

}
