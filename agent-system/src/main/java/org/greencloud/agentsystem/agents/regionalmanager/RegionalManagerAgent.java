package org.greencloud.agentsystem.agents.regionalmanager;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static org.greencloud.commons.constants.DFServiceConstants.RMA_SERVICE_NAME;
import static org.greencloud.commons.constants.DFServiceConstants.RMA_SERVICE_TYPE;
import static org.jrba.utils.yellowpages.YellowPagesRegister.deregister;
import static org.jrba.utils.yellowpages.YellowPagesRegister.prepareDF;
import static org.jrba.utils.yellowpages.YellowPagesRegister.register;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.slf4j.Logger;

import jade.core.behaviours.Behaviour;

/**
 * Agent representing the network component that orchestrates work in part of the Regional Manager.
 */
public class RegionalManagerAgent extends AbstractRegionalManagerAgent {

	private static final Logger logger = getLogger(RegionalManagerAgent.class);

	@Override
	protected void initializeAgent(final Object[] args) {
		if (args.length == 7) {
			final int pollingBatchSize = parseInt(args[2].toString());
			final int maximumQueueSize = parseInt(args[3].toString());
			this.properties = new RegionalManagerAgentProps(getName(), maximumQueueSize, pollingBatchSize);

			properties.setParentDFAddress(prepareDF(args[0].toString(), args[1].toString()));
		} else {
			logger.error("Incorrect arguments: some parameters for RMA are missing");
			doDelete();
		}
	}

	@Override
	protected void takeDown() {
		deregister(this, properties.getParentDFAddress(), RMA_SERVICE_TYPE, RMA_SERVICE_NAME);
		super.takeDown();
	}

	@Override
	protected List<Behaviour> prepareStartingBehaviours() {
		register(this, properties.getParentDFAddress(), RMA_SERVICE_TYPE, RMA_SERVICE_NAME);
		return emptyList();
	}

	@Override
	protected void runInitialBehavioursForRuleSet() {
		properties.setUpPriorityQueue(properties.getMaximumQueueSize(), getJobPriority());
		super.runInitialBehavioursForRuleSet();
	}

}
