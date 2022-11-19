package timescale;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationActions;
import static com.database.knowledge.domain.agent.DataType.PROCESSED_API_REQUEST;
import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.DataType;
import com.database.knowledge.domain.agent.MonitoringData;
import com.database.knowledge.domain.agent.monitoring.ImmutableProcessedApiRequest;
import com.database.knowledge.domain.goal.AdaptationGoal;
import com.database.knowledge.domain.goal.GoalEnum;
import com.database.knowledge.timescale.TimescaleDatabase;

@ExtendWith(MockitoExtension.class)
class TimescaleDatabaseIntegrationTest {

	private TimescaleDatabase database;

	@BeforeEach
	void init() {
		database = new TimescaleDatabase();
		database.initDatabase();
	}

	@AfterEach
	void cleanUp() {
		database.close();
	}

	@Test
	void shouldSuccessfullySaveDataToDatabase() {
		// given
		String aid = "test_agent";
		DataType dataType = PROCESSED_API_REQUEST;
		MonitoringData monitoringData = ImmutableProcessedApiRequest.builder()
				.callType("testCallType")
				.requestedTimeslot("testTimeslot")
				.requestedType("testRequestType")
				.build();

		// when
		database.writeMonitoringData(aid, dataType, monitoringData);

		// then
		List<AgentData> result = database.readMonitoringData();

		// then
		assertThat(result)
				.as("After insertion there should be data in the timescaledb.")
				.isNotEmpty();
		assertThat(result.get(0))
				.as("Returned data should be of AgentData record type")
				.isInstanceOf(AgentData.class)
				.as("Returned data should be equal to the saved one")
				.matches(agentData -> agentData.aid().equals(aid))
				.matches(agentData -> agentData.dataType().equals(PROCESSED_API_REQUEST))
				.matches(agentData -> agentData.monitoringData() instanceof ImmutableProcessedApiRequest)
				.matches(agentData -> agentData.monitoringData().equals(monitoringData));
	}

	@Test
	void shouldCorrectlyReadAdaptationGoals() {
		// given
		var expectedAdaptationGoals = List.of(
				new AdaptationGoal(1, "Maximize job success ratio", 0.8, true, 0.6),
				new AdaptationGoal(2,"Minimize used backup power", 0.2, false, 0.2),
				new AdaptationGoal(3,"Distribute traffic evenly", 0.7, false, 0.2)
		);

		// when
		List<AdaptationGoal> result = database.readAdaptationGoals();

		// then
		assertThat(result)
				.as("There should be 3 adaptation goals")
				.hasSize(3)
				.as("GoalEnum should be equal to the expected ones")
				.usingRecursiveFieldByFieldElementComparator()
				.containsExactlyInAnyOrderElementsOf(expectedAdaptationGoals);
	}

	@Test
	void shouldCorrectlyReadAdaptationActions() {
		// given
		var expectedActions = getAdaptationActions();

		// when
		List<AdaptationAction> readActions = database.readAdaptationActions();

		// then
		assertThat(readActions)
				.as("All defined adaptation actions should be returned")
				.usingRecursiveFieldByFieldElementComparator()
				.containsExactlyInAnyOrderElementsOf(expectedActions);
	}

	@Test
	void shouldCorrectReadSpecificAdaptationAction() {
		// given
		var expectedAction = getAdaptationActions().get(0);

		// when
		AdaptationAction readAction = database.readAdaptationAction(1);

		// then
		assertThat(readAction)
				.as("Returned specific adaptation action should be equal to the specific one")
				.usingRecursiveComparison()
				.isEqualTo(expectedAction);
	}

	@ParameterizedTest
	@MethodSource("actionResultsProvider")
	void shouldCorrectlyUpdateAdaptationAction(List<Map<GoalEnum, Double>> actionResults,
			Map<Integer, Double> expectedActionResults) {
		// given
		int actionId = 1;

		// when
		actionResults.forEach(actionResult -> database.updateAdaptationAction(actionId, actionResult));

		// then
		var updatedAction = database.readAdaptationAction(actionId);
		assertThat(updatedAction.getRuns())
				.as("action number of runs should be equal to number of updates")
				.isEqualTo(actionResults.size());
		assertThat(updatedAction.getActionResults())
				.as("final action results should have the expected values")
				.usingRecursiveComparison()
				.isEqualTo(expectedActionResults);
		assertThat(updatedAction.getAvailable())
				.as("After update action should be unavailable")
				.isFalse();
	}

