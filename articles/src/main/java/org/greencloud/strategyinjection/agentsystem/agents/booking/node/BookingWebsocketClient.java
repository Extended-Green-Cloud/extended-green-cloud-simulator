package org.greencloud.strategyinjection.agentsystem.agents.booking.node;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.greencloud.commons.enums.rules.RuleStepType.CFP_COMPARE_MESSAGES_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.CFP_HANDLE_SELECTED_PROPOSAL_STEP;
import static org.greencloud.commons.enums.rules.RuleType.BASIC_CFP_RULE;
import static org.greencloud.commons.mapper.JsonMapper.getMapper;
import static org.greencloud.rulescontroller.rule.AgentRuleType.BASIC;

import java.io.IOException;
import java.net.URI;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.rulescontroller.rest.domain.RuleRest;
import org.greencloud.rulescontroller.rest.domain.RuleSetRest;
import org.greencloud.strategyinjection.agentsystem.domain.ClientOrder;
import org.greencloud.strategyinjection.agentsystem.domain.CuisineType;
import org.greencloud.strategyinjection.agentsystem.domain.ImmutableClientOrder;
import org.greencloud.strategyinjection.agentsystem.domain.RestaurantLookUpMessage;
import org.greencloud.strategyinjection.agentsystem.domain.RestaurantOfferResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gui.websocket.GuiWebSocketClient;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BookingWebsocketClient extends GuiWebSocketClient {

	private static final Logger logger = LoggerFactory.getLogger(BookingWebsocketClient.class);
	final BookingNode node;

	public BookingWebsocketClient(final BookingNode node) {
		super(URI.create(("ws://localhost:8080/")));
		this.node = node;
	}

	@Override
	public void onMessage(final String message) {
		if (!message.equals("Welcoming message!")) {
			if (message.contains("RESTAURANT_LOOK_UP")) {
				handleNewClientOrder(message);
			}
			if (message.contains("ACCEPT_ORDER")) {
				handleOrderResponse(message);
			}
		}
	}

	private void handleNewClientOrder(final String message) {
		try {
			final RestaurantLookUpMessage receivedMessage = getMapper().readValue(message,
					RestaurantLookUpMessage.class);
			final int orderId = node.getLatestId().incrementAndGet();

			String additionalInstructionStrategy = "";

			if (isNotEmpty(receivedMessage.getAdditionalInstructions())) {
				additionalInstructionStrategy = createStrategyForAdditionalInstructions(
						receivedMessage.getAdditionalInstructions(), orderId);
			}

			final ClientOrder order = ImmutableClientOrder.builder()
					.orderId(orderId)
					.cuisine(CuisineType.valueOf(receivedMessage.getCuisine()))
					.dish(receivedMessage.getDish())
					.maxPrice(receivedMessage.getPrice())
					.additionalInstructions(additionalInstructionStrategy)
					.build();
			node.getClientEvents().add(Pair.of("RESTAURANT_LOOK_UP", order));
			getConnection().send(format("Order was assigned with id: %d and is being processed!", orderId));
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	private void handleOrderResponse(final String message) {
		try {
			final RestaurantOfferResponseMessage receivedMessage = getMapper().readValue(message,
					RestaurantOfferResponseMessage.class);
			node.getClientEvents().add(Pair.of("ACCEPT_ORDER", receivedMessage));
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	private String createStrategyForAdditionalInstructions(final String instructions, final int orderId) {
		final String strategyName = "CUSTOM_INSTRUCTIONS_ORDER_" + orderId;
		final RuleRest handleProposalsRule = createProposeHandlerRule(instructions);
		final RuleRest compareProposalsRule = createComparatorRule(instructions);

		final RuleSetRest strategyRest = new RuleSetRest();
		strategyRest.setName(strategyName);
		strategyRest.setRules(new ArrayList<>(List.of(handleProposalsRule, compareProposalsRule)));

		try {
			final String json = getMapper().writeValueAsString(strategyRest);
			final RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
			final Request request = new Request.Builder()
					.url("http://localhost:5000/ruleSet")
					.post(body)
					.build();
			final OkHttpClient client = new OkHttpClient();
			final Call call = client.newCall(request);
			final Response response = call.execute();
			logger.info("Personalized client rule set was sent to server! Response: {}", response);

		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		} catch (IOException e) {
			throw new InvalidParameterException("Cloud not send rule set to REST!");
		}

		return strategyName;
	}

	private RuleRest createComparatorRule(final String instructions) {
		final RuleRest handleProposalsRule = new RuleRest();
		handleProposalsRule.setAgentRuleType(BASIC);
		handleProposalsRule.setAgentType("BOOKING");
		handleProposalsRule.setType(BASIC_CFP_RULE);
		handleProposalsRule.setStepType(CFP_COMPARE_MESSAGES_STEP);
		handleProposalsRule.setName("compare restaurant offers with custom filter");
		handleProposalsRule.setDescription("comparing restaurant offers with custom filter");
		handleProposalsRule.setImports(List.of(
				"import org.greencloud.commons.constants.FactTypeConstants;",
				"import org.greencloud.commons.utils.messaging.MessageReader;",
				"import java.lang.Class;"
		));
		handleProposalsRule.setExecute("""
				bestProposal = facts.get(FactTypeConstants.CFP_BEST_MESSAGE);
				newProposal = facts.get(FactTypeConstants.CFP_NEW_PROPOSAL);
				restaurantData = MessageReader.readMessageContent(newProposal, Class.forName("org.greencloud.strategyinjection.agentsystem.domain.RestaurantData"));
				restaurantDataBest = MessageReader.readMessageContent(bestProposal, Class.forName("org.greencloud.strategyinjection.agentsystem.domain.RestaurantData"));
				
				if($instruction) {
				restaurantDataNewPrice = restaurantData.getPrice();
				restaurantData = restaurantDataBest;
				
				if($instruction) {
				result = (int) (restaurantDataBest.getPrice() - restaurantDataNewPrice);
				facts.put(FactTypeConstants.CFP_RESULT, result);
				}
				else {
				facts.put(FactTypeConstants.CFP_RESULT, -1);
				}
				}
				else { facts.put(FactTypeConstants.CFP_RESULT, 1); }
				""".replace("$instruction", instructions));
		return handleProposalsRule;
	}

	private RuleRest createProposeHandlerRule(final String instructions) {
		final String responseMsgLiteral = """
				There is a restaurant which fulfills the criteria.\s
				Strategy used in processing: $strategy\s
				Restaurant information:\s
				price $price,\s
				additional information $additionalInfo
				""";

		final RuleRest handleProposalsRule = new RuleRest();
		handleProposalsRule.setAgentRuleType(BASIC);
		handleProposalsRule.setAgentType("BOOKING");
		handleProposalsRule.setType(BASIC_CFP_RULE);
		handleProposalsRule.setStepType(CFP_HANDLE_SELECTED_PROPOSAL_STEP);
		handleProposalsRule.setName("look for restaurant to complete client order with custom filter");
		handleProposalsRule.setDescription(
				"process looking for restaurant to complete client order with custom filter");
		handleProposalsRule.setImports(List.of(
				"import org.greencloud.commons.constants.FactTypeConstants;",
				"import org.greencloud.commons.utils.messaging.MessageReader;",
				"import java.lang.Class;",
				"import jade.lang.acl.ACLMessage;",
				"import org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory;"
		));
		handleProposalsRule.setExecute("""
				order = facts.get(FactTypeConstants.RESULT);
				bestProposal = facts.get(FactTypeConstants.CFP_BEST_MESSAGE);
				restaurantData = MessageReader.readMessageContent(bestProposal, Class.forName("org.greencloud.strategyinjection.agentsystem.domain.RestaurantData"));
				if($instruction) {
				agentNode.passRestaurantMessageToClient("$msgLiteral".replace("$strategy", controller.getRuleSets().get((int) facts.get(FactTypeConstants.STRATEGY_IDX)).getName()).replace("$price", restaurantData.getPrice().toString()).replace("$additionalInfo", restaurantData.getRestaurantInformation().toString()));
				agentProps.getRestaurantForOrder().put(order.getOrderId(), bestProposal);}
				else { agent.send(ReplyMessageFactory.prepareReply(bestProposal, ACLMessage.REJECT_PROPOSAL, ACLMessage.REJECT_PROPOSAL)); agentNode.passRestaurantMessageToClient("No restaurants that fulfill additional instructions were found!");}
				""".replace("$instruction", instructions).replace("$msgLiteral", responseMsgLiteral));
		return handleProposalsRule;
	}
}
