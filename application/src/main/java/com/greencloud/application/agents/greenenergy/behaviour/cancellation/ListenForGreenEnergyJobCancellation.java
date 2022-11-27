package com.greencloud.application.agents.greenenergy.behaviour.cancellation;

import static com.google.common.collect.Collections2.filter;
import static com.greencloud.application.agents.scheduler.behaviour.job.cancellation.logs.JobCancellationLogs.CANCELLED_JOB_PART_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.cancellation.logs.JobCancellationLogs.CANCELLING_JOB_PARTS_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.cancellation.templates.JobCancellationMessageTemplates.CANCEL_JOB_ANNOUNCEMENT;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.ACCEPTED;
import static com.greencloud.application.domain.job.JobStatusEnum.CREATED;
import static com.greencloud.application.domain.job.JobStatusEnum.ON_HOLD_PLANNED;
import static com.greencloud.application.domain.job.JobStatusEnum.PROCESSING;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareRefuseReply;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareStringReply;
import static jade.lang.acl.ACLMessage.AGREE;
import static jade.lang.acl.ACLMessage.INFORM;
import static java.util.List.of;
import static java.util.Objects.nonNull;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.commons.job.JobResultType;
import com.greencloud.commons.job.PowerJob;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Listens for job cancellation announcements. If any part of the job is processed by the
 * agent, it is removed from processing, otherwise refusal message is sent.
 */
public class ListenForGreenEnergyJobCancellation extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForGreenEnergyJobCancellation.class);

	private static final List<JobStatusEnum> JOB_NOT_STARTED_STATUSES =
			of(CREATED, PROCESSING, ACCEPTED, ON_HOLD_PLANNED);

	private GreenEnergyAgent myGreenEnergyAgent;

	@Override
	public void onStart() {
		super.onStart();
		myGreenEnergyAgent = (GreenEnergyAgent) myAgent;
	}

	@Override
	public void action() {
		var message = myGreenEnergyAgent.receive(CANCEL_JOB_ANNOUNCEMENT);

		if (nonNull(message)) {
			processJobCancellation(message.getContent(), message);
		}
	}

	private void processJobCancellation(String originalJobId, ACLMessage message) {
		var jobParts = List.copyOf(filter(myGreenEnergyAgent.getPowerJobs().keySet(),
				job -> job.getJobId().split("#")[0].equals(originalJobId)));
		if (!jobParts.isEmpty()) {
			myGreenEnergyAgent.send(prepareStringReply(message.createReply(), originalJobId, AGREE));
			MDC.put(MDC_JOB_ID, originalJobId);
			logger.info(CANCELLING_JOB_PARTS_LOG, jobParts.size());
			jobParts.forEach(this::processJobPart);
			myGreenEnergyAgent.send(prepareReply(message.createReply(), jobParts, INFORM));
		} else {
			myGreenEnergyAgent.send(prepareRefuseReply(message.createReply()));
		}
	}

	private void processJobPart(PowerJob jobPart) {
		var jobPartStatus = myGreenEnergyAgent.getPowerJobs().get(jobPart);
		myGreenEnergyAgent.getPowerJobs().remove(jobPart);
		if (!JOB_NOT_STARTED_STATUSES.contains(jobPartStatus)) {
			myGreenEnergyAgent.manage().incrementJobCounter(mapToJobInstanceId(jobPart), JobResultType.FINISH);
		}
		myGreenEnergyAgent.manage().incrementJobCounter(mapToJobInstanceId(jobPart), JobResultType.FAILED);
		MDC.put(MDC_JOB_ID, jobPart.getJobId());
		logger.info(CANCELLED_JOB_PART_LOG);
		myGreenEnergyAgent.manage().updateGreenSourceGUI();
	}
}
