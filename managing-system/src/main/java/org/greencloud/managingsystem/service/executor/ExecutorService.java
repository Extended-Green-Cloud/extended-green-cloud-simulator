package org.greencloud.managingsystem.service.executor;

import static com.greencloud.commons.managingsystem.executor.ExecutorMessageTemplates.EXECUTE_ACTION_PROTOCOL;
import static jade.lang.acl.ACLMessage.REQUEST;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;

import com.greencloud.commons.managingsystem.planner.AdaptationActionParameters;
import com.greencloud.commons.message.MessageBuilder;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Service containing methods used in execution of the adaptation plan
 */
public class ExecutorService extends AbstractManagingService {

	public ExecutorService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	public void executeAdaptationAction(AID targetAgent, Integer adaptationActionId,
			AdaptationActionParameters actionParameters) {

		ACLMessage adaptationActionRequest = MessageBuilder.builder()
				.withPerformative(REQUEST)
				.withConversationId(adaptationActionId.toString())
				.withMessageProtocol(EXECUTE_ACTION_PROTOCOL)
				.withObjectContent(actionParameters)
				.withReceivers(targetAgent)
				.build();

		managingAgent.addBehaviour(new InitiateAdaptationActionRequest(managingAgent, adaptationActionRequest));
	}
}
