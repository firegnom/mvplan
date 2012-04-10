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
public interface  IMvplan {
    public String getResource(String res);
    public String getAppName();
    public Prefs getPrefs();
    public int getDebug();
    public Version getVersion();
    public void init();
}
