package org.greencloud.managingsystem.agent;

import static com.greencloud.application.common.constant.LoggingConstant.MDC_AGENT_NAME;
import static org.greencloud.managingsystem.service.planner.domain.AdaptationPlanVariables.POWER_SHORTAGE_THRESHOLD;

import java.util.List;
import java.util.Objects;

import org.greencloud.managingsystem.agent.behaviour.knowledge.ReadAdaptationGoals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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
		if (Objects.nonNull(args) && args.length >= 1) {
			try {
				final double systemQuality = Double.parseDouble(args[0].toString());

				if (systemQuality <= 0 || systemQuality > 1) {
					logger.info("Incorrect argument: System quality must be from a range (0,1]");
					doDelete();
				}
				this.systemQualityThreshold = systemQuality;

				if (args.length > 1) {
					// in separate if as more params will be added
					if (Objects.nonNull(args[1])) {
						POWER_SHORTAGE_THRESHOLD = Integer.parseInt(String.valueOf(args[1]));
					}
				}

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
