package mvplan.util;

import static org.junit.Assert.*;
import static mvplan.util.PressureConverter.*;

import org.junit.Test;

public class PressureConverterTest {
	
	
	@Test
	public void testConverter() {
        System.out.println("Altitude: "+0.0+" Pressure: "+altitudeToPressure(0.0));
        assertEquals(10.0, PressureConverter.altitudeToPressure(0.0),0.00001);
        System.out.println("Altitude: "+500.0+" Pressure: "+altitudeToPressure(500.0));
        assertEquals(9.422750314361055, altitudeToPressure(500.0),0.000000001);
        System.out.println("Altitude: "+1000.0+" Pressure: "+altitudeToPressure(1000.0));
        assertEquals(8.871340767364433, altitudeToPressure(1000.0),0.000000001);
        System.out.println("Altitude: "+2000.0+" Pressure: "+altitudeToPressure(2000.0));
        assertEquals(7.846815182615748, altitudeToPressure(2000.0),0.000000001);
        System.out.println("Altitude: "+3000.0+" Pressure: "+altitudeToPressure(3000.0));
        assertEquals(6.920275502715149, altitudeToPressure(3000.0),0.000000001);
        System.out.println("Altitude: "+4000.0+" Pressure: "+altitudeToPressure(4000.0));
        assertEquals(6.084385478794324, altitudeToPressure(4000.0),0.000000001);
        System.out.println("Altitude: "+5000.0+" Pressure: "+altitudeToPressure(5000.0));
        assertEquals(5.332198499669897, altitudeToPressure(5000.0),0.000000001);
	}

}
