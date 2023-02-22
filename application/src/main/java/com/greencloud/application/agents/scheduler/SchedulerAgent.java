package com.greencloud.application.agents.scheduler;

import static com.greencloud.application.common.constant.LoggingConstant.MDC_AGENT_NAME;
import static com.greencloud.application.yellowpages.YellowPagesService.register;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SCHEDULER_SERVICE_NAME;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SCHEDULER_SERVICE_TYPE;
import static com.greencloud.commons.utils.CommonUtils.isFibonacci;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.behaviour.df.SubscribeCloudNetworkService;
import com.greencloud.application.agents.scheduler.behaviour.job.scheduling.handler.HandleJobAnnouncement;
import com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.ListenForClientJob;
import com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.ListenForJobUpdate;
import com.greencloud.application.agents.scheduler.managment.SchedulerConfigurationManagement;
import com.greencloud.application.agents.scheduler.managment.SchedulerStateManagement;
import com.greencloud.application.behaviours.ReceiveGUIController;

import jade.core.behaviours.Behaviour;

/**
 * Agent representing the Scheduler Agent that orchestrate the job announcement in Cloud Network
 */
public class SchedulerAgent extends AbstractSchedulerAgent {

	private static final Logger logger = LoggerFactory.getLogger(SchedulerAgent.class);

	/**
	 * Method run at the agent's start. In initialize the Scheduler Agent with given parameters
	 * (initial priority weights)
	 */
	@Override
	protected void setup() {
		super.setup();
		MDC.put(MDC_AGENT_NAME, super.getLocalName());
		final Object[] args = getArguments();
		initializeAgent(args);
		addBehaviour(new ReceiveGUIController(this, prepareBehaviours()));
	}

	private void initializeAgent(final Object[] args) {
		if (Objects.nonNull(args) && args.length == 5) {
			try {
				final int deadlineWeight = Integer.parseInt(args[0].toString());
				final int powerWeight = Integer.parseInt(args[1].toString());
				final int maxQueueSize = Integer.parseInt(args[2].toString());
				final int jobSplitThreshold = Integer.parseInt(args[3].toString());
				final int splittingFactor = Integer.parseInt(args[4].toString());

				if (!isFibonacci(powerWeight) || !isFibonacci(deadlineWeight)) {
					logger.info("Incorrect arguments: Weights must be in a Fibonacci sequence");
					doDelete();
				}
				if (maxQueueSize < 1) {
					logger.info("Incorrect arguments: Queue size must be a positive integer!");
					doDelete();
				}
				this.configManagement = new SchedulerConfigurationManagement(this, deadlineWeight, powerWeight,
						maxQueueSize,
						jobSplitThreshold, splittingFactor);
				this.stateManagement = new SchedulerStateManagement(this);
				this.setUpPriorityQueue();

				register(this, SCHEDULER_SERVICE_TYPE, SCHEDULER_SERVICE_NAME);
			} catch (final NumberFormatException e) {
				logger.info("Weight arguments must be double values!");
				doDelete();
			}
		} else {
			logger.info("Incorrect arguments: some parameters for scheduler agent are missing - "
					+ "check the parameters in the documentation");
			doDelete();
		}
	}

	private List<Behaviour> prepareBehaviours() {
		return List.of(
				SubscribeCloudNetworkService.create(this),
				new HandleJobAnnouncement(this),
				new ListenForClientJob(),
				new ListenForJobUpdate()
		);
	}
}
