package com.greencloud.application.agents.server.behaviour.errorhandling.announcer;

import static com.greencloud.application.agents.server.behaviour.errorhandling.announcer.logs.ErrorHandlingAnnouncerLog.INTERNAL_SERVER_ERROR_START_DETECTED_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.announcer.logs.ErrorHandlingAnnouncerLog.INTERNAL_SERVER_ERROR_START_NO_IMPACT_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.announcer.logs.ErrorHandlingAnnouncerLog.INTERNAL_SERVER_ERROR_START_TRANSFER_REQUEST_LOG;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.mapper.JobMapper.mapToPowerShortageJob;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.INTERNAL_SERVER_ERROR_ALERT_PROTOCOL;
import static com.greencloud.application.messages.factory.NetworkErrorMessageFactory.prepareNetworkFailureInformation;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.ACTIVE_JOB_STATUSES;
import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.errorhandling.handler.HandleInternalServerError;
import com.greencloud.application.agents.server.behaviour.errorhandling.initiator.InitiateJobTransferInCloudNetwork;
import com.greencloud.application.domain.job.JobDivided;
import com.greencloud.application.domain.job.JobPowerShortageTransfer;
import com.greencloud.commons.domain.job.ClientJob;
import com.gui.event.domain.PowerShortageEvent;

import jade.core.behaviours.OneShotBehaviour;

/**
 * Behaviour sends the information to the Cloud Network Agent that the internal server error event has occurred
 */
public class AnnounceInternalServerErrorStart extends OneShotBehaviour {

	private static final Logger logger = getLogger(AnnounceInternalServerErrorStart.class);

	private final ServerAgent myServerAgent;
	private final Instant startTime;

	/**
	 * Behaviour constructor
	 *
	 * @param myAgent       agent executing the behaviour
	 * @param powerShortage power shortage event that was detected
	 */
	public AnnounceInternalServerErrorStart(final ServerAgent myAgent, final PowerShortageEvent powerShortage) {
		super(myAgent);

		this.startTime = powerShortage.getOccurrenceTime();
		this.myServerAgent = myAgent;
	}

	/**
	 * Method is responsible for announcing to the cloud network that there will be some power shortage
	 * which cannot be handled by the server itself
	 */
	@Override
	public void action() {
		logger.info(INTERNAL_SERVER_ERROR_START_DETECTED_LOG, startTime);
		final List<ClientJob> affectedJobs = getAffectedPowerJobs();

		if (affectedJobs.isEmpty()) {
			logger.info(INTERNAL_SERVER_ERROR_START_NO_IMPACT_LOG);
			myServerAgent.addBehaviour(
					HandleInternalServerError.createFor(emptyList(), startTime, myServerAgent, true));
			return;
		}

		affectedJobs.forEach(job -> {
			MDC.put(MDC_JOB_ID, job.getJobId());
			logger.info(INTERNAL_SERVER_ERROR_START_TRANSFER_REQUEST_LOG, job.getJobId());

			final JobDivided<ClientJob> instances = myServerAgent.manage().divideJobForTransfer(job, startTime);
			final JobPowerShortageTransfer jobTransfer = mapToPowerShortageJob(job.getJobInstanceId(), instances,
					startTime);

			myServerAgent.addBehaviour(InitiateJobTransferInCloudNetwork.create(myServerAgent, jobTransfer,
					mapToJobInstanceId(instances.getSecondInstance()), null));
			myServerAgent.send(prepareNetworkFailureInformation(jobTransfer, INTERNAL_SERVER_ERROR_ALERT_PROTOCOL,
					myServerAgent.getGreenSourceForJobMap().get(job.getJobId())));
		});
		myServerAgent.addBehaviour(HandleInternalServerError.createFor(affectedJobs, startTime, myServerAgent, true));

	}

	private List<ClientJob> getAffectedPowerJobs() {
		return myServerAgent.getServerJobs().keySet().stream()
				.filter(job -> startTime.isBefore(job.getEndTime()))
				.filter(job -> ACTIVE_JOB_STATUSES.contains(myServerAgent.getServerJobs().get(job)))
				.toList();
	}
}
