package org.greencloud.agentsystem.agents.client;

import static java.util.Collections.emptyList;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.utils.time.TimeConverter.convertToInstantTime;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTimeMinusError;
import static org.greencloud.commons.utils.yellowpages.YellowPagesRegister.prepareDF;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.List;

import org.greencloud.agentsystem.agents.AbstractAgent;
import org.greencloud.agentsystem.utils.AgentConnector;
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

	private static void connectClient(AbstractAgent<?, ?> abstractAgent) {
		AgentConnector.connectAgentObject(abstractAgent, abstractAgent.getO2AObject());
		AgentConnector.connectAgentObject(abstractAgent, abstractAgent.getO2AObject());
	}

	@Override
	public void validateAgentArguments() {
		final Instant currentTime = getCurrentTimeMinusError();
		final Instant startTime = properties.getJobSimulatedStart();
		final Instant endTime = properties.getJobSimulatedEnd();
		final Instant deadline = properties.getJobSimulatedDeadline();

		if (startTime.isBefore(currentTime) || endTime.isBefore(currentTime)) {
			logger.error("The job execution dates cannot be before current time!");
			doDelete();
		}
		if (endTime.isBefore(startTime)) {
			logger.error("The job execution end date cannot be before job execution start date!");
			doDelete();
		}
		if (deadline.isBefore(endTime)) {
			logger.error("The job deadline cannot be before job execution end time!");
			doDelete();
		}
	}

	@Override
	public void initializeAgent(final Object[] arguments) {
		if (arguments.length == 8) {
			try {
				final Instant start = convertToInstantTime(arguments[2].toString());
				final Instant end = convertToInstantTime(arguments[3].toString());
				final Instant deadline = convertToInstantTime(arguments[4].toString());
				final JobArgs jobArgs = (JobArgs) arguments[5];
				final String jobId = arguments[6].toString();

				this.properties = new ClientAgentProps(getName(), getAID(), start, end, deadline, jobArgs, jobId);
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

	/**
	 * Method run at the agent's start.
	 * <p> In initialize the Client Agent based on the given by the user arguments and runs the starting behaviours. </p>
	 */
	@Override
	protected void setup() {
		super.setup();
		logClientSetUp();
	}

	private void logClientSetUp() {
		MDC.put(MDC_JOB_ID, properties.getJob().getJobId());
		logger.info("[{}] Job simulation time: from {} to {} (deadline: {}). Job type: {}",
				getName(),
				properties.getJobSimulatedStart(), properties.getJobSimulatedEnd(),
				properties.getJobSimulatedDeadline(),
				properties.getJobType());
	}
}
