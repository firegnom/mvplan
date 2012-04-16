package mvplan;

public class MVPlanException  extends  Exception{

	public MVPlanException(String desc) {
		super(desc);
	}

	public MVPlanException() {
		super();
	}

	public MVPlanException(String string, Throwable e) {
		super(string,e);
	}


	/**this is a toplevel exception for library
	 * @author Maciej Kaniewski
	 */
	private static final long serialVersionUID = 1L;

}
