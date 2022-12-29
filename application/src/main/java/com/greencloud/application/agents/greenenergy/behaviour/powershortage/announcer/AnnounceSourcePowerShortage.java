package com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer;

import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_NO_IMPACT_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_TRANSFER_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_WEATHER_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.mapper.JobMapper.mapToPowerShortageJob;
import static com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory.preparePowerShortageTransferRequest;
import static com.greencloud.application.utils.AlgorithmUtils.findJobsWithinPower;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.commons.args.event.powershortage.PowerShortageCause.PHYSICAL_CAUSE;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ACCEPTED;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.IN_PROGRESS;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.handler.HandleSourcePowerShortage;
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.InitiateServerJobTransfer;
import com.greencloud.commons.args.event.powershortage.PowerShortageCause;
import com.greencloud.commons.job.ServerJob;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour sends the information to the server that the green source will have the power shortage
 * at the given time
 */
public class AnnounceSourcePowerShortage extends OneShotBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(AnnounceSourcePowerShortage.class);

	private final Instant shortageStartTime;
	private final ServerJob serverJobToInclude;
	private final Double maxAvailablePower;
	private final GreenEnergyAgent myGreenAgent;
	private final PowerShortageCause cause;

	/**
	 * Behaviour constructor
	 *
	 * @param myAgent            agent executing the behaviour* @param serverJobToInclude (optional) power job which must be included in transfer
	 * @param serverJobToInclude (optional) server job which must be included in transfer
	 * @param shortageStartTime  start time of the power shortage
	 * @param maxAvailablePower  power available during the power shortage
	 */
	public AnnounceSourcePowerShortage(GreenEnergyAgent myAgent, ServerJob serverJobToInclude,
			Instant shortageStartTime, Double maxAvailablePower, PowerShortageCause cause) {
		super(myAgent);
		this.shortageStartTime = shortageStartTime;
		this.maxAvailablePower = maxAvailablePower;
		this.myGreenAgent = myAgent;
		this.serverJobToInclude = serverJobToInclude;
		this.cause = cause;
	}

	/**
	 * Method sends the information about the detected power shortage to the parent server.
	 */
	@Override
	public void action() {
		logPowerShortageStart();
		final List<ServerJob> affectedJobs = getAffectedServerJobs();
		if (affectedJobs.isEmpty() && Objects.isNull(serverJobToInclude)) {
			handlePowerShortageWithoutTransfer();
		} else {
			final List<ServerJob> jobsToKeep = findJobsWithinPower(affectedJobs, maxAvailablePower);
			final List<ServerJob> jobsToTransfer = prepareJobsToTransfer(affectedJobs, jobsToKeep);

			if (jobsToTransfer.isEmpty()) {
				handlePowerShortageWithoutTransfer();
				return;
			}

			jobsToTransfer.forEach(serverJob -> {
				final ServerJob jobToTransfer = myGreenAgent.manage()
						.divideServerJobForPowerShortage(serverJob, shortageStartTime);
				MDC.put(MDC_JOB_ID, serverJob.getJobId());
				logger.info(POWER_SHORTAGE_SOURCE_START_TRANSFER_LOG, mapToJobInstanceId(jobToTransfer));
				requestJobTransferInServer(serverJob, jobToTransfer);
				myGreenAgent.manage().updateGreenSourceGUI();
			});
			initiatePowerShortageHandler(jobsToTransfer);
		}
	}

	private void handlePowerShortageWithoutTransfer() {
		logger.info(POWER_SHORTAGE_SOURCE_START_NO_IMPACT_LOG);
		initiatePowerShortageHandler(Collections.emptyList());
	}

	private void initiatePowerShortageHandler(final List<ServerJob> jobsToTransfer) {
		final Integer maximumCapacity = cause.equals(PHYSICAL_CAUSE) ? maxAvailablePower.intValue() : null;
		myGreenAgent.addBehaviour(
				HandleSourcePowerShortage.createFor(jobsToTransfer, shortageStartTime, maximumCapacity,
						myGreenAgent));
	}

	private void requestJobTransferInServer(final ServerJob originalJob, final ServerJob jobToTransfer) {
		final ACLMessage transferMessage = preparePowerShortageTransferRequest(
				mapToPowerShortageJob(originalJob, shortageStartTime), originalJob.getServer());

		myGreenAgent.addBehaviour(new InitiateServerJobTransfer(myGreenAgent, transferMessage, jobToTransfer));
	}

	private List<ServerJob> prepareJobsToTransfer(final List<ServerJob> affectedJobs,
			final List<ServerJob> jobsToKeep) {
		final List<ServerJob> jobsToTransfer = affectedJobs.stream()
				.filter(job -> !jobsToKeep.contains(job))
				.collect(Collectors.toCollection(ArrayList::new));
		if (Objects.nonNull(serverJobToInclude)) {
			jobsToTransfer.add(serverJobToInclude);
		}
		return jobsToTransfer;
	}

	private List<ServerJob> getAffectedServerJobs() {
		return myGreenAgent.getServerJobs().keySet().stream()
				.filter(job -> Objects.isNull(serverJobToInclude) || !job.equals(serverJobToInclude))
				.filter(job -> shortageStartTime.isBefore(convertToRealTime(job.getEndTime())) &&
						List.of(IN_PROGRESS, ACCEPTED).contains(myGreenAgent.getServerJobs().get(job)))
				.toList();
	}

	private void logPowerShortageStart() {
		final String logMessage = cause.equals(PHYSICAL_CAUSE) ? POWER_SHORTAGE_SOURCE_START_LOG :
				POWER_SHORTAGE_SOURCE_START_WEATHER_LOG;
		logger.info(logMessage, shortageStartTime, shortageStartTime);
	}
}
