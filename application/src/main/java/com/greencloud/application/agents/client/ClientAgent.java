package com.greencloud.application.agents.client;

import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.gui.GuiConnectionProvider.connectToGui;
import static com.greencloud.application.utils.TimeUtils.convertToSimulationTime;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.client.behaviour.df.FindCloudNetworkAgents;
import com.greencloud.application.agents.client.behaviour.jobannouncement.initiator.InitiateNewJobAnnouncement;
import com.greencloud.application.agents.client.behaviour.jobannouncement.listener.ListenForJobUpdate;
import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.domain.job.ImmutableClientJob;
import com.greencloud.application.exception.IncorrectTaskDateException;
import com.greencloud.application.utils.TimeUtils;


import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;

/**
 * Agent representing the Client that wants to have the job executed by the Cloud Network
 */
public class ClientAgent extends AbstractClientAgent {

	private static final Logger logger = LoggerFactory.getLogger(ClientAgent.class);

	/**
	 * Method run at the agent's start. In initialize the Client Agent based on the given by the user arguments and
	 * runs the starting behaviours - requesting the job execution and listening for job result information
	 */
	@Override
	protected void setup() {
		super.setup();
		final Object[] args = getArguments();

		if (Objects.nonNull(args) && args.length == 5) {
			initializeAgent();
			final ClientJob jobToBeExecuted = initializeAgentJob(args);
			MDC.put(MDC_JOB_ID, jobToBeExecuted.getJobId());
			connectToGui(this);
			prepareStartingBehaviour(jobToBeExecuted).forEach(this::addBehaviour);
		} else {
			logger.error("Incorrect arguments: some parameters for client's job are missing -"
					+ " check the parameters in the documentation");
			doDelete();
		}
	}

	/**
	 * Method run before the Client is being deleted. It logs the Client's finish information.
	 */
	@Override
	protected void takeDown() {
		logger.info("I'm finished. Bye!");
		super.takeDown();
	}

	private void initializeAgent() {
		this.chosenCloudNetworkAgent = null;
	}

	private ClientJob initializeAgentJob(final Object[] arguments) {
		try {
			final Instant startTime = TimeUtils.convertToInstantTime(arguments[0].toString());
			final Instant endTime = TimeUtils.convertToInstantTime(arguments[1].toString());
			final Instant deadline = TimeUtils.convertToInstantTime(arguments[2].toString());
			final Instant currentTime = TimeUtils.getCurrentTimeMinusError();
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
			prepareSimulatedTimes(startTime, endTime, deadline);
			logger.info("[{}] Job simulation time: from {} to {}", this.getName(), simulatedJobStart, simulatedJobEnd);
			return ImmutableClientJob.builder()
					.clientIdentifier(getAID().getName())
					.startTime(getSimulatedJobStart())
					.endTime(getSimulatedJobEnd())
					.deadline(getSimulatedDeadline())
					.power(Integer.parseInt(arguments[3].toString()))
					.jobId(arguments[4].toString())
					.build();
		} catch (IncorrectTaskDateException e) {
			logger.error(e.getMessage());
			doDelete();
		} catch (NumberFormatException e) {
			logger.error("The given power is not a number!");
			doDelete();
		}
		return null;
	}

	private void prepareSimulatedTimes(final Instant startTime, final Instant endTime, final Instant deadline) {
		final Instant currentTime = getCurrentTime();
		final long expectedJobStart = convertToSimulationTime(ChronoUnit.SECONDS.between(currentTime, startTime));
		final long expectedJobEnd = convertToSimulationTime(ChronoUnit.SECONDS.between(currentTime, endTime));
		final long expectedJobDeadline = convertToSimulationTime(ChronoUnit.SECONDS.between(currentTime, deadline));
		setSimulatedJobStart(currentTime.plus(expectedJobStart, ChronoUnit.MILLIS));
		setSimulatedJobEnd(currentTime.plus(expectedJobEnd, ChronoUnit.MILLIS));
		setSimulatedDeadline(currentTime.plus(expectedJobDeadline, ChronoUnit.MILLIS));
	}

	private List<Behaviour> prepareStartingBehaviour(final ClientJob job) {
		var startingBehaviour = new SequentialBehaviour(this);
		startingBehaviour.addSubBehaviour(new FindCloudNetworkAgents());
		startingBehaviour.addSubBehaviour(new InitiateNewJobAnnouncement(this, null, job));
		return List.of(
				new ListenForJobUpdate(this, job),
				startingBehaviour
		);
	}
}
