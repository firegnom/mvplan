package mvplan.util;

import mvplan.gas.Gas;
import mvplan.gas.GasList;

public class BlendingUtils {
	
	/**
	 * Calculate blend. 
	 *
	 * @param blend the blend
	 * @return calculated gas or null if the list is empty
	 */
	public static Gas calculateBlend(GasList blend){
		if (blend== null || blend.isEmpty()) return null;
		Gas ret =  new Gas();
		return ret;
	}
}
