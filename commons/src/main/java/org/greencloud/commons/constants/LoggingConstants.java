package org.greencloud.commons.constants;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;

import java.util.function.Function;

import org.jeasy.rules.api.Facts;

public class LoggingConstants {

	public static final String MDC_JOB_ID = "jobId";
	public static final String MDC_STRATEGY_ID = "strategyId";
	public static final String MDC_AGENT_NAME = "agentName";
	public static final String MDC_CLIENT_NAME = "clientName";

	public static Function<Facts, String> getIdxFromFacts = (facts -> valueOf((int) facts.get(STRATEGY_IDX)));
}
