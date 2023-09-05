package com.greencloud.application.agents.client.behaviour.jobannouncement.handler;

import static com.database.knowledge.domain.agent.DataType.CLIENT_STATISTICS;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;

import java.time.Duration;
import java.time.Instant;

import com.database.knowledge.domain.agent.client.ImmutableClientStatisticsData;
import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.enums.ClientJobUpdateEnum;
import com.greencloud.application.domain.job.JobStatusUpdate;
import com.gui.agents.ClientAgentNode;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Abstract class extended by all handlers that process the information about job status update
 */
public abstract class AbstractJobUpdateHandler extends OneShotBehaviour {

	protected ACLMessage message;
	protected ClientAgent myClient;
	protected ClientJobUpdateEnum updateEnum;

	protected AbstractJobUpdateHandler() {
	}

	/**
	 * Behaviour constructor
	 *
	 * @param message    message with job status update
	 * @param myClient   client executing the behaviour
	 * @param updateEnum type of the update message
	 */
	protected AbstractJobUpdateHandler(final ACLMessage message, final ClientAgent myClient,
			final ClientJobUpdateEnum updateEnum) {
		this.myClient = myClient;
		this.message = message;
		this.updateEnum = updateEnum;
	}

	@Override
	public int onEnd() {
		if (!(this instanceof HandleJobFinishUpdate || this instanceof HandleJobFailedUpdate)) {
			myClient.manage().writeClientData(false);
		}
		return 0;
	}

	/**
	 * Method updates information after job status update (for original job, not split part)
	 *
	 * @param jobUpdate data of job update
	 */
	protected void updateInformationOfJobStatusUpdate(final JobStatusUpdate jobUpdate) {
		((ClientAgentNode) myClient.getAgentNode()).updateJobStatus(updateEnum.getJobStatus());
		myClient.getJobExecution().updateJobStatusDuration(updateEnum.getJobStatus(), jobUpdate.getChangeTime());
	}

	/**
	 * Method passes the information about message retrieval time to the database
	 *
	 * @param jobUpdate data of job update
	 */
	protected void measureTimeToRetrieveTheMessage(final JobStatusUpdate jobUpdate) {
		final Instant timeWhenTheMessageWasSent = jobUpdate.getChangeTime();
		final Instant timeWhenTheMessageWasReceived = getCurrentTime();
		final long elapsedTime = Duration.between(timeWhenTheMessageWasSent, timeWhenTheMessageWasReceived).toMillis();

		myClient.writeMonitoringData(CLIENT_STATISTICS, ImmutableClientStatisticsData.builder()
				.messageRetrievalTime(elapsedTime)
				.build());
	}

	/**
	 * Method updates time frames of client job (for original job, not job part)
	 *
	 * @param newStart new job start time
	 * @param newEnd   new job end time
	 */
	protected void readjustJobTimeFrames(final Instant newStart, final Instant newEnd) {
		myClient.getJobExecution().setJobSimulatedStart(newStart);
		myClient.getJobExecution().setJobSimulatedEnd(newEnd);

		((ClientAgentNode) myClient.getAgentNode()).updateJobTimeFrame(
				convertToRealTime(myClient.getJobExecution().getJobSimulatedStart()),
				convertToRealTime(myClient.getJobExecution().getJobSimulatedEnd()));
	}
}
