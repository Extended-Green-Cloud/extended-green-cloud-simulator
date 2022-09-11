package agents.server.behaviour.powershortage.handler;

import static agents.server.behaviour.powershortage.handler.logs.PowerShortageServerHandlerLog.POWER_SHORTAGE_HANDLE_JOB_ON_BACKUP_LOG;
import static agents.server.behaviour.powershortage.handler.logs.PowerShortageServerHandlerLog.POWER_SHORTAGE_HANDLE_JOB_ON_HOLD_LOG;
import static agents.server.behaviour.powershortage.handler.logs.PowerShortageServerHandlerLog.POWER_SHORTAGE_HANDLE_JOB_ON_HOLD_TEMPORARY_LOG;
import static utils.TimeUtils.getCurrentTime;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.server.ServerAgent;
import domain.job.ClientJob;
import domain.job.JobStatusEnum;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

/**
 * Behaviour updates the server state upon power shortage
 */
public class HandleServerPowerShortage extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleServerPowerShortage.class);

	private final ServerAgent myServerAgent;
	private final String guid;
	private final List<ClientJob> affectedJobs;
	private final Integer newMaximumCapacity;

	/**
	 * Behaviour constructor.
	 *
	 * @param myAgent            agent executing the behaviour
	 * @param shortageTime       time when the power shortage starts
	 * @param affectedJobs       list of the jobs to be finished
	 * @param newMaximumCapacity maximum capacity value available during power shortage
	 *                           (if null then it means that shortage does not concern server directly)
	 */
	private HandleServerPowerShortage(Agent myAgent, Date shortageTime, List<ClientJob> affectedJobs,
			final Integer newMaximumCapacity) {
		super(myAgent, shortageTime);
		this.myServerAgent = (ServerAgent) myAgent;
		this.guid = myAgent.getName();
		this.affectedJobs = affectedJobs;
		this.newMaximumCapacity = newMaximumCapacity;
	}

	/**
	 * Method creates the behaviour based on the passed parameters
	 *
	 * @param serverAgent     agent executing the behaviour
	 * @param affectedJobs    list of the jobs affected by power shortage
	 * @param newMaximumPower maximum power value during power shortage
	 * @return behaviour scheduling the power shortage handling
	 */
	public static HandleServerPowerShortage createFor(final List<ClientJob> affectedJobs,
			final Instant shortageStartTime, final ServerAgent serverAgent, final Integer newMaximumPower) {
		final Instant startTime = getCurrentTime().isAfter(shortageStartTime) ?
				getCurrentTime() :
				shortageStartTime;
		return new HandleServerPowerShortage(serverAgent, Date.from(startTime), affectedJobs,
				newMaximumPower);
	}

	/**
	 * Method is responsible for logging the information about the job status during power shortage.
	 * It updates also the maximum capacity of given server during the power shortage.
	 **/
	@Override
	protected void onWake() {
		affectedJobs.forEach(job -> {
			if (myServerAgent.getServerJobs().containsKey(job)) {
				final JobStatusEnum jobStatus = myServerAgent.getServerJobs().get(job);
				final String jobId = job.getJobId();

				switch (jobStatus) {
					case ON_HOLD_TRANSFER -> logger.info(POWER_SHORTAGE_HANDLE_JOB_ON_HOLD_TEMPORARY_LOG, guid, jobId);
					case IN_PROGRESS_BACKUP_ENERGY -> logger.info(POWER_SHORTAGE_HANDLE_JOB_ON_BACKUP_LOG, guid, jobId);
					default -> logger.info(POWER_SHORTAGE_HANDLE_JOB_ON_HOLD_LOG, guid, jobId);
				}
				myServerAgent.manage().updateServerGUI();
			}
		});
		if (Objects.nonNull(newMaximumCapacity)) {
			myServerAgent.manage().updateMaximumCapacity(newMaximumCapacity);
		}
	}
}
