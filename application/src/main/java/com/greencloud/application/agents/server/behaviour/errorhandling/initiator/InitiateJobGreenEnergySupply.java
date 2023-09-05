package com.greencloud.application.agents.server.behaviour.errorhandling.initiator;

import static com.greencloud.application.agents.server.behaviour.errorhandling.initiator.logs.ErrorHandlingInitiatorLog.SERVER_RE_SUPPLY_NOT_FOUND_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.initiator.logs.ErrorHandlingInitiatorLog.SERVER_RE_SUPPLY_REFUSE_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.initiator.logs.ErrorHandlingInitiatorLog.SERVER_RE_SUPPLY_REFUSE_NOT_FOUND_SERVER_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.initiator.logs.ErrorHandlingInitiatorLog.SERVER_RE_SUPPLY_SUCCESSFUL_LOG;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.messages.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static com.greencloud.application.messages.factory.NetworkErrorMessageFactory.prepareGreenPowerSupplyRequest;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_BACK_UP;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_GREEN;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_HOLD_SOURCE;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.isStatusActive;
import static java.util.Objects.nonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour sends request to Green Source to supply jobs which are ON_HOLD/IN_PROGRESS_BACK_UP with green energy
 */
public class InitiateJobGreenEnergySupply extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateJobGreenEnergySupply.class);

	private final ServerAgent myServerAgent;
	private final ClientJob jobToSupply;

	public InitiateJobGreenEnergySupply(final ServerAgent serverAgent, final ClientJob jobToSupply,
			final ACLMessage supplyRequest) {
		super(serverAgent, supplyRequest);

		this.myServerAgent = serverAgent;
		this.jobToSupply = jobToSupply;
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent       agent that is executing the behaviour
	 * @param greenSource green source that is asked for green power
	 * @param job         job of interest
	 * @return InitiateJobGreenEnergySupply
	 */
	public static InitiateJobGreenEnergySupply create(final ServerAgent agent, final AID greenSource,
			final ClientJob job) {
		final ACLMessage supplyRequest = prepareGreenPowerSupplyRequest(job, greenSource);
		return new InitiateJobGreenEnergySupply(agent, job, supplyRequest);
	}

	/**
	 * Method handles the information that green source successfully supplied job with green energy
	 *
	 * @param inform information retrieved from the Green Source
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		MDC.put(MDC_JOB_ID, jobToSupply.getJobId());
		logger.info(SERVER_RE_SUPPLY_SUCCESSFUL_LOG, jobToSupply.getJobId());

		if (myServerAgent.getServerJobs().containsKey(jobToSupply)) {
			final JobExecutionStatusEnum jobStatus = myServerAgent.getServerJobs().get(jobToSupply);
			final Boolean isActive = isStatusActive(jobStatus, EXECUTING_ON_HOLD_SOURCE, EXECUTING_ON_BACK_UP);

			if (nonNull(isActive)) {
				final JobExecutionStatusEnum newStatus = EXECUTING_ON_GREEN.getStatus(isActive);
				myServerAgent.getServerJobs().replace(jobToSupply, newStatus);
			}
			myServerAgent.manage().updateGUI();
		} else {
			logger.info(SERVER_RE_SUPPLY_REFUSE_NOT_FOUND_SERVER_LOG, jobToSupply.getJobId());
		}
	}

	/**
	 * Method handles the information that Green Source refused to supply job using green energy
	 *
	 * @param refuse retrieved refuse message
	 */
	@Override
	protected void handleRefuse(ACLMessage refuse) {
		final String cause = refuse.getContent();

		MDC.put(MDC_JOB_ID, jobToSupply.getJobId());
		if (nonNull(cause) && cause.equals(JOB_NOT_FOUND_CAUSE_MESSAGE)) {
			logger.info(SERVER_RE_SUPPLY_NOT_FOUND_LOG, jobToSupply.getJobId());
		} else {
			logger.info(SERVER_RE_SUPPLY_REFUSE_LOG, jobToSupply.getJobId());
		}
	}
}
