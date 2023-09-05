package com.greencloud.application.agents.greenenergy.behaviour.powershortage.handler;

import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.handler.logs.PowerShortageSourceHandlerLog.POWER_SHORTAGE_HANDLING_PUT_ON_HOLD_LOG;
import static com.greencloud.application.utils.TimeUtils.alignStartTimeToCurrentTime;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.commons.domain.job.ServerJob;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

/**
 * Behaviour updates Green Source internal state in response to the power shortage
 */
public class HandleSourcePowerShortage extends WakerBehaviour {

	private static final Logger logger = getLogger(HandleSourcePowerShortage.class);
	private final GreenEnergyAgent myGreenEnergyAgent;
	private final List<ServerJob> jobsToHalt;
	private final boolean setError;

	private HandleSourcePowerShortage(final Agent myAgent, final Date powerShortageStart,
			final List<ServerJob> jobsToHalt, final boolean setError) {
		super(myAgent, powerShortageStart);

		this.myGreenEnergyAgent = (GreenEnergyAgent) myAgent;
		this.jobsToHalt = jobsToHalt;
		this.setError = setError;
	}

	/**
	 * Method creates the behaviour
	 *
	 * @param jobsToHalt        list of jobs which state should change upon power shortage
	 * @param shortageStartTime time when the power shortage begin
	 * @param agent             agent executing the behaviour
	 * @param setError          flag indicating if the error should be set for green source
	 * @return HandleSourcePowerShortage
	 */
	public static HandleSourcePowerShortage createFor(final List<ServerJob> jobsToHalt, final Instant shortageStartTime,
			final GreenEnergyAgent agent, final boolean setError) {
		final Instant startTime = alignStartTimeToCurrentTime(shortageStartTime);
		return new HandleSourcePowerShortage(agent, Date.from(startTime), jobsToHalt, setError);
	}

	/**
	 * Method updates the internal state of the green source when the power shortage happens.
	 * It changes the value of the maximum capacity
	 */
	@Override
	protected void onWake() {
		jobsToHalt.forEach(jobToHalt -> {
			if (myGreenEnergyAgent.getServerJobs().containsKey(jobToHalt)) {
				MDC.put(MDC_JOB_ID, jobToHalt.getJobId());
				logger.info(POWER_SHORTAGE_HANDLING_PUT_ON_HOLD_LOG, jobToHalt.getJobId());
			}
		});
		if (setError) {
			myGreenEnergyAgent.setHasError(true);
		}
	}
}
