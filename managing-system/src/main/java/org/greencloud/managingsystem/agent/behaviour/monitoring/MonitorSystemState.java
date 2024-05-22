package org.greencloud.managingsystem.agent.behaviour.monitoring;

import static org.greencloud.managingsystem.agent.behaviour.monitoring.logs.ManagingMonitoringLog.MONITOR_SYSTEM_STATE_LOG;
import static org.greencloud.managingsystem.agent.behaviour.monitoring.logs.ManagingMonitoringLog.SYSTEM_STABLE_STATE_LOG;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_TIMEOUT;

import java.util.Comparator;
import java.util.Map;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.goal.AdaptationGoal;
import com.database.knowledge.types.GoalType;

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

		final boolean isSystemStable = myManagingAgent.monitor().getGoalsFulfillment().stream()
				.allMatch(goalFulfillment -> goalFulfillment.equals(true));

		myManagingAgent.monitor().updateSystemStatistics();
		if (isSystemStable) {
			logger.info(SYSTEM_STABLE_STATE_LOG);
			//end feedback iteration
			return;
		}

		final GoalType worstGoal = getGoalWithWorstQuality();
		myManagingAgent.analyze().trigger(worstGoal);
	}

	private GoalType getGoalWithWorstQuality() {
		return myManagingAgent.monitor().getLastMeasuredGoalQualities().entrySet()
				.stream()
				.min(Comparator.comparingDouble(this::getGoalQuality))
				.orElseThrow()
				.getKey();
	}

	private double getGoalQuality(final Map.Entry<GoalType, Double> goalEntry) {
		final AdaptationGoal goal = myManagingAgent.monitor().getAdaptationGoal(goalEntry.getKey());
		final double quality = goalEntry.getValue();
		final double difference = quality - goal.threshold();

		return goal.isAboveThreshold() ? difference : (-1) * difference;
	}
}
