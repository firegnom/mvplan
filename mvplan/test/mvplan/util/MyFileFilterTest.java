/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mvplan.util;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 *
 * @author Maciej Kaniewski
 */
public class MyFileFilterTest {

    public MyFileFilterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of accept method, of class MyFileFilter.
     */
    @Test
    public void testAccept() {
        File f = null;
        MyFileFilter instance = new MyFileFilter();
        boolean result = instance.accept(f);
        assertEquals(false, result);
        f=mock(File.class);
        when(f.isDirectory()).thenReturn(true);
        assertEquals(true, instance.accept(f));
       
        
    }

    /**
     * Test of getExtension method, of class MyFileFilter.
     */
    @Test
    public void testGetExtension() {
        File f = null;
        File f1 = null;
        f=mock(File.class);
        f1=mock(File.class);
        when(f.isDirectory()).thenReturn(false);
        when(f.getName()).thenReturn("test.tst");
        when(f1.isDirectory()).thenReturn(false);
        when(f1.getName()).thenReturn("test.tst1");
        MyFileFilter instance = new MyFileFilter();
        String expResult = "tst";
        instance.addExtension("tst");
        String result = instance.getExtension(f);
        assertEquals(expResult, result);
        assertEquals(true, instance.accept(f));
        assertEquals(false, instance.accept(f1));
    }

    /**
     * Test of getName method, of class MyFileFilter.
     */
    @Test
    public void testGetName() {
        File f = null;
        f=mock(File.class);
        when(f.isDirectory()).thenReturn(false);
        when(f.getAbsolutePath()).thenReturn("/sdfsdfs/dsfds/test.tst");
        MyFileFilter instance = new MyFileFilter();
        String expResult = "/sdfsdfs/dsfds/test";
        String result = instance.getName(f);
        assertEquals(expResult, result);
    }

    /**
     * Test of addExtension method, of class MyFileFilter.
     */
    @Test
    public void testAddExtension() {
        File f = null;
        File f1 = null;
        f=mock(File.class);
        f1=mock(File.class);
        when(f.isDirectory()).thenReturn(false);
        when(f.getName()).thenReturn("test.tst");
        when(f1.isDirectory()).thenReturn(false);
        when(f1.getName()).thenReturn("test.tst1");
        MyFileFilter instance = new MyFileFilter();
        String expResult = "tst";
        instance.addExtension("tst");
        String result = instance.getExtension(f);
        assertEquals(expResult, result);
        assertEquals(true, instance.accept(f));
        assertEquals(false, instance.accept(f1));
    }

    /**
     * Test of getDescription method, of class MyFileFilter.
     */
    @Test
    public void testGetDescription() {
        File f = null;
        File f1 = null;
        f=mock(File.class);
        f1=mock(File.class);
        when(f.isDirectory()).thenReturn(false);
        when(f.getName()).thenReturn("test.tst");
        when(f1.isDirectory()).thenReturn(false);
        when(f1.getName()).thenReturn("test.tst1");
        MyFileFilter instance = new MyFileFilter();
        instance.addExtension("tst");
        instance.addExtension("tst1");
        String result = instance.getExtension(f);
        String expResult = "tst";
        assertEquals(expResult, result);
        assertEquals(true, instance.accept(f));
        assertEquals(true, instance.accept(f1));
        result = instance.getDescription();
        expResult = "(.tst1, .tst)";
        assertEquals(expResult, result);
    }

    /**
     * Test of setDescription method, of class MyFileFilter.
     */
    @Test
    public void testSetDescription() {
        String description = "aaaa";
        MyFileFilter instance = new MyFileFilter();
        instance.setDescription(description);
        instance.addExtension("tst");
        instance.addExtension("tst1");
        String result = instance.getDescription();
        String expResult = "aaaa (.tst1, .tst)";
        assertEquals(expResult, result);
    }

    /**
     * Test of setExtensionListInDescription method, of class MyFileFilter.
     */
    @Test
    public void testSetExtensionListInDescription() {
        String description = "aaaa";
        MyFileFilter instance = new MyFileFilter();
        instance.setDescription(description);
        instance.addExtension("tst");
        instance.addExtension("tst1");
        instance.setExtensionListInDescription(false);
        String result = instance.getDescription();
        String expResult = "aaaa";
        assertEquals(expResult, result);
    }

    /**
     * Test of isExtensionListInDescription method, of class MyFileFilter.
     */
    @Test
    public void testIsExtensionListInDescription() {
        MyFileFilter instance = new MyFileFilter();
        assertEquals(true, instance.isExtensionListInDescription());
        instance.setExtensionListInDescription(false);
        assertEquals(false, instance.isExtensionListInDescription());
    }

}