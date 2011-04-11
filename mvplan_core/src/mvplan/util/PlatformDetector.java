/*
 * PlatformDetector.java
 *
 * Created on May 12, 2007, 11:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mvplan.util;

/**
 *
 * @author guy
 */
public class PlatformDetector {
    public static final int MACOS=0;
    public static final int UNIX=1;
    public static final int WINDOWS=3;
        
    /** Creates a new instance of PlatformDetector */
    public static int detect() {
         String osName = System.getProperty("os.name");
         if (osName.startsWith("Mac OS"))
             return MACOS;
         else if (osName.startsWith("Windows")) 
             return WINDOWS;
         else  //assume Unix or Linux
             return UNIX;
    }    
}
