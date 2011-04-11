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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
//import javax.swing.JDialog;
import javax.swing.JFrame;
import mvplan.gui.MainFrame;
import mvplan.prefs.Prefs;
import mvplan.prefs.PrefsDAO;
import mvplan.util.*;

public class Mvplan implements IMvplan
{
    public static final String NAME = "MV-Plan";    // Application name
    public static final int MAJOR_VERSION = 1;      // Application version codes
    public static final int MINOR_VERSION = 5;
    public static final int PATCH_VERSION = 3;
    public static final String VERSION_STATUS = "RELEASED";      // Application status
    public static final String BUILD_DATE = "25-Dec-2010";     // Application release date
    private static final float MIN_JVM = 1.5f;               // Minimum Java JVM requirement. Checked on startup.
    static final String PREF_FILE = "mvplan.xml";            // Preferences file name
    public static String prefFile;
    public static final int DEBUG = 1;      // 0 == no debug, 1 == basic or current debug, 2 == full trace
    public static Prefs prefs;              // Preferences object. Pseudo Singleton.
    private static ResourceBundle stringResource;   // String resources for the application
    public static Locale preferredLocale=null;     // For language preference
    public static String appName;
    public static Version mvplanVersion;    // App version. See Version Class    

    private JFrame frame;
    
    
    public void init(){
     float vmVersion=0.0f;   // For Virtual Machine version
        ArrayList<Locale> availableLocales = new ArrayList();     // Stores available locales                               
        
        // Construct app name and version
        mvplanVersion=new Version(MAJOR_VERSION,MINOR_VERSION,PATCH_VERSION,VERSION_STATUS,BUILD_DATE);
        appName=NAME+" "+mvplanVersion.toString();
        
        // Set patform specifics
        if (Mvplan.DEBUG > 0 ) System.out.println("Platform:"+PlatformDetector.detect());
        
        
        // Make it nice and avoid the XP L&F
        //JFrame.setDefaultLookAndFeelDecorated(true);
        //JDialog.setDefaultLookAndFeelDecorated(true);  
        
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
            PrefsDAO dao=new PrefsDAO();    
            if (Mvplan.DEBUG >0 ) System.out.println("Restoring preferences from "+prefFile);
            prefs = dao.getPrefs(prefFile);        
            // Create new preferences if no file found
            if (prefs == null) {
                prefs = new Prefs();
                prefs.setDefaultPrefs();
            }            
            
            // Open main screen             
            frame = new MainFrame();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
        }             
        
    }
    
    /* 
     * Provide safe access to resources. 
     * Does not throw exception if a resoure is not found, but displays it's key so it can be debugged.
     */
    @Override
    public String getResource (String key) {
        try {
           return stringResource.getString(key);        
        } catch (java.util.MissingResourceException e) {
            // DIsplay resource name for debugging resource files
            if (DEBUG>0) System.out.println("Missing string resource: "+key);
            return "<"+key+">";
        }
    }
    
    
    public static void main(String[] args)
    {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                MvplanInstance.setMvplan(new Mvplan());
            }
        });        
    }

    @Override
    public Prefs getPrefs() {
        return Mvplan.prefs;
    }

    @Override
    public int getDebug() {
        return Mvplan.DEBUG;
    }

    @Override
    public Version getVersion() {
        return Mvplan.mvplanVersion;
    }
}
