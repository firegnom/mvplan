/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mvplan.main;

/**
 *
 * @author Maciej Kaniewski
 */
public class MvplanInstance {
    private static IMvplan mvplan;
    public static IMvplan getMvplan(){
        return mvplan;
    }
    public static void setMvplan(IMvplan m){
        mvplan = m;
        mvplan.init();
    }
    
}
