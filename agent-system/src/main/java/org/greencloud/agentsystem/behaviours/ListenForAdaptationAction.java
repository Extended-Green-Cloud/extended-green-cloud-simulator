package org.greencloud.agentsystem.behaviours;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getActionParametersClass;
import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.utils.facts.AdaptationFactsFactory.constructFactsForAdaptationRequest;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.EXECUTE_ACTION_REQUEST;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareFailureReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareInformReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.utils.messages.MessageReader.readMessageContent;

import java.util.Optional;

import javax.annotation.Nullable;

import org.greencloud.agentsystem.agents.EGCSAgent;
import org.greencloud.commons.args.adaptation.AdaptationActionParameters;
import org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import com.database.knowledge.domain.action.AdaptationAction;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Generic behaviour that listens for adaptation requests.
 */
public class ListenForAdaptationAction extends CyclicBehaviour {

	private static final LogBuilder log = constructLogBuilder(ListenForAdaptationAction.class);
	final EGCSAgent<?, ?, ?> myAbstractAgent;

	public ListenForAdaptationAction(EGCSAgent<?, ?, ?> myAbstractAgent) {
		this.myAbstractAgent = myAbstractAgent;
	}

	/**
	 * Method listens for adaptation requests and then handles it as specified for a given agent.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAbstractAgent.receive(EXECUTE_ACTION_REQUEST);

		if (nonNull(message)) {
			final AdaptationActionTypeEnum actionType = AdaptationActionTypeEnum.valueOf(message.getConversationId());
			final AdaptationAction adaptationAction = getAdaptationAction(actionType).getFirst();
			final AdaptationActionParameters params = readMessageContent(message, getActionParametersClass(actionType));

			of(params)
					.filter(AdaptationActionParameters::dependsOnOtherAgents)
					.ifPresentOrElse(
							_ -> executeActionDependentOnOtherAgents(adaptationAction, message, params),
							() -> executeActionIndependentOnOtherAgents(adaptationAction, message, params)
					);
		}
	}

	private void executeActionIndependentOnOtherAgents(final AdaptationAction adaptation, final ACLMessage message,
			final AdaptationActionParameters parameters) {
		final boolean isActionCompleted =
				executeAdaptationRequest(myAbstractAgent.getRulesController(), adaptation.getAction(), parameters, null)
						.map(facts -> facts.get(RESULT))
						.map(Boolean.class::cast)
						.orElse(false);
		final ACLMessage responseMessage = isActionCompleted ?
				prepareInformReply(message) :
				prepareFailureReply(message);

		myAbstractAgent.send(responseMessage);
	}

	private void executeActionDependentOnOtherAgents(final AdaptationAction adaptation, final ACLMessage message,
			final AdaptationActionParameters parameters) {
		executeAdaptationRequest(myAbstractAgent.getRulesController(), adaptation.getAction(), parameters, message);
	}

	private Optional<RuleSetFacts> executeAdaptationRequest(final RulesController<?, ?> rulesController,
			final AdaptationActionTypeEnum actionType,
			final AdaptationActionParameters params,
			final @Nullable ACLMessage adaptationMessage) {
		if (nonNull(rulesController)) {
			final int index = rulesController.getLatestLongTermRuleSetIdx().get();
			final RuleSetFacts facts = constructFactsForAdaptationRequest(index, actionType, params);
			ofNullable(adaptationMessage).ifPresent(message -> facts.put(MESSAGE, message));
			rulesController.fire(facts);

			return of(facts);
		} else {
			log.messageInfo("Cannot execute adaptation - rules controller has not been initialized.");
			return empty();
		}
	}
}
