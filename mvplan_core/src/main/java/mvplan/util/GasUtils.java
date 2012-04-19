package mvplan.util;

import mvplan.gas.Gas;
import mvplan.main.MvplanInstance;
import mvplan.prefs.Prefs;

public class GasUtils {
	/**
	 * Calculate equivalent narcosis depth (END) of gas for given depth<br/>
	 * <br/>
	 * <br/>
	 * 
	 * more information can be found here({@link http://www.techdiver.ws/trimix_narcosis.shtml})
	 * @see Gas
	 * @see Prefs 
	 * @param g gas that is to be calculated
	 * @param depth for calculations; 
	 */
	public static double calculateEND(Gas g, double depth) {
		Prefs p = MvplanInstance.getPrefs();
		double ppAir = .79 +
				(.21 * p.getOxygenNarcoticLevel());
		
		double ppF = g.getFN2() +
				(g.getFO2() * p.getOxygenNarcoticLevel())+
				(g.getFHe() * p.getHeliumNarcoticLevel());
		
		ppF = (ppF*(depth+p.getPConversion())/ppAir)-p.getPConversion();
		
		return ppF;
	}
	/**
	 * Calculate equivalent narcosis depth (END) of gas on MOD specifies gas<br/>
	 * <br/>
	 * <br/>
	 * more information can be found here({@link http://www.techdiver.ws/trimix_narcosis.shtml})
	 * @see Gas
	 * @see Prefs 
	 * @param g gas that is to be calculated
	 */
	public static double calculateEND(Gas g) {
		return calculateEND(g,g.getMod());
	}
	
	/** 
     * Method to validate a field for limits only 
     */
    public static boolean validate(String field, double value) {
        if(field.equals("fHe") || field.equals("fO2"))
            return ( value >= 0.0 && value <=1.0);
        if(field.equals("mod"))
            // Need to hard code nominal max value due to potential of prefs not being fully set up when this is called
            return (value >=0.0 && value <= 900.0);
        return false;        
    }
    
    /** 
     * Method to validate all inputs (fO2, fHe and MOD)
     */
    public static boolean validate(double fHe, double fO2, double mod) {
        boolean passed=true;
        Prefs prefs = MvplanInstance.getPrefs();
        // Check individual fields for bounds   
        passed = (passed && validate("fHe",fHe));
        passed = (passed && validate("fO2",fO2));
        passed = (passed && validate("mod",mod));
        if(!passed) return false;
        // Check combined fractions
        passed = (passed && (fHe+fO2)<=1.0);
        if(!passed) return false;
        
        // Check MOD for sensible value   
        if(fO2 == 0.0 && mod == 0.0)    // Leave empty gases alone to allow construction
            return passed;
        if(MvplanInstance.getMvplan() != null && prefs != null) {   // Need to check that prefs exists. We can get to this point during the initilisation of the prefs object
            double d = ((mod+prefs.getPConversion())/prefs.getPConversion()*fO2);
            passed = (d <= prefs.getMaxMOD()+0.05);  // Tolerance of 0.05 to prevent unneccessary failure due to rounding
        }
            
        return passed;        
    }        
    /** 
     * Method to get a maximum MOD based on O2 fraction
     */
    public static double getMaxMod(double o) {
    	Prefs prefs = MvplanInstance.getPrefs();
        return (prefs.getMaxMOD()/o * prefs.getPConversion())-prefs.getPConversion();
    }
    /** 
     * Method to get a MOD based on O2 fraction and maximum ppO2
     */
    public static double getMod(double fO2, double ppO2) {
    	Prefs prefs = MvplanInstance.getPrefs();
        return (ppO2/fO2 * prefs.getPConversion())-prefs.getPConversion();
    }
    /** 
     * Method to get a ppO2 based on O2 fraction and MOD
     */
    public static double getppO2(double f, double m) {
    	Prefs prefs = MvplanInstance.getPrefs();
        return ( (m+prefs.getPConversion())*f/prefs.getPConversion());
    }

}
