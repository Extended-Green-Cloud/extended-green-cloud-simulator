package com.greencloud.application.agents.client.domain;

import java.time.temporal.ValueRange;

/**
 * Class storing Cloud Network Agent constants:
 * <p> SCHEDULER_AGENT	- data store key under which the Scheduler Agent found in the DF will be stored </p>
 * <p> MAX_TIME_DIFFERENCE	- maximum job delay time </p>
 */
public class ClientAgentConstants {
	public static final ValueRange MAX_TIME_DIFFERENCE = ValueRange.of(-1500, 1500);
	public static final String SCHEDULER_AGENT = "SCHEDULER_AGENT";
}
