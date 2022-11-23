package com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator;

import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.logs.PowerShortageSourceInitiatorLog.SOURCE_JOB_TRANSFER_FAILURE_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.logs.PowerShortageSourceInitiatorLog.SOURCE_JOB_TRANSFER_FAILURE_NOT_FOUND_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.logs.PowerShortageSourceInitiatorLog.SOURCE_JOB_TRANSFER_PROCESSING_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.logs.PowerShortageSourceInitiatorLog.SOURCE_JOB_TRANSFER_REFUSE_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.logs.PowerShortageSourceInitiatorLog.SOURCE_JOB_TRANSFER_REFUSE_NOT_FOUND_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.logs.PowerShortageSourceInitiatorLog.SOURCE_JOB_TRANSFER_SUCCESSFUL_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.logs.PowerShortageSourceInitiatorLog.SOURCE_JOB_TRANSFER_SUCCESSFUL_NOT_FOUND_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.domain.constants.PowerShortageMessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.commons.job.ExecutionJobStatusEnum;
import com.greencloud.commons.job.PowerJob;

import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour initiates the transfer of power jobs affected by the power shortage
 */
public class InitiatePowerJobTransfer extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiatePowerJobTransfer.class);

	private final GreenEnergyAgent myGreenAgent;
	private final PowerJob jobToTransfer;

	/**
	 * Behaviours constructor
	 *
	 * @param agent           green source executing the behaviour
	 * @param transferRequest message with request for the power job transfer
	 * @param jobToTransfer   job that is requested to be transferred
	 */
	public InitiatePowerJobTransfer(GreenEnergyAgent agent, ACLMessage transferRequest, PowerJob jobToTransfer) {
		super(agent, transferRequest);
		this.myGreenAgent = agent;
		this.jobToTransfer = jobToTransfer;
	}

	/**
	 * Method handles the AGREE response retrieved from Server informing that the job transfer will
	 * be processed.
	 *
	 * @param agree retrieved response
	 */
	@Override
	protected void handleAgree(ACLMessage agree) {
		MDC.put(MDC_JOB_ID, jobToTransfer.getJobId());
		logger.info(SOURCE_JOB_TRANSFER_PROCESSING_LOG, myGreenAgent.getOwnerServer().getLocalName(),
				jobToTransfer.getJobId());
	}

	/**
	 * Method handles the REFUSE response retrieved from Server informing that the job transfer
	 * won't be processed.
	 *
	 * @param refuse retrieved response
	 */
	@Override
	protected void handleRefuse(ACLMessage refuse) {
		final String messageContent = refuse.getContent();
		MDC.put(MDC_JOB_ID, jobToTransfer.getJobId());
		logger.info(SOURCE_JOB_TRANSFER_REFUSE_LOG, jobToTransfer.getJobId());
		if (messageContent.equals(JOB_NOT_FOUND_CAUSE_MESSAGE)) {
			logger.info(SOURCE_JOB_TRANSFER_REFUSE_NOT_FOUND_LOG, jobToTransfer.getJobId());
			if (myGreenAgent.getPowerJobs().containsKey(jobToTransfer)) {
				finishNonExistingJob(false);
			}
		}
	}

	/**
	 * Method handles the INFORM response retrieved from Server informing that the job transfer was established
	 * successfully.
	 * It finished the job instance that will be executed by another server.
	 *
	 * @param inform retrieved response
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		MDC.put(MDC_JOB_ID, jobToTransfer.getJobId());
		if (myGreenAgent.getPowerJobs().containsKey(jobToTransfer)) {
			final String jobId = jobToTransfer.getJobId();
			logger.info(SOURCE_JOB_TRANSFER_SUCCESSFUL_LOG, jobId);

			if (jobToTransfer.getStartTime().isBefore(getCurrentTime())) {
				myGreenAgent.manage().incrementFinishedJobs(mapToJobInstanceId(jobToTransfer));
			}
			myGreenAgent.getPowerJobs().remove(jobToTransfer);
			myGreenAgent.manage().updateGreenSourceGUI();
		} else {
			logger.info(SOURCE_JOB_TRANSFER_SUCCESSFUL_NOT_FOUND_LOG, jobToTransfer.getJobId());
		}
	}

	/**
	 * Method handles the FAILURE response retrieved from Server informing that the job transfer was established
	 * unsuccessfully.
	 * It then updates the state of the given job to ON_HOLD
	 *
	 * @param failure retrieved response
	 */
	@Override
	protected void handleFailure(ACLMessage failure) {
		final String cause = failure.getContent();
		MDC.put(MDC_JOB_ID, jobToTransfer.getJobId());
		if (myGreenAgent.getPowerJobs().containsKey(jobToTransfer) &&
				!cause.equals(JOB_NOT_FOUND_CAUSE_MESSAGE)) {
			final boolean hasJobStarted = !jobToTransfer.getStartTime().isAfter(getCurrentTime());
			logger.info(SOURCE_JOB_TRANSFER_FAILURE_LOG, jobToTransfer.getJobId());
			myGreenAgent.getPowerJobs()
					.replace(jobToTransfer, hasJobStarted ? ExecutionJobStatusEnum.ON_HOLD : ExecutionJobStatusEnum.ON_HOLD_PLANNED);
			myGreenAgent.manage().updateGreenSourceGUI();
		} else if (cause.equals(JOB_NOT_FOUND_CAUSE_MESSAGE)) {
			finishNonExistingJob(true);
		} else {
			logger.info(SOURCE_JOB_TRANSFER_FAILURE_NOT_FOUND_LOG, jobToTransfer.getJobId());
		}
	}

	private void finishNonExistingJob(final boolean incrementFinishCounter) {
		myGreenAgent.getPowerJobs().entrySet()
				.removeIf(entry -> {
					if (entry.getKey().getStartTime().isBefore(jobToTransfer.getStartTime())) {
						if (incrementFinishCounter && entry.getKey().getStartTime().isBefore(getCurrentTime())) {
							myGreenAgent.manage().incrementFinishedJobs(mapToJobInstanceId(entry.getKey()));
						}
						return true;
					}
					return false;
				});
		myGreenAgent.manage().updateGreenSourceGUI();
	}
}
