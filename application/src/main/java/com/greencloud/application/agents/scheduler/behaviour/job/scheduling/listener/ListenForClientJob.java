package com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener;

import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.logs.JobSchedulingListenerLog.JOB_ALREADY_EXISTING_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.logs.JobSchedulingListenerLog.JOB_ENQUEUED_SUCCESSFULLY_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.logs.JobSchedulingListenerLog.QUEUE_THRESHOLD_EXCEEDED_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.templates.JobSchedulingMessageTemplates.NEW_JOB_ANNOUNCEMENT_TEMPLATE;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.CREATED;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.SCHEDULED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.SPLIT_JOB_ID;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static java.lang.String.format;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.application.domain.job.SplitJob;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.ImmutableClientJob;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for upcoming new client jobs
 */
public class ListenForClientJob extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForClientJob.class);

	private SchedulerAgent myScheduler;

	/**
	 * Method casts the abstract agent to the agent of type SchedulerAgent
	 */
	@Override
	public void onStart() {
		super.onStart();
		myScheduler = (SchedulerAgent) myAgent;
	}

	/**
	 * Method listens for the upcoming job announcement information messages coming from the Cloud Network.
	 * It evaluates the job priority and puts it into the job schedule queue.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(NEW_JOB_ANNOUNCEMENT_TEMPLATE);

		if (Objects.nonNull(message)) {
			final ClientJob job = readMessageContent(message, ClientJob.class);
			final String jobId = job.getJobId();
			MDC.put(MDC_JOB_ID, jobId);
			addJobToPriorityQueue(job, message.getSender().getName());
		} else {
			block();
		}
	}

	private void addJobToPriorityQueue(final ClientJob job, final String client) {
		if (myScheduler.getClientJobs().containsKey(job)) {
			logger.info(JOB_ALREADY_EXISTING_LOG, job.getJobId(), myScheduler.getClientJobs().get(job));
			return;
		}

		if (job.getPower() >= myScheduler.config().getJobSplitThreshold()) {
			splitJobAndPutToQueue(job, client);
		} else {
			putJobToQueue(job, client);
		}
	}

	private void splitJobAndPutToQueue(ClientJob job, String client) {
		var jobParts = splitJob(job);
		var messageContent = new SplitJob(jobParts);
		myScheduler.send(prepareJobStatusMessageForClient(client, messageContent, SPLIT_JOB_ID));
		jobParts.forEach(jobPart -> {
			myScheduler.getJobParts().put(job.getJobId(), jobPart);
			putJobToQueue(jobPart, client);
		});
	}

	private void putJobToQueue(ClientJob job, String client) {
		myScheduler.getClientJobs().put(job, CREATED);
		if (myScheduler.getJobsToBeExecuted().offer(job)) {
			logger.info(JOB_ENQUEUED_SUCCESSFULLY_LOG, job.getJobId());
			myScheduler.manage().updateJobQueue();
			myScheduler.send(prepareJobStatusMessageForClient(client, job.getJobId(), SCHEDULED_JOB_ID));
		} else {
			logger.info(QUEUE_THRESHOLD_EXCEEDED_LOG);
		}
	}

	private List<ClientJob> splitJob(ClientJob clientJob) {
		return IntStream.range(0, myScheduler.config().getSplittingFactor())
				.mapToObj(i -> createJobPart(clientJob, i + 1))
				.toList();
	}

	private ClientJob createJobPart(ClientJob clientJob, int partNumber) {
		return ImmutableClientJob.builder()
				.from(clientJob)
				.jobId(format("%s#part%d", clientJob.getJobId(), partNumber))
				.power(clientJob.getPower() / myScheduler.config().getSplittingFactor())
				.build();
	}
}
