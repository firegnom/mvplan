package mvplan.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import mvplan.gas.Gas;
import mvplan.main.IMvplan;
import mvplan.main.MvplanInstance;
import mvplan.prefs.Prefs;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GasUtilsTest {
	
	@Mock IMvplan mv;
	@Mock Prefs p;
	
	@Before
	public void prepeare(){
		MockitoAnnotations.initMocks(this);
		when(mv.getPrefs()).thenReturn(p);
		when(p.getPConversion()).thenReturn(10.0);
		when(p.getMaxMOD()).thenReturn(1.607);
		MvplanInstance.setMvplan(mv);
	}
	
	@Test
	public void testCalculateEND1() {
		when(p.getHeliumNarcoticLevel()).thenReturn(0.0);
		when(p.getOxygenNarcoticLevel()).thenReturn(0.0);
		double o2 = .30;
		double he = .0;
		Gas g = new Gas(he,o2,GasUtils.getMaxMod(o2));
		double end = GasUtils.calculateEND(g,40);
		assertEquals(34, ((int)end));
	}
	
	@Test
	public void testCalculateEND2() {
		when(p.getHeliumNarcoticLevel()).thenReturn(0.0);
		when(p.getOxygenNarcoticLevel()).thenReturn(1.0);
		double o2 = .30;
		double he = .0;
		Gas g = new Gas(he,o2,GasUtils.getMaxMod(o2));
		double end = GasUtils.calculateEND(g,40);
		assertEquals(40, ((int)end));
	}
	
	@Test
	public void testCalculateEND3() {
		when(p.getHeliumNarcoticLevel()).thenReturn(0.0);
		when(p.getOxygenNarcoticLevel()).thenReturn(1.0);
		double o2 = .30;
		double he = .39;
		Gas g = new Gas(he,o2,GasUtils.getMaxMod(o2));
		double end = GasUtils.calculateEND(g,40);
		assertEquals(20, ((int)end));
	}
	
	@Test
	public void testCalculateEND4() {
		when(p.getHeliumNarcoticLevel()).thenReturn(0.0);
		when(p.getOxygenNarcoticLevel()).thenReturn(0.0);
		double o2 = .30;
		double he = .39;
		Gas g = new Gas(he,o2,GasUtils.getMaxMod(o2));
		double end = GasUtils.calculateEND(g,40);
		assertEquals(10,(int)Math.rint(end));
	}
	
	@Test
	public void testCalculateEND5() {
		when(p.getHeliumNarcoticLevel()).thenReturn(.23);
		when(p.getOxygenNarcoticLevel()).thenReturn(1.0);
		double o2 = .12;
		double he = .51;
		Gas g = new Gas(he,o2,GasUtils.getMaxMod(o2));
		double end = GasUtils.calculateEND(g,100);
		assertEquals(57,(int)Math.rint(end));
	}
	
	
	@Test
	public void testCalculateENDImperial1() {
		when(p.getHeliumNarcoticLevel()).thenReturn(.0);
		when(p.getOxygenNarcoticLevel()).thenReturn(.0);
		when(p.getPConversion()).thenReturn(33.0);
		
		double o2 = .21;
		double he = .0;
		Gas g = new Gas(he,o2,GasUtils.getMaxMod(o2));
		double end = GasUtils.calculateEND(g,99);
		assertEquals(99,(int)Math.rint(end));
	}
	
	@Test
	public void testCalculateENDImperial2() {
		when(p.getHeliumNarcoticLevel()).thenReturn(.23);
		when(p.getOxygenNarcoticLevel()).thenReturn(.1);
		when(p.getPConversion()).thenReturn(33.0);
		
		double o2 = .19;
		double he = .35;
		Gas g = new Gas(he,o2,GasUtils.getMaxMod(o2));
		double end = GasUtils.calculateEND(g,139);
		assertEquals(86,(int)Math.rint(end));
	}
	
	@Test
	public void testCalculateENDImperial3() {
		when(p.getHeliumNarcoticLevel()).thenReturn(.0);
		when(p.getOxygenNarcoticLevel()).thenReturn(.1);
		when(p.getPConversion()).thenReturn(33.0);
		
		double o2 = .19;
		double he = .35;
		Gas g = new Gas(he,o2,GasUtils.getMaxMod(o2));
		double end = GasUtils.calculateEND(g,139);
		assertEquals(69,(int)Math.rint(end));
	}
	@Test
	public void testCalculateENDImperial4() {
		when(p.getHeliumNarcoticLevel()).thenReturn(.0);
		when(p.getOxygenNarcoticLevel()).thenReturn(.0);
		when(p.getPConversion()).thenReturn(33.0);
		
		double o2 = .19;
		double he = .35;
		Gas g = new Gas(he,o2,GasUtils.getMaxMod(o2));
		double end = GasUtils.calculateEND(g,139);
		assertEquals(67,(int)Math.rint(end));
	}
	
	
	@Test 
	public void testBlend(){
		Gas g1 = new Gas (0.20);
		g1.setVolume(1000);
		Gas g2 = new Gas (0.80);
		g2.setVolume(1000);
		Gas blend = GasUtils.blend(g1, g2);
		assertEquals(2000, blend.getVolume(),0.1);
		assertEquals(0.5, blend.getFO2(),0.00001);
		
	}
	
	
	@Test 
	public void testBlendHe(){
		Gas g1 = new Gas (0.20);
		g1.setVolume(1000);
		Gas g2 = new Gas (0.50,0.18);
		g2.setVolume(1000);
		Gas blend = GasUtils.blend(g1, g2);
		assertEquals(2000, blend.getVolume(),0.1);
		assertEquals(0.19, blend.getFO2(),0.00001);
		assertEquals(0.25, blend.getFHe(),0.00001);
		
	}
	
	@Test 
	public void testBlendempty(){
		Gas g1 = new Gas ();
		Gas g2 = new Gas (0.50,0.18);
		g2.setVolume(1000);
		Gas blend = GasUtils.blend(g1, g2);
		assertEquals(1000, blend.getVolume(),0.1);
		assertEquals(0.18, blend.getFO2(),0.00001);
		assertEquals(0.5, blend.getFHe(),0.00001);
		
	}
	
	@Test 
	public void testBlend0Volume(){
		Gas g1 = new Gas (0.20);
		Gas g2 = new Gas (0.80);
		Gas blend = GasUtils.blend(g1, g2);
		assertNull(blend);
	}
	@Test 
	public void testBlend0Volume1(){
		Gas g1 = new Gas (0.20);
		g1.setVolume(1000);
		Gas g2 = new Gas (0.80);
		Gas blend = GasUtils.blend(g1, g2);
		assertEquals(1000, blend.getVolume(),0.1);
		assertEquals(0.2, blend.getFO2(),0.0001);
	}
	@Test 
	public void testBlend0Volume2(){
		Gas g1 = new Gas (0.20);
		Gas g2 = new Gas (0.80);
		g2.setVolume(1000);
		Gas blend = GasUtils.blend(g1, g2);
		assertEquals(1000, blend.getVolume(),0.1);
		assertEquals(0.8, blend.getFO2(),0.0001);
	}

}
