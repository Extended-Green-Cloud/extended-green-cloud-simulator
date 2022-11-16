package com.greencloud.application.agents.greenenergy.behaviour.powershortage.handler;

import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.handler.logs.PowerShortageSourceHandlerLog.POWER_SHORTAGE_HANDLING_PUT_ON_HOLD_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.commons.job.PowerJob;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

/**
 * Behaviour schedules when the green source should update its internal state due to the power shortage
 */
public class HandleSourcePowerShortage extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleSourcePowerShortage.class);
	private final GreenEnergyAgent myGreenEnergyAgent;
	private final List<PowerJob> jobsToHalt;
	private final Integer newMaximumCapacity;

	/**
	 * Behaviour constructor.
	 *
	 * @param myAgent            agent executing the behaviour
	 * @param powerShortageStart time when the power shortage starts
	 * @param jobsToHalt         list of the jobs to be halted
	 */
	private HandleSourcePowerShortage(Agent myAgent, Date powerShortageStart, List<PowerJob> jobsToHalt,
			Integer newMaximumCapacity) {
		super(myAgent, powerShortageStart);
		this.myGreenEnergyAgent = (GreenEnergyAgent) myAgent;
		this.jobsToHalt = jobsToHalt;
		this.newMaximumCapacity = newMaximumCapacity;
	}

	/**
	 * Method creates the behaviour based on the passed parameters
	 *
	 * @param jobsToHalt         list of jobs which state should change upon power shortage
	 * @param shortageStartTime  time when the power shortage begin
	 * @param newMaximumCapacity updated capacity
	 * @param greenEnergyAgent   agent executing the behaviour
	 * @return behaviour scheduling the power shortage handling
	 */
	public static HandleSourcePowerShortage createFor(final List<PowerJob> jobsToHalt,
			final Instant shortageStartTime, final Integer newMaximumCapacity,
			final GreenEnergyAgent greenEnergyAgent) {
		final Instant startTime = getCurrentTime().isAfter(shortageStartTime) ?
				getCurrentTime() :
				shortageStartTime;
		return new HandleSourcePowerShortage(greenEnergyAgent, Date.from(startTime), jobsToHalt,
				newMaximumCapacity);
	}

	/**
	 * Method updates the internal state of the green source when the power shortage happens.
	 * It changes the value of the maximum capacity
	 */
	@Override
	protected void onWake() {
		jobsToHalt.forEach(jobToHalt -> {
			if (myGreenEnergyAgent.getPowerJobs().containsKey(jobToHalt)) {
				MDC.put(MDC_JOB_ID, jobToHalt.getJobId());
				logger.info(POWER_SHORTAGE_HANDLING_PUT_ON_HOLD_LOG, jobToHalt.getJobId());
			}
		});
		if (Objects.nonNull(newMaximumCapacity)) {
			myGreenEnergyAgent.manage().updateMaximumCapacity(newMaximumCapacity);
		}
	}
}
