/*
 * Version.java
 *
 *  Version object for maintaining an application version. Used for version checking over the internet.
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

package mvplan.util;

import java.util.Hashtable;

public class Version implements Comparable {
    
    private int majorVersion;
    private int minorVersion;
    private int patchVersion;
    private String statusString;
    private String dateString;
    private  Hashtable lookupTable;
    
    public static final int UNDEFINED=0;
    public static final int TEST=10;
    public static final int ALPHA=20;
    public static final int BETA=30;
    public static final int RELEASED=100;
    
    
    /** Creates a new instance of Version */
    public Version() {
       this(0,0, 0, "UNDEFINED", "");      
    }
    
    public Version(int major, int minor, int patch, String status, String date){
        // Create hashmap
        lookupTable = new Hashtable();
        lookupTable.put("UNDEFINED",new Integer(0));
        lookupTable.put("TEST",new Integer(1));
        lookupTable.put("ALPHA",new Integer(2));
        lookupTable.put("BETA",new Integer(3));
        lookupTable.put("RELEASED",new Integer(4));
        // Initialise fields
        majorVersion=major;
        minorVersion=minor;
        patchVersion=patch;
        setStatus(status);
        dateString=date;
    }
    
    @Override
    public String toString() {
        String s = majorVersion+"."+minorVersion;
        if( patchVersion > 0 ) s=s+"."+patchVersion;
        if(!statusString.equals("RELEASED"))    s=s+" "+statusString;
        //if(dateString.length()>0) s=s+" "+dateString;
        return s;
    }
    
    //* Compares two status strings based on their status numbers in the hashtable */
    private int compareStatus(String s1, String s2) {
        Integer n1 = (Integer)lookupTable.get(s1);
        Integer n2 = (Integer)lookupTable.get(s2);
        if (n1 == null || n2==null) return -1;
        return n1-n2;        
    }
    
    public int compareTo(Object o){
        Version v;
        try {
            v = (Version)o;
        } catch (Exception e) {
            return -1;
        }
        if(v.getMajorVersion()==majorVersion)
            if(v.getMinorVersion()==minorVersion)
                if(compareStatus(v.getStatus(), statusString) ==0)
                //if(v.getPatchVersion()==patchVersion) 
                    return v.getPatchVersion()-patchVersion;
                    //return compareStatus(v.getStatus(),statusString);
                else return compareStatus(v.getStatus(), statusString);
                //else return v.getPatchVersion()-patchVersion;
            else return v.getMinorVersion()-minorVersion;
        else return v.getMajorVersion()-majorVersion;   
    }
    public int getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = (majorVersion>=0) ? majorVersion : 0;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = (minorVersion>=0) ? minorVersion : 0;
    }

    public int getPatchVersion() {
        return patchVersion;
    }

    public void setPatchVersion(int patchVersion) {
        this.patchVersion = (patchVersion>=0) ? patchVersion : 0;
    }

    public  Object [] getLookupTable(){
        return lookupTable.keySet().toArray();
    }
    
    public String getStatus() {
        return statusString;
    }

    public void setStatus(String status) {
        if(lookupTable.get(status)!= null)
            statusString = status;
        else
            statusString="UNKNOWN";
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }
    
    /** Test method */
    public static void main(String [] args) {
        Version testV = new Version(1,1,0, "BETA", "Today");
        System.out.println("Version: testing "+testV);
        
        Version testV2 = new Version(2, 1, 1, "UNDEFINED", "Tomorrow");
        System.out.println("Compared with "+testV2+" is "+testV.compareTo(testV2));
        
        testV2 = new Version(0, 1, 1, "UNDEFINED", "Tomorrow");
        System.out.println("Compared with "+testV2+" is "+testV.compareTo(testV2));
        
        testV2 = new Version(1,2 , 1, "UNDEFINED", "Tomorrow");
        System.out.println("Compared with "+testV2+" is "+testV.compareTo(testV2));
        
        testV2 = new Version(1, 0, 1, "UNDEFINED", "Tomorrow");
        System.out.println("Compared with "+testV2+" is "+testV.compareTo(testV2));
        
        testV2 = new Version(1, 1, 2, "UNDEFINED", "Tomorrow");
        System.out.println("Compared with "+testV2+" is "+testV.compareTo(testV2));
                
        testV2 = new Version(1, 1, 0, "UNDEFINED", "Tomorrow");
        System.out.println("Compared with "+testV2+" is "+testV.compareTo(testV2));
                        
        testV2 = new Version(1, 1, 2, "BETA", "Tomorrow");
        System.out.println("Compared with "+testV2+" is "+testV.compareTo(testV2));
        
        testV2 = new Version(1, 1, 1, "RELEASED", "Tomorrow");
        System.out.println("Compared with "+testV2+" is "+testV.compareTo(testV2));
        
    }
    
    
}
