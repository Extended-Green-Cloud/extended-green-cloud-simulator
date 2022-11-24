package org.greencloud.managingsystem.agent.behaviour.monitoring;

import static org.greencloud.managingsystem.agent.behaviour.monitoring.logs.ManagingMonitoringLog.MONITOR_SYSTEM_STATE_LOG;
import static org.greencloud.managingsystem.agent.behaviour.monitoring.logs.ManagingMonitoringLog.SYSTEM_STABLE_STATE_LOG;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_TIMEOUT;

import java.util.Comparator;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.goal.GoalEnum;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

/**
 * Behaviour monitors the current system state and, if necessary, calls the analyzer service
 */
public class MonitorSystemState extends TickerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(MonitorSystemState.class);

	private final ManagingAgent myManagingAgent;

	/**
	 * Default constructor
	 *
	 * @param agent agent executing the behaviour
	 */
	public MonitorSystemState(final Agent agent) {
		super(agent, MONITOR_SYSTEM_TIMEOUT);

		myManagingAgent = (ManagingAgent) agent;
	}

	/**
	 * Method retrieves the current system monitoring data from the database and performs the pre-analysis verifying if
	 * potential adaptation is necessary
	 */
	@Override
	protected void onTick() {
		logger.info(MONITOR_SYSTEM_STATE_LOG);

		//HERE WE WANT TO ADD MORE GOALS IN THE FUTURE
		final boolean areGoalsSatisfied = myManagingAgent.monitor().isSuccessRatioMaximized();

		if (areGoalsSatisfied) {
			logger.info(SYSTEM_STABLE_STATE_LOG);
			//end feedback iteration
			return;
		}
		myManagingAgent.monitor().updateSystemStatistics();
		myManagingAgent.analyze().trigger(getGoalWithWorstQuality());
	}

	private GoalEnum getGoalWithWorstQuality() {
		return myManagingAgent.monitor().getCurrentGoalQualities().entrySet()
				.stream()
				.min(Comparator.comparingDouble(
						goal -> myManagingAgent.monitor().getAdaptationGoal(goal.getKey()).isAboveThreshold() ?
								goal.getValue() :
								1 - goal.getValue()))
				.orElseThrow().getKey();
	}
}
