package com.greencloud.application.agents.server.behaviour.jobexecution.listener;

import static com.google.common.collect.Collections2.filter;
import static com.greencloud.application.agents.scheduler.behaviour.job.cancellation.logs.JobCancellationLogs.CANCELLED_JOB_PART_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.cancellation.logs.JobCancellationLogs.CANCELLING_JOB_PARTS_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.cancellation.templates.JobCancellationMessageTemplates.CANCEL_JOB_ANNOUNCEMENT;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareRefuseReply;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.RUNNING_JOB_STATUSES;
import static com.greencloud.commons.job.JobResultType.FAILED;
import static com.greencloud.commons.job.JobResultType.FINISH;
import static jade.lang.acl.ACLMessage.INFORM;
import static java.util.Objects.nonNull;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Listens for job cancellation announcements. If any part of the job is processed by the
 * agent, it is removed from processing, otherwise refusal message is sent.
 */
public class ListenForServerJobCancellation extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForServerJobCancellation.class);

	private ServerAgent myServerAgent;

	@Override
	public void onStart() {
		super.onStart();
		this.myServerAgent = (ServerAgent) myAgent;
	}

	@Override
	public void action() {
		var message = myAgent.receive(CANCEL_JOB_ANNOUNCEMENT);

		if (nonNull(message)) {
			processJobCancellation(message.getContent(), message);
		}
	}

	private void processJobCancellation(String originalJobId, ACLMessage message) {
		var jobParts = List.copyOf(filter(myServerAgent.getServerJobs().keySet(),
				job -> job.getJobId().split("#")[0].equals(originalJobId)));
		if (!jobParts.isEmpty()) {
			MDC.put(MDC_JOB_ID, originalJobId);
			logger.info(CANCELLING_JOB_PARTS_LOG, jobParts.size());
			jobParts.forEach(jobPart -> {
				if(RUNNING_JOB_STATUSES.contains(myServerAgent.getServerJobs().get(jobPart))) {
					myServerAgent.manage().incrementJobCounter(mapToJobInstanceId(jobPart), FINISH);
				}
				myServerAgent.manage().finishJobExecutionWithResult(jobPart, false, FAILED);
				MDC.put(MDC_JOB_ID, jobPart.getJobId());
				logger.info(CANCELLED_JOB_PART_LOG);
			});
			myServerAgent.manage().updateServerGUI();
			myServerAgent.send(prepareReply(message.createReply(), jobParts, INFORM));
		} else {
			myServerAgent.send(prepareRefuseReply(message.createReply()));
		}
	}
}
