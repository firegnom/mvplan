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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

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
        if(MvplanInstance.getMvplan().getPrefs().isUpdateCheckDisable()) return false; 
        Calendar cal=Calendar.getInstance();        
        cal.add(Calendar.DATE, -MvplanInstance.getMvplan().getPrefs().getUpdateCheckFrequency());             
        // Check when last done. Zero means always check.   
        if(MvplanInstance.getMvplan().getPrefs().getUpdateCheckFrequency()>0 && MvplanInstance.getMvplan().getPrefs().getLastUpdateCheck().after(cal.getTime())) {
            if(MvplanInstance.getMvplan().getDebug()>0) System.out.println("Version Manager is skipping check for now.");
            return false;
        }             
        return true;
    }
        
    public void updateCheck() {    
                
        Version latestVersion;
        String url;

        url = MvplanInstance.getMvplan().getPrefs().getUpdateVersionURL();

        try {
            latestVersion = readURL(url);
        }   catch (Exception e) {
            if(MvplanInstance.getMvplan().getDebug()>0) {
            	System.err.println("Failed to read from network.\n");
            	System.err.println("Url:"+url);
            	e.printStackTrace();
            }
            resultCode=ERROR;
            return;
        }
 
        if(MvplanInstance.getMvplan().getDebug()>0) System.out.println("Read version: "+latestVersion.toString()+'\n');
        
       // If we get here we were successful. Update last check time
       MvplanInstance.getMvplan().getPrefs().setLastUpdateCheck(Calendar.getInstance().getTime());   
       if(MvplanInstance.getMvplan().getDebug()>0) System.out.println("Current version is "+MvplanInstance.getVersion().toString()+". Latest version is "+latestVersion.toString());       
       if(MvplanInstance.getVersion().compareTo(latestVersion)>0 ) {
           if(MvplanInstance.getMvplan().getDebug()>0) System.out.println("Version Manager has determined that a newer version is available.");     
           resultCode=UPDATE;
           return;
        }  else {
           if(MvplanInstance.getMvplan().getDebug()>0) System.out.println("Version Manager has determined that you are using the latest version.");
           resultCode=CURRENT;
           return;   
       }   
    }   
    
    public Version readURL(String url) throws Exception {
        URL target = new URL(url);

        URLConnection targetConnection = target.openConnection();
            targetConnection.setConnectTimeout(15000);        
            XStream x = new XStream(new DomDriver());
    		Version v = (Version) x.fromXML(
                new BufferedInputStream(
                targetConnection.getInputStream()));
            return v;

     }
    
}

