package org.greencloud.agentsystem.behaviours;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getActionParametersClass;
import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static org.greencloud.commons.utils.messaging.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.EXECUTE_ACTION_REQUEST;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareFailureReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareInformReply;
import static java.util.Objects.nonNull;

import org.greencloud.agentsystem.agents.AbstractAgent;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.action.AdaptationActionEnum;
import org.greencloud.commons.args.adaptation.AdaptationActionParameters;

import jade.core.behaviours.CyclicBehaviour;

/**
 * Generic behaviour that listens for adaptation requests
 */
public class ListenForAdaptationAction extends CyclicBehaviour {

	final AbstractAgent<?, ?> myAbstractAgent;

	public ListenForAdaptationAction(AbstractAgent<?, ?> myAbstractAgent) {
		this.myAbstractAgent = myAbstractAgent;
	}

	/**
	 * Method listens for adaptation requests and then handles it as specified for a given agent
	 */
	@Override
	public void action() {
		var message = myAbstractAgent.receive(EXECUTE_ACTION_REQUEST);

		if (nonNull(message)) {
			final AdaptationActionEnum adaptationActionEnum = AdaptationActionEnum.valueOf(message.getConversationId());
			final AdaptationAction adaptationAction = getAdaptationAction(adaptationActionEnum).get(0);
			final AdaptationActionParameters adaptationActionParameters =
					readMessageContent(message, getActionParametersClass(adaptationActionEnum));

			if (!adaptationActionParameters.dependsOnOtherAgents()) {
				if (myAbstractAgent.executeAction(adaptationAction.getAction(), adaptationActionParameters)) {
					myAbstractAgent.send(prepareInformReply(message));
				} else {
					myAbstractAgent.send(prepareFailureReply(message));
				}
			} else {
				myAbstractAgent.executeAction(adaptationAction.getAction(), adaptationActionParameters, message);
			}
		}
	}
}
