/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mvplan.main;

import mvplan.prefs.Prefs;

/**
 *
 * @author Maciej Kaniewski
 */
public class MvplanInstance {
    private static IMvplan mvplan;
    
    public static Prefs getPrefs(){
        if (mvplan != null){
            return mvplan.getPrefs();
        }
        return null;
    }
    
    public static IMvplan getMvplan(){
        return mvplan;
    }
    public static void setMvplan(IMvplan m){
        mvplan = m;
        mvplan.init();
    }
    
}
