/* =============================================================================== */
/*     Varying Permeability Model (VPM) Decompression Program in FORTRAN */
/*     with Boyle's Law compensation algorithm (VPM-B) */
/* */
/*     Author:  Erik C. Baker */
/* */
/*     "DISTRIBUTE FREELY - CREDIT THE AUTHORS" */
/* */
/*     This program extends the 1986 VPM algorithm (Yount & Hoffman) to include */
/*     mixed gas, repetitive, and altitude diving.  Developments to the algorithm */
/*     were made by David E. Yount, Eric B. Maiken, and Erik C. Baker over a */
/*     period from 1999 to 2001.  This work is dedicated in remembrance of */
/*     Professor David E. Yount who passed away on April 27, 2000. */
/* */
/*     Notes: */
/*     1.  This program uses the sixteen (16) half-time compartments of the */
/*         Buhlmann ZH-L16 model.  The optional Compartment 1b is used here with */
/*         half-times of 1.88 minutes for helium and 5.0 minutes for nitrogen. */
/* */
/*     2.  This program uses various DEC, IBM, and Microsoft extensions which */
/*         may not be supported by all FORTRAN compilers.  Comments are made with */
/*         a capital "C" in the first column or an exclamation point "!" placed */
/*         in a line after code.  An asterisk "*" in column 6 is a continuation */
/*         of the previous line.  All code, except for line numbers, starts in */
/*         column 7. */
/* */
/*     3.  Comments and suggestions for improvements are welcome.  Please */
/*         respond by e-mail to:  EBaker@se.aeieng.com */
/* */
/*     Acknowledgment:  Thanks to Kurt Spaugh for recommendations on how to clean */
/*     up the code. */
/* =============================================================================== */
/*     Converted to vpmdeco.c using f2c; R.McGinnis (CABER Swe) 5/01 */
/* =============================================================================== */
/* =============================================================================== */
/*     Added  Boyles law compensation from Bakers FORTRAN code and ported to JAVA  */
/*     Jure Zelic 6/2006                                                           */
/*                                                                                 */
/*     Revisions:                                                                  */
/*       Feb.  2007: J. Zelic - VPMOpen 1.2.0 - added firstDecoProfilePoint        */
/*       Mar.  2007: J. Zelic - VPMOpen 1.3.0 - repeated dives                     */
/*       May.  2007: J. Zelic - VPMOpen 1.3.2 - 6m/20ft last deco stop             */
/*       May.  2007: J. Zelic - VPMOpen 1.4.0 - multilevel algorithm - first stage              */
/* =============================================================================== */
package mvplan.model;

