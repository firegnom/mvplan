package mvplan.gas;

import mvplan.util.GasUtils;

/**
 * The Class Gas Blend is representing blend of multiple gases and is limited by volume and maximum pressure;.
 */
public class GasBlend {

	/** The volume available in 1 bar of pressure. */
	private double volume;

	/** The maximum pressure of the blend . */
	private double maxPressure;

	/** The list of gases and their volume. */
	private GasList gases;
	
	
	
	/**
	 * Instantiates a new gas blend.
	 *
	 * @param volume the volume
	 * @param maxPressure the max pressure
	 */
	public GasBlend(double volume,double maxPressure) {
		gases = new GasList();
		this.volume = volume;
		this.maxPressure = maxPressure;
	}

	/**
	 * Gets the volume.
	 * 
	 * @return the volume
	 */
	public double getVolume() {
		return volume;
	}

	/**
	 * Sets the volume. if new value can not handle new amount return false and
	 * do not modify
	 * 
	 * 
	 * @param volume
	 *            the new volume
	 * @return true, if successful
	 */
	public boolean setVolume(double volume) {
		// TODO add logic checking if it
		this.volume = volume;
		return true;
	}

	/**
	 * Gets the max pressure.
	 * 
	 * @return the max pressure
	 */
	public double getMaxPressure() {
		return maxPressure;
	}

	/**
	 * Sets the max pressure if new value can not handle new amount return false
	 * and do not modify.
	 *
	 * @param maxPressure the new max pressure
	 * @return true, if successful
	 */
	public boolean setMaxPressure(double maxPressure) {
		this.maxPressure = maxPressure;
		return true;
	}

	/**
	 * Gets the current volume.
	 * 
	 * @return the current volume
	 */
	public double getCurrentVolume() {
		double ret =0;
		for (Gas g : gases) {
			ret += g.getVolume();
		}
		return ret;
	}
	
	/**
	 * Gets the max volume under maximum pressure.
	 *
	 * @return the max volume
	 */
	public double getMaxVolume(){
		return volume*maxPressure;
	}

	/**
	 * Gets the current pressure.
	 * 
	 * @return the current pressure
	 */
	public double getCurrentPressure() {
		double vol = getCurrentVolume();
		return vol/volume;
	}
	
	

	/**
	 * Before adding gas to blend we need to make sure there is enough volume
	 * available.
	 * 
	 * @param gas
	 *            to be added
	 * @return true, if successful
	 */
	public boolean add(Gas gas) {
		if (gas.getVolume() > getMaxVolume()) return false;
		if ((gas.getVolume()+getCurrentVolume()) > getMaxVolume() ) return false;
		return gases.add(gas);
	}
	
	
	
	public Gas blend(){
		Gas ret = new Gas();
		for (Gas g : gases) {
			ret = GasUtils.blend(ret, g);
		}
		return ret;
		
		
	}
}
