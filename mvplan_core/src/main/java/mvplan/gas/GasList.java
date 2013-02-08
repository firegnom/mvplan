package mvplan.gas;

import java.util.ArrayList;
import java.util.UUID;

public class GasList extends ArrayList<Gas>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Gas get(UUID id) {
		// no elements return null
		if (size() ==0 ) return null;
		
		// iterate over all elements and get first with specified id; 
		for (Gas g : this) {
			if (g.getId().equals(id))return g;	
		}
		// element not found
		return null;
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Gas))return false;
		if (null != get(((Gas) o).getId())) return true;
		return false;
	}
	
	@Override
	public boolean add(Gas e) {
		if (contains(e)) return false;
		return super.add(e);
	}	
	

}
