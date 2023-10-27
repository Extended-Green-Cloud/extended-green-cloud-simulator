package org.greencloud.agentsystem.agents.cloudnetwork;

import static org.greencloud.commons.utils.yellowpages.YellowPagesRegister.deregister;
import static org.greencloud.commons.utils.yellowpages.YellowPagesRegister.prepareDF;
import static org.greencloud.commons.utils.yellowpages.YellowPagesRegister.register;
import static org.greencloud.commons.constants.DFServiceConstants.CNA_SERVICE_NAME;
import static org.greencloud.commons.constants.DFServiceConstants.CNA_SERVICE_TYPE;
import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;

import jade.core.behaviours.Behaviour;

/**
 * Agent representing the network component that orchestrates work in part of the Cloud Network
 */
public class CloudNetworkAgent extends AbstractCloudNetworkAgent {

	private static final Logger logger = getLogger(CloudNetworkAgent.class);

	@Override
	protected void initializeAgent(final Object[] args) {
		if (args.length == 5) {
			properties.setParentDFAddress(prepareDF(args[0].toString(), args[1].toString()));
		} else {
			logger.error("Incorrect arguments: some parameters for CNA are missing");
			doDelete();
		}
	}

	@Override
	protected void takeDown() {
		deregister(this, properties.getParentDFAddress(), CNA_SERVICE_TYPE, CNA_SERVICE_NAME);
		super.takeDown();
	}


	@Override
	protected List<Behaviour> prepareStartingBehaviours() {
		register(this, properties.getParentDFAddress(), CNA_SERVICE_TYPE, CNA_SERVICE_NAME);
		return emptyList();
	}

}
