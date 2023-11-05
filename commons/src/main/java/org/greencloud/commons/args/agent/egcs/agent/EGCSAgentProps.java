package org.greencloud.commons.args.agent.egcs.agent;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_DIVIDED;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_IS_STARTED;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_PREVIOUS;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_HOLD;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_TRANSFER;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_JOB_DIVISION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_JOB_SUBSTITUTION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobEndTimeAndInstanceId;
import static org.greencloud.commons.mapper.JobMapper.mapToJobStartTimeAndInstanceId;
import static org.greencloud.commons.mapper.JobMapper.mapToNewJobInstanceEndTime;
import static org.greencloud.commons.mapper.JobMapper.mapToNewJobInstanceStartTime;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.args.agent.AgentType;
import org.greencloud.commons.constants.FactTypeConstants;
import org.greencloud.commons.constants.LoggingConstants;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.PowerJob;
import org.greencloud.commons.domain.job.counter.JobCounter;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.job.transfer.ImmutableJobDivided;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.commons.enums.job.JobExecutionResultEnum;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.enums.rules.RuleType;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class extended by classes representing properties of individual agent types
 */
@Getter
@Setter
public class EGCSAgentProps extends AgentProps {

	private static final Logger logger = getLogger(EGCSAgentProps.class);

	protected final transient ConcurrentMap<JobExecutionResultEnum, JobCounter> jobCounters;
	protected AID parentDFAddress;

	/**
	 * Default constructor that sets the type of the agent
	 *
	 * @param agentType type of the agent
	 * @param agentName name of the agent
	 */
	public EGCSAgentProps(final AgentType agentType, final String agentName) {
		super(agentType, agentName);
		this.jobCounters = getJobCountersMap();
	}

	/**
	 * Method that builds job counters map (to be overridden)
	 */
	protected ConcurrentMap<JobExecutionResultEnum, JobCounter> getJobCountersMap() {
		return new ConcurrentHashMap<>();
	}

	/**
	 * Method used for incrementing the counters of the jobs
	 *
	 * @param jobInstance job identifier
	 * @param type        type of counter to increment
	 */
	public void incrementJobCounter(final JobInstanceIdentifier jobInstance, final JobExecutionResultEnum type) {
		final JobCounter counter = jobCounters.get(type);
		counter.count().getAndIncrement();

		MDC.put(LoggingConstants.MDC_JOB_ID, jobInstance.getJobId());
		counter.handler().accept(jobInstance);

		updateGUI();
	}

	/**
	 * Method retrieves list of agents excluding particular one
	 *
	 * @param agent           agent to exclude
	 * @param connectedAgents list of connected agents
	 * @return list of remaining agents AIDs
	 */
	public List<AID> getRemainingAgents(final AID agent, final Collection<AID> connectedAgents) {
		return connectedAgents.stream()
				.filter(server -> !server.equals(agent))
				.toList();
	}

	/**
	 * Method retrieves jobs which execution hasn't finished and their status is on hold
	 *
	 * @param jobMap map of jobs of interest
	 * @return list of jobs on hold
	 */
	public <T extends PowerJob> List<T> getActiveJobsOnHold(final ConcurrentMap<T, JobExecutionStatusEnum> jobMap) {
		return jobMap.entrySet().stream()
				.filter(job -> EXECUTING_ON_HOLD.getStatuses().contains(job.getValue()))
				.filter(job -> job.getKey().getEndTime().isAfter(getCurrentTime()))
				.map(Map.Entry::getKey)
				.toList();
	}

