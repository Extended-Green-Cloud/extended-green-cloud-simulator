package org.greencloud.agentsystem.agents.centralmanager;

import static java.lang.Integer.parseInt;
import static org.greencloud.commons.constants.DFServiceConstants.CMA_SERVICE_NAME;
import static org.greencloud.commons.constants.DFServiceConstants.CMA_SERVICE_TYPE;
import static org.jrba.utils.yellowpages.YellowPagesRegister.deregister;
import static org.jrba.utils.yellowpages.YellowPagesRegister.register;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.slf4j.Logger;

/**
 * Agent representing the Central Manager that orchestrate the job announcement in system regions.
 */
public class CentralManagerAgent extends AbstractCentralManagerAgent {

	private static final Logger logger = getLogger(CentralManagerAgent.class);

	@Override
	protected void initializeAgent(final Object[] args) {
		if (args.length == 5) {
			try {
				final int maximumQueueSize = parseInt(args[0].toString());
				final int pollingBatchSize = parseInt(args[1].toString());
				this.properties = new CentralManagerAgentProps(getName(), maximumQueueSize, pollingBatchSize);

				register(this, getDefaultDF(), CMA_SERVICE_TYPE, CMA_SERVICE_NAME);
			} catch (final NumberFormatException e) {
				logger.info("Maximum queue size arguments must be an integer value!");
				doDelete();
			}
		} else {
			logger.info("Incorrect arguments: some parameters for Central Manager Agent are missing.");
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
		deregister(this, getDefaultDF(), CMA_SERVICE_TYPE, CMA_SERVICE_NAME);
		super.takeDown();
	}

	@Override
	protected void validateAgentArguments() {
		if (this.properties.getMaximumQueueSize() < 1) {
			logger.info("Incorrect arguments: Queue size must be a positive integer!");
			doDelete();
		}

		if (this.properties.getPollingBatchSize() < 1) {
			logger.info("Incorrect arguments: Polling batch size must be a positive integer!");
			doDelete();
		}
	}

	@Override
	protected void runInitialBehavioursForRuleSet() {
		properties.setUpPriorityQueue(getJobPriority());
		super.runInitialBehavioursForRuleSet();
	}
}
