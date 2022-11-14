package com.greencloud.application.behaviours;

import static com.database.knowledge.domain.agent.DataType.HEALTH_CHECK;

import com.database.knowledge.domain.agent.HealthCheck;
import com.greencloud.application.agents.AbstractAgent;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

/**
 * Common behaviour for all agents to report theirs health check to the monitoring_data database table
 */
public class ReportHealthCheck extends TickerBehaviour {

	/**
	 * The period defined in MS on how often agents should report their health check. When setting the value is
	 * important to take into account the pressure it puts on the environment - every created agent reports that metric.
	 */
	// TODO to be adjusted
	private static final long HEALTH_CHECK_PERIOD = 250;

	public ReportHealthCheck(Agent a) {
		super(a, HEALTH_CHECK_PERIOD);
	}

	@Override
	protected void onTick() {
		((AbstractAgent) getAgent()).writeMonitoringData(HEALTH_CHECK, new HealthCheck(true));
	}
}
