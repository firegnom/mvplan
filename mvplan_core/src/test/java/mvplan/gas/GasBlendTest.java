package mvplan.gas;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import mvplan.main.IMvplan;
import mvplan.main.MvplanInstance;
import mvplan.prefs.Prefs;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GasBlendTest {

	@Mock
	IMvplan mv;
	@Mock
	Prefs p;

	@Before
	public void prepeare() {
		MockitoAnnotations.initMocks(this);
		when(mv.getPrefs()).thenReturn(p);
		when(p.getPConversion()).thenReturn(10.0);
		when(p.getMaxMOD()).thenReturn(1.607);
		MvplanInstance.setMvplan(mv);
	}

	@Test
	public void testGetVolume() {
		GasBlend b = new GasBlend(15, 230);
		assertEquals(15, b.getVolume(), 0.00001);
	}

	@Test
	public void testSetVolume() {
		
		GasBlend b = new GasBlend(10,200);
		
		Gas g = new Gas(0.5);
		g.setVolume(1800);
		b.add(g);
		
		boolean set = b.setVolume(9);
		assertTrue(set);
	
		 set = b.setVolume(8);
		 assertFalse(set);

	}

	@Test
	public void testGetMaxPressure() {
		GasBlend b = new GasBlend(15, 230);
		assertEquals(230, b.getMaxPressure(), 0.00001);
	}

	@Test
	public void testSetMaxPressure() {
		GasBlend b = new GasBlend(10,200);
		
		Gas g = new Gas(0.5);
		g.setVolume(1800);
		b.add(g);
		
		boolean set = b.setMaxPressure(180);
		assertTrue(set);
	
		 set = b.setMaxPressure(179);
		 assertFalse(set);
	}

	@Test
	public void testGetCurrentVolume() {
		GasBlend b = new GasBlend(15, 230);

		assertEquals(0, b.getCurrentVolume(), 0.1);

		Gas g = new Gas(0.5);
		g.setVolume(3000);
		boolean add = b.add(g);
		assertEquals(3000, b.getCurrentVolume(), 0.1);

		Gas g1 = new Gas(0.2);
		g1.setVolume(300);
		add = b.add(g1);
		assertEquals(3300, b.getCurrentVolume(), 0.1);

		Gas g2 = new Gas(0.35);
		g2.setVolume(150);
		add = b.add(g2);
		assertEquals(3450, b.getCurrentVolume(), 0.1);

	}

	@Test
	public void testGetMaxVolume() {
		GasBlend b = new GasBlend(15, 230);
		assertEquals(3450, b.getMaxVolume(), 0.00001);
	}

	@Test
	public void testGetCurrentPressure() {
		GasBlend b = new GasBlend(15, 230);

		assertEquals(0, b.getCurrentPressure(), 0.1);

		Gas g = new Gas(0.5);
		g.setVolume(3000);
		boolean add = b.add(g);
		assertEquals(200, b.getCurrentPressure(), 0.1);

		Gas g1 = new Gas(0.2);
		g1.setVolume(300);
		add = b.add(g1);
		assertEquals(220, b.getCurrentPressure(), 0.1);

		Gas g2 = new Gas(0.35);
		g2.setVolume(150);
		add = b.add(g2);
		assertEquals(230, b.getCurrentPressure(), 0.1);
	}

	@Test
	public void testAdd() {
		GasBlend b = new GasBlend(15, 230);

		Gas g = new Gas(0.5);
		g.setVolume(30000);
		boolean add = b.add(g);
		assertFalse(add);

		g.setVolume(3000);
		add = b.add(g);
		assertTrue(add);

		Gas g1 = new Gas(0.2);
		g1.setVolume(300);
		add = b.add(g1);
		assertTrue(add);

		Gas g2 = new Gas(0.35);
		g2.setVolume(150);
		add = b.add(g2);
		assertTrue(add);

		Gas g3 = new Gas(0.35);
		g3.setVolume(1);
		add = b.add(g3);
		assertFalse(add);

	}

	@Test
	public void testblend() {
		GasBlend b = new GasBlend(15, 230);

		boolean add;
		Gas g = new Gas(0.5);
		g.setVolume(30000);
		g.setVolume(3000);
		add = b.add(g);

		Gas g1 = new Gas(0.5, 0.18);
		g1.setVolume(300);
		add = b.add(g1);

		Gas g2 = new Gas(1, 0);
		g2.setVolume(150);
		add = b.add(g2);

		Gas blend = b.blend();

	}
}
