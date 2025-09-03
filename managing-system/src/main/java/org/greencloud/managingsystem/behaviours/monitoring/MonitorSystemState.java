package org.greencloud.managingsystem.behaviours.monitoring;

import static java.lang.Boolean.TRUE;
import static java.util.Comparator.comparingDouble;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;

import java.util.Map;

import org.greencloud.commons.domain.adaptation.goal.AdaptationGoal;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.managingsystem.agents.ManagingAgent;

import com.database.knowledge.types.GoalType;

import jade.core.behaviours.TickerBehaviour;

/**
 * Behaviour monitors the current system state and, if necessary, calls the analyzer service.
 */
public class MonitorSystemState extends TickerBehaviour {

	private static final LogBuilder log = constructLogBuilder(MonitorSystemState.class);

	private final ManagingAgent myManagingAgent;

	/**
	 * Default constructor
	 *
	 * @param agent agent executing the behaviour
	 */
	public MonitorSystemState(final ManagingAgent agent) {
		super(agent, agent.getProperties().getConfig().getMonitoringSystemTimeout());
		this.myManagingAgent = agent;
	}

	/**
	 * Method retrieves the current system monitoring data from the database and performs the pre-analysis verifying if
	 * potential adaptation is necessary.
	 */
	@Override
	protected void onTick() {
		log.messageInfo("Managing Agent started monitoring current system state...");
		final boolean isSystemStable = myManagingAgent.monitor().getGoalsFulfillment().stream().allMatch(TRUE::equals);
		myManagingAgent.monitor().updateSystemStatistics();

		if (isSystemStable) {
			log.messageInfo("System satisfies adaptation goals.");
			return;
		}
		myManagingAgent.analyze().trigger(getGoalWithWorstQuality());
	}

	private GoalType getGoalWithWorstQuality() {
		return myManagingAgent.monitor().getLastMeasuredGoalQualities().entrySet()
				.stream()
				.min(comparingDouble(this::getGoalQuality))
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
