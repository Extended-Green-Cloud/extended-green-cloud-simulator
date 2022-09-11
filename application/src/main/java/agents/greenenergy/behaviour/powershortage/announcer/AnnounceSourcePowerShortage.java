package agents.greenenergy.behaviour.powershortage.announcer;

import static agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_LOG;
import static agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_NO_IMPACT_LOG;
import static agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_TRANSFER_LOG;
import static agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_WEATHER_LOG;
import static domain.powershortage.PowerShortageCause.PHYSICAL_CAUSE;
import static messages.domain.factory.PowerShortageMessageFactory.preparePowerShortageTransferRequest;
import static utils.AlgorithmUtils.findJobsWithinPower;
import static utils.GUIUtils.displayMessageArrow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.greenenergy.GreenEnergyAgent;
import agents.greenenergy.behaviour.powershortage.handler.HandleSourcePowerShortage;
import agents.greenenergy.behaviour.powershortage.initiator.InitiatePowerJobTransfer;
import mapper.JobMapper;
import domain.job.JobStatusEnum;
import domain.job.PowerJob;
import domain.powershortage.PowerShortageCause;
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
	private final String guid;
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
		this.guid = myAgent.getName();
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
			logger.info(POWER_SHORTAGE_SOURCE_START_NO_IMPACT_LOG, guid);
			initiatePowerShortageHandler(Collections.emptyList());
		} else {
			final List<PowerJob> jobsToKeep = findJobsWithinPower(affectedJobs, maxAvailablePower);
			final List<PowerJob> jobsToTransfer = prepareJobsToTransfer(affectedJobs, jobsToKeep);

			jobsToTransfer.forEach(powerJob -> {
				logger.info(POWER_SHORTAGE_SOURCE_START_TRANSFER_LOG, guid, powerJob.getJobId());
				final PowerJob jobToTransfer = myGreenAgent.manage()
						.dividePowerJobForPowerShortage(powerJob, shortageStartTime);
				requestJobTransferInServer(powerJob, jobToTransfer);
				myGreenAgent.manage().updateGreenSourceGUI();
			});
			initiatePowerShortageHandler(jobsToTransfer);
		}
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

		displayMessageArrow(myGreenAgent, myGreenAgent.getOwnerServer());
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
		final EnumSet<JobStatusEnum> notAffectedJobs = EnumSet.of(JobStatusEnum.PROCESSING, JobStatusEnum.ON_HOLD,
				JobStatusEnum.ON_HOLD_TRANSFER);
		return myGreenAgent.getPowerJobs().keySet().stream()
				.filter(job -> Objects.isNull(powerJobToInclude) || !job.equals(powerJobToInclude))
				.filter(job -> shortageStartTime.isBefore(job.getEndTime()) && !notAffectedJobs.contains(
						myGreenAgent.getPowerJobs().get(job)))
				.toList();
	}

	private void logPowerShortageStart() {
		final String logMessage = cause.equals(PHYSICAL_CAUSE) ?
				POWER_SHORTAGE_SOURCE_START_LOG :
				POWER_SHORTAGE_SOURCE_START_WEATHER_LOG;
		logger.info(logMessage, guid, shortageStartTime);
	}
}
