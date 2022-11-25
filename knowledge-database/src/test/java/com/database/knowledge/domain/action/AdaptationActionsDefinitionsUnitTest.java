package com.database.knowledge.domain.action;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationActions;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdaptationActionsDefinitionsUnitTest {

	@Test
	@DisplayName("Test getting adaptation action by enum")
	void testGetAdaptationAction() {
		getAdaptationActions().forEach(action -> assertThat(getAdaptationAction(action.getAction())).isEqualTo(action));
	}
}
