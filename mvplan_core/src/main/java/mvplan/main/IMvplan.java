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
    public void setPrefs(Prefs p);
    public int getDebug();
    public void init();
}