	/**
	 * <p/>
	 * Method creates new instances for given job with respect to given job transfer time.
	 * If the transfer time is set after the start of job execution -> job will be divided into 2 instances.
	 * <p/>
	 * Example:
	 * Job1 (start: 08:00, finish: 10:00)
	 * Transfer time: 09:00
	 *
	 * <p> Job1Instance1: (start: 08:00, finish: 09:00) <- job not affected by power shortage </p>
	 * <p> Job1Instance2: (start: 09:00, finish: 10:00) <- job affected by power shortage </p>
	 *
	 * @param job                job that is to be divided into instances
	 * @param powerShortageStart time when the power shortage will start
	 * @param jobMap             map of jobs of interest
	 * @return facts with Pair consisting of previous job instance and job instance for transfer (if there is only job instance
	 * for transfer then previous job instance element is null)
	 */
	public <T extends PowerJob> RuleSetFacts divideJobForPowerShortage(final T job, final Instant powerShortageStart,
			final ConcurrentMap<T, JobExecutionStatusEnum> jobMap, final RuleSetFacts facts,
			final ConcurrentMap<T, Integer> strategyForJob) {
		if (powerShortageStart.isAfter(job.getStartTime())) {
			final T affectedJobInstance = mapToNewJobInstanceStartTime(job, powerShortageStart);
			final T notAffectedJobInstance = mapToNewJobInstanceEndTime(job, powerShortageStart);

			handleJobDivisionInstanceSubstitution(job, affectedJobInstance, notAffectedJobInstance, jobMap,
					strategyForJob);

			final RuleSetFacts newFacts = new RuleSetFacts(facts.get(FactTypeConstants.RULE_SET_IDX));
			newFacts.put(JOB_PREVIOUS, job);
			newFacts.put(JOB_DIVIDED, new ImmutableJobDivided<>(notAffectedJobInstance, affectedJobInstance));
			newFacts.put(FactTypeConstants.RULE_TYPE, PROCESS_JOB_DIVISION_RULE);
			return newFacts;
		} else {
			jobMap.replace(job, EXECUTING_TRANSFER.getStatus(isJobStarted(job, jobMap)));
			updateGUI();

			final RuleSetFacts newFacts = new RuleSetFacts(facts.get(FactTypeConstants.RULE_SET_IDX));
			newFacts.put(JOB_DIVIDED, new ImmutableJobDivided<>(null, job));
			return newFacts;
		}
	}

	/**
	 * Method substitutes existing job instance with new instances associated with power shortage transfer
	 *
	 * @param jobTransfer job transfer information
	 * @param originalJob original job that is to be divided
	 * @param jobMap      map of jobs of interest
	 * @return facts with Pair of new job instances
	 */
	public <T extends PowerJob> RuleSetFacts divideJobForPowerShortage(final JobPowerShortageTransfer jobTransfer,
			final T originalJob, final ConcurrentMap<T, JobExecutionStatusEnum> jobMap, final RuleSetFacts facts,
			final ConcurrentMap<T, Integer> strategyForJob) {
		final JobInstanceIdentifier newJobInstanceId = jobTransfer.getSecondJobInstanceId();
		final JobInstanceIdentifier previousInstanceId = jobTransfer.getFirstJobInstanceId();

		MDC.put(LoggingConstants.MDC_JOB_ID, newJobInstanceId.getJobId());
		logger.info("Dividing jobs for original job: {}", originalJob.getJobInstanceId());

		if (isNull(previousInstanceId)) {
			final T newJobInstance = mapToJobStartTimeAndInstanceId(originalJob, newJobInstanceId);
			final boolean hasStarted = isJobStarted(originalJob, jobMap);
			final JobExecutionStatusEnum newStatus = EXECUTING_TRANSFER.getStatus(hasStarted);

			MDC.put(LoggingConstants.MDC_JOB_ID, newJobInstanceId.getJobId());
			logger.info("Current status: {}", newStatus);

			jobMap.remove(originalJob);
			jobMap.put(newJobInstance, newStatus);

			final RuleSetFacts newFacts = new RuleSetFacts(facts.get(FactTypeConstants.RULE_SET_IDX));
			newFacts.put(JOB_DIVIDED, new ImmutableJobDivided<>(null, newJobInstance));
			newFacts.put(JOB_IS_STARTED, hasStarted);
			newFacts.put(JOB, newJobInstance);
			newFacts.put(JOB_PREVIOUS, originalJob);
			newFacts.put(FactTypeConstants.RULE_TYPE, PROCESS_JOB_SUBSTITUTION_RULE);

			return newFacts;
		}
		final T nonAffectedInstance = mapToJobEndTimeAndInstanceId(originalJob, previousInstanceId.getJobInstanceId(),
				newJobInstanceId.getStartTime());
		final T affectedInstance = mapToJobStartTimeAndInstanceId(originalJob, newJobInstanceId);

		handleJobDivisionInstanceSubstitution(originalJob, affectedInstance, nonAffectedInstance, jobMap,
				strategyForJob);

		final RuleSetFacts newFacts = new RuleSetFacts(facts.get(FactTypeConstants.RULE_SET_IDX));
		newFacts.put(JOB_DIVIDED, new ImmutableJobDivided<>(nonAffectedInstance, affectedInstance));
		newFacts.put(JOB_PREVIOUS, originalJob);
		newFacts.put(FactTypeConstants.RULE_TYPE, PROCESS_JOB_DIVISION_RULE);
		return newFacts;
	}

