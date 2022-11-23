package com.greencloud.application.behaviours;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationActionById;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareInformReply;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareRefuseReply;
import static com.greencloud.commons.managingsystem.executor.ExecutorMessageTemplates.EXECUTE_ACTION_REQUEST;
import static java.lang.Integer.parseInt;
import static java.util.Objects.nonNull;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.commons.managingsystem.planner.AdaptationActionParameters;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class ListenForAdaptationAction extends CyclicBehaviour {

	AbstractAgent myAbstractAgent;

	public ListenForAdaptationAction(AbstractAgent myAbstractAgent) {
		this.myAbstractAgent = myAbstractAgent;
	}

	@Override
	public void action() {
		var message = myAbstractAgent.receive(EXECUTE_ACTION_REQUEST);

		if (nonNull(message)) {
			processAdaptationActionRequest(message);
		}
	}

	private void processAdaptationActionRequest(ACLMessage message) {
		var adaptationActionId = parseInt(message.getConversationId());
		var adaptationAction = getAdaptationActionById(adaptationActionId);
		var adaptationActionParameters = readMessageContent(message, AdaptationActionParameters.class);

		if (myAbstractAgent.executeAction(adaptationAction, adaptationActionParameters)) {
			myAbstractAgent.send(prepareInformReply(message.createReply()));
		} else {
			myAbstractAgent.send(prepareRefuseReply(message.createReply()));
		}
	}
}
