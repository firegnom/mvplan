/**
 * 
 */
package mvplan.util;

import java.io.File;
import java.io.FileOutputStream;

import mvplan.main.MvplanInstance;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


/**
 * @author Maciej Kaniewski
 * 
 */
public class VersionXmlTest {
	private final static String FILE = "test_version.xml"; 
	@Before
	public void exportCurrentVersionToFile() throws Exception{
		XStream x = new XStream(new DomDriver());
		Version v = MvplanInstance.getVersion();
		x.toXML(v, new FileOutputStream(new File(FILE)));
		
	} 
	@Test
	public void testLoad() throws Exception{
		XStream x = new XStream(new DomDriver());
		Version v = (Version) x.fromXML(new File(FILE));
		assertEquals(0,v.compareTo(MvplanInstance.getVersion()));
	}
	


}
