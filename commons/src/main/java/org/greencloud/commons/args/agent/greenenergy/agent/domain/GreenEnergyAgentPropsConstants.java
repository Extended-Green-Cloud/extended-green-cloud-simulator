package org.greencloud.commons.args.agent.greenenergy.agent.domain;

/**
 * Class storing Green Energy Agent constants
 *
 * <p> INTERVAL_LENGTH_MIN					    - length of the sub-interval used in calculating available power </p>
 * <p> TEST_MULTIPLIER				  			- multiplier used in manipulating power management in conducted tests
 * 												  (1 = getting true result) </p>
 * <p> MOCK_SOLAR_ENERGY			  			- flag indicating that the solar energy should be mocked,
 * 												  not calculated </p>
 * <p> SUB_INTERVAL_ERROR						- error associated with obtaining the maximum value
 * 												  based on the sub intervals </p>
 * <p> SOLAR_MODULE_RATED_POWER_AT_STC          - the manufacturer-rated output of a single solar panel module at
 * 												  Standard Test Conditions </p>
 * <p> SOLAR_IRRADIANCE_RATED_AT_STC            - the manufacturer-rated solar irradiance at Standard Test Conditions </p>
 * <p> SOLAR_TEMP_AT_STC						- the manufacturer-rated temperature at Standard Test Conditions </p>
 * <p> POWER_TEMP_COEFFICIENT					- the power temperature coefficient (shows how sensitive the panel is to heat) </p>
 * <p> SOLAR_MODULE_COUNT_TOTAL 				- total number of modules in solar panel (both in series and parallel) </p>
 * <p> NOMINAL_OPERATING_CELL_TEMPERATURE		- the temperature at which the solar panel operates under normal conditions </p>
 */
public class GreenEnergyAgentPropsConstants {

	public static final Long INTERVAL_LENGTH_MIN = 30L;
	public static final Double COEFFICIENT_OF_PERFORMANCE = 0.35;
	public static final Double ROTOR_SWEPT_AREA = 54D;
	public static final int TEST_MULTIPLIER = 1;
	public static final boolean MOCK_SOLAR_ENERGY = true;
	public static final double SUB_INTERVAL_ERROR = 0.01;
	public static final double SOLAR_MODULE_RATED_POWER_AT_STC = 165;
	public static final double SOLAR_IRRADIANCE_RATED_AT_STC = 1000;
	public static final double SOLAR_TEMP_AT_STC = 25;
	public static final double POWER_TEMP_COEFFICIENT = -0.004;
	public static final int SOLAR_MODULE_COUNT_TOTAL = 25;
	public static final double NOMINAL_OPERATING_CELL_TEMPERATURE = 45;



}