public class BakerVpmB
{

/* JURE konfiguracijski parametri */
public  double    conservatism=2; // 0 to 4
public  double    deco_gas_switch_time=3.0;
public double     finalAscentSpeed=-9;
public boolean    lastStop6m20ft=false;

private String    units="msw";
private String    altitude_dive_algorithm="off";
private double    minimum_deco_stop_time=1.0;
private double    critical_radius_n2_microns_basic=0.6;
private double    critical_radius_he_microns_basic=0.5;
private String    critical_volume_algorithm="on";
private double    crit_volume_parameter_lambda=6500.0;
private double    gradient_onset_of_imperm_atm=8.2;
private double    surface_tension_gamma=0.0179;
private double    skin_compression_gammac=0.257;
private double    regeneration_time_constant=20160.0;
private double    pressure_other_gases_mmhg=102.0;

private double    altitude_of_dive=1000.0;
private String    diver_acclimatized_at_altitude="no";
private double    starting_acclimatized_altitude=0.0;
private double    ascent_to_altitude_hours=10.0;
private double    hours_at_altitude_before_dive=5.0;

public final int MAX_DECO_MIXES=5; // maximal number of deco mixes that can be used
public final int MAX_BOTTOM_MIXES=5;
public final int MAX_PROFILE_POINTS=10;
public final int MAX_OUTPUT_POINTS=70;
public final int MAX_DIVES=10; // Must be defined same as in Vpm.java

private int dive_no=0;

/* Deco Mixes */
private double decoMixfO2[][]=new double[MAX_DECO_MIXES+1][MAX_DIVES+1];
private double decoMixfHe[][]=new double[MAX_DECO_MIXES][MAX_DIVES];
private double decoMOD[][]=new double[MAX_DECO_MIXES][MAX_DIVES];

/* Bottom Mixes */
private double bottomMixfO2[][]=new double[MAX_BOTTOM_MIXES+1][MAX_DIVES+1];
private double bottomMixfHe[][]=new double[MAX_BOTTOM_MIXES][MAX_DIVES];

/* Profile */
private double profileDepth[][]=new double[MAX_PROFILE_POINTS+1][MAX_DIVES+1];
private double profileTime[][]=new double[MAX_PROFILE_POINTS][MAX_DIVES];
private int profileMix[][]=new int[MAX_PROFILE_POINTS][MAX_DIVES];
private double profileDecAccSpeed[][]=new double[MAX_PROFILE_POINTS][MAX_DIVES];

private double surfaceIntervals[]=new double[MAX_DIVES];

/* Final ascent data */
private double decoStepSize=3;

/* Output Data */
private double outputProfileDepth[][]=new double[MAX_OUTPUT_POINTS+1][MAX_DIVES+1];
private double outputProfileTime[][]=new double[MAX_OUTPUT_POINTS][MAX_DIVES];
private double outputProfileSegmentTime[][]=new double[MAX_OUTPUT_POINTS][MAX_DIVES];
private double outputProfileMixO2[][]=new double[MAX_OUTPUT_POINTS][MAX_DIVES];
private double outputProfileMixHe[][]=new double[MAX_OUTPUT_POINTS][MAX_DIVES];
private int    outputProfileGas[][]=new int[MAX_OUTPUT_POINTS][MAX_DIVES];
private int outputProfileCounter;
private boolean decoProfileCalculated=false;
public double otputStartOfDecoDepth[]=new double[MAX_DIVES];
public int firstDecoProfilePoint=0;

private double    deco_ceiling_depth;
private double    ascent_ceiling_depth;
private double    deco_stop_depth, next_deco_stop_depth;
private int     err;

/* temp vars to simplify UNFMTLISTs */
private double    fO2, fHe, fN2;
private double    dc, rc, ssc;
private int mc;

/* Common Block Declarations */

private double    water_vapor_pressure;
private double    run_time;
private int segment_number;
private double    segment_time;
private double    ending_ambient_pressure;
private int mix_number;
private double    barometric_pressure;
private boolean units_equal_fsw, 
        units_equal_msw;
private double    units_factor;
private double    helium_time_constant[]=new double[16];
private double    nitrogen_time_constant[]=new double[16];
private double    helium_pressure[]=new double[16], 
        nitrogen_pressure[]=new double[16];
private double    fraction_helium[]=new double[10], 
        fraction_nitrogen[]=new double[10];
private double    initial_critical_radius_he[]=new double[16], 
        initial_critical_radius_n2[]=new double[16];
private double    adjusted_critical_radius_he[]=new double[16], 
        adjusted_critical_radius_n2[]=new double[16];
private double    max_crushing_pressure_he[]=new double[16], 
        max_crushing_pressure_n2[]=new double[16];
private double    surface_phase_volume_time[]=new double[16];
private double    max_actual_gradient[]=new double[16];
private double    amb_pressure_onset_of_imperm[]=new double[16], 
        gas_tension_onset_of_imperm[]=new double[16];
private double    initial_helium_pressure[]=new double[16], 
        initial_nitrogen_pressure[]=new double[16];
private double    regenerated_radius_he[]=new double[16], 
        regenerated_radius_n2[]=new double[16];
private double    adjusted_crushing_pressure_he[]=new double[16], 
        adjusted_crushing_pressure_n2[]=new double[16];
private double    allowable_gradient_he[]=new double[16], 
        allowable_gradient_n2[]=new double[16];
private double    deco_gradient_he[]=new double[16], 
        deco_gradient_n2[]=new double[16];
private double    initial_allowable_gradient_he[]=new double[16], 
        initial_allowable_gradient_n2[]=new double[16];
private double constant_pressure_other_gases;

public BakerVpmB() 
{
newMission();
}

public void newMission()
{
    int i,j;

    for (i=0;i<=MAX_DECO_MIXES;i++)
        for (j=0;j<=MAX_DIVES;j++)
            decoMixfO2[i][j]=-1;
    for (i=0;i<=MAX_BOTTOM_MIXES;i++)
        for (j=0;j<=MAX_DIVES;j++)
            bottomMixfO2[i][j]=-1;
    for (i=0;i<=MAX_PROFILE_POINTS;i++)
        for (j=0;j<=MAX_DIVES;j++)
            profileDepth[i][j]=-1;
    for (i=0;i<=MAX_OUTPUT_POINTS;i++)
        for (j=0;j<=MAX_DIVES;j++)
            outputProfileDepth[i][j]=-1;
    dive_no=0;
    decoProfileCalculated=false;
}

public int addSurfaceInterval(double interval)
{
	  if (dive_no<MAX_DIVES-1)
	  {
	  	  dive_no++;
	  	  surfaceIntervals[dive_no]=interval;
	  	  return dive_no;
	  }
	  return -1;
}

public int addBottomMix(double fO2, double fHe)
{
// add new bottom mix to deco mix list
// returnes index>=0 if OK and -1 if not
// posible errors:
//         fO2+fHe>1
//         fO2<0.01
//         fHe<0
//         the number of mixes already added > MAX_BOTTOM_MIXES

    int i;

    if ((fO2+fHe>1) || (fO2<0.01) || (fHe<0))
        return -1;

    for (i=0;i<MAX_BOTTOM_MIXES;i++)
    {
        if(bottomMixfO2[i][dive_no]==-1) // found empty spot
        {
            bottomMixfO2[i][dive_no]=fO2;
            bottomMixfHe[i][dive_no]=fHe;
            return i;
        }
    }
    return -1;
}

public int addDecoMix(double fO2, double fHe, double mod)
{
// add new deco mix to deco mix list
// mixes must be added deepest mod first
// returnes index>=0 if OK and -1 if not
// posible errors:
//         fO2+fHe>1
//         fO2<0.01
//         fHe<0
//         the number of mixes already added > MAX_BOTTOM_MIXES
//         shalower mod exists in deco mix list

    int i;

    if ((fO2+fHe>1) || (fO2<0.01) || (fHe<0))
        return -1;

    for (i=0;i<MAX_DECO_MIXES;i++)
    {
        if ((i>0) && (mod>=decoMOD[i-1][dive_no]))
          return -1;
        if(decoMixfO2[i][dive_no]==-1) // found empty spot
        {
            decoMixfO2[i][dive_no]=fO2;
            decoMixfHe[i][dive_no]=fHe;
            decoMOD[i][dive_no]=mod;
            return i;
        }
    }
    return -1;
}

public int addProfilePoint(double depth, double time, double speed, int mix)
{
// add new deco profile point to dive profile
// profile points must be added from earlier one to later one
// mix must be added with addBottomMix() metode
// speed must be positive for descent and negative for ascent
// returnes index>=0 if OK and -1 if not
// posible errors:
//         depth <=0
//         mix not existent
//         speed is 0 or wrong signed
//         later time point already exists in dive profile
//         no emty spot left
    int i;

    if ((depth<0) || (mix<0) || (mix>=MAX_DECO_MIXES))
        return -1;

    for (i=0;i<MAX_PROFILE_POINTS;i++)
    {
        if(profileDepth[i][dive_no]==-1) // found empty spot
        {
//            if ((i!=0) && (time <= profileTime[i-1][dive_no]))
//                return -1;
            if ((i!=0) && (depth < profileDepth[i-1][dive_no]) && (speed>=0))
                return -1;
            if ((i!=0) && (depth > profileDepth[i-1][dive_no]) && (speed<=0))
                return -1;
            if ((i==0) && (speed<=0))
                return -1;
            profileDepth[i][dive_no]=depth;
            profileTime[i][dive_no]=time;
            profileMix[i][dive_no]=mix;
            profileDecAccSpeed[i][dive_no]=speed;
            return i;                              
        }
    }
    return 1;//-1;
}

public double getProfilePoint(int index, int parameter, int dive)
{
// returnes profile point depth, time, f02 or fHe from calculated dive-deco profile
// returnes profile point parameter>=0 if OK and -1 if not
// posible errors:
//          index too big
//          wrong parameter
//          dive-deco profile not yet calculated
    if ((decoProfileCalculated==false) || (index>=MAX_OUTPUT_POINTS) || (dive>=MAX_DIVES))
        return -1;
    if (outputProfileDepth[index][dive]==-1)
        return -1;

    switch(parameter)
    {
    case 1:
        return outputProfileDepth[index][dive];
    case 2:
        return outputProfileTime[index][dive];
    case 3:
        return outputProfileSegmentTime[index][dive];
    case 4:
        return outputProfileMixO2[index][dive];
    case 5:
        return outputProfileMixHe[index][dive];  
    }
    return -1;

}

public double getProfilePoint(int index, int parameter)
{
// returnes profile point depth, time, f02 or fHe from calculated dive-deco profile
//          for the first dive - FUNCTION FOR BACK COMPATIBILITY
// returnes profile point parameter>=0 if OK and -1 if not
// posible errors:
//          index too big
//          wrong parameter
//          dive-deco profile not yet calculated
    int dive=0;
    
    if ((decoProfileCalculated==false) || (index>=MAX_OUTPUT_POINTS) || (dive>=MAX_DIVES))
        return -1;
    if (outputProfileDepth[index][dive]==-1)
        return -1;

    switch(parameter)
    {
    case 1:
        return outputProfileDepth[index][dive];
    case 2:
        return outputProfileTime[index][dive];
    case 3:
        return outputProfileSegmentTime[index][dive];
    case 4:
        return outputProfileMixO2[index][dive];
    case 5:
        return outputProfileMixHe[index][dive];  
    }
    return -1;

}

public int getProfileGasIndex(int index, int dive)
{
// returnes the index of the mix used on a given dive point
// returnes profile point parameter>=0 if OK and -1 if not
// posible errors:
//          index too big
//          wrong parameter
//          dive-deco profile not yet calculated
    if ((decoProfileCalculated==false) || (index>=MAX_OUTPUT_POINTS) || (dive>=MAX_DIVES))
        return -1;
    if (outputProfileDepth[index][dive]==-1)
        return -1;
    return outputProfileGas[index][dive];  
}

public int getProfileGasIndex(int index)
{
// returnes the index of the mix used on a given dive point
//          for the first dive - FUNCTION FOR BACK COMPATIBILITY
// returnes profile point parameter>=0 if OK and -1 if not
// posible errors:
//          index too big
//          wrong parameter
//          dive-deco profile not yet calculated
    int dive=0;
    
    if ((decoProfileCalculated==false) || (index>=MAX_OUTPUT_POINTS) || (dive>=MAX_DIVES))
        return -1;
    if (outputProfileDepth[index][dive]==-1)
        return -1;
    return outputProfileGas[index][dive];  
}

public int calculate()
{

/* =============================================================================== */
/*     ASSIGN HALF-TIME VALUES TO BUHLMANN COMPARTMENT ARRAYS */
/* =============================================================================== */
    /* Initialized data */

    double helium_half_time[] = 
        {  1.88,   3.02,   4.72,   6.99,
          10.21,  14.48,  20.53,  29.11,
          41.2,   55.19,  70.69,  90.34,
         115.29, 147.42, 188.24, 240.03 };
    double nitrogen_half_time[] = 
        { 5.,    8.,  12.5, 18.5,
          27.,  38.3, 54.3, 77.,
          109.,146., 187.,  239.,
          305.,390., 498.,  635. };

    int i1;
    double    r1;
    double    critical_radius_n2_microns=critical_radius_n2_microns_basic+conservatism/20;
    double    critical_radius_he_microns=critical_radius_he_microns_basic+conservatism/20;

    /* Local variables */
    double    fraction_oxygen[]=new double[10];
    double    run_time_end_of_segment, 
            rate, 
	        n2_pressure_start_of_ascent[]=new double[16];
    double    depth_change[]=new double[10];
    boolean altitude_dive_algorithm_off;
    double    ending_depth;
    double    run_time_start_of_deco_zone;
    double    deepest_possible_stop_depth, 
            he_pressure_start_of_ascent[]=new double[16], 
            altitude_of_dive, 
            step_size_change[]=new double[10];
    int i,j;
    double    first_stop_depth;
    double    depth, depth_start_of_deco_zone;
    double    run_time_start_of_ascent;
    int number_of_changes;
    double    stop_time;
    double    step_size,
            next_stop, 
            last_run_time, 
	        phase_volume_time[]=new double[16], 
            surface_interval_time;
    int mix_change[]=new int[10];
              
    boolean critical_volume_algorithm_off;
    boolean schedule_converged;
    double    starting_depth, 
	      deco_phase_volume_time;
    double    rate_change[]=new double[10],  
	        last_phase_volume_time[]=new double[16], 
            n2_pressure_start_of_deco_zone[]=new double[16];
    double    critical_volume_comparison;
    int segment_number_start_of_ascent;
    double    rounding_operation, 
            rounding_operation2, 
	        he_pressure_start_of_deco_zone[]=new double[16];

    double lastDivePointDepth=0;
    int noOfDecoMixes, noOfBottomMixes;
    double min_deco_stop_time=0; 
    int dive_number=0;
    // JURE multilevel START
    int decoDivePoints;
    boolean checkForDecoMix=true;
    // JURE multilevel END

/* =============================================================================== */
/*     READ IN PROGRAM SETTINGS AND CHECK FOR ERRORS */
/*     IF THERE ARE ERRORS, WRITE AN ERROR MESSAGE AND TERMINATE PROGRAM */
/* =============================================================================== */

    if (units.equals("fsw") || units.equals("FSW")) {
	    units_equal_fsw = true;
	    units_equal_msw = false;
    } else if (units.equals("msw") || units.equals("MSW")) {
	    units_equal_fsw = false;
	    units_equal_msw = true;
    } else {

        return -1;
    }
    if (altitude_dive_algorithm.equals("ON") || 
	    altitude_dive_algorithm.equals("on"))
	     {
	    altitude_dive_algorithm_off = false;
    } else {
	    altitude_dive_algorithm_off = true;
    }

    if (critical_radius_n2_microns < .2 || 
	    critical_radius_n2_microns > 1.35) {
        return -1;
    }
    if (critical_radius_he_microns < .2 || 
	    critical_radius_he_microns > 1.35) {
        return -1;
    }
    if (critical_volume_algorithm.equals("ON") ||
	        critical_volume_algorithm.equals("on")) {
	    critical_volume_algorithm_off = false;
    } else {
	    critical_volume_algorithm_off = true;
    } 

/* =============================================================================== */
/*     INITIALIZE CONSTANTS/VARIABLES BASED ON SELECTION OF UNITS - FSW OR MSW */
/*     fsw = feet of seawater, a unit of pressure */
/*     msw = meters of seawater, a unit of pressure */
/* =============================================================================== */

    if (units_equal_fsw) {
	    units_factor = 33.;
	    water_vapor_pressure = 1.607;/* (Schreiner value)  based on respiratory quotient */
    }
    else {
	    units_factor = 10.1325;
	    water_vapor_pressure = .493;/* (Schreiner value)  based on respiratory quotien */
    }

/* =============================================================================== */
/*     INITIALIZE CONSTANTS/VARIABLES */
/* =============================================================================== */

    constant_pressure_other_gases = 
        pressure_other_gases_mmhg / 760. * units_factor;
    run_time = 0.;
    segment_number = 0;
    for (i = 1; i <= 16; ++i) {
	    helium_time_constant[i - 1] = Math.log(2.) / helium_half_time[i - 1];
	    nitrogen_time_constant[i - 1] = Math.log(2.) / nitrogen_half_time[i - 1];
	    max_crushing_pressure_he[i - 1] = 0.;
	    max_crushing_pressure_n2[i - 1] = 0.;
	    max_actual_gradient[i - 1] = 0.;
	    surface_phase_volume_time[i - 1] = 0.;
	    amb_pressure_onset_of_imperm[i - 1] = 0.;
	    gas_tension_onset_of_imperm[i - 1] = 0.;
	    initial_critical_radius_n2[i - 1] = 
	    	critical_radius_n2_microns * 1e-6;
	    initial_critical_radius_he[i - 1] = 
		    critical_radius_he_microns * 1e-6;
    }

/* =============================================================================== */
/*     INITIALIZE VARIABLES FOR SEA LEVEL OR ALTITUDE DIVE */
/*     See subroutines for explanation of altitude calculations.  Purposes are */
/*     1) to determine barometric pressure and 2) set or adjust the VPM critical */
/*     radius variables and gas loadings, as applicable, based on altitude, */
/*     ascent to altitude before the dive, and time at altitude before the dive */
/* =============================================================================== */

    if (altitude_dive_algorithm_off) {
	    altitude_of_dive = 0.;
	    calc_barometric_pressure(altitude_of_dive); 

	    for (i = 1; i <= 16; ++i) {
	        adjusted_critical_radius_n2[i - 1] = 
		        initial_critical_radius_n2[i - 1];
	        adjusted_critical_radius_he[i - 1] = 
		        initial_critical_radius_he[i - 1];
	        helium_pressure[i - 1] = 0.;
	        nitrogen_pressure[i - 1] = 
		        (barometric_pressure - water_vapor_pressure) * .79;
	    }
    } else {
	    vpm_altitude_dive_algorithm();  
    }

/* =============================================================================== */
/*     START OF REPETITIVE DIVE LOOP */
/*     This is the largest loop in the main program and operates between Lines */
/*     30 and 330.  If there is one or more repetitive dives, the program will */
/*     return to this point to process each repetitive dive. */
/* =============================================================================== */
/* L30: */
    while(true) {                      /* until there is an break statement */
                                        /* loop will run continuous */
          outputProfileCounter=0;
          run_time_end_of_segment=0;
/* =============================================================================== */
/*     INPUT DIVE DESCRIPTION AND GAS MIX DATA FROM ASCII TEXT INPUT FILE */
/*     BEGIN WRITING HEADINGS/OUTPUT TO ASCII TEXT OUTPUT FILE */
/*     See separate explanation of format for input file. */
/* =============================================================================== */

	    for (i = 1; i <= MAX_BOTTOM_MIXES; ++i) {
                if (bottomMixfO2[i-1][dive_number]<0)
                    break;

                fraction_oxygen[i - 1] = bottomMixfO2[i-1][dive_number];
                fraction_helium[i - 1] = bottomMixfHe[i-1][dive_number];
                fraction_nitrogen[i - 1] = 1-bottomMixfO2[i-1][dive_number]-bottomMixfHe[i-1][dive_number];
	    }
            noOfBottomMixes=i-1;

	    for (j = 1; j <= MAX_DECO_MIXES; ++j) {
                if (decoMixfO2[j-1][dive_number]<0)
                    break;

                fraction_oxygen[i + j - 2] = decoMixfO2[j-1][dive_number];
                fraction_helium[i + j - 2] = decoMixfHe[j-1][dive_number];
                fraction_nitrogen[i + j - 2] = 1-decoMixfO2[j-1][dive_number]-decoMixfHe[j-1][dive_number];
	    }
            noOfDecoMixes=j-1;

/* =============================================================================== */
/*     DIVE PROFILE LOOP - INPUT DIVE PROFILE DATA FROM ASCII TEXT INPUT FILE */
/*     AND PROCESS DIVE AS A SERIES OF ASCENT/DESCENT AND CONSTANT DEPTH */
/*     SEGMENTS.  THIS ALLOWS FOR MULTI-LEVEL DIVES AND UNUSUAL PROFILES.  UPDATE */
/*     GAS LOADINGS FOR EACH SEGMENT.  IF IT IS A DESCENT SEGMENT, CALC CRUSHING */
/*     PRESSURE ON CRITICAL RADII IN EACH COMPARTMENT. */
/*     "Instantaneous" descents are not used in the VPM.  All ascent/descent */
/*     segments must have a realistic rate of ascent/descent.  Unlike Haldanian */
/*     models, the VPM is actually more conservative when the descent rate is */
/*     slower becuase the effective crushing pressure is reduced.  Also, a */
/*     realistic actual supersaturation gradient must be calculated during */
/*     ascents as this affects critical radii adjustments for repetitive dives. */
/*     Profile codes: 1 = Ascent/Descent, 2 = Constant Depth, 99 = Decompress */
/* =============================================================================== */
        i=0;
        deepest_possible_stop_depth=-1; // JURE multilevel
	while(true) {          /* until there is an exit statement loop will run continuous */
            if (profileDepth[i][dive_number]<0)
                break;
            // JURE multilevel START - end when the first dive shalower then deepest possible deco stop
            if (profileDepth[i][dive_number]<deepest_possible_stop_depth)
                break;            
            // JURE multilevel END
            
            /* Ascent - Descent */  
            if (i==0)
            {
            	  mix_number=profileMix[i][dive_number]+1;
                starting_depth=0;
            }
            else
            {
            	  mix_number=profileMix[i-1][dive_number]+1;
                starting_depth=profileDepth[i-1][dive_number];
            }
            ending_depth=profileDepth[i][dive_number];
            rate=profileDecAccSpeed[i][dive_number];

            gas_loadings_ascent_descen(starting_depth, 
                                       ending_depth, 
                                       rate);           
	    if (ending_depth > starting_depth) {
		        calc_crushing_pressure(starting_depth, 
                                    ending_depth, rate);       
	    }

            /* Constant Depth */  
            depth=profileDepth[i][dive_number];
            run_time_end_of_segment+=profileTime[i][dive_number];
            if (i>0)
            {   // if ascend add ascend time
            	  if(profileDepth[i][dive_number]<profileDepth[i-1][dive_number])
            	      run_time_end_of_segment+=Math.floor((profileDepth[i][dive_number]-profileDepth[i-1][dive_number])/profileDecAccSpeed[i][dive_number]);
            }
            mix_number=profileMix[i][dive_number]+1;
	    gas_loadings_constant_depth(depth, 
                                        run_time_end_of_segment);       
            lastDivePointDepth=ending_depth;
            i++; 

            outputProfileDepth[outputProfileCounter][dive_number]=depth;
            outputProfileTime[outputProfileCounter][dive_number]=run_time;
            outputProfileSegmentTime[outputProfileCounter][dive_number]=segment_time;
            outputProfileMixO2[outputProfileCounter][dive_number]=fraction_oxygen[mix_number-1];
            outputProfileMixHe[outputProfileCounter][dive_number]=fraction_helium[mix_number-1];
            outputProfileGas[outputProfileCounter][dive_number]=mix_number-1;
            // JURE multilevel START - calculate depest deco stop
            depth_start_of_deco_zone=calc_start_of_deco_zone(depth, finalAscentSpeed);
	          if (units_equal_fsw) {
	              if (decoStepSize < 10.) {
	          	    rounding_operation = depth_start_of_deco_zone / decoStepSize - .5;
	          	    deepest_possible_stop_depth = Math.rint(rounding_operation) * decoStepSize;
	              } else {
	          	    rounding_operation = depth_start_of_deco_zone /  10. - .5;
	          	    deepest_possible_stop_depth = Math.rint(rounding_operation) * 10.;
	              }
	          }
	          if (units_equal_msw) {
	              if (decoStepSize < 3.) {
	          	    rounding_operation = depth_start_of_deco_zone / decoStepSize - .5;
	          	    deepest_possible_stop_depth = Math.rint(rounding_operation) * decoStepSize;
	              } else {
	          	    rounding_operation = depth_start_of_deco_zone /  3. - .5;
	          	    deepest_possible_stop_depth = Math.rint(rounding_operation) * 3.;
	              }
	          }     
            // JURE multilevel END 
            outputProfileCounter++;
	}
  firstDecoProfilePoint=outputProfileCounter;
/* =============================================================================== */
/*     BEGIN PROCESS OF ASCENT AND DECOMPRESSION */
/*     First, calculate the regeneration of critical radii that takes place over */
/*     the dive time.  The regeneration time constant has a time scale of weeks */
/*     so this will have very little impact on dives of normal length, but will */
/*     have major impact for saturation dives. */
/* =============================================================================== */

	nuclear_regeneration(run_time);

/* =============================================================================== */
/*     CALCULATE INITIAL ALLOWABLE GRADIENTS FOR ASCENT */
/*     This is based on the maximum effective crushing pressure on critical radii */
/*     in each compartment achieved during the dive profile. */
/* =============================================================================== */

	calc_initial_allowable_gradient();

/* =============================================================================== */
/*     SAVE VARIABLES AT START OF ASCENT (END OF BOTTOM TIME) SINCE THESE WILL */
/*     BE USED LATER TO COMPUTE THE FINAL ASCENT PROFILE THAT IS WRITTEN TO THE */
/*     OUTPUT FILE. */
/*     The VPM uses an iterative process to compute decompression schedules so */
/*     there will be more than one pass through the decompression loop. */
/* =============================================================================== */

	for (i = 1; i <= 16; ++i) {
	    he_pressure_start_of_ascent[i - 1] = 
		    helium_pressure[i - 1];
	    n2_pressure_start_of_ascent[i - 1] = 
		    nitrogen_pressure[i - 1];
	}
	run_time_start_of_ascent = run_time;
	segment_number_start_of_ascent = segment_number;

/* =============================================================================== */
/*     INPUT PARAMETERS TO BE USED FOR STAGED DECOMPRESSION AND SAVE IN ARRAYS. */
/*     ASSIGN INITAL PARAMETERS TO BE USED AT START OF ASCENT */
/*     The user has the ability to change mix, ascent rate, and step size in any */
/*     combination at any depth during the ascent. */
/* =============================================================================== */

  /* first point in deco profile is the last dive point */

  number_of_changes=noOfDecoMixes+1;

  depth_change[0]=lastDivePointDepth;
	mix_change[0] = mix_number; /* last bottom mix */
	rate_change[0] = finalAscentSpeed;
	step_size_change[0] = decoStepSize;

	for (i = 1; i <= noOfDecoMixes; i++) {
  
	    depth_change[i] = decoMOD[i - 1][dive_number];
	    mix_change[i] = noOfBottomMixes+i;
	    rate_change[i] = finalAscentSpeed;
	    step_size_change[i] = decoStepSize;
	}
  // JURE - 6m/20ft START	
  if (lastStop6m20ft)
  {
	    for (i = 1; i <= noOfDecoMixes; i++) {
	    	  if (depth_change[i]==2*decoStepSize)
	    	      step_size_change[i] = 2*decoStepSize;
	    }
	    
	    if (i>=noOfDecoMixes)
	    {
	    	  depth_change[i] = 2*decoStepSize;
	    	  mix_change[i] = mix_change[i-1];
	    	  rate_change[i] = finalAscentSpeed;
	    	  step_size_change[i] = 2*decoStepSize; 
	    	  number_of_changes++; 
      }
  }
	// JURE - 6m/20ft END
	starting_depth = depth_change[0];
	mix_number = mix_change[0];
	rate = rate_change[0];
	step_size = step_size_change[0];

/* =============================================================================== */
/*     CALCULATE THE DEPTH WHERE THE DECOMPRESSION ZONE BEGINS FOR THIS PROFILE */
/*     BASED ON THE INITIAL ASCENT PARAMETERS AND WRITE THE DEEPEST POSSIBLE */
/*     DECOMPRESSION STOP DEPTH TO THE OUTPUT FILE */
/*     Knowing where the decompression zone starts is very important.  Below */
/*     that depth there is no possibility for bubble formation because there */
/*     will be no supersaturation gradients.  Deco stops should never start */
/*     below the deco zone.  The deepest possible stop deco stop depth is */
/*     defined as the next "standard" stop depth above the point where the */
/*     leading compartment enters the deco zone.  Thus, the program will not */
/*     base this calculation on step sizes larger than 10 fsw or 3 msw.  The */
/*     deepest possible stop depth is not used in the program, per se, rather */
/*     it is information to tell the diver where to start putting on the brakes */
/*     during ascent.  This should be prominently displayed by any deco program. */
/* =============================================================================== */

	depth_start_of_deco_zone=calc_start_of_deco_zone(starting_depth, rate);
        otputStartOfDecoDepth[dive_number]=depth_start_of_deco_zone;  
	if (units_equal_fsw) {
	    if (step_size < 10.) {
		    rounding_operation = depth_start_of_deco_zone / step_size - .5;
		    deepest_possible_stop_depth = Math.rint(rounding_operation) * step_size;
	    } else {
		    rounding_operation = depth_start_of_deco_zone /  10. - .5;
		    deepest_possible_stop_depth = Math.rint(rounding_operation) * 10.;
	    }
	}
	if (units_equal_msw) {
	    if (step_size < 3.) {
		    rounding_operation = depth_start_of_deco_zone / step_size - .5;
		    deepest_possible_stop_depth = Math.rint(rounding_operation) * step_size;
	    } else {
		    rounding_operation = depth_start_of_deco_zone /  3. - .5;
		    deepest_possible_stop_depth = Math.rint(rounding_operation) * 3.;
	    }
	}

/* =============================================================================== */
/*     TEMPORARILY ASCEND PROFILE TO THE START OF THE DECOMPRESSION ZONE, SAVE */
/*     VARIABLES AT THIS POINT, AND INITIALIZE VARIABLES FOR CRITICAL VOLUME LOOP */
/*     The iterative process of the VPM Critical Volume Algorithm will operate */
/*     only in the decompression zone since it deals with excess gas volume */
/*     released as a result of supersaturation gradients (not possible below the */
/*     decompression zone). */
/* =============================================================================== */

	gas_loadings_ascent_descen(starting_depth, depth_start_of_deco_zone, rate);
	run_time_start_of_deco_zone = run_time;
	deco_phase_volume_time = 0.;
	last_run_time = 0.;
	schedule_converged = false;
	for (i = 1; i <= 16; ++i) {
	    last_phase_volume_time[i - 1] = 0.;
	    he_pressure_start_of_deco_zone[i - 1] = 
		    helium_pressure[i - 1];
	    n2_pressure_start_of_deco_zone[i - 1] = 
		    nitrogen_pressure[i - 1];
	    max_actual_gradient[i - 1] = 0.;
	}
/* =============================================================================== */
/*     START OF CRITICAL VOLUME LOOP */
/*     This loop operates between Lines 50 and 100.  If the Critical Volume */
/*     Algorithm is toggled "off" in the program settings, there will only be */
/*     one pass through this loop.  Otherwise, there will be two or more passes */
/*     through this loop until the deco schedule is "converged" - that is when a */
/*     comparison between the phase volume time of the present iteration and the */
/*     last iteration is less than or equal to one minute.  This implies that */
/*     the volume of released gas in the most recent iteration differs from the */
/*     "critical" volume limit by an acceptably small amount.  The critical */
/*     volume limit is set by the Critical Volume Parameter Lambda in the program */
/*     settings (default setting is 7500 fsw-min with adjustability range from */
/*     from 6500 to 8300 fsw-min according to Bruce Wienke). */
/* =============================================================================== */
/* L50: */

	while(true) {          /* loop will run continuous there is an exit stateme */

/* =============================================================================== */
/*     CALCULATE CURRENT ASCENT CEILING BASED ON ALLOWABLE SUPERSATURATION */
/*     GRADIENTS AND SET FIRST DECO STOP.  CHECK TO MAKE SURE THAT SELECTED STEP */
/*     SIZE WILL NOT ROUND UP FIRST STOP TO A DEPTH THAT IS BELOW THE DECO ZONE. */
/* =============================================================================== */

	    calc_ascent_ceiling();
	    if (ascent_ceiling_depth <= 0.) {
		    deco_stop_depth = 0.;
	    } else {
		    rounding_operation2 = 
                ascent_ceiling_depth / step_size + .5;
		    deco_stop_depth = 
                Math.rint(rounding_operation2) * step_size;
	    }
	    if (deco_stop_depth > depth_start_of_deco_zone) {
            return -1;
	    }

/* =============================================================================== */
/*     PERFORM A SEPARATE "PROJECTED ASCENT" OUTSIDE OF THE MAIN PROGRAM TO MAKE */
/*     SURE THAT AN INCREASE IN GAS LOADINGS DURING ASCENT TO THE FIRST STOP WILL */
/*     NOT CAUSE A VIOLATION OF THE DECO CEILING.  IF SO, ADJUST THE FIRST STOP */
/*     DEEPER BASED ON STEP SIZE UNTIL A SAFE ASCENT CAN BE MADE. */
/*     Note: this situation is a possibility when ascending from extremely deep */
/*     dives or due to an unusual gas mix selection. */
/*     CHECK AGAIN TO MAKE SURE THAT ADJUSTED FIRST STOP WILL NOT BE BELOW THE */
/*     DECO ZONE. */
/* =============================================================================== */

	    projected_ascent(depth_start_of_deco_zone, rate, step_size);
	    if (deco_stop_depth > depth_start_of_deco_zone) {
            return -1;
	    }

/* =============================================================================== */
/*     HANDLE THE SPECIAL CASE WHEN NO DECO STOPS ARE REQUIRED - ASCENT CAN BE */
/*     MADE DIRECTLY TO THE SURFACE */
/*     Write ascent data to output file and exit the Critical Volume Loop. */
/* =============================================================================== */

	    if (deco_stop_depth == 0.) {
		    for (i = 1; i <= 16; ++i) {
		        helium_pressure[i - 1] = 
			        he_pressure_start_of_ascent[i - 1];
		        nitrogen_pressure[i - 1] = 
			        n2_pressure_start_of_ascent[i - 1];
		    }
		    run_time = run_time_start_of_ascent;
		    segment_number = 
			    segment_number_start_of_ascent;
		    starting_depth = depth_change[0];
		    ending_depth = 0.;
		    gas_loadings_ascent_descen(starting_depth, 
                                       ending_depth, rate);

		    break;          /* exit the critical volume l */
	    }

/* =============================================================================== */
/*     ASSIGN VARIABLES FOR ASCENT FROM START OF DECO ZONE TO FIRST STOP.  SAVE */
/*     FIRST STOP DEPTH FOR LATER USE WHEN COMPUTING THE FINAL ASCENT PROFILE */
/* =============================================================================== */

	    starting_depth = depth_start_of_deco_zone;
	    // JURE multilevel START
	    decoDivePoints=0;
	    if ( (profileDepth[firstDecoProfilePoint][dive_number]>=deco_stop_depth) && (profileDepth[firstDecoProfilePoint][dive_number]>0))
	    {
	    	  deco_stop_depth=profileDepth[firstDecoProfilePoint][dive_number];
	    	  min_deco_stop_time=profileTime[firstDecoProfilePoint][dive_number];
	    	  mix_number=profileMix[firstDecoProfilePoint][dive_number]+1;
	    	  rate=profileDecAccSpeed[firstDecoProfilePoint][dive_number];
	    	  decoDivePoints++;
	    	  checkForDecoMix=false;
	    }
	    // JURE multilevel END 
	    first_stop_depth = deco_stop_depth; 

/* =============================================================================== */
/*     DECO STOP LOOP BLOCK WITHIN CRITICAL VOLUME LOOP */
/*     This loop computes a decompression schedule to the surface during each */
/*     iteration of the critical volume loop.  No output is written from this */
/*     loop, rather it computes a schedule from which the in-water portion of the */
/*     total phase volume time (Deco_Phase_Volume_Time) can be extracted.  Also, */
/*     the gas loadings computed at the end of this loop are used the subroutine */
/*     which computes the out-of-water portion of the total phase volume time */
/*     (Surface_Phase_Volume_Time) for that schedule. */

/*     Note that exit is made from the loop after last ascent is made to a deco */
/*     stop depth that is less than or equal to zero.  A final deco stop less */
/*     than zero can happen when the user makes an odd step size change during */
/*     ascent - such as specifying a 5 msw step size change at the 3 msw stop! */
/* =============================================================================== */
    while(true) {          /* loop will run continuous there is an break statement */
        // JURE multilevel - the code in a loop was changed
         gas_loadings_ascent_descen(starting_depth, deco_stop_depth, rate);
         if (deco_stop_depth <= 0.) {
                 break;
         }
         if (checkForDecoMix) {
             if (number_of_changes > 1) {
                 i1 = number_of_changes;
                 for (i = 2; i <= i1; ++i) {
                                 if (depth_change[i - 1] == deco_stop_depth)
                                     min_deco_stop_time=Math.max(deco_gas_switch_time,min_deco_stop_time);
                     if (depth_change[i - 1] >= deco_stop_depth) {
                         mix_number = mix_change[i - 1];
                         rate = rate_change[i - 1];
                         step_size = step_size_change[i - 1];
                     }
                 }
             }
         } //if (checkForDecoMix) 
         checkForDecoMix=true; 

         next_deco_stop_depth = deco_stop_depth - step_size;
         next_deco_stop_depth=roundDecoStop(next_deco_stop_depth, step_size); 

         if ( (profileDepth[firstDecoProfilePoint+decoDivePoints][dive_number]>=(next_deco_stop_depth-step_size/2)) && (profileDepth[firstDecoProfilePoint+decoDivePoints][dive_number]>0))
         {
              if (profileDepth[firstDecoProfilePoint+decoDivePoints][dive_number]>profileDepth[firstDecoProfilePoint+decoDivePoints-1][dive_number])
                  return ( -((firstDecoProfilePoint+decoDivePoints)+100*dive_number) );  // no recompression after start of deco
              next_deco_stop_depth=profileDepth[firstDecoProfilePoint+decoDivePoints][dive_number];
                          
              boyles_law_compensation(first_stop_depth, deco_stop_depth, deco_stop_depth-next_deco_stop_depth);      
              decompression_stop(deco_stop_depth, deco_stop_depth-next_deco_stop_depth, 0, min_deco_stop_time);
               
              min_deco_stop_time=profileTime[firstDecoProfilePoint+decoDivePoints][dive_number];             
              mix_number=profileMix[firstDecoProfilePoint+decoDivePoints][dive_number]+1;
              rate=profileDecAccSpeed[firstDecoProfilePoint+decoDivePoints][dive_number];
              decoDivePoints++;
              checkForDecoMix=false;    
          }
          else
          {          
              boyles_law_compensation(first_stop_depth, deco_stop_depth, deco_stop_depth-next_deco_stop_depth);      
              decompression_stop(deco_stop_depth, deco_stop_depth-next_deco_stop_depth, 0, min_deco_stop_time);
              min_deco_stop_time=0;
          }
          
          if (next_deco_stop_depth<0) next_deco_stop_depth=0;

          starting_depth = deco_stop_depth;
          last_run_time = run_time; 
          deco_stop_depth=next_deco_stop_depth;                              
    }

/* =============================================================================== */
/*     COMPUTE TOTAL PHASE VOLUME TIME AND MAKE CRITICAL VOLUME COMPARISON */
/*     The deco phase volume time is computed from the run time.  The surface */
/*     phase volume time is computed in a subroutine based on the surfacing gas */
/*     loadings from previous deco loop block.  Next the total phase volume time */
/*     (in-water + surface) for each compartment is compared against the previous */
/*     total phase volume time.  The schedule is converged when the difference is */
/*     less than or equal to 1 minute in any one of the 16 compartments. */

/*     Note:  the "phase volume time" is somewhat of a mathematical concept. */
/*     It is the time divided out of a total integration of supersaturation */
/*     gradient x time (in-water and surface).  This integration is multiplied */
/*     by the excess bubble number to represent the amount of free-gas released */
/*     as a result of allowing a certain number of excess bubbles to form. */
/* =============================================================================== */
/* end of deco stop loop */

	    deco_phase_volume_time = run_time - run_time_start_of_deco_zone;
	    calc_surface_phase_volume_time();

	    for (i = 1; i <= 16; ++i) {
		    phase_volume_time[i - 1] = 
                deco_phase_volume_time + surface_phase_volume_time[i - 1];
		    critical_volume_comparison = 
                Math.abs(r1 = phase_volume_time[i - 1] - last_phase_volume_time[i - 1]);
			
		    if (critical_volume_comparison <= 1.) {
		        schedule_converged = true;
		    }
	    }

/* =============================================================================== */
/*     CRITICAL VOLUME DECISION TREE BETWEEN LINES 70 AND 99 */
/*     There are two options here.  If the Critical Volume Agorithm setting is */
/*     "on" and the schedule is converged, or the Critical Volume Algorithm */
/*     setting was "off" in the first place, the program will re-assign variables */
/*     to their values at the start of ascent (end of bottom time) and process */
/*     a complete decompression schedule once again using all the same ascent */
/*     parameters and first stop depth.  This decompression schedule will match */
/*     the last iteration of the Critical Volume Loop and the program will write */
/*     the final deco schedule to the output file. */

/*     Note: if the Critical Volume Agorithm setting was "off", the final deco */
/*     schedule will be based on "Initial Allowable Supersaturation Gradients." */
/*     If it was "on", the final schedule will be based on "Adjusted Allowable */
/*     Supersaturation Gradients" (gradients that are "relaxed" as a result of */
/*     the Critical Volume Algorithm). */

/*     If the Critical Volume Agorithm setting is "on" and the schedule is not */
/*     converged, the program will re-assign variables to their values at the */
/*     start of the deco zone and process another trial decompression schedule. */
/* =============================================================================== */

	    if (schedule_converged || critical_volume_algorithm_off) {
		    for (i = 1; i <= 16; ++i) {
		        helium_pressure[i - 1] = 
			        he_pressure_start_of_ascent[i - 1];
		        nitrogen_pressure[i - 1] = 
			        n2_pressure_start_of_ascent[i - 1];
		    }
		    run_time = run_time_start_of_ascent;
		    segment_number = 
			    segment_number_start_of_ascent;
		    starting_depth = depth_change[0];
		    mix_number = mix_change[0];
		    rate = rate_change[0];
		    step_size = step_size_change[0];
		    deco_stop_depth = first_stop_depth;
		    last_run_time = 0.;		    
	      // JURE multilevel START
	      decoDivePoints=0;
	      if (profileDepth[firstDecoProfilePoint][dive_number]==deco_stop_depth) 
	      {
	      	  min_deco_stop_time=profileTime[firstDecoProfilePoint][dive_number];
	      	  mix_number=profileMix[firstDecoProfilePoint][dive_number]+1;
	      	  rate=profileDecAccSpeed[firstDecoProfilePoint][dive_number];
	      	  decoDivePoints++;
	      	  checkForDecoMix=false;
	      }
	      // JURE multilevel END 

/* =============================================================================== */
/*     DECO STOP LOOP BLOCK FOR FINAL DECOMPRESSION SCHEDULE */
/* =============================================================================== */

    while(true) {          /* loop will run continuous there is an break statement */
        // JURE multilevel - the code in a loop was changed
         calc_max_actual_gradient(deco_stop_depth);
         gas_loadings_ascent_descen(starting_depth, deco_stop_depth, rate);
        /* =============================================================================== */
        /*     DURING FINAL DECOMPRESSION SCHEDULE PROCESS, COMPUTE MAXIMUM ACTUAL */
        /*     SUPERSATURATION GRADIENT RESULTING IN EACH COMPARTMENT */
        /*     If there is a repetitive dive, this will be used later in the VPM */
        /*     Repetitive Algorithm to adjust the values for critical radii. */
        /* =============================================================================== */         
         if (deco_stop_depth <= 0.) {
                 break;
         }
         if (checkForDecoMix) {
             if (number_of_changes > 1) {
                 i1 = number_of_changes;
                 for (i = 2; i <= i1; ++i) {
                                 if (depth_change[i - 1] == deco_stop_depth)
                                     min_deco_stop_time=Math.max(deco_gas_switch_time,min_deco_stop_time);
                     if (depth_change[i - 1] >= deco_stop_depth) {
                         mix_number = mix_change[i - 1];
                         rate = rate_change[i - 1];
                         step_size = step_size_change[i - 1];
                     }
                 }
             }
         } //if (checkForDecoMix) 
         checkForDecoMix=true; 

         next_deco_stop_depth = deco_stop_depth - step_size;
         next_deco_stop_depth=roundDecoStop(next_deco_stop_depth, step_size); 

         if ( (profileDepth[firstDecoProfilePoint+decoDivePoints][dive_number]>=(next_deco_stop_depth-step_size/2)) && (profileDepth[firstDecoProfilePoint+decoDivePoints][dive_number]>0))
         {
              if (profileDepth[firstDecoProfilePoint+decoDivePoints][dive_number]>profileDepth[firstDecoProfilePoint+decoDivePoints-1][dive_number])
                  return ( -((firstDecoProfilePoint+decoDivePoints)+100*dive_number) );  // no recompression after start of deco
              next_deco_stop_depth=profileDepth[firstDecoProfilePoint+decoDivePoints][dive_number];
                           
              boyles_law_compensation(first_stop_depth, deco_stop_depth, deco_stop_depth-next_deco_stop_depth);      
              decompression_stop(deco_stop_depth, deco_stop_depth-next_deco_stop_depth, 0, min_deco_stop_time);
              
              min_deco_stop_time=profileTime[firstDecoProfilePoint+decoDivePoints][dive_number];              
              /* =============================================================================== */
              /*     This next bit justs rounds up the stop time at the first stop to be in */
              /*     whole increments of the minimum stop time (to make for a nice deco table). */
              /* =============================================================================== */
		          if (last_run_time == 0.) {
			          r1 = segment_time / minimum_deco_stop_time + .5;
			          stop_time = Math.rint(r1) * minimum_deco_stop_time;
		          } else {
			          stop_time = run_time - last_run_time;
		          }
              outputProfileMixO2[outputProfileCounter][dive_number]=fraction_oxygen[mix_number-1];
              outputProfileMixHe[outputProfileCounter][dive_number]=fraction_helium[mix_number-1];
              outputProfileGas[outputProfileCounter][dive_number]=mix_number-1;
              		                                     
              mix_number=profileMix[firstDecoProfilePoint+decoDivePoints][dive_number]+1;
              rate=profileDecAccSpeed[firstDecoProfilePoint+decoDivePoints][dive_number];
              decoDivePoints++;
              checkForDecoMix=false;    
          }
          else
          {
          
              boyles_law_compensation(first_stop_depth, deco_stop_depth, deco_stop_depth-next_deco_stop_depth);      
              decompression_stop(deco_stop_depth, deco_stop_depth-next_deco_stop_depth, 0, min_deco_stop_time);
              min_deco_stop_time=0;              
              /* =============================================================================== */
              /*     This next bit justs rounds up the stop time at the first stop to be in */
              /*     whole increments of the minimum stop time (to make for a nice deco table). */
              /* =============================================================================== */
		          if (last_run_time == 0.) {
			          r1 = segment_time / minimum_deco_stop_time + .5;
			          stop_time = Math.rint(r1) * minimum_deco_stop_time;
		          } else {
			          stop_time = run_time - last_run_time;
		          }
              outputProfileMixO2[outputProfileCounter][dive_number]=fraction_oxygen[mix_number-1];
              outputProfileMixHe[outputProfileCounter][dive_number]=fraction_helium[mix_number-1];
              outputProfileGas[outputProfileCounter][dive_number]=mix_number-1;		                        
          }

          outputProfileDepth[outputProfileCounter][dive_number]=deco_stop_depth;
          outputProfileTime[outputProfileCounter][dive_number]=run_time;
          outputProfileSegmentTime[outputProfileCounter][dive_number]=stop_time;      
          outputProfileCounter++;
          outputProfileDepth[outputProfileCounter][dive_number]=-1;
                       
          starting_depth = deco_stop_depth;
          last_run_time = run_time; 
          deco_stop_depth=next_deco_stop_depth;                              
    }
/* for final deco sche */
/* end of deco stop lo */
		break;
/* final deco schedule */
/* exit critical volume l */

/* =============================================================================== */
/*     IF SCHEDULE NOT CONVERGED, COMPUTE RELAXED ALLOWABLE SUPERSATURATION */
/*     GRADIENTS WITH VPM CRITICAL VOLUME ALGORITHM AND PROCESS ANOTHER */
/*     ITERATION OF THE CRITICAL VOLUME LOOP */
/* =============================================================================== */

	    } else {
		    critical_volume(deco_phase_volume_time);

		    deco_phase_volume_time = 0.;
		    run_time = run_time_start_of_deco_zone;
		    starting_depth = depth_start_of_deco_zone;
		    mix_number = mix_change[0];
		    rate = rate_change[0];
		    step_size = step_size_change[0];
		    for (i = 1; i <= 16; ++i) {
		        last_phase_volume_time[i - 1] = phase_volume_time[i - 1];
		        helium_pressure[i - 1] = 
			        he_pressure_start_of_deco_zone[i - 1];
		        nitrogen_pressure[i - 1] = 
			        n2_pressure_start_of_deco_zone[i - 1];
		    }
		    continue;
	    }
    }                                       /* end of critical vol loop */

/* =============================================================================== */
/*     PROCESSING OF DIVE COMPLETE.  READ INPUT FILE TO DETERMINE IF THERE IS A */
/*     REPETITIVE DIVE.  IF NONE, THEN EXIT REPETITIVE LOOP. */
/* =============================================================================== */
  if ((dive_number++)>=dive_no)
      break;

/* =============================================================================== */
/*     IF THERE IS A REPETITIVE DIVE, COMPUTE GAS LOADINGS (OFF-GASSING) DURING */
/*     SURFACE INTERVAL TIME.  ADJUST CRITICAL RADII USING VPM REPETITIVE */
/*     ALGORITHM.  RE-INITIALIZE SELECTED VARIABLES AND RETURN TO START OF */
/*     REPETITIVE LOOP AT LINE 30. */
/* =============================================================================== */

      surface_interval_time=surfaceIntervals[dive_number];

	    gas_loadings_surface_interval(surface_interval_time);

	    vpm_repetitive_algorithm(surface_interval_time);

	    for (i = 1; i <= 16; ++i) {
		    max_crushing_pressure_he[i - 1] = 0.;
		    max_crushing_pressure_n2[i - 1] = 0.;
		    max_actual_gradient[i - 1] = 0.;
	    }
	    run_time = 0.;
	    segment_number = 0;
    }

/* =============================================================================== */
/*     FINAL WRITES TO OUTPUT AND CLOSE PROGRAM FILES */
/* =============================================================================== */
/* End of repetit */

    decoProfileCalculated=true;
    return 0;
} /* MAIN__ */

/* =============================================================================== */
/*     NOTE ABOUT PRESSURE UNITS USED IN CALCULATIONS: */
/*     It is the convention in decompression calculations to compute all gas */
/*     loadings, absolute pressures, partial pressures, etc., in the units of */
/*     depth pressure that you are diving - either feet of seawater (fsw) or */
/*     meters of seawater (msw).  This program follows that convention with the */
/*     the exception that all VPM calculations are performed in SI units (by */
/*     necessity).  Accordingly, there are several conversions back and forth */
/*     between the diving pressure units and the SI units. */
/* =============================================================================== */
/* =============================================================================== */
/*     FUNCTION SUBPROGRAM FOR GAS LOADING CALCULATIONS - ASCENT AND DESCENT */
/* =============================================================================== */

private double schreiner_equation__(double initial_inspired_gas_pressure, 
                                double rate_change_insp_gas_pressure, 
	                            double interval_time, 
                                double gas_time_constant, 
                                double initial_gas_pressure)
{
    /* System generated locals */
    double ret_val;

/* =============================================================================== */
/*     Note: The Schreiner equation is applied when calculating the uptake or */
/*     elimination of compartment gases during linear ascents or descents at a */
/*     constant rate.  For ascents, a negative number for rate must be used. */
/* =============================================================================== */

    ret_val = 
        initial_inspired_gas_pressure + 
	    rate_change_insp_gas_pressure * 
            (interval_time - 1. / gas_time_constant) - 
	    (initial_inspired_gas_pressure - 
	        initial_gas_pressure - 
            rate_change_insp_gas_pressure / gas_time_constant) * 
	     Math.exp(-(gas_time_constant) * interval_time);
    return ret_val;
} /* schreiner_equation__ */

/* =============================================================================== */
/*     FUNCTION SUBPROGRAM FOR GAS LOADING CALCULATIONS - CONSTANT DEPTH */
/* =============================================================================== */

private double haldane_equation__(double initial_gas_pressure, 
                              double inspired_gas_pressure, 
                              double gas_time_constant, 
	                          double interval_time)
{
    /* System generated locals */
    double ret_val;

/* =============================================================================== */
/*     Note: The Haldane equation is applied when calculating the uptake or */
/*     elimination of compartment gases during intervals at constant depth (the */
/*     outside ambient pressure does not change). */
/* =============================================================================== */

    ret_val = 
        initial_gas_pressure + 
        (inspired_gas_pressure - initial_gas_pressure) * 
            (1. - Math.exp(-(gas_time_constant) * interval_time));
    return ret_val;
} /* haldane_equation__ */

/* =============================================================================== */
/*     SUBROUTINE GAS_LOADINGS_ASCENT_DESCENT */
/*     Purpose: This subprogram applies the Schreiner equation to update the */
/*     gas loadings (partial pressures of helium and nitrogen) in the half-time */
/*     compartments due to a linear ascent or descent segment at a constant rate. */
/* =============================================================================== */

private int gas_loadings_ascent_descen(double starting_depth, 
                               double ending_depth, 
                               double rate)
{
    int last_segment_number, i;
    double initial_inspired_n2_pressure, 
	       initial_inspired_he_pressure, nitrogen_rate, 
           last_run_time, 
	       starting_ambient_pressure;

    double helium_rate;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/* =============================================================================== */

    segment_time = (ending_depth - starting_depth) / rate;
    last_run_time = run_time;
    run_time = last_run_time + segment_time;
    last_segment_number = segment_number;
    segment_number = last_segment_number + 1;
    ending_ambient_pressure = ending_depth + barometric_pressure;
    starting_ambient_pressure = starting_depth +barometric_pressure;
    initial_inspired_he_pressure = 
        (starting_ambient_pressure - 
	        water_vapor_pressure) * fraction_helium[mix_number - 1];
    initial_inspired_n2_pressure = 
        (starting_ambient_pressure - 
	         water_vapor_pressure) * fraction_nitrogen[mix_number - 1];
    helium_rate = rate * fraction_helium[mix_number - 1];
    nitrogen_rate = rate * fraction_nitrogen[mix_number - 1];
    for (i = 1; i <= 16; ++i) {
	    initial_helium_pressure[i - 1] = helium_pressure[i - 1];
	    initial_nitrogen_pressure[i - 1] = nitrogen_pressure[i - 1];
	    helium_pressure[i - 1] = 
            schreiner_equation__(initial_inspired_he_pressure, 
                                 helium_rate, 
                                 segment_time, 
                                 helium_time_constant[i - 1], 
                                 initial_helium_pressure[i - 1]);
	    nitrogen_pressure[i - 1] = 
            schreiner_equation__(initial_inspired_n2_pressure, 
                                 nitrogen_rate, 
                                 segment_time, 
                                 nitrogen_time_constant[i - 1], 
                                 initial_nitrogen_pressure[i - 1]);
    }
    return 0;
} /* gas_loadings_ascent_descen */

/* =============================================================================== */
/*     SUBROUTINE CALC_CRUSHING_PRESSURE */
/*     Purpose: Compute the effective "crushing pressure" in each compartment as */
/*     a result of descent segment(s).  The crushing pressure is the gradient */
/*     (difference in pressure) between the outside ambient pressure and the */
/*     gas tension inside a VPM nucleus (bubble seed).  This gradient acts to */
/*     reduce (shrink) the radius smaller than its initial value at the surface. */
/*     This phenomenon has important ramifications because the smaller the radius */
/*     of a VPM nucleus, the greater the allowable supersaturation gradient upon */
/*     ascent.  Gas loading (uptake) during descent, especially in the fast */
/*     compartments, will reduce the magnitude of the crushing pressure.  The */
/*     crushing pressure is not cumulative over a multi-level descent.  It will */
/*     be the maximum value obtained in any one discrete segment of the overall */
/*     descent.  Thus, the program must compute and store the maximum crushing */
/*     pressure for each compartment that was obtained across all segments of */
/*     the descent profile. */

/*     The calculation of crushing pressure will be different depending on */
/*     whether or not the gradient is in the VPM permeable range (gas can diffuse */
/*     across skin of VPM nucleus) or the VPM impermeable range (molecules in */
/*     skin of nucleus are squeezed together so tight that gas can no longer */
/*     diffuse in or out of nucleus; the gas becomes trapped and further resists */
/*     the crushing pressure).  The solution for crushing pressure in the VPM */
/*     permeable range is a simple linear equation.  In the VPM impermeable */
/*     range, a cubic equation must be solved using a numerical method. */

/*     Separate crushing pressures are tracked for helium and nitrogen because */
/*     they can have different critical radii.  The crushing pressures will be */
/*     the same for helium and nitrogen in the permeable range of the model, but */
/*     they will start to diverge in the impermeable range.  This is due to */
/*     the differences between starting radius, radius at the onset of */
/*     impermeability, and radial compression in the impermeable range. */
/* =============================================================================== */

private int calc_crushing_pressure(double starting_depth, 
                           double ending_depth, 
                           double rate)
{
    /* System generated locals */
    double r1, r2;

    double low_bound_n2, 
                ending_radius_n2, 
                ending_ambient_pressure,
	            gradient_onset_of_imperm_pa;
    double low_bound_he, 
                ending_radius_he, 
                high_bound_n2, 
	            crushing_pressure_n2=0;
    int i;
    double crushing_pressure_pascals_n2, 
                gradient_onset_of_imperm, 
	            starting_gas_tension, 
                high_bound_he, 
                crushing_pressure_he=0, 
	            amb_press_onset_of_imperm_pa, 
                crushing_pressure_pascals_he, 
	            radius_onset_of_imperm_n2, 
                starting_gradient, 
	            radius_onset_of_imperm_he, 
                starting_ambient_pressure, 
	            ending_gas_tension;

    double ending_ambient_pressure_pa, 
                a_n2, 
                b_n2, 
                c_n2, 
	            ending_gradient, 
                gas_tension_onset_of_imperm_pa, 
                a_he, 
	            b_he, c_he;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/*     First, convert the Gradient for Onset of Impermeability from units of */
/*     atmospheres to diving pressure units (either fsw or msw) and to Pascals */
/*     (SI units).  The reason that the Gradient for Onset of Impermeability is */
/*     given in the program settings in units of atmospheres is because that is */
/*     how it was reported in the original research papers by Yount and */
/*     colleauges. */
/* =============================================================================== */

    gradient_onset_of_imperm = gradient_onset_of_imperm_atm * units_factor;
    gradient_onset_of_imperm_pa = gradient_onset_of_imperm_atm * 101325.;

/* =============================================================================== */
/*     Assign values of starting and ending ambient pressures for descent segment */
/* =============================================================================== */

    starting_ambient_pressure = starting_depth + barometric_pressure;
    ending_ambient_pressure = ending_depth + barometric_pressure;

/* =============================================================================== */
/*     MAIN LOOP WITH NESTED DECISION TREE */
/*     For each compartment, the program computes the starting and ending */
/*     gas tensions and gradients.  The VPM is different than some dissolved gas */
/*     algorithms, Buhlmann for example, in that it considers the pressure due to */
/*     oxygen, carbon dioxide, and water vapor in each compartment in addition to */
/*     the inert gases helium and nitrogen.  These "other gases" are included in */
/*     the calculation of gas tensions and gradients. */
/* =============================================================================== */

    for (i = 1; i <= 16; ++i) {
	    starting_gas_tension = initial_helium_pressure[i - 
		    1] + initial_nitrogen_pressure[i - 1] + 
		    constant_pressure_other_gases;
	    starting_gradient = starting_ambient_pressure - 
		    starting_gas_tension;
	    ending_gas_tension = helium_pressure[i - 1] + 
		    nitrogen_pressure[i - 1] + 
		    constant_pressure_other_gases;
	    ending_gradient = ending_ambient_pressure - ending_gas_tension;

/* =============================================================================== */
/*     Compute radius at onset of impermeability for helium and nitrogen */
/*     critical radii */
/* =============================================================================== */

	    radius_onset_of_imperm_he = 1. / (
		    gradient_onset_of_imperm_pa / 
		    ((skin_compression_gammac - 
		    surface_tension_gamma) * 2.) +  
		        1. / adjusted_critical_radius_he[i - 1]);
	    radius_onset_of_imperm_n2 = 1. / (
		    gradient_onset_of_imperm_pa / 
		    ((skin_compression_gammac - 
		        surface_tension_gamma) * 2.) +  
		        1. / adjusted_critical_radius_n2[i - 1]);

/* =============================================================================== */
/*     FIRST BRANCH OF DECISION TREE - PERMEABLE RANGE */
/*     Crushing pressures will be the same for helium and nitrogen */
/* =============================================================================== */

	    if (ending_gradient <= gradient_onset_of_imperm) {
	        crushing_pressure_he = ending_ambient_pressure - ending_gas_tension;
	        crushing_pressure_n2 = ending_ambient_pressure - ending_gas_tension;
	    }

/* =============================================================================== */
/*     SECOND BRANCH OF DECISION TREE - IMPERMEABLE RANGE */
/*     Both the ambient pressure and the gas tension at the onset of */
/*     impermeability must be computed in order to properly solve for the ending */
/*     radius and resultant crushing pressure.  The first decision block */
/*     addresses the special case when the starting gradient just happens to be */
/*     equal to the gradient for onset of impermeability (not very likely!). */
/* =============================================================================== */

	if (ending_gradient > gradient_onset_of_imperm) {
	    if (starting_gradient == gradient_onset_of_imperm) {
		    amb_pressure_onset_of_imperm[i - 1] = 
			    starting_ambient_pressure;
		    gas_tension_onset_of_imperm[i - 1] = 
			    starting_gas_tension;
	    }

/* =============================================================================== */
/*     In most cases, a subroutine will be called to find these values using a */
/*     numerical method. */
/* =============================================================================== */

	    if (starting_gradient < gradient_onset_of_imperm) {
		    onset_of_impermeability(starting_ambient_pressure, 
                                      ending_ambient_pressure, rate, i);
	    }

/* =============================================================================== */
/*     Next, using the values for ambient pressure and gas tension at the onset */
/*     of impermeability, the equations are set up to process the calculations */
/*     through the radius root finder subroutine.  This subprogram will find the */
/*     root (solution) to the cubic equation using a numerical method.  In order */
/*     to do this efficiently, the equations are placed in the form */
/*     Ar^3 - Br^2 - C = 0, where r is the ending radius after impermeable */
/*     compression.  The coefficients A, B, and C for helium and nitrogen are */
/*     computed and passed to the subroutine as arguments.  The high and low */
/*     bounds to be used by the numerical method of the subroutine are also */
/*     computed (see separate page posted on Deco List ftp site entitled */
/*     "VPM: Solving for radius in the impermeable regime").  The subprogram */
/*     will return the value of the ending radius and then the crushing */
/*     pressures for helium and nitrogen can be calculated. */
/* =============================================================================== */

	    ending_ambient_pressure_pa = ending_ambient_pressure / 
		    units_factor * 101325.;
	    amb_press_onset_of_imperm_pa = 
		    amb_pressure_onset_of_imperm[i - 1] / units_factor * 101325.;
	    gas_tension_onset_of_imperm_pa = 
		    gas_tension_onset_of_imperm[i - 1] / units_factor * 101325.;
	    b_he = (skin_compression_gammac - surface_tension_gamma) * 2.;
	    a_he = ending_ambient_pressure_pa - 
		    amb_press_onset_of_imperm_pa + 
		    gas_tension_onset_of_imperm_pa + 
		    (skin_compression_gammac - 
		        surface_tension_gamma) * 
		            2. / radius_onset_of_imperm_he;
        /* Computing 3rd power */
	    r1 = radius_onset_of_imperm_he;
	    c_he = gas_tension_onset_of_imperm_pa * (r1 * (r1 * r1))
		    ;
	    high_bound_he = radius_onset_of_imperm_he;
	    low_bound_he = b_he / a_he;
	    ending_radius_he=radius_root_finder(a_he, b_he, c_he,low_bound_he, high_bound_he);
        /* Computing 3rd power */
	    r1 = radius_onset_of_imperm_he;
        /* Computing 3rd power */
	    r2 = ending_radius_he;
	    crushing_pressure_pascals_he = 
            gradient_onset_of_imperm_pa + 
		        ending_ambient_pressure_pa - 
		        amb_press_onset_of_imperm_pa + 
		        gas_tension_onset_of_imperm_pa * 
                (1. - r1 * (r1 * r1) / (r2 * (r2 * r2)));
	    crushing_pressure_he = 
            crushing_pressure_pascals_he / 101325. * units_factor;
	    b_n2 = (skin_compression_gammac - surface_tension_gamma) * 2.;
	    a_n2 = ending_ambient_pressure_pa - 
		        amb_press_onset_of_imperm_pa + 
		        gas_tension_onset_of_imperm_pa + 
		        (skin_compression_gammac - surface_tension_gamma) * 
                    2. / radius_onset_of_imperm_n2;
        /* Computing 3rd power */
	    r1 = radius_onset_of_imperm_n2;
	    c_n2 = gas_tension_onset_of_imperm_pa * (r1 * (r1 * r1))
		    ;
	    high_bound_n2 = radius_onset_of_imperm_n2;
	    low_bound_n2 = b_n2 / a_n2;
	    ending_radius_n2=radius_root_finder(a_n2, b_n2, c_n2, low_bound_n2, high_bound_n2);
        /* Computing 3rd power */
	    r1 = radius_onset_of_imperm_n2;
        /* Computing 3rd power */
	    r2 = ending_radius_n2;
	    crushing_pressure_pascals_n2 = 
            gradient_onset_of_imperm_pa + 
		    ending_ambient_pressure_pa - 
		    amb_press_onset_of_imperm_pa + 
		    gas_tension_onset_of_imperm_pa * (1. - r1 * 
		    (r1 * r1) / (r2 * (r2 * r2)));
	    crushing_pressure_n2 = crushing_pressure_pascals_n2 / 101325. * units_factor;
	}

/* =============================================================================== */
/*     UPDATE VALUES OF MAX CRUSHING PRESSURE IN GLOBAL ARRAYS */
/* =============================================================================== */

        /* Computing MAX */
	    r1 = max_crushing_pressure_he[i - 1];
	    max_crushing_pressure_he[i - 1] = Math.max(r1, crushing_pressure_he);
        /* Computing MAX */
	    r1 = max_crushing_pressure_n2[i - 1];
	    max_crushing_pressure_n2[i - 1] = Math.max(r1, crushing_pressure_n2);
    }
    return 0;
} /* calc_crushing_pressure */

/* =============================================================================== */
/*     SUBROUTINE ONSET_OF_IMPERMEABILITY */
/*     Purpose:  This subroutine uses the Bisection Method to find the ambient */
/*     pressure and gas tension at the onset of impermeability for a given */
/*     compartment.  Source:  "Numerical Recipes in Fortran 77", */
/*     Cambridge University Press, 1992. */
/* =============================================================================== */

private int onset_of_impermeability(double starting_ambient_pressure, 
                              double ending_ambient_pressure, 
                              double rate,
                              int i)
{
    boolean printError=true;
    /* Local variables */
    double time, last_diff_change, mid_range_nitrogen_pressure;
    int j;
    double gas_tension_at_mid_range=0, 
           initial_inspired_n2_pressure, 
	       gradient_onset_of_imperm, 
           starting_gas_tension, 
           low_bound, 
	       initial_inspired_he_pressure, 
           high_bound_nitrogen_pressure, 
	       nitrogen_rate, 
           function_at_mid_range, 
           function_at_low_bound,
	       high_bound, 
           mid_range_helium_pressure, 
           mid_range_time, 
	       ending_gas_tension, 
           function_at_high_bound;

    double mid_range_ambient_pressure=0, 
                high_bound_helium_pressure, 
	            helium_rate, 
                differential_change;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/*     First convert the Gradient for Onset of Impermeability to the diving */
/*     pressure units that are being used */
/* =============================================================================== */

    gradient_onset_of_imperm = gradient_onset_of_imperm_atm * units_factor;

/* =============================================================================== */
/*     ESTABLISH THE BOUNDS FOR THE ROOT SEARCH USING THE BISECTION METHOD */
/*     In this case, we are solving for time - the time when the ambient pressure */
/*     minus the gas tension will be equal to the Gradient for Onset of */
/*     Impermeabliity.  The low bound for time is set at zero and the high */
/*     bound is set at the elapsed time (segment time) it took to go from the */
/*     starting ambient pressure to the ending ambient pressure.  The desired */
/*     ambient pressure and gas tension at the onset of impermeability will */
/*     be found somewhere between these endpoints.  The algorithm checks to */
/*     make sure that the solution lies in between these bounds by first */
/*     computing the low bound and high bound function values. */
/* =============================================================================== */

    initial_inspired_he_pressure = 
        (starting_ambient_pressure - water_vapor_pressure) * fraction_helium[mix_number - 1];
    initial_inspired_n2_pressure = 
        (starting_ambient_pressure - water_vapor_pressure) * fraction_nitrogen[mix_number - 1];
    helium_rate = rate * fraction_helium[mix_number - 1];
    nitrogen_rate = rate * fraction_nitrogen[mix_number - 1];
    low_bound = 0.;
    high_bound = (ending_ambient_pressure - starting_ambient_pressure) / rate;
    starting_gas_tension = 
        initial_helium_pressure[i - 1] +
	    initial_nitrogen_pressure[i - 1] + 
	    constant_pressure_other_gases;
    function_at_low_bound = 
        starting_ambient_pressure - 
	    starting_gas_tension - 
        gradient_onset_of_imperm;
    high_bound_helium_pressure = 
        schreiner_equation__(initial_inspired_he_pressure, 
                             helium_rate, 
                             high_bound, 
                             helium_time_constant[i - 1], 
                             initial_helium_pressure[i - 1]);
    high_bound_nitrogen_pressure = 
        schreiner_equation__(initial_inspired_n2_pressure, 
                             nitrogen_rate, 
                             high_bound, 
                             nitrogen_time_constant[i - 1], 
                             initial_nitrogen_pressure[i - 1]);
    ending_gas_tension = 
        high_bound_helium_pressure + 
	    high_bound_nitrogen_pressure + 
	    constant_pressure_other_gases;
    function_at_high_bound = 
        ending_ambient_pressure - 
	    ending_gas_tension - 
        gradient_onset_of_imperm;
    if (function_at_high_bound * function_at_low_bound >= 0.) {
        pause();
    }

/* =============================================================================== */
/*     APPLY THE BISECTION METHOD IN SEVERAL ITERATIONS UNTIL A SOLUTION WITH */
/*     THE DESIRED ACCURACY IS FOUND */
/*     Note: the program allows for up to 100 iterations.  Normally an exit will */
/*     be made from the loop well before that number.  If, for some reason, the */
/*     program exceeds 100 iterations, there will be a pause to alert the user. */
/* =============================================================================== */

    if (function_at_low_bound < 0.) {
	    time = low_bound;
	    differential_change = high_bound - low_bound;
    } else {
	    time = high_bound;
	    differential_change = low_bound - high_bound;
    }
    for (j = 1; j <= 100; ++j) {
	    last_diff_change = differential_change;
	    differential_change = last_diff_change * .5;
	    mid_range_time = time + differential_change;
	    mid_range_ambient_pressure = starting_ambient_pressure + rate * mid_range_time;
	    mid_range_helium_pressure = 
            schreiner_equation__(initial_inspired_he_pressure, 
                                 helium_rate, 
                                 mid_range_time, 
                                 helium_time_constant[i - 1], 
                                 initial_helium_pressure[i - 1]);
	    mid_range_nitrogen_pressure = 
            schreiner_equation__(initial_inspired_n2_pressure, 
                                 nitrogen_rate, 
                                 mid_range_time, 
                                 nitrogen_time_constant[i - 1], 
                                 initial_nitrogen_pressure[i - 1]);
	    gas_tension_at_mid_range = 
            mid_range_helium_pressure + 
		    mid_range_nitrogen_pressure + 
		    constant_pressure_other_gases;
	    function_at_mid_range = 
            mid_range_ambient_pressure - 
		    gas_tension_at_mid_range - 
            gradient_onset_of_imperm;
	    if (function_at_mid_range <= 0.) {
	        time = mid_range_time;
	    }
	    if (Math.abs(differential_change) < .001 || 
		    function_at_mid_range == 0.) {
                printError=false;
	        break;
	    }
    }
    if (printError)
    { 
        pause();
    }

/* =============================================================================== */
/*     When a solution with the desired accuracy is found, the program jumps out */
/*     of the loop to Line 100 and assigns the solution values for ambient */
/*     pressure and gas tension at the onset of impermeability. */
/* =============================================================================== */

    amb_pressure_onset_of_imperm[i - 1] = mid_range_ambient_pressure;
    gas_tension_onset_of_imperm[i - 1] = gas_tension_at_mid_range;
    return 0;
} /* onset_of_impermeability */

/* =============================================================================== */
/*     SUBROUTINE RADIUS_ROOT_FINDER */
/*     Purpose: This subroutine is a "fail-safe" routine that combines the */
/*     Bisection Method and the Newton-Raphson Method to find the desired root. */
/*     This hybrid algorithm takes a bisection step whenever Newton-Raphson would */
/*     take the solution out of bounds, or whenever Newton-Raphson is not */
/*     converging fast enough.  Source:  "Numerical Recipes in Fortran 77", */
/*     Cambridge University Press, 1992. */
/* =============================================================================== */

private double radius_root_finder (double a, 
                        double b, 
                        double c, 
                        double low_bound, 
                        double high_bound)
{
    /* System generated locals */
    double r1, r2;
    double ending_radius;

    /* Local variables */
    double radius_at_low_bound, 
                last_diff_change, 
                function, 
	            radius_at_high_bound;
    int i;
    double function_at_low_bound, 
                last_ending_radius, 
	            function_at_high_bound, 
                derivative_of_function, 
	            differential_change;

/* loop */
/* =============================================================================== */
/*     BEGIN CALCULATIONS BY MAKING SURE THAT THE ROOT LIES WITHIN BOUNDS */
/*     In this case we are solving for radius in a cubic equation of the form, */
/*     Ar^3 - Br^2 - C = 0.  The coefficients A, B, and C were passed to this */
/*     subroutine as arguments. */
/* =============================================================================== */

    function_at_low_bound = 
        low_bound * (low_bound * (a * low_bound - b)) - c;
    function_at_high_bound = 
        high_bound * (high_bound * (a * high_bound - b)) - c;
    if (function_at_low_bound > 0. && function_at_high_bound > 0.) { 
        pause();
    }

/* =============================================================================== */
/*     Next the algorithm checks for special conditions and then prepares for */
/*     the first bisection. */
/* =============================================================================== */

    if (function_at_low_bound < 0. && function_at_high_bound < 0.) { 
        pause();
    }
    if (function_at_low_bound == 0.) {
	    ending_radius = low_bound;
	    return(ending_radius);
    } else if (function_at_high_bound == 0.) {
	    ending_radius = high_bound;
	    return(ending_radius);
    } else if (function_at_low_bound < 0.) {
	    radius_at_low_bound = low_bound;
	    radius_at_high_bound = high_bound;
    } else {
	    radius_at_high_bound = low_bound;
	    radius_at_low_bound = high_bound;
    }
    ending_radius = (low_bound + high_bound) * .5;
    last_diff_change = Math.abs(r1 = high_bound - low_bound);
    differential_change = last_diff_change;

/* =============================================================================== */
/*     At this point, the Newton-Raphson Method is applied which uses a function */
/*     and its first derivative to rapidly converge upon a solution. */
/*     Note: the program allows for up to 100 iterations.  Normally an exit will */
/*     be made from the loop well before that number.  If, for some reason, the */
/*     program exceeds 100 iterations, there will be a pause to alert the user. */
/*     When a solution with the desired accuracy is found, exit is made from the */
/*     loop by returning to the calling program.  The last value of ending */
/*     radius has been assigned as the solution. */
/* =============================================================================== */

    function = 
        ending_radius * (ending_radius * (a * ending_radius - b)) - c;
    derivative_of_function = 
        ending_radius * (ending_radius *  3. * a - b * 2.);
    for (i = 1; i <= 100; ++i) {
	    if (((ending_radius - radius_at_high_bound) * derivative_of_function - function) * 
		        ((ending_radius - radius_at_low_bound) * derivative_of_function - function) >= 0. 
          || ( Math.abs(r1 = function * 2.)) > 
                ( Math.abs(r2 = last_diff_change * derivative_of_function))) {
	        last_diff_change = differential_change;
	        differential_change = 
                (radius_at_high_bound - radius_at_low_bound) * .5;
	        ending_radius = radius_at_low_bound + differential_change;
	        if (radius_at_low_bound == ending_radius) {
		    return(ending_radius);
	        }
	    } else {
	        last_diff_change = differential_change;
	        differential_change = function / derivative_of_function;
	        last_ending_radius = ending_radius;
	        ending_radius -= differential_change;
	        if (last_ending_radius == ending_radius) {
		        return(ending_radius);
	        }
	    }
	    if (Math.abs(differential_change) < 1e-12) {
	        return(ending_radius);
	    }
	    function = 
            ending_radius * (ending_radius * (a * ending_radius - b)) - c;
	    derivative_of_function = 
            ending_radius * (ending_radius * 3. * a - b * 2.);
	    if (function < 0.) {
	        radius_at_low_bound = ending_radius;
	    } else {
	        radius_at_high_bound = ending_radius;
	    }
    } 
    pause();
    return 0;
} /* radius_root_finder */

/* =============================================================================== */
/*     SUBROUTINE GAS_LOADINGS_CONSTANT_DEPTH */
/*     Purpose: This subprogram applies the Haldane equation to update the */
/*     gas loadings (partial pressures of helium and nitrogen) in the half-time */
/*     compartments for a segment at constant depth. */
/* =============================================================================== */

private void gas_loadings_constant_depth(double depth, 
                                double run_time_end_of_segment)
{
    double inspired_nitrogen_pressure;
    int last_segment_number;
    double initial_helium_pressure;

    int i;
    double ambient_pressure, 
                inspired_helium_pressure, 
	            last_run_time, 
                initial_nitrogen_pressure;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/* =============================================================================== */

    segment_time = run_time_end_of_segment - run_time;
    last_run_time = run_time_end_of_segment;
    run_time = last_run_time;
    last_segment_number = segment_number;
    segment_number = last_segment_number + 1;
    ambient_pressure = depth + barometric_pressure;
    inspired_helium_pressure = 
        (ambient_pressure -water_vapor_pressure) * fraction_helium[mix_number - 1];
	                            
    inspired_nitrogen_pressure = 
        (ambient_pressure - water_vapor_pressure) * fraction_nitrogen[mix_number - 1];
    ending_ambient_pressure = ambient_pressure;
    for (i = 1; i <= 16; ++i) {
	    initial_helium_pressure = helium_pressure[i - 1];
	    initial_nitrogen_pressure = nitrogen_pressure[i - 1];
	    helium_pressure[i - 1] = 
            haldane_equation__(initial_helium_pressure, 
                               inspired_helium_pressure, 
                               helium_time_constant[i - 1], 
                               segment_time);
	nitrogen_pressure[i - 1] = 
            haldane_equation__(initial_nitrogen_pressure, 
                               inspired_nitrogen_pressure, 
                               nitrogen_time_constant[i - 1], 
                               segment_time);
    }
    return;
} /* gas_loadings_constant_depth */

/* =============================================================================== */
/*     SUBROUTINE NUCLEAR_REGENERATION */
/*     Purpose: This subprogram calculates the regeneration of VPM critical */
/*     radii that takes place over the dive time.  The regeneration time constant */
/*     has a time scale of weeks so this will have very little impact on dives of */
/*     normal length, but will have a major impact for saturation dives. */
/* =============================================================================== */

private int nuclear_regeneration(double dive_time)
{
    /* Local variables */
    double crush_pressure_adjust_ratio_he, 
                ending_radius_n2, 
	            ending_radius_he;
    int i;
    double crushing_pressure_pascals_n2, 
	            crushing_pressure_pascals_he, 
                adj_crush_pressure_n2_pascals, 
	            adj_crush_pressure_he_pascals, 
                crush_pressure_adjust_ratio_n2;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/*     First convert the maximum crushing pressure obtained for each compartment */
/*     to Pascals.  Next, compute the ending radius for helium and nitrogen */
/*     critical nuclei in each compartment. */
/* =============================================================================== */

    for (i = 1; i <= 16; ++i) {
	    crushing_pressure_pascals_he = 
		        max_crushing_pressure_he[i - 1] / units_factor * 101325.;
	    crushing_pressure_pascals_n2 = 
		        max_crushing_pressure_n2[i - 1] / units_factor * 101325.;
	    ending_radius_he = 
            1. / (crushing_pressure_pascals_he / 
                  ((skin_compression_gammac - surface_tension_gamma) * 2.) +
                    1. / adjusted_critical_radius_he[i - 1]);
	    ending_radius_n2 = 
            1. / (crushing_pressure_pascals_n2 / 
                  ((skin_compression_gammac - surface_tension_gamma) * 2.) + 
                    1. / adjusted_critical_radius_n2[i - 1]);

/* =============================================================================== */
/*     A "regenerated" radius for each nucleus is now calculated based on the */
/*     regeneration time constant.  This means that after application of */
/*     crushing pressure and reduction in radius, a nucleus will slowly grow */
/*     back to its original initial radius over a period of time.  This */
/*     phenomenon is probabilistic in nature and depends on absolute temperature. */
/*     It is independent of crushing pressure. */
/* =============================================================================== */

	    regenerated_radius_he[i - 1] = 
		    adjusted_critical_radius_he[i - 1] + 
		    (ending_radius_he - adjusted_critical_radius_he[i - 1]) *
		    Math.exp(-(dive_time) / regeneration_time_constant);
	    regenerated_radius_n2[i - 1] = 
		    adjusted_critical_radius_n2[i - 1] + 
		    (ending_radius_n2 - adjusted_critical_radius_n2[i - 1]) * 
		    Math.exp(-(dive_time) / regeneration_time_constant);

/* =============================================================================== */
/*     In order to preserve reference back to the initial critical radii after */
/*     regeneration, an "adjusted crushing pressure" for the nuclei in each */
/*     compartment must be computed.  In other words, this is the value of */
/*     crushing pressure that would have reduced the original nucleus to the */
/*     to the present radius had regeneration not taken place.  The ratio */
/*     for adjusting crushing pressure is obtained from algebraic manipulation */
/*     of the standard VPM equations.  The adjusted crushing pressure, in lieu */
/*     of the original crushing pressure, is then applied in the VPM Critical */
/*     Volume Algorithm and the VPM Repetitive Algorithm. */
/* =============================================================================== */

	    crush_pressure_adjust_ratio_he = 
            ending_radius_he * (adjusted_critical_radius_he[i - 1] - 
		                        regenerated_radius_he[i - 1]) / 
		                            (regenerated_radius_he[i - 1] * 
		                            (adjusted_critical_radius_he[i - 1] - 
		                                ending_radius_he));
	    crush_pressure_adjust_ratio_n2 = 
            ending_radius_n2 * (adjusted_critical_radius_n2[i - 1] - 
		                        regenerated_radius_n2[i - 1]) / 
		                            (regenerated_radius_n2[i - 1] * 
		                            (adjusted_critical_radius_n2[i - 1] - 
		                                ending_radius_n2));
	    adj_crush_pressure_he_pascals = 
            crushing_pressure_pascals_he * crush_pressure_adjust_ratio_he;
	    adj_crush_pressure_n2_pascals = 
            crushing_pressure_pascals_n2 * crush_pressure_adjust_ratio_n2;
	    adjusted_crushing_pressure_he[i - 1] = 
		    adj_crush_pressure_he_pascals / 101325. * units_factor;
	    adjusted_crushing_pressure_n2[i - 1] = 
		    adj_crush_pressure_n2_pascals / 101325. * units_factor;
    }
    return 0;
} /* nuclear_regeneration */

/* =============================================================================== */
/*     SUBROUTINE CALC_INITIAL_ALLOWABLE_GRADIENT */
/*     Purpose: This subprogram calculates the initial allowable gradients for */
/*     helium and nitrogren in each compartment.  These are the gradients that */
/*     will be used to set the deco ceiling on the first pass through the deco */
/*     loop.  If the Critical Volume Algorithm is set to "off", then these */
/*     gradients will determine the final deco schedule.  Otherwise, if the */
/*     Critical Volume Algorithm is set to "on", these gradients will be further */
/*     "relaxed" by the Critical Volume Algorithm subroutine.  The initial */
/*     allowable gradients are referred to as "PssMin" in the papers by Yount */
/*     and colleauges, i.e., the minimum supersaturation pressure gradients */
/*     that will probe bubble formation in the VPM nuclei that started with the */
/*     designated minimum initial radius (critical radius). */

/*     The initial allowable gradients are computed directly from the */
/*     "regenerated" radii after the Nuclear Regeneration subroutine.  These */
/*     gradients are tracked separately for helium and nitrogen. */
/* =============================================================================== */

private int calc_initial_allowable_gradient()
{
    double initial_allowable_grad_n2_pa, 
	            initial_allowable_grad_he_pa;
    int i;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/*     The initial allowable gradients are computed in Pascals and then converted */
/*     to the diving pressure units.  Two different sets of arrays are used to */
/*     save the calculations - Initial Allowable Gradients and Allowable */
/*     Gradients.  The Allowable Gradients are assigned the values from Initial */
/*     Allowable Gradients however the Allowable Gradients can be changed later */
/*     by the Critical Volume subroutine.  The values for the Initial Allowable */
/*     Gradients are saved in a global array for later use by both the Critical */
/*     Volume subroutine and the VPM Repetitive Algorithm subroutine. */
/* =============================================================================== */

    for (i = 1; i <= 16; ++i) {
	    initial_allowable_grad_n2_pa = 
            surface_tension_gamma * 2. * 
                (skin_compression_gammac - surface_tension_gamma) / 
		           (regenerated_radius_n2[i - 1] * skin_compression_gammac);
	    initial_allowable_grad_he_pa = 
            surface_tension_gamma * 2. * 
                (skin_compression_gammac - surface_tension_gamma) / 
		           (regenerated_radius_he[i - 1] * skin_compression_gammac);
	    initial_allowable_gradient_n2[i - 1] = 
		    initial_allowable_grad_n2_pa / 101325. * units_factor;
	    initial_allowable_gradient_he[i - 1] = 
		    initial_allowable_grad_he_pa / 101325. * units_factor;
	    allowable_gradient_he[i - 1] = 
		    initial_allowable_gradient_he[i - 1];
	    allowable_gradient_n2[i - 1] = 
		    initial_allowable_gradient_n2[i - 1];
    }
    return 0;
} /* calc_initial_allowable_gradient */

/* =============================================================================== */
/*     SUBROUTINE CALC_ASCENT_CEILING */
/*     Purpose: This subprogram calculates the ascent ceiling (the safe ascent */
/*     depth) in each compartment, based on the allowable gradients, and then */
/*     finds the deepest ascent ceiling across all compartments. */
/* =============================================================================== */

private int calc_ascent_ceiling()
{
    /* System generated locals */
    double r1, r2;

    /* Local variables */
    double weighted_allowable_gradient;
    int i;
    double compartment_ascent_ceiling[]=new double[16], 
                gas_loading, 
	            tolerated_ambient_pressure;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/*     Since there are two sets of allowable gradients being tracked, one for */
/*     helium and one for nitrogen, a "weighted allowable gradient" must be */
/*     computed each time based on the proportions of helium and nitrogen in */
/*     each compartment.  This proportioning follows the methodology of */
/*     Buhlmann/Keller.  If there is no helium and nitrogen in the compartment, */
/*     such as after extended periods of oxygen breathing, then the minimum value */
/*     across both gases will be used.  It is important to note that if a */
/*     compartment is empty of helium and nitrogen, then the weighted allowable */
/*     gradient formula cannot be used since it will result in division by zero. */
/* =============================================================================== */

    for (i = 1; i <= 16; ++i) {
	    gas_loading = 
            helium_pressure[i - 1] + nitrogen_pressure[i - 1];
	    if (gas_loading > 0.) {
	        weighted_allowable_gradient = 
		        (allowable_gradient_he[i - 1] * helium_pressure[i - 1] + 
		            allowable_gradient_n2[i - 1] * nitrogen_pressure[i - 1]) / 
		                (helium_pressure[i - 1] + nitrogen_pressure[i - 1]);
	        tolerated_ambient_pressure = 
                gas_loading + 
		        constant_pressure_other_gases - 
		        weighted_allowable_gradient;
	    } else {
            /* Computing MIN */
	        r1 = allowable_gradient_he[i - 1];
            r2 = allowable_gradient_n2[i - 1];
	        weighted_allowable_gradient = Math.min(r1,r2);
	        tolerated_ambient_pressure = 
		        constant_pressure_other_gases - weighted_allowable_gradient;
	    }

/* =============================================================================== */
/*     The tolerated ambient pressure cannot be less than zero absolute, i.e., */
/*     the vacuum of outer space! */
/* =============================================================================== */

	    if (tolerated_ambient_pressure < 0.) {
	        tolerated_ambient_pressure = 0.;
	    }
	    compartment_ascent_ceiling[i - 1] = 
            tolerated_ambient_pressure - barometric_pressure;
    }

/* =============================================================================== */
/*     The Ascent Ceiling Depth is computed in a loop after all of the individual */
/*     compartment ascent ceilings have been calculated.  It is important that the */
/*     Ascent Ceiling Depth (max ascent ceiling across all compartments) only be */
/*     extracted from the compartment values and not be compared against some */
/*     initialization value.  For example, if MAX(Ascent_Ceiling_Depth . .) was */
/*     compared against zero, this could cause a program lockup because sometimes */
/*     the Ascent Ceiling Depth needs to be negative (but not less than zero */
/*     absolute ambient pressure) in order to decompress to the last stop at zero */
/*     depth. */
/* =============================================================================== */

    ascent_ceiling_depth = compartment_ascent_ceiling[0];
    for (i = 2; i <= 16; ++i) {
        /* Computing MAX */
	    r1 = ascent_ceiling_depth;
        r2 = compartment_ascent_ceiling[i - 1];
	    ascent_ceiling_depth = Math.max(r1,r2);
    }
    return 0;
} /* calc_ascent_ceiling */

/* =============================================================================== */
/*     SUBROUTINE CALC_MAX_ACTUAL_GRADIENT */
/*     Purpose: This subprogram calculates the actual supersaturation gradient */
/*     obtained in each compartment as a result of the ascent profile during */
/*     decompression.  Similar to the concept with crushing pressure, the */
/*     supersaturation gradients are not cumulative over a multi-level, staged */
/*     ascent.  Rather, it will be the maximum value obtained in any one discrete */
/*     step of the overall ascent.  Thus, the program must compute and store the */
/*     maximum actual gradient for each compartment that was obtained across all */
/*     steps of the ascent profile.  This subroutine is invoked on the last pass */
/*     through the deco stop loop block when the final deco schedule is being */
/*     generated. */
/**/
/*     The max actual gradients are later used by the VPM Repetitive Algorithm to */
/*     determine if adjustments to the critical radii are required.  If the max */
/*     actual gradient did not exceed the initial alllowable gradient, then no */
/*     adjustment will be made.  However, if the max actual gradient did exceed */
/*     the intitial allowable gradient, such as permitted by the Critical Volume */
/*     Algorithm, then the critical radius will be adjusted (made larger) on the */
/*     repetitive dive to compensate for the bubbling that was allowed on the */
/*     previous dive.  The use of the max actual gradients is intended to prevent */
/*     the repetitive algorithm from being overly conservative. */
/* =============================================================================== */

private int calc_max_actual_gradient(double deco_stop_depth)
{
    /* System generated locals */
    double r1;

    /* Local variables */
    int i;
    double compartment_gradient;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/*     Note: negative supersaturation gradients are meaningless for this */
/*     application, so the values must be equal to or greater than zero. */
/* =============================================================================== */

    for (i = 1; i <= 16; ++i) {
	    compartment_gradient = 
            helium_pressure[i - 1] + 
		    nitrogen_pressure[i - 1] + 
		    constant_pressure_other_gases - 
		    (deco_stop_depth + barometric_pressure);
	    if (compartment_gradient <= 0.) {
	        compartment_gradient = 0.;
	    }
        /* Computing MAX */
	    r1 = max_actual_gradient[i - 1];
	    max_actual_gradient[i - 1] = Math.max(r1, compartment_gradient);
    }
    return 0;
} /* calc_max_actual_gradient */

/* =============================================================================== */
/*     SUBROUTINE CALC_SURFACE_PHASE_VOLUME_TIME */
/*     Purpose: This subprogram computes the surface portion of the total phase */
/*     volume time.  This is the time factored out of the integration of */
/*     supersaturation gradient x time over the surface interval.  The VPM */
/*     considers the gradients that allow bubbles to form or to drive bubble */
/*     growth both in the water and on the surface after the dive. */

/*     This subroutine is a new development to the VPM algorithm in that it */
/*     computes the time course of supersaturation gradients on the surface */
/*     when both helium and nitrogen are present.  Refer to separate write-up */
/*     for a more detailed explanation of this algorithm. */
/* =============================================================================== */

private int calc_surface_phase_volume_time()
{
    /* Local variables */
    double decay_time_to_zero_gradient;
    int i;
    double integral_gradient_x_time, 
                surface_inspired_n2_pressure;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/* =============================================================================== */

    surface_inspired_n2_pressure = 
        (barometric_pressure - water_vapor_pressure) * .79;
    for (i = 1; i <= 16; ++i) {
	    if (nitrogen_pressure[i - 1] > surface_inspired_n2_pressure) {
	        surface_phase_volume_time[i - 1] = 
		        (helium_pressure[i - 1] / helium_time_constant[i - 1] + 
		            (nitrogen_pressure[i - 1] - surface_inspired_n2_pressure) / 
		                nitrogen_time_constant[i - 1]) / 
		        (helium_pressure[i - 1] + nitrogen_pressure[i - 1] - 
		            surface_inspired_n2_pressure);
	} else if (nitrogen_pressure[i - 1] <= surface_inspired_n2_pressure && 
		helium_pressure[i - 1] + nitrogen_pressure[i - 1] >= surface_inspired_n2_pressure) {
	        decay_time_to_zero_gradient = 
                1. / (nitrogen_time_constant[i - 1] - helium_time_constant[i - 1]) * 
                Math.log((surface_inspired_n2_pressure - nitrogen_pressure[i - 1]) / 
		        helium_pressure[i - 1]);
	        integral_gradient_x_time = 
                helium_pressure[i - 1] /
		            helium_time_constant[i - 1] * 
                        (1. - Math.exp(-helium_time_constant[i - 1] * 
                            decay_time_to_zero_gradient)) +
                   (nitrogen_pressure[i - 1] - surface_inspired_n2_pressure) / 
		            nitrogen_time_constant[i - 1] * 
                        (1. - Math.exp(-nitrogen_time_constant[i - 1] * 
		                    decay_time_to_zero_gradient));
	        surface_phase_volume_time[i - 1] = 
		        integral_gradient_x_time / 
		        (helium_pressure[i - 1] + 
		            nitrogen_pressure[i - 1] - 
		            surface_inspired_n2_pressure);
	    } else {
	        surface_phase_volume_time[i - 1] = 0.;
	    }
    }
    return 0;
} /* calc_surface_phase_volume_time */

/* =============================================================================== */
/*     SUBROUTINE CRITICAL_VOLUME */
/*     Purpose: This subprogram applies the VPM Critical Volume Algorithm.  This */
/*     algorithm will compute "relaxed" gradients for helium and nitrogen based */
/*     on the setting of the Critical Volume Parameter Lambda. */
/* =============================================================================== */

private int critical_volume(double deco_phase_volume_time)
{
    /* System generated locals */
    double r1;

    /* Local variables */
    double initial_allowable_grad_n2_pa, 
	            initial_allowable_grad_he_pa, 
                parameter_lambda_pascals, b, 
	            c;
    int i;
    double new_allowable_grad_n2_pascals, 
                phase_volume_time[]=new double[16], 
	            new_allowable_grad_he_pascals, 
                adj_crush_pressure_n2_pascals, 
	            adj_crush_pressure_he_pascals;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/*     Note:  Since the Critical Volume Parameter Lambda was defined in units of */
/*     fsw-min in the original papers by Yount and colleauges, the same */
/*     convention is retained here.  Although Lambda is adjustable only in units */
/*     of fsw-min in the program settings (range from 6500 to 8300 with default */
/*     7500), it will convert to the proper value in Pascals-min in this */
/*     subroutine regardless of which diving pressure units are being used in */
/*     the main program - feet of seawater (fsw) or meters of seawater (msw). */
/*     The allowable gradient is computed using the quadratic formula (refer to */
/*     separate write-up posted on the Deco List web site). */
/* =============================================================================== */

    parameter_lambda_pascals = 
        crit_volume_parameter_lambda / 33. * 101325.;
    for (i = 1; i <= 16; ++i) {
	    phase_volume_time[i - 1] = 
            deco_phase_volume_time + surface_phase_volume_time[i - 1];
    }
    for (i = 1; i <= 16; ++i) {
    	adj_crush_pressure_he_pascals = 
		    adjusted_crushing_pressure_he[i - 1] / units_factor * 101325.;
	    initial_allowable_grad_he_pa = 
		    initial_allowable_gradient_he[i - 1] / units_factor * 101325.;
    	b = initial_allowable_grad_he_pa + parameter_lambda_pascals * 
		    surface_tension_gamma / (
		    skin_compression_gammac * phase_volume_time[i - 1]);
	    c = surface_tension_gamma * (
		    surface_tension_gamma * (
		    parameter_lambda_pascals * adj_crush_pressure_he_pascals)) / 
		    (skin_compression_gammac * 
		     (skin_compression_gammac * phase_volume_time[i - 1]));
        /* Computing 2nd power */
	    r1 = b;
	    new_allowable_grad_he_pascals = 
            (b + Math.sqrt(r1 * r1 - c * 4.)) / 2.;
	    allowable_gradient_he[i - 1] = 
		    new_allowable_grad_he_pascals / 101325. * units_factor;
    }
    for (i = 1; i <= 16; ++i) {
	    adj_crush_pressure_n2_pascals = 
		    adjusted_crushing_pressure_n2[i - 1] / units_factor * 101325.;
	    initial_allowable_grad_n2_pa = 
		    initial_allowable_gradient_n2[i - 1] / units_factor * 101325.;
	    b = initial_allowable_grad_n2_pa + parameter_lambda_pascals * 
		    surface_tension_gamma / (
		    skin_compression_gammac * phase_volume_time[i - 1]);
	    c = surface_tension_gamma * 
		    (surface_tension_gamma * 
		    (parameter_lambda_pascals * adj_crush_pressure_n2_pascals)) / 
		    (skin_compression_gammac * 
		    (skin_compression_gammac * phase_volume_time[i - 1]));
        /* Computing 2nd power */
	    r1 = b;
	    new_allowable_grad_n2_pascals = 
            (b + Math.sqrt(r1 * r1 - c * 4.)) / 2.;
	    allowable_gradient_n2[i - 1] = 
		    new_allowable_grad_n2_pascals / 101325. * units_factor;
    }
    return 0;
} /* critical_volume */

/* =============================================================================== */
/*     SUBROUTINE CALC_START_OF_DECO_ZONE */
/*     Purpose: This subroutine uses the Bisection Method to find the depth at */
/*     which the leading compartment just enters the decompression zone. */
/*     Source:  "Numerical Recipes in Fortran 77", Cambridge University Press, */
/*     1992. */
/* =============================================================================== */

private double calc_start_of_deco_zone(double starting_depth, 
                            double rate)
{
    boolean printError=true;
    /* Local variables */
    double depth_start_of_deco_zone;
    double last_diff_change, 
                initial_helium_pressure, 
	            mid_range_nitrogen_pressure;
    int i, j;
    double initial_inspired_n2_pressure, 
     	        cpt_depth_start_of_deco_zone, 
                low_bound, 
	            initial_inspired_he_pressure, 
                high_bound_nitrogen_pressure, 
	            nitrogen_rate, 
                function_at_mid_range, 
                function_at_low_bound,
	            high_bound, 
                mid_range_helium_pressure, 
                mid_range_time, 
	            starting_ambient_pressure, 
                initial_nitrogen_pressure, 
     	        function_at_high_bound;

    double time_to_start_of_deco_zone, 
                high_bound_helium_pressure, 
	            helium_rate, 
                differential_change;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/*     First initialize some variables */
/* =============================================================================== */

    depth_start_of_deco_zone = 0.;
    starting_ambient_pressure = 
        starting_depth + barometric_pressure;
    initial_inspired_he_pressure = 
        (starting_ambient_pressure - water_vapor_pressure) * 
            fraction_helium[mix_number - 1];
    initial_inspired_n2_pressure = 
        (starting_ambient_pressure - water_vapor_pressure) * 
	        fraction_nitrogen[mix_number - 1];
    helium_rate = rate * fraction_helium[mix_number - 1];
    nitrogen_rate = rate * fraction_nitrogen[mix_number - 1];

/* =============================================================================== */
/*     ESTABLISH THE BOUNDS FOR THE ROOT SEARCH USING THE BISECTION METHOD */
/*     AND CHECK TO MAKE SURE THAT THE ROOT WILL BE WITHIN BOUNDS.  PROCESS */
/*     EACH COMPARTMENT INDIVIDUALLY AND FIND THE MAXIMUM DEPTH ACROSS ALL */
/*     COMPARTMENTS (LEADING COMPARTMENT) */
/*     In this case, we are solving for time - the time when the gas tension in */
/*     the compartment will be equal to ambient pressure.  The low bound for time */
/*     is set at zero and the high bound is set at the time it would take to */
/*     ascend to zero ambient pressure (absolute).  Since the ascent rate is */
/*     negative, a multiplier of -1.0 is used to make the time positive.  The */
/*     desired point when gas tension equals ambient pressure is found at a time */
/*     somewhere between these endpoints.  The algorithm checks to make sure that */
/*     the solution lies in between these bounds by first computing the low bound */
/*     and high bound function values. */
/* =============================================================================== */

    low_bound = 0.;
    high_bound = starting_ambient_pressure / rate * -1.;
    for (i = 1; i <= 16; ++i) {
	    initial_helium_pressure = helium_pressure[i - 1];
	    initial_nitrogen_pressure = nitrogen_pressure[i - 1];
	    function_at_low_bound = 
            initial_helium_pressure + 
		    initial_nitrogen_pressure + 
		    constant_pressure_other_gases - 
		    starting_ambient_pressure;
	    high_bound_helium_pressure = 
            schreiner_equation__(initial_inspired_he_pressure, 
                                 helium_rate, 
                                 high_bound,
		                         helium_time_constant[i - 1], 
                                 initial_helium_pressure);
	    high_bound_nitrogen_pressure = 
            schreiner_equation__(initial_inspired_n2_pressure, 
                                 nitrogen_rate, 
                                 high_bound, 
                                 nitrogen_time_constant[i - 1], 
		                         initial_nitrogen_pressure);
	    function_at_high_bound = high_bound_helium_pressure + 
		    high_bound_nitrogen_pressure + 
		    constant_pressure_other_gases;
	    if (function_at_high_bound * function_at_low_bound >= 0.) {
            pause();
	    }

/* =============================================================================== */
/*     APPLY THE BISECTION METHOD IN SEVERAL ITERATIONS UNTIL A SOLUTION WITH */
/*     THE DESIRED ACCURACY IS FOUND */
/*     Note: the program allows for up to 100 iterations.  Normally an exit will */
/*     be made from the loop well before that number.  If, for some reason, the */
/*     program exceeds 100 iterations, there will be a pause to alert the user. */
/* =============================================================================== */

	    if (function_at_low_bound < 0.) {
	        time_to_start_of_deco_zone = low_bound;
	        differential_change = high_bound - low_bound;
	    } else {
	        time_to_start_of_deco_zone = high_bound;
	        differential_change = low_bound - high_bound;
	    }
	    for (j = 1; j <= 100; ++j) {
	        last_diff_change = differential_change;
	        differential_change = last_diff_change * .5;
	        mid_range_time = 
                time_to_start_of_deco_zone + 
		        differential_change;
	        mid_range_helium_pressure = 
                schreiner_equation__(initial_inspired_he_pressure, 
                                     helium_rate, 
                                     mid_range_time, 
                                     helium_time_constant[i - 1], 
                                     initial_helium_pressure);
	        mid_range_nitrogen_pressure = 
                schreiner_equation__(initial_inspired_n2_pressure, 
                                     nitrogen_rate, 
                                     mid_range_time, 
                                     nitrogen_time_constant[i - 1], 
                                     initial_nitrogen_pressure);
	        function_at_mid_range = 
                mid_range_helium_pressure + 
		        mid_range_nitrogen_pressure + 
		        constant_pressure_other_gases - 
		        (starting_ambient_pressure + rate * mid_range_time);
	        if (function_at_mid_range <= 0.) {
		        time_to_start_of_deco_zone = mid_range_time;
	        }
	        if (Math.abs(differential_change) < .001 || 
		        function_at_mid_range == 0.) {
		        printError=false;
                        break;
	        }
/* L150: */
	    }
            if (printError)
            {
                pause();
            }

/* =============================================================================== */
/*     When a solution with the desired accuracy is found, the program jumps out */
/*     of the loop to Line 170 and assigns the solution value for the individual */
/*     compartment. */
/* =============================================================================== */


	    cpt_depth_start_of_deco_zone = 
            starting_ambient_pressure + 
            rate * time_to_start_of_deco_zone - 
		    barometric_pressure;

/* =============================================================================== */
/*     The overall solution will be the compartment with the maximum depth where */
/*     gas tension equals ambient pressure (leading compartment). */
/* =============================================================================== */

	    depth_start_of_deco_zone = 
            Math.max(depth_start_of_deco_zone, cpt_depth_start_of_deco_zone);
/* L200: */
    }
    return depth_start_of_deco_zone;
} /* calc_start_of_deco_zone */

/* =============================================================================== */
/*     SUBROUTINE PROJECTED_ASCENT */
/*     Purpose: This subprogram performs a simulated ascent outside of the main */
/*     program to ensure that a deco ceiling will not be violated due to unusual */
/*     gas loading during ascent (on-gassing).  If the deco ceiling is violated, */
/*     the stop depth will be adjusted deeper by the step size until a safe */
/*     ascent can be made. */
/* =============================================================================== */

private int projected_ascent(double starting_depth, 
                     double rate, 
                     double step_size)
{
    /* System generated locals */
    double r1, r2;

    /* Local variables */
    double weighted_allowable_gradient, 
                ending_ambient_pressure, 
	            initial_helium_pressure[]=new double[16], 
                temp_gas_loading[]=new double[16], 
	            segment_time;
    int i;
    double initial_inspired_n2_pressure, 
                new_ambient_pressure, 
	            temp_helium_pressure, 
                initial_inspired_he_pressure, 
	            allowable_gas_loading[]=new double[16], 
                nitrogen_rate, 
	            starting_ambient_pressure, 
                initial_nitrogen_pressure[]=new double[16];

    double helium_rate, 
                temp_nitrogen_pressure;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/* =============================================================================== */

    new_ambient_pressure = deco_stop_depth + barometric_pressure;
    starting_ambient_pressure = starting_depth + barometric_pressure;
    initial_inspired_he_pressure = 
        (starting_ambient_pressure - water_vapor_pressure) * 
            fraction_helium[mix_number - 1];
    initial_inspired_n2_pressure = 
        (starting_ambient_pressure - water_vapor_pressure) * 
	        fraction_nitrogen[mix_number - 1];
    helium_rate = rate * fraction_helium[mix_number - 1];
    nitrogen_rate = rate * fraction_nitrogen[mix_number - 1];
    for (i = 1; i <= 16; ++i) {
	    initial_helium_pressure[i - 1] = helium_pressure[i - 1];
	    initial_nitrogen_pressure[i - 1] = nitrogen_pressure[i - 1];
    }

outer:
for(;;)
{
    ending_ambient_pressure = new_ambient_pressure;
    segment_time = (ending_ambient_pressure - starting_ambient_pressure) / rate;
    for (i = 1; i <= 16; ++i) {
	    temp_helium_pressure = 
            schreiner_equation__(initial_inspired_he_pressure, 
                                 helium_rate, 
                                 segment_time, 
                                 helium_time_constant[i - 1], 
		                         initial_helium_pressure[i - 1]);
	    temp_nitrogen_pressure = 
            schreiner_equation__(initial_inspired_n2_pressure, 
                                 nitrogen_rate, 
                                 segment_time, 
                                 nitrogen_time_constant[i - 1], 
                                 initial_nitrogen_pressure[i - 1]);
	    temp_gas_loading[i - 1] = 
            temp_helium_pressure + 
		    temp_nitrogen_pressure;
	    if (temp_gas_loading[i - 1] > 0.) {
	        weighted_allowable_gradient = 
		        (allowable_gradient_he[i - 1] * 
		            temp_helium_pressure + 
		            allowable_gradient_n2[i - 1] * 
		            temp_nitrogen_pressure) / temp_gas_loading[i - 1];
	    } else {
            /* Computing MIN */
	        r1 = allowable_gradient_he[i - 1];
            r2 = allowable_gradient_n2[i - 1];
	        weighted_allowable_gradient = Math.min(r1,r2);
	    }
	    allowable_gas_loading[i - 1] = 
            ending_ambient_pressure + 
		    weighted_allowable_gradient - 
		    constant_pressure_other_gases;
/* L670: */
        }
        for (i = 1; i <= 16; ++i) {
	    if (temp_gas_loading[i - 1] > allowable_gas_loading[i - 1]) {
	        new_ambient_pressure = ending_ambient_pressure + step_size;
	        deco_stop_depth += step_size;
	    }
            else
               break outer; // JURE preglej !!
/* L671: */
    }
}

    return 0;
} /* projected_ascent */

/* =============================================================================== */
/*     SUBROUTINE BOYLES_LAW_COMPENSATION */
/*     Purpose: This subprogram calculates the reduction in allowable gradients */
/*     with decreasing ambient pressure during the decompression profile based */
/*     on Boyle's Law considerations. */
/* =============================================================================== */
private int boyles_law_compensation(double first_stop_depth,
                            double deco_stop_depth,
                            double step_size)
{
    /* Local variables */
    int i;
    double next_stop;
    double ambient_pressure_first_stop, ambient_pressure_next_stop;
    double amb_press_first_stop_pascals, amb_press_next_stop_pascals;
    double a, b, c, low_bound, high_bound, ending_radius;
    double deco_gradient_pascals;
    double allow_grad_first_stop_he_pa, radius_first_stop_he;
    double allow_grad_first_stop_n2_pa, radius_first_stop_n2;
    
    double radius1_he[]=new double[16], radius2_he[]=new double[16];
    double radius1_n2[]=new double[16], radius2_n2[]=new double[16];

/* =============================================================================== */
/*      CALCULATIONS */
/* =============================================================================== */
    next_stop = deco_stop_depth - step_size;     
    ambient_pressure_first_stop = first_stop_depth + barometric_pressure;
    ambient_pressure_next_stop = next_stop + barometric_pressure;
    amb_press_first_stop_pascals = (ambient_pressure_first_stop/units_factor) * 101325.0;
    amb_press_next_stop_pascals =(ambient_pressure_next_stop/units_factor) * 101325.0;
       
    for (i = 1; i <= 16; ++i) {
	allow_grad_first_stop_he_pa = (allowable_gradient_he[i - 1]/units_factor) * 101325.0;	
	radius_first_stop_he = (2.0 * surface_tension_gamma) / allow_grad_first_stop_he_pa;
	radius1_he[i - 1] = radius_first_stop_he;
	a = amb_press_next_stop_pascals;
	b = -2.0 * surface_tension_gamma;
	c = (amb_press_first_stop_pascals + (2.0*surface_tension_gamma)/
	    radius_first_stop_he)* radius_first_stop_he*
	    (radius_first_stop_he*(radius_first_stop_he));
	low_bound = radius_first_stop_he;
	high_bound = radius_first_stop_he*cbrtf((amb_press_first_stop_pascals/amb_press_next_stop_pascals)); /* JURE  ** */
	ending_radius=radius_root_finder (a, b, c, low_bound, high_bound);
	radius2_he[i - 1] = ending_radius;
	deco_gradient_pascals = (2.0 * surface_tension_gamma) / ending_radius;
	deco_gradient_he[i - 1] = (deco_gradient_pascals / 101325.0) * units_factor;
    }

    for (i = 1; i <= 16; ++i) {
	allow_grad_first_stop_n2_pa = (allowable_gradient_n2[i - 1]/units_factor) * 101325.0;
	radius_first_stop_n2 = (2.0 * surface_tension_gamma) / allow_grad_first_stop_n2_pa;	
	radius1_n2[i - 1] = radius_first_stop_n2;
	a = amb_press_next_stop_pascals;
	b = -2.0 * surface_tension_gamma;
	c = (amb_press_first_stop_pascals + (2.0*surface_tension_gamma)/
	     radius_first_stop_n2)* radius_first_stop_n2*
	    (radius_first_stop_n2*(radius_first_stop_n2));
	low_bound = radius_first_stop_n2;
	high_bound = radius_first_stop_n2*cbrtf((amb_press_first_stop_pascals/amb_press_next_stop_pascals));	
	ending_radius=radius_root_finder (a, b, c, low_bound, high_bound);	
	radius2_n2[i - 1] = ending_radius;
	deco_gradient_pascals = (2.0 * surface_tension_gamma) / ending_radius;
	deco_gradient_n2[i - 1] = (deco_gradient_pascals / 101325.0) * units_factor;
    }
/* =============================================================================== */
/*      END OF SUBROUTINE */
/* =============================================================================== */
    return 0;
}

/* =============================================================================== */
/*     SUBROUTINE DECOMPRESSION_STOP */
/*     Purpose: This subprogram calculates the required time at each */
/*     decompression stop. */
/* =============================================================================== */

private int decompression_stop(double deco_stop_depth, double step_size, double addition_time, double min_stop_time)
// JURE extended decostops - added parameters addition_time - the time you want prolonge deco stop for
//                           min_stop_time - mimimum deco stop time
{
    /* System generated locals */
    double r1;

    /* Local variables */
    double inspired_nitrogen_pressure;
    int last_segment_number;
    double weighted_allowable_gradient, 
                initial_helium_pressure[]=new double[16];

    double time_counter;
    int i;
    double ambient_pressure;
    double inspired_helium_pressure, 
                next_stop, last_run_time, 
	            temp_segment_time;

    double initial_nitrogen_pressure[]=new double[16], 
	            round_up_operation;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/* =============================================================================== */

    last_run_time = run_time;
    r1 = last_run_time / minimum_deco_stop_time + .5;
    round_up_operation = Math.rint(r1) * minimum_deco_stop_time;
    segment_time = round_up_operation - run_time;
    run_time = round_up_operation;
    temp_segment_time = segment_time;
    last_segment_number = segment_number;
    segment_number = last_segment_number + 1;
    ambient_pressure = deco_stop_depth + barometric_pressure;
    ending_ambient_pressure = ambient_pressure;
    next_stop = deco_stop_depth - step_size;
    inspired_helium_pressure = 
        (ambient_pressure - water_vapor_pressure) * fraction_helium[mix_number - 1];
    inspired_nitrogen_pressure = 
        (ambient_pressure - water_vapor_pressure) * fraction_nitrogen[mix_number - 1];

/* =============================================================================== */
/*     Check to make sure that program won't lock up if unable to decompress */
/*     to the next stop.  If so, write error message and terminate program. */
/* =============================================================================== */

    for (i = 1; i <= 16; ++i) {
	    if (inspired_helium_pressure + inspired_nitrogen_pressure > 0.) {
	        weighted_allowable_gradient = 
		        (deco_gradient_he[i - 1] * inspired_helium_pressure + 
		            deco_gradient_n2[i - 1] * inspired_nitrogen_pressure) / 
		        (inspired_helium_pressure + inspired_nitrogen_pressure);
	        if (inspired_helium_pressure + inspired_nitrogen_pressure + 
		            constant_pressure_other_gases - weighted_allowable_gradient > 
                    next_stop + barometric_pressure) {
                exit(1);
	        }
	    }
    }

for(;;)
{
    for (i = 1; i <= 16; ++i) {
	    initial_helium_pressure[i - 1] = helium_pressure[i - 1];
	    initial_nitrogen_pressure[i - 1] = nitrogen_pressure[i - 1];
    	helium_pressure[i - 1] = 
            haldane_equation__(initial_helium_pressure[i - 1], 
                               inspired_helium_pressure, 
                               helium_time_constant[i - 1], 
                               segment_time);
	    nitrogen_pressure[i - 1] = 
            haldane_equation__(initial_nitrogen_pressure[i - 1], 
                               inspired_nitrogen_pressure, 
                               nitrogen_time_constant[i - 1], 
                               segment_time);
/* L720: */
    }
    calc_deco_ceiling();
    if (deco_ceiling_depth > next_stop) {
	    segment_time = minimum_deco_stop_time;
	    time_counter = temp_segment_time;
	    temp_segment_time = time_counter + minimum_deco_stop_time;
	    run_time = run_time + minimum_deco_stop_time;
    }
    else
        break;
}

    // JURE extended stops - START
    if (addition_time>0)
    {
        segment_time = addition_time;
        temp_segment_time += segment_time;
        run_time += segment_time;
        
        for (i = 1; i <= 16; ++i) {
	        initial_helium_pressure[i - 1] = helium_pressure[i - 1];
	        initial_nitrogen_pressure[i - 1] = nitrogen_pressure[i - 1];
        	helium_pressure[i - 1] = 
                haldane_equation__(initial_helium_pressure[i - 1], 
                                   inspired_helium_pressure, 
                                   helium_time_constant[i - 1], 
                                   segment_time);
	        nitrogen_pressure[i - 1] = 
                haldane_equation__(initial_nitrogen_pressure[i - 1], 
                                   inspired_nitrogen_pressure, 
                                   nitrogen_time_constant[i - 1], 
                                   segment_time);
        }
        calc_deco_ceiling();
    }
    
    if ((run_time-Math.floor(last_run_time))<min_stop_time)
    {
        segment_time = min_stop_time-(run_time-Math.floor(last_run_time));
        temp_segment_time += segment_time;
        run_time += segment_time;
        
        for (i = 1; i <= 16; ++i) {
	        initial_helium_pressure[i - 1] = helium_pressure[i - 1];
	        initial_nitrogen_pressure[i - 1] = nitrogen_pressure[i - 1];
        	helium_pressure[i - 1] = 
                haldane_equation__(initial_helium_pressure[i - 1], 
                                   inspired_helium_pressure, 
                                   helium_time_constant[i - 1], 
                                   segment_time);
	        nitrogen_pressure[i - 1] = 
                haldane_equation__(initial_nitrogen_pressure[i - 1], 
                                   inspired_nitrogen_pressure, 
                                   nitrogen_time_constant[i - 1], 
                                   segment_time);
        }
        calc_deco_ceiling();    	
    }
    // JURE extended stops - END
       
    segment_time = temp_segment_time;
    return 0;
} /* decompression_stop */

/* =============================================================================== */
/*      SUBROUTINE CALC_DECO_CEILING */
/*      Purpose: This subprogram calculates the deco ceiling (the safe ascent */
/*      depth) in each compartment, based on the allowable "deco gradients" */
/*      computed in the Boyle's Law Compensation subroutine, and then finds the */
/*      deepest deco ceiling across all compartments.  This deepest value */
/*      (Deco Ceiling Depth) is then used by the Decompression Stop subroutine */
/*      to determine the actual deco schedule. */
/* =============================================================================== */
private int calc_deco_ceiling ()
{
    /* Local variables */
    int i;
    double gas_loading, weighted_allowable_gradient;
    double tolerated_ambient_pressure;
    double compartment_deco_ceiling[]=new double[16];

/* =============================================================================== */
/*      CALCULATIONS */
/*      Since there are two sets of deco gradients being tracked, one for */
/*      helium and one for nitrogen, a "weighted allowable gradient" must be */
/*      computed each time based on the proportions of helium and nitrogen in */
/*      each compartment.  This proportioning follows the methodology of */
/*      Buhlmann/Keller.  If there is no helium and nitrogen in the compartment, */
/*      such as after extended periods of oxygen breathing, then the minimum value */
/*      across both gases will be used.  It is important to note that if a */
/*      compartment is empty of helium and nitrogen, then the weighted allowable */
/*      gradient formula cannot be used since it will result in division by zero. */
/* =============================================================================== */
    for (i = 1; i <= 16; ++i) {
        gas_loading = helium_pressure[i - 1] + nitrogen_pressure[i - 1];
    
	if (gas_loading > 0.0) {
	    weighted_allowable_gradient =
	    (deco_gradient_he[i - 1]* helium_pressure[i - 1] +
	     deco_gradient_n2[i - 1]* nitrogen_pressure[i - 1]) /
	    (helium_pressure[i - 1] + nitrogen_pressure[i - 1]);
	 
	    tolerated_ambient_pressure = (gas_loading +
	    constant_pressure_other_gases) - weighted_allowable_gradient;
	} 
	else {
	    weighted_allowable_gradient =
	    Math.min(deco_gradient_he[i - 1], deco_gradient_n2[i - 1]); 
	 
	    tolerated_ambient_pressure =
	    constant_pressure_other_gases - weighted_allowable_gradient;
	}
/* =============================================================================== */
/*      The tolerated ambient pressure cannot be less than zero absolute, i.e., */
/*      the vacuum of outer space! */
/* =============================================================================== */
	if (tolerated_ambient_pressure < 0.0) {
	    tolerated_ambient_pressure = 0.0;
	}
/* =============================================================================== */
/*      The Deco Ceiling Depth is computed in a loop after all of the individual */
/*      compartment deco ceilings have been calculated.  It is important that the */
/*      Deco Ceiling Depth (max deco ceiling across all compartments) only be */
/*      extracted from the compartment values and not be compared against some */
/*      initialization value.  For example, if MAX(Deco_Ceiling_Depth . .) was */
/*      compared against zero, this could cause a program lockup because sometimes */
/*      the Deco Ceiling Depth needs to be negative (but not less than absolute */
/*      zero) in order to decompress to the last stop at zero depth. */
/* =============================================================================== */
	compartment_deco_ceiling[i - 1] = tolerated_ambient_pressure - barometric_pressure;
    }
    
    deco_ceiling_depth = compartment_deco_ceiling[0];
    for (i = 2; i <= 16; ++i) {
	deco_ceiling_depth =Math.max(deco_ceiling_depth, compartment_deco_ceiling[i - 1]);
    }  
/* =============================================================================== */
/*      END OF SUBROUTINE */
/* =============================================================================== */
    return 0;
}

/* =============================================================================== */
/*     SUBROUTINE GAS_LOADINGS_SURFACE_INTERVAL */
/*     Purpose: This subprogram calculates the gas loading (off-gassing) during */
/*     a surface interval. */
/* =============================================================================== */

private int gas_loadings_surface_interval(double surface_interval_time)
{
    double inspired_nitrogen_pressure, 
                initial_helium_pressure;

    int i;
    double inspired_helium_pressure, 
                initial_nitrogen_pressure;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/* =============================================================================== */

    inspired_helium_pressure = 0.;
    inspired_nitrogen_pressure = (barometric_pressure - water_vapor_pressure) * .79;
    for (i = 1; i <= 16; ++i) {
	    initial_helium_pressure = helium_pressure[i - 1];
	    initial_nitrogen_pressure = nitrogen_pressure[i - 1];
	    helium_pressure[i - 1] = 
            haldane_equation__(initial_helium_pressure, 
                               inspired_helium_pressure, 
                               helium_time_constant[i - 1], 
		                       surface_interval_time);
	    nitrogen_pressure[i - 1] = 
            haldane_equation__(initial_nitrogen_pressure, 
                               inspired_nitrogen_pressure, 
                               nitrogen_time_constant[i - 1], 
		                       surface_interval_time);
    }
    return 0;
} /* gas_loadings_surface_interval */

/* =============================================================================== */
/*     SUBROUTINE VPM_REPETITIVE_ALGORITHM */
/*     Purpose: This subprogram implements the VPM Repetitive Algorithm that was */
/*     envisioned by Professor David E. Yount only months before his passing. */

/* =============================================================================== */
private int vpm_repetitive_algorithm(double surface_interval_time)
{
    /* Local variables */
    double max_actual_gradient_pascals, 
                initial_allowable_grad_n2_pa,
	            initial_allowable_grad_he_pa;
    int i;
    double adj_crush_pressure_n2_pascals, 
                new_critical_radius_n2, 
	            adj_crush_pressure_he_pascals, 
                new_critical_radius_he;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/* =============================================================================== */

    for (i = 1; i <= 16; ++i) {
	    max_actual_gradient_pascals = max_actual_gradient[i 
		    - 1] / units_factor * 101325.;
	    adj_crush_pressure_he_pascals = 
		    adjusted_crushing_pressure_he[i - 1] / units_factor * 101325.;
	    adj_crush_pressure_n2_pascals = 
		    adjusted_crushing_pressure_n2[i - 1] / units_factor * 101325.;
	    initial_allowable_grad_he_pa = 
		    initial_allowable_gradient_he[i - 1] / units_factor * 101325.;
	    initial_allowable_grad_n2_pa = 
		    initial_allowable_gradient_n2[i - 1] / units_factor * 101325.;
	    if (max_actual_gradient[i - 1] > initial_allowable_gradient_n2[i - 1]) {
	        new_critical_radius_n2 = 
                surface_tension_gamma * 2. * 
                (skin_compression_gammac - surface_tension_gamma) / 
		        (max_actual_gradient_pascals * skin_compression_gammac - 
		            surface_tension_gamma * adj_crush_pressure_n2_pascals);
	        adjusted_critical_radius_n2[i - 1] = 
		        initial_critical_radius_n2[i - 1] + 
		        (initial_critical_radius_n2[i - 1] - 
		            new_critical_radius_n2) *  Math.exp(-(surface_interval_time) / 
                    regeneration_time_constant);
		            
	    } else {
	        adjusted_critical_radius_n2[i - 1] = 
		        initial_critical_radius_n2[i - 1];
	    }
	    if (max_actual_gradient[i - 1] > initial_allowable_gradient_he[i - 1]) {
	        new_critical_radius_he = 
                surface_tension_gamma * 2. * 
                (skin_compression_gammac - surface_tension_gamma) / 
		        (max_actual_gradient_pascals * skin_compression_gammac - 
		            surface_tension_gamma * adj_crush_pressure_he_pascals);
	        adjusted_critical_radius_he[i - 1] = 
		        initial_critical_radius_he[i - 1] + (
		        initial_critical_radius_he[i - 1] - 
		            new_critical_radius_he) * Math.exp(-(surface_interval_time) / 
                    regeneration_time_constant);
	    } else {
	        adjusted_critical_radius_he[i - 1] = 
		        initial_critical_radius_he[i - 1];
	    }
    }
    return 0;
} /* vpm_repetitive_algorithm */

/* =============================================================================== */
/*     SUBROUTINE CALC_BAROMETRIC_PRESSURE */
/*     Purpose: This sub calculates barometric pressure at altitude based on the */
/*     publication "U.S. Standard Atmosphere, 1976", U.S. Government Printing */
/*     Office, Washington, D.C. The source for this code is a Fortran 90 program */
/*     written by Ralph L. Carmichael (retired NASA researcher) and endorsed by */
/*     the National Geophysical Data Center of the National Oceanic and */
/*     Atmospheric Administration.  It is available for download free from */
/*     Public Domain Aeronautical Software at:  http://www.pdas.com/atmos.htm */
/* =============================================================================== */

private int calc_barometric_pressure(double altitude)
{
    /* Local variables */
    double altitude_meters, 
                molecular_weight_of_air, 
	            acceleration_of_operation, 
                altitude_kilometers, 
                altitude_feet,
	            temp_gradient, 
                temp_at_sea_level, 
                pressure_at_sea_level, 
	            geopotential_altitude, 
                gmr_factor, 
	            pressure_at_sea_level_fsw, 
                pressure_at_sea_level_msw, 
	            temp_at_geopotential_altitude, 
                gas_constant_r, 
	            radius_of_earth;

/* =============================================================================== */
/*     CALCULATIONS */
/* =============================================================================== */

    radius_of_earth = 6369.;                 /* ki */
    acceleration_of_operation = 9.80665;     /* meters/ */
    molecular_weight_of_air = 28.9644;
    gas_constant_r = 8.31432;                /* Joules/mol*de */
    temp_at_sea_level = 288.15;              /* degree */
    pressure_at_sea_level_fsw = 33.;         /* at sea level (Standard Atm */
                                             /* feet of seawater based on 1 */
    pressure_at_sea_level_msw = 10.;         /* at sea level (European */
                                             /* meters of seawater based on 1 */
    temp_gradient = -6.5;                    /* change in geopotential a */
                                             /* valid for first layer of at */
                                             /* up to 11 kilometers or 36, */
                                             /* Change in Temp deg Kel */
    gmr_factor = 
        acceleration_of_operation * molecular_weight_of_air / gas_constant_r;
    if (units_equal_fsw) {
	    altitude_feet = altitude;
	    altitude_kilometers = altitude_feet / 3280.839895;
	    pressure_at_sea_level = pressure_at_sea_level_fsw;
    }
    else {
	    altitude_meters = altitude;
	    altitude_kilometers = altitude_meters / 1e3;
	    pressure_at_sea_level = pressure_at_sea_level_msw;
    }
    geopotential_altitude = 
        altitude_kilometers * radius_of_earth / (altitude_kilometers + radius_of_earth);
    temp_at_geopotential_altitude = 
        temp_at_sea_level + temp_gradient * geopotential_altitude;
    barometric_pressure = 
        pressure_at_sea_level * 
        Math.exp(Math.log(temp_at_sea_level / temp_at_geopotential_altitude) * gmr_factor / temp_gradient);

    return 0;
} /* calc_barometric_pressure */

/* =============================================================================== */
/*     SUBROUTINE VPM_ALTITUDE_DIVE_ALGORITHM */
/*     Purpose:  This subprogram updates gas loadings and adjusts critical radii */
/*     (as required) based on whether or not diver is acclimatized at altitude or */
/*     makes an ascent to altitude before the dive. */
/* =============================================================================== */

private int vpm_altitude_dive_algorithm()
{

    int i;

    double inspired_nitrogen_pressure, 
                ending_radius_n2, rate,
                  ascent_to_altitude_time,  
                ending_ambient_pressure, 
	            ending_radius_he;

    double gradient_n2_bubble_formation;

    double gradient_he_bubble_formation, 
	            time_at_altitude_before_dive, 
                compartment_gradient, 
	            initial_inspired_n2_pressure;

    double regenerated_radius_n2,
	            compartment_gradient_pascals, 
                nitrogen_rate, 
	            regenerated_radius_he;

    double new_critical_radius_n2, 
	            starting_ambient_pressure;
    boolean diver_acclimatized;
    double initial_nitrogen_pressure, 
                new_critical_radius_he;

    if (units_equal_fsw && altitude_of_dive > 30000.0) {
        exit(1);
    }
    if (units_equal_msw && altitude_of_dive > 9144.) {
        exit(1);
    }
    if (diver_acclimatized_at_altitude.equals("YES") || 
	    diver_acclimatized_at_altitude.equals("yes")) {
	    diver_acclimatized = true;
    } else {
	    diver_acclimatized = false;
    }

    ascent_to_altitude_time = ascent_to_altitude_hours * 60.;
    time_at_altitude_before_dive = hours_at_altitude_before_dive *  60.;
    if (diver_acclimatized) {
	    calc_barometric_pressure(altitude_of_dive);

	    for (i = 1; i <= 16; ++i) {
	        adjusted_critical_radius_n2[i - 1] = 
		        initial_critical_radius_n2[i - 1];
	        adjusted_critical_radius_he[i - 1] = 
		        initial_critical_radius_he[i - 1];
	        helium_pressure[i - 1] = 0.;
	        nitrogen_pressure[i - 1] = 
		        (barometric_pressure - water_vapor_pressure) * .79;
	    }
    } else {
	    if (starting_acclimatized_altitude >= altitude_of_dive || 
		    starting_acclimatized_altitude < 0.) {
            exit(1);
	    }
	    calc_barometric_pressure(starting_acclimatized_altitude);

	    starting_ambient_pressure = barometric_pressure;
	    for (i = 1; i <= 16; ++i) {
	        helium_pressure[i - 1] = 0.;
	        nitrogen_pressure[i - 1] = 
		        (barometric_pressure - water_vapor_pressure) * .79;
	    }
	    calc_barometric_pressure(altitude_of_dive);

	    ending_ambient_pressure = barometric_pressure;
	    initial_inspired_n2_pressure = (starting_ambient_pressure - 
		    water_vapor_pressure) * .79;
	    rate = (ending_ambient_pressure - starting_ambient_pressure) / 
		    ascent_to_altitude_time;
	    nitrogen_rate = rate * .79;
	    for (i = 1; i <= 16; ++i) {
	        initial_nitrogen_pressure = nitrogen_pressure[i - 1];
	        nitrogen_pressure[i - 1] = 
                schreiner_equation__(initial_inspired_n2_pressure, 
                                     nitrogen_rate, 
                                     ascent_to_altitude_time, 
                                     nitrogen_time_constant[i - 1], 
                                     initial_nitrogen_pressure);
	        compartment_gradient = 
                nitrogen_pressure[i - 1] + 
		        constant_pressure_other_gases - 
		        ending_ambient_pressure;
	        compartment_gradient_pascals = 
                compartment_gradient / units_factor * 101325.;
	        gradient_he_bubble_formation = 
		        surface_tension_gamma * 2. * 
		        (skin_compression_gammac - 
		        surface_tension_gamma) / (
		        initial_critical_radius_he[i - 1] * 
		        skin_compression_gammac);
	        if (compartment_gradient_pascals > gradient_he_bubble_formation) {
		        new_critical_radius_he = 
			        surface_tension_gamma * 2. * 
			        (skin_compression_gammac - 
			        surface_tension_gamma) / (
			        compartment_gradient_pascals * 
			        skin_compression_gammac);
		        adjusted_critical_radius_he[i - 1] = 
			        initial_critical_radius_he[i - 1] + 
			        (initial_critical_radius_he[i - 1] - 
			        new_critical_radius_he) * 
			        Math.exp(-time_at_altitude_before_dive / 
			        regeneration_time_constant);
		        initial_critical_radius_he[i - 1] = 
			        adjusted_critical_radius_he[i - 1];
	        } else {
	        	ending_radius_he = 1. / (
			        compartment_gradient_pascals / 
			        ((surface_tension_gamma - 
			        skin_compression_gammac) * 2.) + 
			        1. / initial_critical_radius_he[i - 1]);
		        regenerated_radius_he = 
			        initial_critical_radius_he[i - 1] + 
			        (ending_radius_he - 
			        initial_critical_radius_he[i - 1]) * 
			        Math.exp(-time_at_altitude_before_dive / 
			        regeneration_time_constant);
		        initial_critical_radius_he[i - 1] = 
			        regenerated_radius_he;
		        adjusted_critical_radius_he[i - 1] = 
			        initial_critical_radius_he[i - 1];
	        }
	        gradient_n2_bubble_formation = 
		        surface_tension_gamma * 2. * 
		        (skin_compression_gammac - 
		        surface_tension_gamma) / (
		        initial_critical_radius_n2[i - 1] * 
		        skin_compression_gammac);
	        if (compartment_gradient_pascals > gradient_n2_bubble_formation) {
		        new_critical_radius_n2 = 
			        surface_tension_gamma * 2. * 
			        (skin_compression_gammac - 
			        surface_tension_gamma) / 
			        (compartment_gradient_pascals * 
			        skin_compression_gammac);
		        adjusted_critical_radius_n2[i - 1] = 
			        initial_critical_radius_n2[i - 1] + 
			        (initial_critical_radius_n2[i - 1] - 
			        new_critical_radius_n2) * 
			        Math.exp(-time_at_altitude_before_dive / 
			        regeneration_time_constant);
		        initial_critical_radius_n2[i - 1] = 
			        adjusted_critical_radius_n2[i - 1];
	        } else {
		        ending_radius_n2 = 1. / (
			        compartment_gradient_pascals / 
			        ((surface_tension_gamma - 
			        skin_compression_gammac) * 2.) + 
			        1. / initial_critical_radius_n2[i - 1]);
		        regenerated_radius_n2 = 
			        initial_critical_radius_n2[i - 1] + 
			        (ending_radius_n2 - 
			        initial_critical_radius_n2[i - 1]) * 
			        Math.exp(-time_at_altitude_before_dive / 
			        regeneration_time_constant);
		        initial_critical_radius_n2[i - 1] = 
			        regenerated_radius_n2;
		        adjusted_critical_radius_n2[i - 1] = 
			        initial_critical_radius_n2[i - 1];
	        }
	    }
	    inspired_nitrogen_pressure = 
            (barometric_pressure - water_vapor_pressure) * .79;
	    for (i = 1; i <= 16; ++i) {
	        initial_nitrogen_pressure = nitrogen_pressure[i - 1];
	        nitrogen_pressure[i - 1] = 
                haldane_equation__(initial_nitrogen_pressure, 
                                   inspired_nitrogen_pressure, 
                                   nitrogen_time_constant[i - 1], 
                                   time_at_altitude_before_dive);
	    }
    }
    
    return 0;
} // vpm_altitude_dive_algorithm

private void exit(int a)
{
}

private void pause()
{
}

private double cbrtf(double a)
{
// computes the cube root of the argument
    if (a>0)
        return (Math.exp(Math.log(a)/3));
    else if (a<0)
        return (-Math.exp(Math.log(-a)/3));
    else // a==0
        return 0;
}

// JURE multilevel START
private double roundDecoStop(double stop_depth, double step_size)
{
	  double rounding_operation;

    if (units_equal_fsw) {
        if (step_size < 10.) {
    	    rounding_operation = stop_depth / step_size + .5;
    	    stop_depth = Math.floor(rounding_operation) * step_size;
        } else {
    	    rounding_operation = stop_depth /  10. + .5;
    	    stop_depth = Math.floor(rounding_operation) * 10.;
        }
    }
    if (units_equal_msw) {
        if (step_size < 3.) {
    	    rounding_operation = stop_depth / step_size + .5;
    	    stop_depth = Math.floor(rounding_operation) * step_size;
        } else {
    	    rounding_operation = stop_depth /  3. + .5;
    	    stop_depth = Math.floor(rounding_operation) * 3.;
        }
    }
    
    if (stop_depth<0)
    stop_depth=0;

    return stop_depth;
}
// JURE multilevel END
} // end of class
