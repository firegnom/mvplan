/*
 * VersionManager.java
 *
 *  Checks for updated versions. Reads a Version object from the network in
 *  XMLEncoded format.
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

package mvplan.updater;

import mvplan.main.*;
import mvplan.util.Version;
import java.util.Calendar;
import java.io.*;
import java.net.*;
import java.beans.XMLDecoder;

public class VersionManager {
   public final static int UNDEFINED=0;
    public final static int CURRENT=1;
    public final static int UPDATE=2;
    public final static int ERROR=-1;
    
    private int resultCode;
    
    /** Creates a new instance of VersionManager */
    public VersionManager() {
        resultCode=UNDEFINED;
    }
    
    public int getResultCode() {   return resultCode;     }
    
    public boolean updateRequired() {       
        // Check if we have done this recently
        if(Mvplan.prefs.isUpdateCheckDisable()) return false; 
        Calendar cal=Calendar.getInstance();        
        cal.add(Calendar.DATE, -Mvplan.prefs.getUpdateCheckFrequency());             
        // Check when last done. Zero means always check.   
        if(Mvplan.prefs.getUpdateCheckFrequency()>0 && Mvplan.prefs.getLastUpdateCheck().after(cal.getTime())) {
            if(Mvplan.DEBUG>0) System.out.println("Version Manager is skipping check for now.");
            return false;
        }             
        return true;
    }
        
    public void updateCheck() {    
                
        Version latestVersion;
        String url;

        // Build url of the form: "http://192.168.1.100:8080/mvplan/version.jsp?command=current&id=1234-1234-1234-1234-1234-1234-1234-1234-1234"
        url = Mvplan.prefs.getUpdateVersionURL()+"?command=current&id=1234-1234-1234-1234-1234-1234-1234-1234-1234";

        try {
            latestVersion = readURL(url);
        }   catch (Exception e) {
            if(Mvplan.DEBUG>0) System.err.println("Failed to read from network.\n");
            resultCode=ERROR;
            return;
        }
 
        if(Mvplan.DEBUG>0) System.out.println("Read version: "+latestVersion.toString()+'\n');
        
       // If we get here we were successful. Update last check time
       Mvplan.prefs.setLastUpdateCheck(Calendar.getInstance().getTime());   
       if(Mvplan.DEBUG>0) System.out.println("Current version is "+Mvplan.mvplanVersion.toString()+". Latest version is "+latestVersion.toString());       
       if(Mvplan.mvplanVersion.compareTo(latestVersion)>0 ) {
           if(Mvplan.DEBUG>0) System.out.println("Version Manager has determined that a newer version is available.");     
           resultCode=UPDATE;
           return;
        }  else {
           if(Mvplan.DEBUG>0) System.out.println("Version Manager has determined that you are using the latest version.");
           resultCode=CURRENT;
           return;   
       }   
    }   
    
    public Version readURL(String url) throws Exception {
        URL target = new URL(url);

        URLConnection targetConnection = target.openConnection();
            // Java 1.5 only 
            targetConnection.setConnectTimeout(15000);        
            XMLDecoder decoder = new XMLDecoder(
                new BufferedInputStream(
                targetConnection.getInputStream()));
            Version v = (Version)decoder.readObject();
            decoder.close();  
            return v;

     }
    
}

