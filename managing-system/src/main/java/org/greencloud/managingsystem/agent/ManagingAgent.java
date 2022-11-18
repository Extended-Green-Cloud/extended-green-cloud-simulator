package org.greencloud.managingsystem.agent;

import static com.greencloud.application.common.constant.LoggingConstant.MDC_AGENT_NAME;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.greencloud.managingsystem.agent.behaviour.knowledge.ReadAdaptationGoals;
import com.greencloud.application.behaviours.ReceiveGUIController;

import jade.core.behaviours.Behaviour;

/**
 * Agent representing the Managing Agent that is responsible for the system's adaptation
 */
public class ManagingAgent extends AbstractManagingAgent {

	private static final Logger logger = LoggerFactory.getLogger(ManagingAgent.class);

	/**
	 * Method initializes the agents and start the behaviour which upon connecting with the agent node, reads
	 * the adaptation goals from the database
	 */
	@Override
	protected void setup() {
		super.setup();
		MDC.put(MDC_AGENT_NAME, super.getLocalName());
		initializeAgent(getArguments());
		addBehaviour(new ReceiveGUIController(this, behavioursRunAtStart()));
	}

	/**
	 * Method logs the information to the console.
	 */
	@Override
	protected void takeDown() {
		logger.info("I'm finished. Bye!");
		getGuiController().removeAgentNodeFromGraph(getAgentNode());
		super.takeDown();
	}

	private void initializeAgent(final Object[] args) {
		if (Objects.nonNull(args) && args.length == 1) {
			try {
				final double systemQuality = Double.parseDouble(args[0].toString());

				if (systemQuality <= 0 || systemQuality > 1) {
					logger.info("Incorrect argument: System quality must be from a range (0,1]");
					doDelete();
				}
				this.systemQualityThreshold = systemQuality;
			} catch (NumberFormatException e) {
				logger.info("Incorrect argument: please check arguments in the documentation");
				doDelete();
			}
		} else {
			logger.info("Incorrect arguments: some parameters for green source agent are missing - "
					+ "check the parameters in the documentation");
			doDelete();
		}
	}

	private List<Behaviour> behavioursRunAtStart() {
		return List.of(new ReadAdaptationGoals());
	}
}
