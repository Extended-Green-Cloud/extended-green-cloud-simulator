package com.greencloud.application.agents.server.management;

import static com.greencloud.application.mapper.JobMapper.mapToPowerShortageJob;
import static com.greencloud.application.messages.factory.JobStatusMessageFactory.prepareJobStatusMessageForCNA;

import java.time.Instant;

import com.greencloud.application.agents.AbstractAgentManagement;
import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.errorhandling.initiator.InitiateJobTransferInCloudNetwork;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobPowerShortageTransfer;

import jade.lang.acl.ACLMessage;

/**
 * Set of utilities used to manage the communication of the server agent
 */
public class ServerCommunicationManagement extends AbstractAgentManagement {

	private final ServerAgent serverAgent;

	public ServerCommunicationManagement(ServerAgent serverAgent) {
		this.serverAgent = serverAgent;
	}

	/**
	 * Method resends the job transfer request to parent Cloud Network
	 *
	 * @param jobInstanceId      job that is to be transferred
	 * @param powerShortageStart time when the power shortage starts
	 * @param request            initial green source request
	 */
	public void passTransferRequestToCloudNetwork(final JobInstanceIdentifier jobInstanceId,
			final Instant powerShortageStart, final ACLMessage request) {
		final JobPowerShortageTransfer job = mapToPowerShortageJob(jobInstanceId, powerShortageStart);
		serverAgent.addBehaviour(InitiateJobTransferInCloudNetwork.create(serverAgent, job, jobInstanceId, request));
	}

	/**
	 * Method informs CNA that the status of given job has changed
	 *
	 * @param jobInstance job which status has changed
	 * @param type        new status type
	 */
	public void informCNAAboutStatusChange(final JobInstanceIdentifier jobInstance, final String type) {
		final ACLMessage information = prepareJobStatusMessageForCNA(jobInstance, type, serverAgent);
		serverAgent.send(information);
	}
}
