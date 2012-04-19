package mvplan;

/**This is a toplevel exception
 * @author Maciej Kaniewski
 */
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


	private static final long serialVersionUID = 1L;

}
