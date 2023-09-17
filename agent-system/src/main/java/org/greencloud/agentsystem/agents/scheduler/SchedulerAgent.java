package org.greencloud.agentsystem.agents.scheduler;

import static com.greencloud.commons.utils.CommonUtils.isFibonacci;
import static com.greencloud.commons.yellowpages.YellowPagesService.deregister;
import static com.greencloud.commons.yellowpages.YellowPagesService.register;
import static com.greencloud.commons.yellowpages.domain.DFServiceConstants.SCHEDULER_SERVICE_NAME;
import static com.greencloud.commons.yellowpages.domain.DFServiceConstants.SCHEDULER_SERVICE_TYPE;
import static java.lang.Integer.parseInt;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import com.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;

/**
 * Agent representing the Scheduler that orchestrate the job announcement in Cloud Network
 */
public class SchedulerAgent extends AbstractSchedulerAgent {

	private static final Logger logger = getLogger(SchedulerAgent.class);

	@Override
	protected void initializeAgent(final Object[] args) {
		if (args.length == 5) {
			try {
				final int deadlinePriority = parseInt(args[0].toString());
				final int cpuPriority = parseInt(args[1].toString());
				final int maximumQueueSize = parseInt(args[2].toString());
				this.properties = new SchedulerAgentProps(getName(), deadlinePriority, cpuPriority, maximumQueueSize);

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
	protected void runStartingBehaviours() {
		super.runStartingBehaviours();
		properties.updateGUI();
	}

	@Override
	protected void takeDown() {
		deregister(this, getDefaultDF(), SCHEDULER_SERVICE_TYPE, SCHEDULER_SERVICE_NAME);
		super.takeDown();
	}

	@Override
	protected void validateAgentArguments() {
		if (!isFibonacci(this.properties.getCpuPriority()) || !isFibonacci(this.properties.getDeadlinePriority())) {
			logger.info("Incorrect arguments: Weights must be in a Fibonacci sequence");
			doDelete();
		}
		if (this.properties.getMaximumQueueSize() < 1) {
			logger.info("Incorrect arguments: Queue size must be a positive integer!");
			doDelete();
		}
	}

	@Override
	protected void runInitialBehavioursForStrategy() {
		properties.setUpPriorityQueue(getJobPriority());
		super.runInitialBehavioursForStrategy();
	}
}
