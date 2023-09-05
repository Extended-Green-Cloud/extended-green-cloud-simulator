package com.greencloud.application.agents.server.behaviour.errorhandling.handler;

import static com.greencloud.application.agents.server.behaviour.errorhandling.handler.logs.ErrorHandlingHandlerLog.GS_TRANSFER_EXECUTION_LOG;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static com.greencloud.application.messages.factory.JobStatusMessageFactory.prepareJobStartedMessage;
import static com.greencloud.application.utils.JobUtils.isJobStarted;
import static com.greencloud.application.utils.TimeUtils.alignStartTimeToCurrentTime;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.JobDivided;
import com.greencloud.commons.constants.LoggingConstant;
import com.greencloud.commons.domain.job.ClientJob;

import jade.core.AID;
import jade.core.behaviours.WakerBehaviour;

/**
 * Behaviour transfers a job to a new green source
 */
public class HandleSourceJobTransfer extends WakerBehaviour {

	private static final Logger logger = getLogger(HandleSourceJobTransfer.class);

	private final ServerAgent myServerAgent;
	private final JobDivided<ClientJob> newJobInstances;
	private final AID newGreenSource;

	private HandleSourceJobTransfer(final ServerAgent myAgent, final Date transferTime,
			final JobDivided<ClientJob> newJobInstances, final AID newGreenSource) {
		super(myAgent, transferTime);

		this.myServerAgent = myAgent;
		this.newJobInstances = newJobInstances;
		this.newGreenSource = newGreenSource;
	}

	/**
	 * Method creating the behaviour
	 *
	 * @param serverAgent     server executing the behaviour
	 * @param newJobInstances pair of job instances including previous job instance (first) and job to transfer
	 *                        instance (second)
	 * @param newGreenSource  green source which will execute the job after power shortage
	 * @return behaviour which transfer the jobs between green sources
	 */
	public static HandleSourceJobTransfer create(final ServerAgent serverAgent,
			final JobDivided<ClientJob> newJobInstances, final AID newGreenSource) {
		final Instant transferTime = alignStartTimeToCurrentTime(newJobInstances.getSecondInstance().getStartTime());
		return new HandleSourceJobTransfer(serverAgent, Date.from(transferTime), newJobInstances, newGreenSource);
	}

	/**
	 * Method transfers the job between green sources.
	 * It updates the internal server state.
	 */
	@Override
	protected void onWake() {
		final ClientJob jobToExecute = newJobInstances.getSecondInstance();

		MDC.put(LoggingConstant.MDC_JOB_ID, jobToExecute.getJobId());
		finishPreviousInstance();

		if (myServerAgent.getServerJobs().containsKey(jobToExecute)) {
			logger.info(GS_TRANSFER_EXECUTION_LOG);
			myServerAgent.getGreenSourceForJobMap().replace(jobToExecute.getJobId(), newGreenSource);
			myServerAgent.message().informCNAAboutStatusChange(mapToJobInstanceId(jobToExecute), GREEN_POWER_JOB_ID);
			myServerAgent.manage().updateGUI();

			if (isJobStarted(jobToExecute, myServerAgent.getServerJobs())) {
				myAgent.send(prepareJobStartedMessage(jobToExecute, newGreenSource));
			}
		}
	}

	private void finishPreviousInstance() {
		if (nonNull(newJobInstances.getFirstInstance()) && myServerAgent.getServerJobs()
				.containsKey(newJobInstances.getFirstInstance())) {
			myServerAgent.manage().finishJobExecution(newJobInstances.getFirstInstance(), false);
			myServerAgent.manage().updateGUI();
		}
	}
}
