/*
 * MvPlan.java
 *
 *   MV-Plan - an m-value gradient decompression program based on the 
 *   Buhlmann ZHL16-B model and the m-value gradient concepts developed
 *   by Erik Baker.
 *
 *   Copyright 2006 Guy Wittig
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

package mvplan.main;

import mvplan.util.PlatformDetector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
//import javax.swing.JDialog;
import javax.swing.JFrame;
import mvplan.gui.MainFrame;
import mvplan.prefs.Prefs;
import mvplan.prefs.PrefsException;
import mvplan.prefs.PrefsXMLDAO;
import mvplan.prefs.PrefsXStreamDAO;
import mvplan.util.*;
import javax.swing.UIManager;

public class Mvplan implements IMvplan
{
    private static final float MIN_JVM = 1.5f;               // Minimum Java JVM requirement. Checked on startup.
    static final String PREF_FILE = "mvplan.xml";            // Preferences file name
    public static String prefFile;
    public static final int DEBUG = 2;      // 0 == no debug, 1 == basic or current debug, 2 == full trace
    public static Prefs prefs;              // Preferences object. Pseudo Singleton.
    private static ResourceBundle stringResource;   // String resources for the application
    public static Locale preferredLocale=new Locale("en");     // For language preference
    public static String appName;

    private JFrame frame;
    
    private void initProxy(Prefs p){
    	if (p.getProxyHost() != null && !p.getProxyHost().trim().isEmpty()){
    		System.getProperties().put("http.proxyHost", p.getProxyHost());
    	}
    
    	if (p.getProxyPort() != null && !p.getProxyPort().trim().isEmpty()){
    		System.getProperties().put("http.proxyPort", p.getProxyPort());
    	}
       if (p.getProxyUser() != null && !p.getProxyUser().trim().isEmpty()){
		   System.getProperties().put("http.proxyUser", p.getProxyUser());
	   }
    	if (p.getProxyPassword() != null && !p.getProxyPassword().trim().isEmpty()){
    		System.getProperties().put("http.proxyPassword", p.getProxyPassword());
    	}
    }
    
    public void init(){
    	float vmVersion=0.0f;   // For Virtual Machine version
        ArrayList<Locale> availableLocales = new ArrayList<Locale>();     // Stores available locales                               
        
        // Construct app name and version
       
        appName=MvplanInstance.NAME+" "+MvplanInstance.getVersion().toString();
        
        // Set patform specifics
        if (Mvplan.DEBUG > 0 ) System.out.println("Platform:"+PlatformDetector.detect());
        
        
        // Make it nice and avoid the XP L&F
        //JFrame.setDefaultLookAndFeelDecorated(true);
        //JDialog.setDefaultLookAndFeelDecorated(true);  
		try {
			UIManager.setLookAndFeel("com.jgoodies.plaf.plastic.PlasticXPLookAndFeel");
		} catch (Exception e) {}
        
        // Check Java JVM version
        try {
            // Check JVM Version
             vmVersion = Float.parseFloat( System.getProperty("java.specification.version"));           
        } catch (Exception e) { 
            System.err.println("Mvplan: critical error, incorrect JVM.");
            System.exit(0);             
        }
        if(vmVersion < MIN_JVM) {
            // Display error
            JFrame vd = new mvplan.gui.VersionFrame(String.valueOf(vmVersion),String.valueOf(MIN_JVM));                
        } else {     
            // Continue with app startup ....                       
            
            // Load string resources
            
            // This code enumerates locales that are available. To be used for language selection.
            if (Mvplan.DEBUG >0 ) {        
                System.out.println("System Locale: "+Locale.getDefault()+" Preferred Locale: "+preferredLocale);
                // Display installed locals
                Locale[] installedLocales = Locale.getAvailableLocales();
                System.out.println("Searching available locales ...");
                // English is always available
                availableLocales.add(Locale.ENGLISH);
                for ( int i=0; i<installedLocales.length ; i++ ) {
                    try {
                        stringResource = ResourceBundle.getBundle("mvplan/resources/strings", installedLocales[i]);
                        if( stringResource.getLocale().equals(installedLocales[i]) ) 
                            availableLocales.add(installedLocales[i]);                                                    
                    } catch ( Exception ex) {}                     
                }
                System.out.println(availableLocales.size()+ " Locales found.");
                Iterator<Locale> it = availableLocales.iterator();
                while(it.hasNext())
                    System.out.println(it.next().getDisplayLanguage());                   
            }
            
            try {
                if (preferredLocale != null )
                    stringResource = ResourceBundle.getBundle("mvplan/resources/strings", preferredLocale ); 
                else 
                    stringResource = ResourceBundle.getBundle("mvplan/resources/strings");                
            } catch (java.util.MissingResourceException e) {
                System.err.println("Mvplan: critical error, missing string resources.");
                System.exit(0);  
            } 
            
            // Restore last state from preferences   
            prefFile = System.getProperty("user.dir")+System.getProperty("file.separator")+PREF_FILE;       
            // Use Data Access Object for reading preferences from XML encoded file
            PrefsXStreamDAO dao=new PrefsXStreamDAO(prefFile);    
            if (Mvplan.DEBUG >0 ) System.out.println("Restoring preferences from "+prefFile);
            try {
				prefs = dao.loadPrefs();
			} catch (PrefsException e) {
				System.err.println("Mvplan: error, could not load preferences file :"+e.getLocalizedMessage());
				e.printStackTrace();
			}        
            // Create new preferences if no file found
            if (prefs == null) {
                prefs = new Prefs();
                prefs.setDefaultPrefs();
            }
            MvplanInstance.setPrefs(prefs);
            initProxy(prefs);
            
            // Open main screen             
            frame = new MainFrame();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
        }             
        
    }
    
    /**
     * Provide safe access to resources. 
     * Does not throw exception if a resoure is not found, but displays it's key so it can be debugged.
     */
    public String getResource (String key) {
        try {
           return stringResource.getString(key);        
        } catch (java.util.MissingResourceException e) {
            // DIsplay resource name for debugging resource files
            if (DEBUG>0) System.out.println("Missing string resource: "+key);
            return "<"+key+">";
        }
    }
    
    public String getAppName() {
        return appName;
    }
    
    public static void main(String[] args)
    {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                MvplanInstance.setMvplan(new Mvplan());
            }
        });        
    }

    public Prefs getPrefs() {
        return Mvplan.prefs;
    }

    public int getDebug() {
        return Mvplan.DEBUG;
    }

	public void setPrefs(Prefs p) {
		Mvplan.prefs = p;
	}

}
