package agents.greenenergy.domain;

/**
 * Class storing Green Energy Agent constants:
 * <p>
 * MAX_ERROR_IN_JOB_FINISH - time error added to the time after which job execution should finish
 * RATED_WIND_SPEED        - wind speed at which wind turbine produces maximum power, given in meters per second
 */
public class GreenEnergyAgentConstants {

    public static final Long MAX_ERROR_IN_JOB_FINISH = 5000L;

    public static final Double CUT_ON_WIND_SPEED = 5.0;

    public static final Double RATED_WIND_SPEED = 15.0;
}
