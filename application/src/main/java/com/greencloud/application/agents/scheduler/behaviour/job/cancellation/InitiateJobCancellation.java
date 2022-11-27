package com.greencloud.application.agents.scheduler.behaviour.job.cancellation;

import static com.greencloud.application.agents.scheduler.behaviour.job.cancellation.logs.JobCancellationLogs.AGREE_FOR_CANCELLATION_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.cancellation.logs.JobCancellationLogs.JOB_CANCELLING_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.cancellation.logs.JobCancellationLogs.NOT_ALL_JOB_PARTS_CANCELLED_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.cancellation.logs.JobCancellationLogs.SUCCESSFUL_JOB_CANCELLATION_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.cancellation.templates.JobCancellationMessageTemplates.CANCEL_JOB_PROTOCOL;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.messages.MessagingUtils.readMessageListContent;
import static com.greencloud.application.messages.MessagingUtils.retrieveForPerformative;
import static com.greencloud.application.utils.JobUtils.getJobById;
import static com.greencloud.application.yellowpages.YellowPagesService.search;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.GS_SERVICE_TYPE;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SA_SERVICE_TYPE;
import static jade.lang.acl.ACLMessage.CANCEL;
import static jade.lang.acl.ACLMessage.INFORM;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.PowerJob;
import com.greencloud.commons.message.MessageBuilder;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour responsible for announcing cancellation of a job in the network.
 * It is triggered when any of the job parts fails.
 */
public class InitiateJobCancellation extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateJobCancellation.class);

	private final SchedulerAgent mySchedulerAgent;
	private final String originalJobId;
	private final AtomicInteger processedJobs;

	public InitiateJobCancellation(Agent a, ACLMessage msg, String originalJobId) {
		super(a, msg);
		this.mySchedulerAgent = (SchedulerAgent) a;
		this.originalJobId = originalJobId;
		processedJobs = new AtomicInteger(0);
	}

	/**
	 * Builds and prepares message for the behaviour to return a ready to use instance of the behaviour.
	 *
	 * @param agent         owner agent for the build behaviour
	 * @param originalJobId id of the original job - as it was before the split
	 * @return ready to use {@link InitiateJobCancellation} behaviour
	 */
	public static InitiateJobCancellation build(SchedulerAgent agent, String originalJobId) {
		ArrayList<AID> receivers = new ArrayList<>(agent.getAvailableCloudNetworks());
		receivers.addAll(search(agent, SA_SERVICE_TYPE));
		receivers.addAll(search(agent, GS_SERVICE_TYPE));
		ACLMessage request = MessageBuilder.builder()
				.withPerformative(CANCEL)
				.withMessageProtocol(CANCEL_JOB_PROTOCOL)
				.withStringContent(originalJobId)
				.withReceivers(receivers)
				.build();
		return new InitiateJobCancellation(agent, request, originalJobId);
	}

	/**
	 * Method prints the information that a given agent has agreed to process job cancellation
	 * (it means that the job has been added to the agents list)
	 *
	 * @param agree received AGREE response
	 */
	@Override
	protected void handleAgree(ACLMessage agree) {
		final String agentName = agree.getSender().getLocalName();
		logger.info(AGREE_FOR_CANCELLATION_LOG, agentName);
	}

	/**
	 * Method handles all received results of job cancellation
	 *
	 * @param responses received results
	 */
	@Override
	protected void handleAllResultNotifications(Vector responses) {
		final Collection<ACLMessage> informs = retrieveForPerformative(responses, INFORM);
		informs.forEach(this::processSuccessfulCancel);

		MDC.put(MDC_JOB_ID, originalJobId);
		logger.info(SUCCESSFUL_JOB_CANCELLATION_LOG, processedJobs);

		if (!mySchedulerAgent.getJobParts().get(originalJobId).isEmpty()) {
			processCancelRetry();
		}
	}

	private void processCancelRetry() {
		var size = mySchedulerAgent.getJobParts().get(originalJobId).size();
		var retryBehaviour = new RetryJobCancellation(mySchedulerAgent, originalJobId, parent);
		logger.warn(NOT_ALL_JOB_PARTS_CANCELLED_LOG, size);
		((ParallelBehaviour) parent).addSubBehaviour(retryBehaviour);
	}

	private void processSuccessfulCancel(final ACLMessage inform) {
		if (search(mySchedulerAgent, GS_SERVICE_TYPE).contains(inform.getSender())) {
			List<PowerJob> jobs = readMessageListContent(inform, PowerJob.class);
			processedJobs.getAndAdd(jobs.size());
		} else {
			List<ClientJob> jobs = readMessageListContent(inform, ClientJob.class);
			jobs.stream()
					.filter(job -> mySchedulerAgent.getCnaForJobMap().containsKey(job.getJobId()))
					.map(job -> getJobById(job.getJobId(), mySchedulerAgent.getClientJobs()))
					.forEach(this::handleReadClientJob);
		}
	}

	private void handleReadClientJob(ClientJob job) {
		mySchedulerAgent.getClientJobs().remove(job);
		mySchedulerAgent.getCnaForJobMap().remove(job.getJobId());
		var jobParts = mySchedulerAgent.getJobParts().get(originalJobId);
		jobParts.stream()
				.filter(jobPart -> jobPart.getJobId().equals(job.getJobId()))
				.findFirst()
				.ifPresent(jobPart -> mySchedulerAgent.getJobParts().remove(originalJobId, jobPart));
		logger.info(JOB_CANCELLING_LOG, job.getJobId());
		processedJobs.getAndIncrement();
	}
}
