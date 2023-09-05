package com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator;

import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.logs.PowerShortageSourceInitiatorLog.SOURCE_JOB_TRANSFER_FAILURE_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.logs.PowerShortageSourceInitiatorLog.SOURCE_JOB_TRANSFER_FAILURE_NOT_FOUND_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.logs.PowerShortageSourceInitiatorLog.SOURCE_JOB_TRANSFER_REFUSE_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.logs.PowerShortageSourceInitiatorLog.SOURCE_JOB_TRANSFER_REFUSE_NOT_FOUND_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.logs.PowerShortageSourceInitiatorLog.SOURCE_JOB_TRANSFER_SUCCESSFUL_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.logs.PowerShortageSourceInitiatorLog.SOURCE_JOB_TRANSFER_SUCCESSFUL_NOT_FOUND_LOG;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.mapper.JobMapper.mapToPowerShortageJob;
import static com.greencloud.application.messages.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static com.greencloud.application.messages.factory.NetworkErrorMessageFactory.prepareJobTransferRequest;
import static com.greencloud.application.utils.JobUtils.isJobStarted;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.FINISH;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_HOLD;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.job.JobDivided;
import com.greencloud.application.domain.job.JobPowerShortageTransfer;
import com.greencloud.commons.domain.job.ServerJob;

import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour initiates the transfer of jobs affected by the power shortage in the given Green Source
 */
public class InitiateServerJobTransfer extends AchieveREInitiator {

	private static final Logger logger = getLogger(InitiateServerJobTransfer.class);

	private final GreenEnergyAgent myGreenAgent;
	private final ServerJob jobToTransfer;

	private InitiateServerJobTransfer(final GreenEnergyAgent agent, final ACLMessage transferRequest,
			final ServerJob jobToTransfer) {
		super(agent, transferRequest);

		this.myGreenAgent = agent;
		this.jobToTransfer = jobToTransfer;
	}

	/**
	 * Method creates the behaviour
	 *
	 * @param agent             agent executing the behaviour
	 * @param originalJob       non-divided job selected for transfer
	 * @param shortageStartTime time when the power shortage will start
	 * @return InitiateServerJobTransfer
	 */
	public static InitiateServerJobTransfer create(final GreenEnergyAgent agent, final ServerJob originalJob,
			final Instant shortageStartTime) {
		final JobDivided<ServerJob> newJobInstances = agent.manage()
				.divideJobForPowerShortage(originalJob, shortageStartTime);
		final JobPowerShortageTransfer transfer = mapToPowerShortageJob(originalJob.getJobInstanceId(), newJobInstances,
				shortageStartTime);
		final ACLMessage transferMessage = prepareJobTransferRequest(transfer, originalJob.getServer());

		return new InitiateServerJobTransfer(agent, transferMessage, newJobInstances.getSecondInstance());
	}

	/**
	 * Method handles the REFUSE response retrieved from Server informing that the job transfer will not be established.
	 *
	 * @param refuse retrieved response
	 */
	@Override
	protected void handleRefuse(ACLMessage refuse) {
		final String messageContent = refuse.getContent();

		MDC.put(MDC_JOB_ID, jobToTransfer.getJobId());
		logger.info(SOURCE_JOB_TRANSFER_REFUSE_LOG, jobToTransfer.getJobId());

		if (messageContent.equals(JOB_NOT_FOUND_CAUSE_MESSAGE)) {
			logger.info(SOURCE_JOB_TRANSFER_REFUSE_NOT_FOUND_LOG, jobToTransfer);

			if (myGreenAgent.getServerJobs().containsKey(jobToTransfer)) {
				finishNonExistingJob();
			}

		} else if (myGreenAgent.getServerJobs().containsKey(jobToTransfer)) {
			final boolean hasJobStarted = isJobStarted(jobToTransfer, myGreenAgent.getServerJobs());
			logger.info(SOURCE_JOB_TRANSFER_FAILURE_LOG, jobToTransfer.getJobId());

			myGreenAgent.getServerJobs().replace(jobToTransfer, EXECUTING_ON_HOLD.getStatus(hasJobStarted));
			myGreenAgent.manage().updateGUI();
		} else {
			logger.info(SOURCE_JOB_TRANSFER_FAILURE_NOT_FOUND_LOG, jobToTransfer.getJobId());
		}
	}

	/**
	 * Method handles the INFORM response retrieved from Server informing that the job transfer was established
	 * successfully.
	 * It finishes the job instance that will be executed by another server.
	 *
	 * @param inform retrieved response
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		MDC.put(MDC_JOB_ID, jobToTransfer.getJobId());
		if (myGreenAgent.getServerJobs().containsKey(jobToTransfer)) {
			final String jobId = jobToTransfer.getJobId();
			logger.info(SOURCE_JOB_TRANSFER_SUCCESSFUL_LOG, jobId);

			if (isJobStarted(jobToTransfer, myGreenAgent.getServerJobs())) {
				myGreenAgent.manage().incrementJobCounter(mapToJobInstanceId(jobToTransfer), FINISH);
			}
			myGreenAgent.manage().removeJob(jobToTransfer);
			myGreenAgent.manage().updateGUI();
		} else {
			logger.info(SOURCE_JOB_TRANSFER_SUCCESSFUL_NOT_FOUND_LOG, jobToTransfer.getJobId());
		}
	}

	private void finishNonExistingJob() {
		myGreenAgent.getServerJobs().entrySet().removeIf(entry -> {
			final ServerJob job = entry.getKey();
			final String jobId = job.getJobId();

			if (jobId.equals(jobToTransfer.getJobId()) && !job.getStartTime().isAfter(jobToTransfer.getStartTime())) {
				if (isJobStarted(entry.getValue())) {
					myGreenAgent.manage().incrementJobCounter(mapToJobInstanceId(jobToTransfer), FINISH);
				}
				return true;
			}
			return false;
		});
		myGreenAgent.manage().updateGUI();
	}
}
