package org.greencloud.agentsystem.strategies.deault.rules.server.initial;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.RESOURCE_CHARACTERISTIC_ADDITION;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.RESOURCE_CHARACTERISTIC_RESERVATION;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.RESOURCE_CHARACTERISTIC_SUBTRACTION;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.RESOURCE_COMPARATOR;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.RESOURCE_VALIDATOR;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.TAKE_FROM_INITIAL_KNOWLEDGE;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.resources.ImmutableResource;
import org.greencloud.commons.domain.resources.ImmutableResourceCharacteristic;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.domain.resources.ResourceCharacteristic;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

public class InitializeResourceKnowledge extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(InitializeResourceKnowledge.class);
	private final Predicate<String> useDefaultMethod = method -> method.equals(TAKE_FROM_INITIAL_KNOWLEDGE);

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
					final String finalKey = String.join("_", key.toUpperCase(), keyC.toUpperCase());
					final ResourceCharacteristic resourceC = characteristicEntry.getValue();

					final String addition = getManagementMethod(resourceC.getResourceCharacteristicAddition(),
							RESOURCE_CHARACTERISTIC_ADDITION, key, keyC, finalKey);
					final String book = getManagementMethod(resourceC.getResourceCharacteristicReservation(),
							RESOURCE_CHARACTERISTIC_RESERVATION, key, keyC, finalKey);
					final String remover = getManagementMethod(resourceC.getResourceCharacteristicSubtraction(),
							RESOURCE_CHARACTERISTIC_SUBTRACTION, key, keyC, finalKey);

					return ImmutableResourceCharacteristic.copyOf(resourceC)
							.withResourceCharacteristicAddition(addition)
							.withResourceCharacteristicSubtraction(remover)
							.withResourceCharacteristicReservation(book);
				}));
	}

	private String getManagementMethod(final String characteristicMethod, final String methodName, final String keyName,
			final String keyCharacteristicName, final String completeKeyName) {
		if (useDefaultMethod.test(characteristicMethod)) {
			return ofNullable(agentProps.getSystemKnowledge().get(methodName))
					.map(methodsMap -> methodsMap.get(completeKeyName).toString())
					.orElseGet(() -> {
						logger.info("{} method for {} -> {} was not found in initial knowledge!",
								methodName, keyName, keyCharacteristicName);
						return characteristicMethod;
					});
		}
		return characteristicMethod;
	}
}
