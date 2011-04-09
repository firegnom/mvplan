
////////////////////////////////////////////////////////
// Bare Bones Browser Launch                          //
// Version 1.2                                        //
// November 11, 2005                                  //
// Supports: Mac OS X, GNU/Linux, Unix, Windows XP    //
// Example Usage:                                     //
//    String url = "http://www.centerkey.com/";       //
//    BareBonesBrowserLaunch.openURL(url);            //
// Public Domain Software -- Free to Use as You Like  //
////////////////////////////////////////////////////////

package mvplan.util;

import java.lang.reflect.Method;
import javax.swing.JOptionPane;

public class BrowserLauncher {

   private static final String errMsg = "Error attempting to launch web browser";

   public static void openURL(String url) {
      int os = PlatformDetector.detect();
      
      try {
         if (os==PlatformDetector.MACOS) {
            Class macUtils = Class.forName("com.apple.mrj.MRJFileUtils");
            Method openURL = macUtils.getDeclaredMethod("openURL",
               new Class[] {String.class});
            openURL.invoke(null, new Object[] {url});
            }
         else if (os==PlatformDetector.WINDOWS)
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
         else { //assume Unix or Linux
            String[] browsers = {
               "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
            String browser = null;
            for (int count = 0; count < browsers.length && browser == null; count++)
               if (Runtime.getRuntime().exec(
                     new String[] {"which", browsers[count]}).waitFor() == 0)
                  browser = browsers[count];
            if (browser == null)
               throw new Exception("Could not find web browser.");
            else
               Runtime.getRuntime().exec(new String[] {browser, url});
            }
         }
      catch (Exception e) {
         JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage());
         }
      }

   }