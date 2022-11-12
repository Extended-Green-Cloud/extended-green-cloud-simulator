package com.greencloud.application.agents.scheduler;

import static com.greencloud.application.common.constant.LoggingConstant.MDC_AGENT_NAME;
import static com.greencloud.application.yellowpages.YellowPagesService.register;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SCHEDULER_SERVICE_NAME;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SCHEDULER_SERVICE_TYPE;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.behaviour.df.SubscribeCloudNetworkService;
import com.greencloud.application.agents.scheduler.behaviour.jobscheduling.handler.HandleJobAnnouncement;
import com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener.ListenForClientJob;
import com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener.ListenForJobUpdate;
import com.greencloud.application.agents.scheduler.managment.SchedulerConfigurationManagement;
import com.greencloud.application.agents.scheduler.managment.SchedulerStateManagement;
import com.greencloud.application.behaviours.ReceiveGUIController;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;

/**
 * Agent representing the Scheduler Agent that orchestrate the job announcement in Cloud Network
 */
public class SchedulerAgent extends AbstractSchedulerAgent {

	private static final Logger logger = LoggerFactory.getLogger(SchedulerAgent.class);

	/**
	 * Method run at the agent's start. In initialize the Scheduler Agent with given parameters (initial priority weights)
	 */
	@Override
	protected void setup() {
		super.setup();
		MDC.put(MDC_AGENT_NAME, super.getLocalName());
		final Object[] args = getArguments();
		initializeAgent(args);
		addBehaviour(new ReceiveGUIController(this, prepareBehaviours()));
	}

	@Override
	protected void takeDown() {
		super.takeDown();
	}

	private void initializeAgent(final Object[] args) {
		if (Objects.nonNull(args) && args.length == 2) {
			try {
				final double deadlineWeight = Double.parseDouble(args[0].toString());
				final double powerWeight = Double.parseDouble(args[1].toString());

				if (deadlineWeight < 0 || powerWeight < 0
						|| deadlineWeight > 1 || powerWeight > 1
						|| deadlineWeight + powerWeight != 1) {
					logger.info("Incorrect arguments: Weights must be from range [0,1] and must sum to 1!");
					doDelete();
				}
				this.configManagement = new SchedulerConfigurationManagement();
				this.stateManagement = new SchedulerStateManagement(this);

				this.config().setDeadlineWeightPriority(Double.parseDouble(args[0].toString()));
				this.config().setPowerWeightPriority(Double.parseDouble(args[1].toString()));

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
		final ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
		parallelBehaviour.addSubBehaviour(SubscribeCloudNetworkService.create(this));
		parallelBehaviour.addSubBehaviour(new HandleJobAnnouncement(this));
		parallelBehaviour.addSubBehaviour(new ListenForClientJob());
		parallelBehaviour.addSubBehaviour(new ListenForJobUpdate());
		return Collections.singletonList(parallelBehaviour);
	}
}
