package com.greencloud.application.agents.greenenergy.domain;


/**
 * Class storing Green Energy Agent constants
 *
 * <p> MAX_ERROR_IN_JOB_FINISH 		  			- variation in milliseconds of job finish time </p>
 * <p> INTERVAL_LENGTH_MS					    - length of the sub-interval used in calculating available power </p>
 * <p> PERIODIC_WEATHER_CHECK_TIMEOUT 			- timeout in between consecutive weather checks </p>
 * <p> GREEN_ENERGY_ENVIRONMENT_SENSOR_TIMEOUT  - timeout in between consecutive environment events checks </p>
 * <p> CUT_ON_WIND_SPEED 			  			- speed in blade rotation </p>
 * <p> RATED_WIND_SPEED 			  			- wind speed at which wind turbine produces maximum power, given in meters per second </p>
 * <p> TEST_MULTIPLIER				  			- multiplier used in manipulating power management in conducted tests (1 = getting true result) </p>
 * <p> MOCK_SOLAR_ENERGY			  			- flag indicating that the solar energy should be mocked, not calculated </p>
 */
public class GreenEnergyAgentConstants {

	public static final Long MAX_ERROR_IN_JOB_FINISH = 500L;
	public static final Long INTERVAL_LENGTH_MS = 500L;
	public static final Long PERIODIC_WEATHER_CHECK_TIMEOUT = 1000L;
	public static final Long GREEN_ENERGY_ENVIRONMENT_SENSOR_TIMEOUT = 100L;
	public static final Double CUT_ON_WIND_SPEED = 5.0;
	public static final Double RATED_WIND_SPEED = 15.0;
	public static final int TEST_MULTIPLIER = 1;
	public static final boolean MOCK_SOLAR_ENERGY = true;

}
