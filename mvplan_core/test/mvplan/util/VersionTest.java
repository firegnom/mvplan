/**
 * 
 */
package mvplan.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author macio
 * 
 */
public class VersionTest {

	/**
	 * Test method for {@link mvplan.util.Version#Version()}.
	 */
	@Test
	public void testVersion() {
		Version testV = new Version();
		// this(0,0, 0, "UNDEFINED", "dateString");
		assertEquals(0, testV.getMajorVersion());
		assertEquals(0, testV.getMinorVersion());
		assertEquals(0, testV.getPatchVersion());
		assertEquals("UNDEFINED", testV.getStatus());
		assertEquals("", testV.getDateString());
	}

	/**
	 * Test method for
	 * {@link mvplan.util.Version#Version(int, int, int, java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public void testVersionIntIntIntStringString() {
		Version testV = new Version(1, 2, 3, "BETA", "dateString");
		assertEquals(1, testV.getMajorVersion());
		assertEquals(2, testV.getMinorVersion());
		assertEquals(3, testV.getPatchVersion());
		assertEquals("BETA", testV.getStatus());
		assertEquals("dateString", testV.getDateString());
	}

	/**
	 * Test method for {@link mvplan.util.Version#toString()}.
	 */
	@Test
	public void testToString() {
		Version testV = new Version(1, 2, 3, "BETA", "dateString");
		assertEquals("1.2.3 BETA", testV.toString());
	}

	/**
	 * Test method for
	 * {@link mvplan.util.Version#compareTo(mvplan.util.Version)}.
	 */
	@Test
	public void testCompareTo() {
		Version testV = new Version(1, 1, 0, "BETA", "Today");
		System.out.println("Version: testing " + testV);

		Version testV2 = new Version(2, 1, 1, "UNDEFINED", "Tomorrow");
		System.out.println("Compared with " + testV2 + " is "
				+ testV.compareTo(testV2));
		assertEquals(1, testV.compareTo(testV2));

		testV2 = new Version(0, 1, 1, "UNDEFINED", "Tomorrow");
		System.out.println("Compared with " + testV2 + " is "
				+ testV.compareTo(testV2));
		assertEquals(-1, testV.compareTo(testV2));

		testV2 = new Version(1, 2, 1, "UNDEFINED", "Tomorrow");
		System.out.println("Compared with " + testV2 + " is "
				+ testV.compareTo(testV2));
		assertEquals(1, testV.compareTo(testV2));

		testV2 = new Version(1, 0, 1, "UNDEFINED", "Tomorrow");
		System.out.println("Compared with " + testV2 + " is "
				+ testV.compareTo(testV2));
		assertEquals(-1, testV.compareTo(testV2));

		testV2 = new Version(1, 1, 2, "UNDEFINED", "Tomorrow");
		System.out.println("Compared with " + testV2 + " is "
				+ testV.compareTo(testV2));
		assertEquals(-3, testV.compareTo(testV2));

		testV2 = new Version(1, 1, 0, "UNDEFINED", "Tomorrow");
		System.out.println("Compared with " + testV2 + " is "
				+ testV.compareTo(testV2));
		assertEquals(-3, testV.compareTo(testV2));

		testV2 = new Version(1, 1, 2, "BETA", "Tomorrow");
		System.out.println("Compared with " + testV2 + " is "
				+ testV.compareTo(testV2));
		assertEquals(2, testV.compareTo(testV2));

		testV2 = new Version(1, 1, 1, "RELEASED", "Tomorrow");
		System.out.println("Compared with " + testV2 + " is "
				+ testV.compareTo(testV2));
		assertEquals(1, testV.compareTo(testV2));

		testV2 = new Version(1, 1, 0, "BETA", "different string");
		System.out.println("Compared with " + testV2 + " is "
				+ testV.compareTo(testV2));
		assertEquals(0, testV.compareTo(testV2));
	}

	/**
	 * Test method for {@link mvplan.util.Version#getMajorVersion()}.
	 */
	@Test
	public void testGetMajorVersion() {
		Version testV = new Version();
		testV.setMajorVersion(1);
		assertEquals(1, testV.getMajorVersion());
	}

	/**
	 * Test method for {@link mvplan.util.Version#setMajorVersion(int)}.
	 */
	@Test
	public void testSetMajorVersion() {
		Version testV = new Version();
		testV.setMajorVersion(1);
		assertEquals(1, testV.getMajorVersion());
		
		testV.setMajorVersion(-1);
		assertEquals(0, testV.getMajorVersion());
	}

	/**
	 * Test method for {@link mvplan.util.Version#getMinorVersion()}.
	 */
	@Test
	public void testGetMinorVersion() {
		Version testV = new Version();
		testV.setMinorVersion(1);
		assertEquals(1, testV.getMinorVersion());
	}

	/**
	 * Test method for {@link mvplan.util.Version#setMinorVersion(int)}.
	 */
	@Test
	public void testSetMinorVersion() {
		Version testV = new Version();
		testV.setMinorVersion(1);
		assertEquals(1, testV.getMinorVersion());
		
		testV.setMinorVersion(-1);
		assertEquals(0, testV.getMinorVersion());
	}

	/**
	 * Test method for {@link mvplan.util.Version#getPatchVersion()}.
	 */
	@Test
	public void testGetPatchVersion() {
		Version testV = new Version();
		testV.setPatchVersion(1);
		assertEquals(1, testV.getPatchVersion());
	}

	/**
	 * Test method for {@link mvplan.util.Version#setPatchVersion(int)}.
	 */
	@Test
	public void testSetPatchVersion() {
		Version testV = new Version();
		testV.setPatchVersion(1);
		assertEquals(1, testV.getPatchVersion());
		
		testV.setPatchVersion(-1);
		assertEquals(0, testV.getPatchVersion());
	}

	/**
	 * Test method for {@link mvplan.util.Version#getLookupTable()}.
	 */
	@Test
	public void testGetLookupTable() {
		Version testV = new Version();
		assertEquals(5, testV.getLookupTable().length);
	}

	/**
	 * Test method for {@link mvplan.util.Version#getStatus()}.
	 */
	@Test
	public void testGetSetStatus() {
		Version testV = new Version();
		testV.setStatus("BETA");
		assertEquals("BETA", testV.getStatus());
		
		testV.setStatus("beta");
		assertEquals("BETA", testV.getStatus());

		testV.setStatus("TEST");
		assertEquals("TEST", testV.getStatus());

		testV.setStatus("ALPHA");
		assertEquals("ALPHA", testV.getStatus());
		
		testV.setStatus("RELEASED");
		assertEquals("RELEASED", testV.getStatus());
		
		testV.setStatus("other");
		assertEquals("UNKNOWN", testV.getStatus());
		
		testV.setStatus("UNDEFINED");
		assertEquals("UNDEFINED", testV.getStatus());

	}


	/**
	 * Test method for {@link mvplan.util.Version#getDateString()}.
	 */
	@Test
	public void testGetDateString() {
		Version testV = new Version();
		testV.setDateString("dateString");
		assertEquals("dateString", testV.getDateString());
	}

	/**
	 * Test method for
	 * {@link mvplan.util.Version#setDateString(java.lang.String)}.
	 */
	@Test
	public void testSetDateString() {
		Version testV = new Version();
		testV.setDateString("dateString");
		assertEquals("dateString", testV.getDateString());
	}


}
