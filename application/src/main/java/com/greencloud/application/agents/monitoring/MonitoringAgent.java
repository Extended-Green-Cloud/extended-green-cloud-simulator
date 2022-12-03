package com.greencloud.application.agents.monitoring;

import static com.greencloud.application.agents.monitoring.domain.MonitoringAgentConstants.BAD_STUB_PROBABILITY;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_AGENT_NAME;

import java.util.Collections;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.monitoring.behaviour.ServeForecastWeather;
import com.greencloud.application.behaviours.ReceiveGUIController;

/**
 * Agent which is responsible for monitoring the com.greencloud.application.weather and sending the data to the Green Source Agent
 */
public class MonitoringAgent extends AbstractMonitoringAgent {

	private static final Logger logger = LoggerFactory.getLogger(MonitoringAgent.class);

	/**
	 * Method starts the behaviour which is listening for the com.greencloud.application.weather requests.
	 */
	@Override
	protected void setup() {
		super.setup();
		MDC.put(MDC_AGENT_NAME, super.getLocalName());
		initializeAgent(getArguments());
		addBehaviour(new ReceiveGUIController(this, Collections.singletonList(new ServeForecastWeather(this))));
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
		if (args.length > 0 && Objects.nonNull(args[0])) {
			this.badStubProbability = Double.parseDouble(String.valueOf(args[0]));
		} else {
			this.badStubProbability = BAD_STUB_PROBABILITY;
		}
	}
}
