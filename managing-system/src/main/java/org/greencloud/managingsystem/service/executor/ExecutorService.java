package org.greencloud.managingsystem.service.executor;

import static com.greencloud.commons.managingsystem.executor.ExecutorMessageTemplates.EXECUTE_ACTION_PROTOCOL;
import static jade.lang.acl.ACLMessage.REQUEST;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.agent.behaviour.executor.InitiateAdaptationActionRequest;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;

import com.greencloud.commons.message.MessageBuilder;

import jade.lang.acl.ACLMessage;

/**
 * Service containing methods used in execution of the adaptation plan
 */
public class ExecutorService extends AbstractManagingService {

	public ExecutorService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	public void executeAdaptationAction(AbstractPlan adaptationPlan) {

		ACLMessage adaptationActionRequest = MessageBuilder.builder()
				.withPerformative(REQUEST)
				.withConversationId(adaptationPlan.getAdaptationActionEnum().toString())
				.withMessageProtocol(EXECUTE_ACTION_PROTOCOL)
				.withObjectContent(adaptationPlan.getActionParameters())
				.withReceivers(adaptationPlan.getTargetAgent())
				.build();

		managingAgent.addBehaviour(new InitiateAdaptationActionRequest(managingAgent, adaptationActionRequest));
	}
}
