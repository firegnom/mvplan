package mvplan.util;

import mvplan.gas.Gas;


public class BlendingUtils {
	
	
	public static Gas gasRequired(Gas g, double fo2, double fhe,double volume){
		Gas ret ; 
				
		double ro2 = (fo2 * volume) - (g.getFO2Vol());
		double rhe = (fhe * volume) - (g.getFHeVol());
		double rvol = (volume - g.getVolume());
		
		ret = new Gas((rhe/rvol),(ro2/rvol));
		ret.setVolume(rvol);
		
		return ret;
	}
	
	
}
