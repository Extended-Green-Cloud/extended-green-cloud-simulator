package com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer;

import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_NO_IMPACT_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_TRANSFER_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_START_WEATHER_LOG;
import static com.greencloud.application.utils.AlgorithmUtils.findJobsWithinPower;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.commons.args.event.powershortage.PowerShortageCause.PHYSICAL_CAUSE;
import static com.greencloud.commons.args.event.powershortage.PowerShortageCause.WEATHER_CAUSE;
import static com.greencloud.commons.constants.LoggingConstant.MDC_AGENT_NAME;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_GREEN;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.handler.HandleSourcePowerShortage;
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.initiator.InitiateServerJobTransfer;
import com.greencloud.commons.args.event.powershortage.PowerShortageCause;
import com.greencloud.commons.domain.job.ServerJob;

import jade.core.behaviours.OneShotBehaviour;

/**
 * Behaviour sends the information to the server that the green source will have the power shortage
 * at the given time
 */
public class AnnounceSourcePowerShortage extends OneShotBehaviour {

	private static final Logger logger = getLogger(AnnounceSourcePowerShortage.class);

	private final Instant shortageStartTime;
	private final ServerJob serverJobToInclude;
	private final Double maxAvailablePower;
	private final GreenEnergyAgent myGreenAgent;
	private final PowerShortageCause cause;

	/**
	 * Behaviour constructor
	 *
	 * @param myAgent            agent executing the behaviour
	 * @param serverJobToInclude (optional) server job which must be included in transfer
	 * @param shortageStartTime  start time of the power shortage
	 * @param maxAvailablePower  power available during the power shortage
	 */
	public AnnounceSourcePowerShortage(final GreenEnergyAgent myAgent, final ServerJob serverJobToInclude,
			final Instant shortageStartTime, final Double maxAvailablePower, final PowerShortageCause cause) {
		super(myAgent);
		this.shortageStartTime = shortageStartTime;
		this.maxAvailablePower = maxAvailablePower;
		this.myGreenAgent = myAgent;
		this.serverJobToInclude = serverJobToInclude;
		this.cause = cause;

		if (cause.equals(WEATHER_CAUSE)) {
			myGreenAgent.manage().getWeatherShortagesCounter().getAndIncrement();
		}
	}

	/**
	 * Method sends the information about the detected power shortage to the parent server.
	 */
	@Override
	public void action() {
		final String logMessage = cause.equals(PHYSICAL_CAUSE) ?
				POWER_SHORTAGE_SOURCE_START_LOG :
				POWER_SHORTAGE_SOURCE_START_WEATHER_LOG;
		logger.info(logMessage, shortageStartTime);

		final List<ServerJob> affectedJobs = getAffectedServerJobs();

		if (affectedJobs.isEmpty() && isNull(serverJobToInclude)) {
			handlePowerShortageWithoutTransfer();
		} else {
			final List<ServerJob> jobsToKeep = findJobsWithinPower(affectedJobs, maxAvailablePower);
			final List<ServerJob> jobsToTransfer = prepareJobTransfer(affectedJobs, jobsToKeep);

			if (jobsToTransfer.isEmpty()) {
				logger.info(POWER_SHORTAGE_SOURCE_START_NO_IMPACT_LOG);
				initiatePowerShortageHandler(emptyList());
				return;
			}

			jobsToTransfer.stream().parallel().forEach(serverJob -> {
				MDC.put(MDC_AGENT_NAME, myAgent.getLocalName());
				MDC.put(MDC_JOB_ID, serverJob.getJobId());
				logger.info(POWER_SHORTAGE_SOURCE_START_TRANSFER_LOG, serverJob.getJobId());

				myGreenAgent.addBehaviour(InitiateServerJobTransfer.create(myGreenAgent, serverJob, shortageStartTime));
				myGreenAgent.manage().updateGUI();
			});
			initiatePowerShortageHandler(jobsToTransfer);
		}
	}

	private void handlePowerShortageWithoutTransfer() {
		logger.info(POWER_SHORTAGE_SOURCE_START_NO_IMPACT_LOG);
		initiatePowerShortageHandler(emptyList());
	}

	private void initiatePowerShortageHandler(final List<ServerJob> jobsToTransfer) {
		myGreenAgent.addBehaviour(HandleSourcePowerShortage.createFor(jobsToTransfer, shortageStartTime, myGreenAgent,
				cause.equals(PHYSICAL_CAUSE)));
	}

	private List<ServerJob> prepareJobTransfer(final List<ServerJob> affectedJobs, final List<ServerJob> jobsToKeep) {
		final List<ServerJob> jobsToTransfer = new ArrayList<>(affectedJobs.stream()
				.filter(job -> !jobsToKeep.contains(job))
				.toList());

		if (nonNull(serverJobToInclude)) {
			jobsToTransfer.add(serverJobToInclude);
		}
		return jobsToTransfer;
	}

	private List<ServerJob> getAffectedServerJobs() {
		return myGreenAgent.getServerJobs().keySet().stream()
				.filter(job -> isNull(serverJobToInclude) || !job.equals(serverJobToInclude))
				.filter(job -> shortageStartTime.isBefore(convertToRealTime(job.getEndTime())))
				.filter(job -> EXECUTING_ON_GREEN.getStatuses().contains(myGreenAgent.getServerJobs().get(job)))
				.toList();
	}
}
