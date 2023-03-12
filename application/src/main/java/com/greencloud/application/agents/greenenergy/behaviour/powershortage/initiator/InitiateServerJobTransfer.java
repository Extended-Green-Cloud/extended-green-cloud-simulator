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
import static com.greencloud.application.utils.JobUtils.isJobStarted;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.ON_HOLD;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.ON_HOLD_PLANNED;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.FINISH;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.commons.domain.job.ServerJob;

import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour initiates the transfer of power jobs affected by the power shortage
 */
public class InitiateServerJobTransfer extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateServerJobTransfer.class);

	private final GreenEnergyAgent myGreenAgent;
	private final ServerJob jobToTransfer;

	/**
	 * Behaviours constructor
	 *
	 * @param agent           green source executing the behaviour
	 * @param transferRequest message with request for the power job transfer
	 * @param jobToTransfer   job that is requested to be transferred
	 */
	public InitiateServerJobTransfer(GreenEnergyAgent agent, ACLMessage transferRequest, ServerJob jobToTransfer) {
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
		logger.info(SOURCE_JOB_TRANSFER_PROCESSING_LOG, jobToTransfer.getServer().getLocalName(),
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
			logger.info(SOURCE_JOB_TRANSFER_REFUSE_NOT_FOUND_LOG, jobToTransfer);
			if (myGreenAgent.getServerJobs().containsKey(jobToTransfer)) {
				finishNonExistingJob();
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
		if (myGreenAgent.getServerJobs().containsKey(jobToTransfer)) {
			final String jobId = jobToTransfer.getJobId();
			logger.info(SOURCE_JOB_TRANSFER_SUCCESSFUL_LOG, jobId);

			if (isJobStarted(jobToTransfer, myGreenAgent.getServerJobs())) {
				myGreenAgent.manage().incrementJobCounter(mapToJobInstanceId(jobToTransfer), FINISH);
			}
			myGreenAgent.manage().removeJob(jobToTransfer);
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
		if (myGreenAgent.getServerJobs().containsKey(jobToTransfer) &&
				!cause.equals(JOB_NOT_FOUND_CAUSE_MESSAGE)) {
			final boolean hasJobStarted = isJobStarted(jobToTransfer, myGreenAgent.getServerJobs());
			logger.info(SOURCE_JOB_TRANSFER_FAILURE_LOG, jobToTransfer.getJobId());
			myGreenAgent.getServerJobs().replace(jobToTransfer, hasJobStarted ? ON_HOLD : ON_HOLD_PLANNED);
			myGreenAgent.manage().updateGreenSourceGUI();
		} else if (cause.equals(JOB_NOT_FOUND_CAUSE_MESSAGE)) {
			finishNonExistingJob();
		} else {
			logger.info(SOURCE_JOB_TRANSFER_FAILURE_NOT_FOUND_LOG, jobToTransfer.getJobId());
		}
	}

	private void finishNonExistingJob() {
		myGreenAgent.getServerJobs().entrySet()
				.removeIf(entry -> {
					if (entry.getKey().getJobId().equals(jobToTransfer.getJobId()) &&
							!entry.getKey().getStartTime().isAfter(jobToTransfer.getStartTime())) {
						if (isJobStarted(entry.getValue())) {
							myGreenAgent.manage().incrementJobCounter(mapToJobInstanceId(entry.getKey()), FINISH);
						}
						return true;
					}
					return false;
				});
		myGreenAgent.manage().updateGreenSourceGUI();
	}
}
