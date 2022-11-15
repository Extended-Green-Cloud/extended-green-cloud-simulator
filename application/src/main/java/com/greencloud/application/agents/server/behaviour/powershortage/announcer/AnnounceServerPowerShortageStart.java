package com.greencloud.application.agents.server.behaviour.powershortage.announcer;

import static com.greencloud.application.agents.server.behaviour.powershortage.announcer.logs.PowerShortageServerAnnouncerLog.POWER_SHORTAGE_START_DETECTED_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.announcer.logs.PowerShortageServerAnnouncerLog.POWER_SHORTAGE_START_NO_IMPACT_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.announcer.logs.PowerShortageServerAnnouncerLog.POWER_SHORTAGE_START_TRANSFER_REQUEST_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.ACTIVE_JOB_STATUSES;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_ALERT_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory.prepareJobPowerShortageInformation;
import static com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory.preparePowerShortageTransferRequest;
import static com.greencloud.application.utils.AlgorithmUtils.findJobsWithinPower;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.powershortage.handler.HandleServerPowerShortage;
import com.greencloud.application.agents.server.behaviour.powershortage.initiator.InitiateJobTransferInCloudNetwork;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.application.domain.powershortage.PowerShortageJob;
import com.greencloud.application.mapper.JobMapper;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour sends the information to the Cloud Network Agent (CNA) that the power shortage has occurred
 */
public class AnnounceServerPowerShortageStart extends OneShotBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(AnnounceServerPowerShortageStart.class);

	private final ServerAgent myServerAgent;
	private final Instant powerShortageStartTime;
	private final int recalculatedAvailablePower;

	/**
	 * Behaviour constructor
	 *
	 * @param myAgent                    agent executing the behaviour
	 * @param powerShortageStartTime     start time when the power shortage will begin
	 * @param recalculatedAvailablePower maximum power available during the power shortage
	 */
	public AnnounceServerPowerShortageStart(final ServerAgent myAgent, final Instant powerShortageStartTime,
			final int recalculatedAvailablePower) {
		super(myAgent);
		this.powerShortageStartTime = powerShortageStartTime;
		this.recalculatedAvailablePower = recalculatedAvailablePower;
		this.myServerAgent = myAgent;
	}

	/**
	 * Method is responsible for announcing to the cloud network that there will be some power shortage
	 * which cannot be handled by the server itself
	 */
	@Override
	public void action() {
		logger.info(POWER_SHORTAGE_START_DETECTED_LOG, powerShortageStartTime);
		final List<ClientJob> affectedJobs = getAffectedPowerJobs();

		if (affectedJobs.isEmpty()) {
			logger.info(POWER_SHORTAGE_START_NO_IMPACT_LOG);
			myServerAgent.addBehaviour(
					HandleServerPowerShortage.createFor(Collections.emptyList(), powerShortageStartTime, myServerAgent,
							recalculatedAvailablePower));
		} else {
			final List<ClientJob> jobsToKeep = findJobsWithinPower(affectedJobs, recalculatedAvailablePower);
			final List<ClientJob> jobsToTransfer = affectedJobs.stream().filter(job -> !jobsToKeep.contains(job))
					.toList();

			jobsToTransfer.forEach(job -> {
				MDC.put(MDC_JOB_ID, job.getJobId());
				logger.info(POWER_SHORTAGE_START_TRANSFER_REQUEST_LOG, job.getJobId());
				final ClientJob jobToTransfer = myServerAgent.manage()
						.divideJobForPowerShortage(job, powerShortageStartTime);
				final PowerShortageJob originalJobForShortage = JobMapper.mapToPowerShortageJob(job,
						powerShortageStartTime);
				final PowerShortageJob jobTransferForShortage = JobMapper.mapToPowerShortageJob(jobToTransfer,
						powerShortageStartTime);

				requestJobTransferInCloudNetwork(originalJobForShortage, jobTransferForShortage);
				informGreenSourceAboutPowerShortage(originalJobForShortage);
			});
			myServerAgent.addBehaviour(
					HandleServerPowerShortage.createFor(jobsToTransfer, powerShortageStartTime, myServerAgent,
							recalculatedAvailablePower));
		}
	}

	private void requestJobTransferInCloudNetwork(final PowerShortageJob originalJob,
			final PowerShortageJob jobToTransfer) {
		final AID cloudNetwork = myServerAgent.getOwnerCloudNetworkAgent();
		final ACLMessage transferMessage = preparePowerShortageTransferRequest(originalJob, cloudNetwork);
		final Behaviour transferRequest = new InitiateJobTransferInCloudNetwork(myServerAgent, transferMessage, null,
				jobToTransfer);

		myServerAgent.addBehaviour(transferRequest);
	}

	private void informGreenSourceAboutPowerShortage(final PowerShortageJob originalJob) {
		final AID greenSource = myServerAgent.getGreenSourceForJobMap().get(originalJob.getJobInstanceId().getJobId());
		final ACLMessage powerShortageInformation = prepareJobPowerShortageInformation(originalJob, greenSource,
				SERVER_POWER_SHORTAGE_ALERT_PROTOCOL);

		myServerAgent.send(powerShortageInformation);
	}

	private List<ClientJob> getAffectedPowerJobs() {
		return myServerAgent.getServerJobs().keySet().stream()
				.filter(job -> powerShortageStartTime.isBefore(job.getEndTime()) && ACTIVE_JOB_STATUSES.contains(
						myServerAgent.getServerJobs().get(job))).toList();
	}
}
