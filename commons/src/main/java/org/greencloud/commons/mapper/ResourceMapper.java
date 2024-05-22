package org.greencloud.commons.mapper;

import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.COST_WEIGHT;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.CPU_COEFFICIENT;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.EXECUTOR_SATISFACTION_WEIGHT;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.JOB_SATISFACTION_WEIGHT;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.MEMORY_COEFFICIENT;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.PERFORMANCE_WEIGHT;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.RELIABILITY_WEIGHT;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.STORAGE_COEFFICIENT;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.TIME_WEIGHT;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.BASIC_RESOURCES;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.ID;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.domain.resources.ImmutableResourcePreferenceCoefficients;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.domain.resources.ResourcePreferenceCoefficients;

/**
 * Class with methods used to map resources
 */
public class ResourceMapper {

	/**
	 * Map basic resources (CPU, MEMORY, STORAGE).
	 *
	 * @param resourceMap map of resources with identifiers
	 * @return list with a map of basic resource values
	 */
	public static List<Map<String, Object>> mapToBasicResourceList(
			final Map<String, Map<String, Resource>> resourceMap) {
		return resourceMap.entrySet().stream()
				.map(ResourceMapper::getResourceMap)
				.toList();
	}

	/**
	 * Method converts map with resource preferences into ResourcePreferenceCoefficients object.
	 *
	 * @param preferenceMap map with preferences
	 * @return ResourcePreferenceCoefficients
	 */
	public static ResourcePreferenceCoefficients mapToResourcePreferencesCoefficients(
			final Map<String, Object> preferenceMap) {
		return ImmutableResourcePreferenceCoefficients.builder()
				.cpuExperienceCoefficient((Double) preferenceMap.get(CPU_COEFFICIENT))
				.memoryExperienceCoefficient((Double) preferenceMap.get(MEMORY_COEFFICIENT))
				.storageExperienceCoefficient((Double) preferenceMap.get(STORAGE_COEFFICIENT))
				.costWeights((Double) preferenceMap.get(COST_WEIGHT))
				.reliabilityWeight((Double) preferenceMap.get(RELIABILITY_WEIGHT))
				.timeWeight((Double) preferenceMap.get(TIME_WEIGHT))
				.performanceWeight((Double) preferenceMap.get(PERFORMANCE_WEIGHT))
				.jobSatisfactionWeight((Double) preferenceMap.get(JOB_SATISFACTION_WEIGHT))
				.executorSatisfactionWeight((Double) preferenceMap.get(EXECUTOR_SATISFACTION_WEIGHT))
				.build();

	}

	private static Map<String, Object> getResourceMap(final Map.Entry<String, Map<String, Resource>> resources) {
		final Map<String, Object> resourceMap = resources.getValue().entrySet().stream()
				.filter(entry -> BASIC_RESOURCES.contains(entry.getKey()))
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getAmountInCommonUnit()));
		final String resourcesIdentifier = resources.getKey();

		resourceMap.put(ID, resourcesIdentifier);
		return resourceMap;
	}
}
