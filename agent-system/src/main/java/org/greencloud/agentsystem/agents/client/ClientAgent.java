package org.greencloud.agentsystem.agents.client;

import static java.util.Collections.emptyList;
import static org.greencloud.commons.utils.time.TimeConverter.convertToInstantTime;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.utils.agent.AgentConnector.connectAgentObject;
import static org.jrba.utils.yellowpages.YellowPagesRegister.prepareDF;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.List;

import org.greencloud.agentsystem.agents.EGCSAgent;
import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.args.job.JobArgs;
import org.greencloud.commons.exception.IncorrectTaskDateException;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;

/**
 * Agent representing the Client that wants to have the job executed in the Cloud
 */
public class ClientAgent extends AbstractClientAgent {

	private static final Logger logger = getLogger(ClientAgent.class);

	@Override
	public void initializeAgent(final Object[] arguments) {
		if (arguments.length == 6) {
			try {
				final Instant deadline = convertToInstantTime(arguments[2].toString());
				final JobArgs jobArgs = (JobArgs) arguments[3];
				final String jobId = arguments[4].toString();

				this.properties = new ClientAgentProps(getName(), getAID(), deadline, jobArgs, jobId);
				properties.setParentDFAddress(prepareDF(arguments[0].toString(), arguments[1].toString()));

			} catch (IncorrectTaskDateException e) {
				logger.error(e.getMessage());
				doDelete();
			} catch (NumberFormatException e) {
				logger.error("Given requirements are have incorrect number format!");
				doDelete();
			}
		} else {
			logger.error("Incorrect arguments: some parameters for client's job are missing");
			doDelete();
		}
	}

	@Override
	protected List<Behaviour> prepareStartingBehaviours() {
		connectClient(this);

		final ParallelBehaviour main = new ParallelBehaviour();
		addBehaviour(main);
		setMainBehaviour(main);
		return emptyList();
	}

	@Override
	protected void setup() {
		super.setup();
		logClientSetUp();
	}

	private void connectClient(EGCSAgent<?, ?> abstractAgent) {
		connectAgentObject(abstractAgent, abstractAgent.getO2AObject());
		connectAgentObject(abstractAgent, abstractAgent.getO2AObject());
		runInitialBehavioursForRuleSet();
	}

	private void logClientSetUp() {
		MDC.put(MDC_JOB_ID, properties.getJob().getJobId());
		logger.info("[{}] Job deadline: {}. Job type: {}",
				getName(), properties.getJobSimulatedDeadline(), properties.getJobType());
	}
}
