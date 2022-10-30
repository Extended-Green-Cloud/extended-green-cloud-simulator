package com.greencloud.application.agents.server.behaviour.powershortage.handler;

import static com.greencloud.application.agents.server.behaviour.powershortage.handler.logs.PowerShortageServerHandlerLog.GS_TRANSFER_EXECUTION_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStartedMessage;
import static com.greencloud.application.utils.GUIUtils.displayMessageArrow;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobStatusEnum;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour transfers a job to a new green source
 */
public class HandleSourceJobTransfer extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleSourceJobTransfer.class);

	private final ServerAgent myServerAgent;
	private final JobInstanceIdentifier jobInstanceId;
	private final AID newGreenSource;

	/**
	 * Behaviour constructor.
	 *
	 * @param myAgent       agent executing the behaviour
	 * @param transferTime  time of the job transfer
	 * @param jobInstanceId unique identifier of the job instance
	 */
	private HandleSourceJobTransfer(Agent myAgent, Date transferTime, JobInstanceIdentifier jobInstanceId,
			AID newGreenSource) {
		super(myAgent, transferTime);
		this.myServerAgent = (ServerAgent) myAgent;
		this.jobInstanceId = jobInstanceId;
		this.newGreenSource = newGreenSource;
	}

	/**
	 * Method creating the behaviour
	 *
	 * @param serverAgent    server executing the behaviour
	 * @param jobInstanceId  unique identifier of the job instance
	 * @param newGreenSource green source which will execute the job after power shortage
	 * @return behaviour which transfer the jobs between green sources
	 */
	public static HandleSourceJobTransfer createFor(final ServerAgent serverAgent,
			final JobInstanceIdentifier jobInstanceId, final AID newGreenSource) {
		final Instant transferTime = getCurrentTime().isAfter(jobInstanceId.getStartTime()) ?
				getCurrentTime() :
				jobInstanceId.getStartTime();
		return new HandleSourceJobTransfer(serverAgent, Date.from(transferTime), jobInstanceId,
				newGreenSource);
	}

	/**
	 * Method transfers the job between green sources.
	 * It updates the internal server state.
	 */
	@Override
	protected void onWake() {
		final ClientJob jobToExecute = myServerAgent.manage().getJobByIdAndStartDate(jobInstanceId);
		if (Objects.nonNull(jobToExecute)) {
			MDC.put(MDC_JOB_ID, jobToExecute.getJobId());
			logger.info(GS_TRANSFER_EXECUTION_LOG);
			myServerAgent.getGreenSourceForJobMap().replace(jobToExecute.getJobId(), newGreenSource);
			myServerAgent.getServerJobs().replace(jobToExecute, JobStatusEnum.IN_PROGRESS);
			myServerAgent.manage().informCNAAboutStatusChange(jobInstanceId, GREEN_POWER_JOB_ID);
			startJobExecutionInNewGreenSource(jobToExecute);
		}
	}

	private void startJobExecutionInNewGreenSource(final ClientJob jobToExecute) {
		final ACLMessage startedJobMessage = prepareJobStartedMessage(jobToExecute.getJobId(),
				jobToExecute.getStartTime(), List.of(newGreenSource));
		displayMessageArrow(myServerAgent, newGreenSource);
		myAgent.send(startedJobMessage);
	}
}
