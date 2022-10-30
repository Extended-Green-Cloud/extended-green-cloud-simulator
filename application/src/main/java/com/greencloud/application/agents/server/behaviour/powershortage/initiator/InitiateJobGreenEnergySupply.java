package com.greencloud.application.agents.server.behaviour.powershortage.initiator;

import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.SERVER_RE_SUPPLY_FAILED_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.SERVER_RE_SUPPLY_JOB_PROCESSING_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.SERVER_RE_SUPPLY_NOT_FOUND_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.SERVER_RE_SUPPLY_REFUSE_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.SERVER_RE_SUPPLY_REFUSE_NOT_FOUND_SERVER_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.SERVER_RE_SUPPLY_SUCCESSFUL_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.ACCEPTED;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.mapper.JsonMapper.getMapper;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static com.greencloud.application.messages.domain.constants.PowerShortageMessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.exception.IncorrectMessageContentException;

import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour sends request to Green Source to supply jobs which are ON_HOLD/IN_PROGRESS_BACK_UP with green energy
 */
public class InitiateJobGreenEnergySupply extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateJobGreenEnergySupply.class);

	private final ServerAgent myServerAgent;
	private final ClientJob jobToSupply;

	/**
	 * Behaviour constructor
	 *
	 * @param jobToSupply   job which is meant to be supplied with green energy power
	 * @param supplyRequest request sent to specific green source
	 */
	public InitiateJobGreenEnergySupply(final ServerAgent serverAgent, final ClientJob jobToSupply,
			final ACLMessage supplyRequest) {
		super(serverAgent, supplyRequest);
		this.myServerAgent = serverAgent;
		this.jobToSupply = jobToSupply;
	}

	/**
	 * Method handles the information that green source will try to supply job again with green energy
	 *
	 * @param agree retrieved agree message
	 */
	@Override
	protected void handleAgree(ACLMessage agree) {
		MDC.put(MDC_JOB_ID, jobToSupply.getJobId());
		logger.info(SERVER_RE_SUPPLY_JOB_PROCESSING_LOG, jobToSupply.getJobId());
	}

	/**
	 * Method handles the information that green source refused to supply job using green energy
	 *
	 * @param refuse retrieved refuse message
	 */
	@Override
	protected void handleRefuse(ACLMessage refuse) {
		final String cause = getRefusalCause(refuse);
		MDC.put(MDC_JOB_ID, jobToSupply.getJobId());
		if (Objects.nonNull(cause) && cause.equals(JOB_NOT_FOUND_CAUSE_MESSAGE)) {
			logger.info(SERVER_RE_SUPPLY_NOT_FOUND_LOG, jobToSupply.getJobId());
		} else {
			logger.info(SERVER_RE_SUPPLY_REFUSE_LOG, jobToSupply.getJobId());
		}
	}

	/**
	 * Method handles the information that green source supplied job successfully with green energy
	 *
	 * @param inform information retrieved from the Green Source
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		MDC.put(MDC_JOB_ID, jobToSupply.getJobId());
		logger.info(SERVER_RE_SUPPLY_SUCCESSFUL_LOG, jobToSupply.getJobId());

		if (myServerAgent.getServerJobs().containsKey(jobToSupply)) {
			final JobStatusEnum jobStatus = myServerAgent.getServerJobs().get(jobToSupply);
			myServerAgent.getServerJobs().replace(jobToSupply, getNewJobStatus(jobStatus));
			myServerAgent.manage().updateServerGUI();
		} else {
			logger.info(SERVER_RE_SUPPLY_REFUSE_NOT_FOUND_SERVER_LOG, jobToSupply.getJobId());
		}
	}

	/**
	 * Method handles the information that the Green Source failed while supplying job with green energy
	 *
	 * @param failure retrieved message with failure cause
	 */
	@Override
	protected void handleFailure(ACLMessage failure) {
		MDC.put(MDC_JOB_ID, jobToSupply.getJobId());
		logger.info(SERVER_RE_SUPPLY_FAILED_LOG, jobToSupply.getJobId(), failure.getContent());
	}

	private JobStatusEnum getNewJobStatus(final JobStatusEnum previousStatus) {
		switch (previousStatus) {
			case ON_HOLD_SOURCE_SHORTAGE, IN_PROGRESS_BACKUP_ENERGY:
				myServerAgent.manage()
						.informCNAAboutStatusChange(mapToJobInstanceId(jobToSupply), GREEN_POWER_JOB_ID);
				return IN_PROGRESS;
			case ON_HOLD_SOURCE_SHORTAGE_PLANNED, IN_PROGRESS_BACKUP_ENERGY_PLANNED:
				return ACCEPTED;
			default:
				return null;
		}
	}

	private String getRefusalCause(final ACLMessage refuse) {
		try {
			return getMapper().readValue(refuse.getContent(), String.class);
		} catch (IncorrectMessageContentException | JsonProcessingException e) {
			return null;
		}
	}
}
