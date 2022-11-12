package com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer;

import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_NO_IMPACT_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_TRANSFER_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_WEATHER_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.ACCEPTED;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.application.domain.powershortage.PowerShortageCause.PHYSICAL_CAUSE;
import static com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory.preparePowerShortageTransferRequest;
import static com.greencloud.application.utils.AlgorithmUtils.findJobsWithinPower;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;

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
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.InitiatePowerJobTransfer;
import com.greencloud.application.domain.job.PowerJob;
import com.greencloud.application.domain.powershortage.PowerShortageCause;
import com.greencloud.application.mapper.JobMapper;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour sends the information to the server that the green source will have the power shortage
 * at the given time
 */
public class AnnounceSourcePowerShortage extends OneShotBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(AnnounceSourcePowerShortage.class);

	private final Instant shortageStartTime;
	private final PowerJob powerJobToInclude;
	private final Double maxAvailablePower;
	private final GreenEnergyAgent myGreenAgent;
	private final PowerShortageCause cause;

	/**
	 * Behaviour constructor
	 *
	 * @param myAgent           agent executing the behaviour* @param powerJobToInclude (optional) power job which must be included in transfer
	 * @param powerJobToInclude (optional) power job which must be included in transfer
	 * @param shortageStartTime start time of the power shortage
	 * @param maxAvailablePower power available during the power shortage
	 */
	public AnnounceSourcePowerShortage(GreenEnergyAgent myAgent, PowerJob powerJobToInclude,
			Instant shortageStartTime, Double maxAvailablePower, PowerShortageCause cause) {
		super(myAgent);
		this.shortageStartTime = shortageStartTime;
		this.maxAvailablePower = maxAvailablePower;
		this.myGreenAgent = myAgent;
		this.powerJobToInclude = powerJobToInclude;
		this.cause = cause;
	}

	/**
	 * Method sends the information about the detected power shortage to the parent server.
	 */
	@Override
	public void action() {
		logPowerShortageStart();
		final List<PowerJob> affectedJobs = getAffectedPowerJobs();
		if (affectedJobs.isEmpty() && Objects.isNull(powerJobToInclude)) {
			handlePowerShortageWithoutTransfer();
		} else {
			final List<PowerJob> jobsToKeep = findJobsWithinPower(affectedJobs, maxAvailablePower);
			final List<PowerJob> jobsToTransfer = prepareJobsToTransfer(affectedJobs, jobsToKeep);

			if (jobsToTransfer.isEmpty()) {
				handlePowerShortageWithoutTransfer();
				return;
			}

			jobsToTransfer.forEach(powerJob -> {
				MDC.put(MDC_JOB_ID, powerJob.getJobId());
				logger.info(POWER_SHORTAGE_SOURCE_START_TRANSFER_LOG, powerJob.getJobId());
				final PowerJob jobToTransfer = myGreenAgent.manage()
						.dividePowerJobForPowerShortage(powerJob, shortageStartTime);
				requestJobTransferInServer(powerJob, jobToTransfer);
				myGreenAgent.manage().updateGreenSourceGUI();
			});
			initiatePowerShortageHandler(jobsToTransfer);
		}
	}

	private void handlePowerShortageWithoutTransfer() {
		logger.info(POWER_SHORTAGE_SOURCE_START_NO_IMPACT_LOG);
		initiatePowerShortageHandler(Collections.emptyList());
	}

	private void initiatePowerShortageHandler(final List<PowerJob> jobsToTransfer) {
		final Integer maximumCapacity = cause.equals(PHYSICAL_CAUSE) ? maxAvailablePower.intValue() : null;
		myGreenAgent.addBehaviour(
				HandleSourcePowerShortage.createFor(jobsToTransfer, shortageStartTime, maximumCapacity,
						myGreenAgent));
	}

	private void requestJobTransferInServer(final PowerJob originalJob, final PowerJob jobToTransfer) {
		final ACLMessage transferMessage = preparePowerShortageTransferRequest(
				JobMapper.mapToPowerShortageJob(originalJob, shortageStartTime), myGreenAgent.getOwnerServer());

		myGreenAgent.addBehaviour(new InitiatePowerJobTransfer(myGreenAgent, transferMessage, jobToTransfer));
	}

	private List<PowerJob> prepareJobsToTransfer(final List<PowerJob> affectedJobs, final List<PowerJob> jobsToKeep) {
		final List<PowerJob> jobsToTransfer = affectedJobs.stream()
				.filter(job -> !jobsToKeep.contains(job))
				.collect(Collectors.toCollection(ArrayList::new));
		if (Objects.nonNull(powerJobToInclude)) {
			jobsToTransfer.add(powerJobToInclude);
		}
		return jobsToTransfer;
	}

	private List<PowerJob> getAffectedPowerJobs() {
		return myGreenAgent.getPowerJobs().keySet().stream()
				.filter(job -> Objects.isNull(powerJobToInclude) || !job.equals(powerJobToInclude))
				.filter(job -> shortageStartTime.isBefore(job.getEndTime()) &&
						List.of(IN_PROGRESS, ACCEPTED).contains(myGreenAgent.getPowerJobs().get(job)))
				.toList();
	}

	private void logPowerShortageStart() {
		final String logMessage = cause.equals(PHYSICAL_CAUSE) ? POWER_SHORTAGE_SOURCE_START_LOG :
				POWER_SHORTAGE_SOURCE_START_WEATHER_LOG;
		logger.info(logMessage, shortageStartTime, convertToRealTime(shortageStartTime));
	}
}
