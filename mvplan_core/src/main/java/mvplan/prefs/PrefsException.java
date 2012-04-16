package mvplan.prefs;

import mvplan.MVPlanException;

public class PrefsException extends MVPlanException{

	public PrefsException(String string) {
		super(string);
	}

	public PrefsException(String string, Throwable e) {
		super(string,e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
