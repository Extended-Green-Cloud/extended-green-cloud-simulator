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
 */
public class GreenEnergyAgentPropsConstants {

	public static final Long INTERVAL_LENGTH_MIN = 30L;
	public static final Double COEFFICIENT_OF_PERFORMANCE = 0.35;
	public static final Double ROTOR_SWEPT_AREA = 54D;
	public static final int TEST_MULTIPLIER = 1;
	public static final boolean MOCK_SOLAR_ENERGY = true;
	public static final double SUB_INTERVAL_ERROR = 0.01;

}