	private static Stream<Arguments> actionResultsProvider() {
		return Stream.of(
				arguments(
						List.of(
								Map.of(
										MAXIMIZE_JOB_SUCCESS_RATIO, 0.5,
										MINIMIZE_USED_BACKUP_POWER, 0.25,
										DISTRIBUTE_TRAFFIC_EVENLY, -0.25
								)
						),
						Map.of(
								MAXIMIZE_JOB_SUCCESS_RATIO, 0.5,
								MINIMIZE_USED_BACKUP_POWER, 0.25,
								DISTRIBUTE_TRAFFIC_EVENLY, -0.25
						)
				),
				arguments(
						List.of(
								Map.of(
										MAXIMIZE_JOB_SUCCESS_RATIO, 0.25,
										MINIMIZE_USED_BACKUP_POWER, 0.25,
										DISTRIBUTE_TRAFFIC_EVENLY, 0.25
								),
								Map.of(
										MAXIMIZE_JOB_SUCCESS_RATIO, 0.75,
										MINIMIZE_USED_BACKUP_POWER, 0.75,
										DISTRIBUTE_TRAFFIC_EVENLY, 0.75
								)
						),
						Map.of(
								MAXIMIZE_JOB_SUCCESS_RATIO, 0.5,
								MINIMIZE_USED_BACKUP_POWER, 0.5,
								DISTRIBUTE_TRAFFIC_EVENLY, 0.5
						)
				),
				arguments(
						List.of(
								Map.of(
										MAXIMIZE_JOB_SUCCESS_RATIO, -0.25,
										MINIMIZE_USED_BACKUP_POWER, -0.25,
										DISTRIBUTE_TRAFFIC_EVENLY, -0.25
								),
								Map.of(
										MAXIMIZE_JOB_SUCCESS_RATIO, 0.25,
										MINIMIZE_USED_BACKUP_POWER, 0.25,
										DISTRIBUTE_TRAFFIC_EVENLY, 0.25
								)
						),
						Map.of(
								MAXIMIZE_JOB_SUCCESS_RATIO, 0.0,
								MINIMIZE_USED_BACKUP_POWER, 0.0,
								DISTRIBUTE_TRAFFIC_EVENLY, 0.0
						)
				),
				arguments(
						List.of(
								Map.of(
										MAXIMIZE_JOB_SUCCESS_RATIO, 0.25,
										MINIMIZE_USED_BACKUP_POWER, 0.25,
										DISTRIBUTE_TRAFFIC_EVENLY, 0.25)
								,
								Map.of(
										MAXIMIZE_JOB_SUCCESS_RATIO, -0.75,
										MINIMIZE_USED_BACKUP_POWER, -0.75,
										DISTRIBUTE_TRAFFIC_EVENLY, -0.75),
								Map.of(
										MAXIMIZE_JOB_SUCCESS_RATIO, 1.0,
										MINIMIZE_USED_BACKUP_POWER, 2.0,
										DISTRIBUTE_TRAFFIC_EVENLY, 3.0
								)
						),
						Map.of(
								MAXIMIZE_JOB_SUCCESS_RATIO, 0.16666666666666666,
								MINIMIZE_USED_BACKUP_POWER, 0.5,
								DISTRIBUTE_TRAFFIC_EVENLY, 0.8333333333333334)
				)
		);
	}

	@Test
	void shouldCorrectlyReleaseAdaptationAction() {
		// given
		int actionId = 1;
		var actionResults = Map.of(
				MAXIMIZE_JOB_SUCCESS_RATIO, 0.5,
				MINIMIZE_USED_BACKUP_POWER, 0.25,
				DISTRIBUTE_TRAFFIC_EVENLY, -0.25
		);
		var updatedAction = database.updateAdaptationAction(actionId, actionResults);

		// when
		var releasedAction = database.releaseAdaptationAction(actionId);

		// then
		assertThat(releasedAction.getRuns())
				.as("action number of runs should be equal to number of updates")
				.isEqualTo(1);
		assertThat(releasedAction.getActionResults())
				.as("final action results should have the expected values")
				.usingRecursiveComparison()
				.isEqualTo(actionResults);
		assertThat(updatedAction.getAvailable())
				.as("After update action should be unavailable")
				.isFalse();
		assertThat(releasedAction.getAvailable())
				.as("After release action should be available")
				.isTrue();
	}
}
