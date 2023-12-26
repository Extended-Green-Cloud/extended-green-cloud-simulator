package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.initial;

import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.RESOURCE_CHARACTERISTIC_ADDITION;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.RESOURCE_COMPARATOR;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.RESOURCE_CHARACTERISTIC_SUBTRACTION;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.RESOURCE_CHARACTERISTIC_RESERVATION;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.RESOURCE_VALIDATOR;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.TAKE_FROM_INITIAL_KNOWLEDGE;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;
import java.util.Objects;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.resources.ImmutableResource;
import org.greencloud.commons.domain.resources.ImmutableResourceCharacteristic;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.domain.resources.ResourceCharacteristic;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

public class InitializeResourceKnowledge extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(InitializeResourceKnowledge.class);

	public InitializeResourceKnowledge(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("INITIALIZE_SERVER_RESOURCE_KNOWLEDGE",
				"initialize knowledge on how to handle resources",
				"rule takes from the agent's knowledge indicated information about resource handlers");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		fillRemainingResourcesInformation();
		logger.info("Initialization of Server resources was completed!");
		agentNode.updateDefaultResources(agentProps.resources());
	}

	private void fillRemainingResourcesInformation() {
		agentProps.resources().replaceAll((key, resource) -> {
			String validate = resource.getResourceValidator();
			String compare = resource.getResourceComparator();

			if (Objects.equals(resource.getResourceValidator(), TAKE_FROM_INITIAL_KNOWLEDGE)) {
				if (agentProps.getSystemKnowledge().get(RESOURCE_VALIDATOR).containsKey(key.toUpperCase())) {
					validate = agentProps.getSystemKnowledge().get(RESOURCE_VALIDATOR).get(key.toUpperCase())
							.toString();
				} else {
					logger.info(
							"Resource sufficiency validation method for {} was not found in initial knowledge!", key);
				}
			}
			if (Objects.equals(resource.getResourceComparator(), TAKE_FROM_INITIAL_KNOWLEDGE)) {
				if (agentProps.getSystemKnowledge().get(RESOURCE_COMPARATOR).containsKey(key.toUpperCase())) {
					compare = agentProps.getSystemKnowledge().get(RESOURCE_COMPARATOR).get(key.toUpperCase())
							.toString();
				} else {
					logger.info("Resource comparator method for {} was not found in initial knowledge!", key);
				}
			}

			return ImmutableResource.copyOf(resource)
					.withEmptyResource(ImmutableResource.copyOf(resource.getEmptyResource())
							.withCharacteristics(getNewCharacteristicsForResource(resource.getEmptyResource(), key))
							.withResourceComparator(compare)
							.withResourceValidator(validate))
					.withCharacteristics(getNewCharacteristicsForResource(resource, key))
					.withResourceComparator(compare)
					.withResourceValidator(validate);
		});
	}

	private Map<String, ResourceCharacteristic> getNewCharacteristicsForResource(final Resource resource,
			final String key) {
		return resource.getCharacteristics().entrySet()
				.stream().collect(toMap(Map.Entry::getKey, characteristicEntry -> {
					final String keyC = characteristicEntry.getKey();
					final ResourceCharacteristic resourceC = characteristicEntry.getValue();
					String book = resourceC.getResourceCharacteristicReservation();
					String addition = resourceC.getResourceCharacteristicAddition();
					String remover = resourceC.getResourceCharacteristicSubtraction();
					String finalKey = String.join("_", key.toUpperCase(), keyC.toUpperCase());

					if (Objects.equals(resourceC.getResourceCharacteristicAddition(), TAKE_FROM_INITIAL_KNOWLEDGE)) {
						if (agentProps.getSystemKnowledge().get(RESOURCE_CHARACTERISTIC_ADDITION).containsKey(finalKey)) {
							addition = agentProps.getSystemKnowledge().get(RESOURCE_CHARACTERISTIC_ADDITION).get(finalKey)
									.toString();
						} else {
							logger.info("Resource characteristic addition method for {} -> {} "
											+ "was not found in initial knowledge!", key, keyC);
						}
					}
					if (Objects.equals(resourceC.getResourceCharacteristicSubtraction(), TAKE_FROM_INITIAL_KNOWLEDGE)) {
						if (agentProps.getSystemKnowledge().get(RESOURCE_CHARACTERISTIC_SUBTRACTION).containsKey(finalKey)) {
							remover = agentProps.getSystemKnowledge().get(RESOURCE_CHARACTERISTIC_SUBTRACTION).get(finalKey)
									.toString();
						} else {
							logger.info("Resource characteristic removal method for {} -> {} "
											+ "was not found in initial knowledge!", key, keyC);
						}
					}
					if (Objects.equals(resourceC.getResourceCharacteristicReservation(), TAKE_FROM_INITIAL_KNOWLEDGE)) {
						if (agentProps.getSystemKnowledge().get(RESOURCE_CHARACTERISTIC_SUBTRACTION).containsKey(finalKey)) {
							book = agentProps.getSystemKnowledge().get(RESOURCE_CHARACTERISTIC_RESERVATION).get(finalKey)
									.toString();
						} else {
							logger.info("Resource characteristic reservation method for {} -> {} "
											+ "was not found in initial knowledge!", key, keyC);
						}
					}
					return ImmutableResourceCharacteristic.copyOf(resourceC)
							.withResourceCharacteristicAddition(addition)
							.withResourceCharacteristicSubtraction(remover)
							.withResourceCharacteristicReservation(book);
				}));
	}
}
