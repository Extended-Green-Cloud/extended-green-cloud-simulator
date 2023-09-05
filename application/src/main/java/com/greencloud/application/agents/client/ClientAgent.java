package com.greencloud.application.agents.client;

import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.CLIENT_MANAGEMENT;
import static com.greencloud.application.utils.GuiUtils.connectToGui;
import static com.greencloud.application.utils.TimeUtils.convertToInstantTime;
import static com.greencloud.application.utils.TimeUtils.convertToSimulationTime;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.utils.TimeUtils.getCurrentTimeMinusError;
import static com.greencloud.application.yellowpages.YellowPagesService.prepareDF;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.client.behaviour.df.FindSchedulerAgent;
import com.greencloud.application.agents.client.behaviour.jobannouncement.initiator.InitiateNewJobAnnouncement;
import com.greencloud.application.agents.client.behaviour.jobannouncement.listener.ListenForJobUpdate;
import com.greencloud.application.agents.client.domain.ClientJobExecution;
import com.greencloud.application.agents.client.management.ClientManagement;
import com.greencloud.application.exception.IncorrectTaskDateException;
import com.greencloud.commons.args.job.JobArgs;
import com.greencloud.commons.domain.job.ImmutableJobStep;
import com.greencloud.commons.domain.job.JobStep;
import com.greencloud.commons.domain.resources.HardwareResources;
import com.greencloud.commons.domain.resources.ImmutableHardwareResources;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;

/**
 * Agent representing the Client that wants to have the job executed in the Cloud
 */
public class ClientAgent extends AbstractClientAgent {

	private static final Logger logger = getLogger(ClientAgent.class);

	@Override
	public void validateAgentArguments() {
		final Instant currentTime = getCurrentTimeMinusError();
		final Instant startTime = jobExecution.getJobSimulatedStart();
		final Instant endTime = jobExecution.getJobSimulatedEnd();
		final Instant deadline = jobExecution.getJobSimulatedDeadline();

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
		if (arguments.length == 7) {
			try {
				parentDFAddress = prepareDF(arguments[0].toString(), arguments[1].toString());

				final Instant startTime = convertToInstantTime(arguments[2].toString());
				final Instant endTime = convertToInstantTime(arguments[3].toString());
				final Instant deadline = convertToInstantTime(arguments[4].toString());
				final JobArgs jobArguments = (JobArgs) arguments[5];
				final String jobId = arguments[6].toString();
				initializeJob(startTime, endTime, deadline, jobArguments, jobId);

			} catch (IncorrectTaskDateException e) {
				logger.error(e.getMessage());
				doDelete();
			} catch (NumberFormatException e) {
				logger.error("The given power is not a number!");
				doDelete();
			}
		} else {
			logger.error("Incorrect arguments: some parameters for client's job are missing");
			doDelete();
		}
	}

	@Override
	protected void initializeAgentManagements() {
		this.agentManagementServices.put(CLIENT_MANAGEMENT, new ClientManagement(this));
	}

	@Override
	protected List<Behaviour> prepareStartingBehaviours() {
		final SequentialBehaviour startingBehaviour = new SequentialBehaviour(this);
		startingBehaviour.addSubBehaviour(new FindSchedulerAgent(this));
		startingBehaviour.addSubBehaviour(new InitiateNewJobAnnouncement(this));

		connectToGui(this);

		final ParallelBehaviour main = new ParallelBehaviour();
		main.addSubBehaviour(new ListenForJobUpdate(this));
		main.addSubBehaviour(startingBehaviour);
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

	private void initializeJob(final Instant startTime, final Instant endTime, final Instant deadline,
			final JobArgs jobArguments, final String jobId) {
		final Instant currentTime = getCurrentTime();
		final long expectedJobStart = convertToSimulationTime(SECONDS.between(currentTime, startTime));
		final long expectedJobEnd = convertToSimulationTime(SECONDS.between(currentTime, endTime));
		final long expectedJobDeadline = convertToSimulationTime(SECONDS.between(currentTime, deadline));

		final HardwareResources jobEstimatedResources = ImmutableHardwareResources.builder()
				.cpu((double) jobArguments.getCpu() / (jobArguments.getDuration() * 1000))
				.memory((double) jobArguments.getMemory() / (jobArguments.getDuration() * 1000))
				.storage((double) jobArguments.getStorage())
				.build();

		final List<JobStep> jobSteps = jobArguments.getJobSteps().stream()
				.map(step -> ImmutableJobStep.builder()
						.cpu(step.getCpu())
						.memory(step.getMemory())
						.duration(step.getDuration())
						.name(step.getName())
						.build())
				.map(JobStep.class::cast)
				.toList();

		jobExecution = new ClientJobExecution(
				getAID(),
				currentTime.plus(expectedJobStart, MILLIS),
				currentTime.plus(expectedJobEnd, MILLIS),
				currentTime.plus(expectedJobDeadline, MILLIS),
				jobEstimatedResources,
				jobSteps,
				jobId,
				jobArguments.processType()
		);
	}

	private void logClientSetUp() {
		MDC.put(MDC_JOB_ID, jobExecution.getJob().getJobId());
		logger.info("[{}] Job simulation time: from {} to {} (deadline: {}). Job type: {}. Required resources: {}",
				getName(),
				jobExecution.getJobSimulatedStart(), jobExecution.getJobSimulatedEnd(),
				jobExecution.getJobSimulatedDeadline(),
				jobExecution.getJobType(),
				jobExecution.getJob().getEstimatedResources());
	}
}
