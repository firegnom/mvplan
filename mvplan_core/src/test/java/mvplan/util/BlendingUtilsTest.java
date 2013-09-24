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

public class BlendingUtilsTest {
	
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
	public void testCalculateGas() {
		Gas g = new Gas(0.21);
		g.setVolume(2000);
		Gas r = BlendingUtils.gasRequired(g, .21, .45, 5000);
		assertEquals(.21,r.getFO2(),.00001);
		assertEquals(.75,r.getFHe(),.00001);
		assertEquals(3000,r.getVolume(),.00001);
	}
	
}