	/**
	 * Method handles substituting given job instances with its partial instances
	 *
	 * @param job             original job instance
	 * @param nextJobInstance second part of job instance
	 * @param prevJobInstance first part of job instance
	 * @param jobMap          map of all jobs
	 */
	public <T extends PowerJob> void handleJobDivisionInstanceSubstitution(final T job, final T nextJobInstance,
			final T prevJobInstance, final ConcurrentMap<T, JobExecutionStatusEnum> jobMap,
			final ConcurrentMap<T, Integer> strategyForJob) {
		final JobExecutionStatusEnum currentJobStatus = jobMap.get(job);

		MDC.put(LoggingConstants.MDC_JOB_ID, nextJobInstance.getJobId());
		logger.info("Current status: {}", currentJobStatus);
		logger.info("Job before shortage: {} Job after shortage {}", prevJobInstance.getJobInstanceId(),
				nextJobInstance.getJobInstanceId());

		if (nonNull(strategyForJob)) {
			final int strategyForRemovedJob = strategyForJob.remove(job);
			strategyForJob.put(nextJobInstance, strategyForRemovedJob);
			strategyForJob.put(prevJobInstance, strategyForRemovedJob);
		}

		jobMap.remove(job);
		jobMap.put(nextJobInstance, JobExecutionStatusEnum.ON_HOLD_TRANSFER_PLANNED);
		jobMap.put(prevJobInstance, currentJobStatus);
	}

	/**
	 * Method constructs facts used to trigger division event
	 *
	 * @param jobTransfer job transfer information
	 * @param originalJob original job that is to be divided
	 * @return division facts
	 */
	public <T extends PowerJob> RuleSetFacts constructDivisionFacts(final JobPowerShortageTransfer jobTransfer,
			final T originalJob, final Integer strategyIdx) {
		final RuleSetFacts divisionFacts = new RuleSetFacts(strategyIdx);
		divisionFacts.put(FactTypeConstants.JOBS, jobTransfer);
		divisionFacts.put(JOB, originalJob);
		divisionFacts.put(FactTypeConstants.RULE_TYPE, RuleType.PROCESS_JOB_NEW_INSTANCE_CREATION_RULE);
		return divisionFacts;
	}

	/**
	 * Method constructs facts used to trigger division event
	 *
	 * @param job                job that is to be divided into instances
	 * @param powerShortageStart time when the server failure will start
	 * @return division facts
	 */
	public <T extends PowerJob> RuleSetFacts constructDivisionFacts(final T job, final Instant powerShortageStart,
			final Integer strategyIdx) {
		final RuleSetFacts divisionFacts = new RuleSetFacts(strategyIdx);
		divisionFacts.put(FactTypeConstants.EVENT_TIME, powerShortageStart);
		divisionFacts.put(JOB, job);
		divisionFacts.put(FactTypeConstants.RULE_TYPE, RuleType.PROCESS_JOB_NEW_INSTANCE_CREATION_RULE);
		return divisionFacts;
	}
}
