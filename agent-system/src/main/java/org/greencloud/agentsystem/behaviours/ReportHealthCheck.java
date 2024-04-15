package org.greencloud.agentsystem.behaviours;

import static com.database.knowledge.domain.agent.DataType.HEALTH_CHECK;
import static java.util.Objects.nonNull;

import org.greencloud.agentsystem.agents.EGCSAgent;
import org.greencloud.commons.args.agent.EGCSAgentType;

import com.database.knowledge.domain.agent.HealthCheck;

import jade.core.behaviours.TickerBehaviour;

/**
 * Generic behaviour that reports health status to the monitoring_data database table for a given agent
 */
public class ReportHealthCheck extends TickerBehaviour {

	/**
	 * The period defined in MS on how often agents should report their health check. When setting the value, it is
	 * important to take into account the pressure it puts on the environment - every created agent reports data
	 * according to that metric
	 */
	private static final long HEALTH_CHECK_PERIOD = 250;
	private final EGCSAgent<?, ?> myAbstractAgent;

	/**
	 * Behaviour constructor
	 *
	 * @param agent agent executing the behaviour
	 */
	public ReportHealthCheck(final EGCSAgent<?, ?> agent) {
		super(agent, HEALTH_CHECK_PERIOD);
		myAbstractAgent = agent;
	}

	/**
	 * Method reports agent's health data to the database
	 */
	@Override
	protected void onTick() {
		if (nonNull(myAbstractAgent.getAgentNode())) {
			myAbstractAgent.getAgentNode().getDatabaseClient().writeMonitoringData(myAbstractAgent.getName(),
					HEALTH_CHECK,
					new HealthCheck(true, EGCSAgentType.valueOf(myAbstractAgent.getProperties().getAgentType())));
		}
	}
}
