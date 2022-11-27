package com.greencloud.application.agents.scheduler.behaviour.job.cancellation;

import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;

/**
 * Behaviour makes another attempt to cancel the remaining job parts
 */
public class RetryJobCancellation extends OneShotBehaviour {

	private final SchedulerAgent mySchedulerAgent;
	private final String originalJobId;
	private final Behaviour parentBehaviour;

	/**
	 * Behaviour constructor
	 *
	 * @param agent           agent executing the behaviour
	 * @param originalJobId   id of the job of interest
	 * @param parentBehaviour parent behaviour
	 */
	public RetryJobCancellation(SchedulerAgent agent, String originalJobId, Behaviour parentBehaviour) {
		super(agent);
		this.mySchedulerAgent = agent;
		this.originalJobId = originalJobId;
		this.parentBehaviour = parentBehaviour;
	}

	@Override
	public void action() {
		var jobCancellation = InitiateJobCancellation.build(mySchedulerAgent, originalJobId);
		((ParallelBehaviour) parentBehaviour).addSubBehaviour(jobCancellation);
		MDC.clear();
	}
}
