package com.greencloud.application.agents.scheduler;

import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.ADAPTATION_MANAGEMENT;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.STATE_MANAGEMENT;
import static com.greencloud.application.yellowpages.YellowPagesService.deregister;
import static com.greencloud.application.yellowpages.YellowPagesService.register;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SCHEDULER_SERVICE_NAME;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SCHEDULER_SERVICE_TYPE;
import static com.greencloud.commons.utils.CommonUtils.isFibonacci;
import static java.lang.Integer.parseInt;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.greencloud.application.agents.scheduler.behaviour.df.SubscribeCloudNetworkService;
import com.greencloud.application.agents.scheduler.behaviour.scheduling.handler.HandleJobAnnouncement;
import com.greencloud.application.agents.scheduler.behaviour.scheduling.listener.ListenForClientJob;
import com.greencloud.application.agents.scheduler.behaviour.scheduling.listener.ListenForJobUpdate;
import com.greencloud.application.agents.scheduler.managment.SchedulerAdaptationManagement;
import com.greencloud.application.agents.scheduler.managment.SchedulerStateManagement;
import com.greencloud.commons.managingsystem.planner.AdaptationActionParameters;

import jade.core.behaviours.Behaviour;

/**
 * Agent representing the Scheduler that orchestrate the job announcement in Cloud Network
 */
public class SchedulerAgent extends AbstractSchedulerAgent {

	private static final Logger logger = getLogger(SchedulerAgent.class);

	@Override
	protected void initializeAgent(final Object[] args) {
		if (args.length == 5) {
			try {
				this.deadlinePriority = parseInt(args[0].toString());
				this.cpuPriority = parseInt(args[1].toString());
				this.maximumQueueSize = parseInt(args[2].toString());

				this.setUpPriorityQueue();
				register(this, getDefaultDF(), SCHEDULER_SERVICE_TYPE, SCHEDULER_SERVICE_NAME);

			} catch (final NumberFormatException e) {
				logger.info("Weight arguments must be double values!");
				doDelete();
			}
		} else {
			logger.info("Incorrect arguments: some parameters for Scheduler Agent are missing");
			doDelete();
		}
	}

	@Override
	protected void initializeAgentManagements() {
		this.agentManagementServices = new EnumMap<>(Map.of(
				STATE_MANAGEMENT, new SchedulerStateManagement(this),
				ADAPTATION_MANAGEMENT, new SchedulerAdaptationManagement(this)
		));
		manage().updateWeightsGUI();
	}

	@Override
	protected void takeDown() {
		deregister(this, getDefaultDF(), SCHEDULER_SERVICE_TYPE, SCHEDULER_SERVICE_NAME);
		super.takeDown();
	}

	@Override
	protected void validateAgentArguments() {
		if (!isFibonacci(cpuPriority) || !isFibonacci(deadlinePriority)) {
			logger.info("Incorrect arguments: Weights must be in a Fibonacci sequence");
			doDelete();
		}
		if (maximumQueueSize < 1) {
			logger.info("Incorrect arguments: Queue size must be a positive integer!");
			doDelete();
		}
	}

	@Override
	protected List<Behaviour> prepareStartingBehaviours() {
		return List.of(
				SubscribeCloudNetworkService.create(this),
				new HandleJobAnnouncement(this),
				new ListenForClientJob(),
				new ListenForJobUpdate()
		);
	}

	@Override
	public boolean executeAction(final AdaptationActionEnum adaptationActionEnum,
			final AdaptationActionParameters actionParameters) {
		return switch (adaptationActionEnum) {
			case INCREASE_DEADLINE_PRIORITY -> adapt().increaseDeadlineWeight();
			case INCREASE_POWER_PRIORITY -> adapt().increaseCPUWeight();
			default -> false;
		};
	}
}
