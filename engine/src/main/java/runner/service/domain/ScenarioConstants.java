package runner.service.domain;

public class ScenarioConstants {

	public static final String RESOURCE_SCENARIO_PATH = "scenarios/";

	public static final long CLIENT_NUMBER = 1000;
	public static final int MAX_JOB_POWER = 30;
	public static final int MIN_JOB_POWER = 10;
	public static final int START_TIME_MIN = 1;
	public static final int START_TIME_MAX = 3;
	public static final int END_TIME_MAX = 6;
	public static final int DEADLINE_MAX = 10;

	public static final boolean MULTI_CONTAINER = false;
	public static final boolean MAIN_HOST = true;
	public static final String HOST_NAME = "127.0.0.1";
	public static final String DATABASE_HOST_NAME = "timescale";
	public static final String WEBSOCKET_HOST_NAME = "socket-server";
	public static final int HOST_ID = 0;
}
