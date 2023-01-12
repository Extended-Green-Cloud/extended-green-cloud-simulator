package com.database.knowledge.timescale;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationActions;
import static com.database.knowledge.domain.agent.DataType.CLIENT_MONITORING;
import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.domain.agent.DataType.PROCESSED_API_REQUEST;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.database.knowledge.domain.agent.DataType.WEATHER_SHORTAGES;
import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static com.greencloud.commons.job.ClientJobStatusEnum.CREATED;
import static com.greencloud.commons.job.ClientJobStatusEnum.FAILED;
import static com.greencloud.commons.job.ClientJobStatusEnum.FINISHED;
import static com.greencloud.commons.job.ClientJobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.ClientJobStatusEnum.PROCESSED;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
import com.database.knowledge.domain.agent.client.ClientMonitoringData;
import com.database.knowledge.domain.agent.client.ImmutableClientMonitoringData;
import com.database.knowledge.domain.agent.greensource.GreenSourceMonitoringData;
import com.database.knowledge.domain.agent.greensource.ImmutableGreenSourceMonitoringData;
import com.database.knowledge.domain.agent.greensource.WeatherShortages;
import com.database.knowledge.domain.agent.monitoring.ImmutableProcessedApiRequest;
import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.database.knowledge.domain.goal.AdaptationGoal;
import com.database.knowledge.domain.goal.GoalEnum;
import com.database.knowledge.domain.systemquality.SystemQuality;

@ExtendWith(MockitoExtension.class)
class TimescaleDatabaseIntegrationTest {

	private TimescaleDatabase database;

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

	@BeforeEach
	void init() {
		database = TimescaleDatabase.setUpForTests();
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
	void shouldSuccessfullySaveSystemQualityDataToDatabase() {

		database.writeSystemQualityData(1, 0.8);
		database.writeSystemQualityData(1, 0.5);
		database.writeSystemQualityData(1, 0.6);
		database.writeSystemQualityData(1, 0.7);

		List<SystemQuality> result = database.readSystemQualityData(1, 30);
		List<SystemQuality> resultLimit = database.readSystemQualityData(1, 2);

		// then
		assertThat(result)
				.as("After that data has size 4.")
				.hasSize(4)
				.as("Has correct structure")
				.allMatch((value) -> List.of(0.8, 0.5, 0.6, 0.7).contains(value.quality()) && value.goalId() == 1);
		assertThat(resultLimit)
				.as("After that result limited data has size 2.")
				.hasSize(2)
				.as("Has correct structure")
				.allMatch((value) -> List.of(0.6, 0.7).contains(value.quality()) && value.goalId() == 1);

	}

	@Test
	void shouldCorrectlyReadAdaptationGoals() {
		// given
		var expectedAdaptationGoals = List.of(
				new AdaptationGoal(1, "Maximize job success ratio", 0.8, true, 0.6),
				new AdaptationGoal(2, "Minimize used backup power", 0.2, false, 0.2),
				new AdaptationGoal(3, "Distribute traffic evenly", 1.0, false, 0.2)
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
		database.updateAdaptationAction(actionId, actionResults);

		// when
		var blockedAction = database.setAdaptationActionAvailability(actionId, false);
		var releasedAction = database.setAdaptationActionAvailability(actionId, true);

		// then
		assertThat(releasedAction.getRuns())
				.as("action number of runs should be equal to number of updates")
				.isEqualTo(1);
		assertThat(releasedAction.getActionResults())
				.as("final action results should have the expected values")
				.usingRecursiveComparison()
				.isEqualTo(actionResults);
		assertThat(blockedAction.getAvailable())
				.as("After update action should be unavailable")
				.isFalse();
		assertThat(releasedAction.getAvailable())
				.as("After release action should be available")
				.isTrue();
	}

	@Test
	void shouldCorrectlyReadMonitoringDataFieldsForDataType() throws InterruptedException {
		var data1 = ImmutableGreenSourceMonitoringData.builder()
				.weatherPredictionError(0.02)
				.currentTraffic(10)
				.successRatio(0.9)
				.isBeingDisconnected(false)
				.build();
		var data2 = ImmutableGreenSourceMonitoringData.builder()
				.weatherPredictionError(0.04)
				.currentTraffic(15)
				.successRatio(0.5)
				.isBeingDisconnected(false)
				.build();

		database.writeMonitoringData("test_data_1", GREEN_SOURCE_MONITORING, data1);
		TimeUnit.SECONDS.sleep(1);
		database.writeMonitoringData("test_data_1", GREEN_SOURCE_MONITORING, data2);

		var result = database.readMonitoringDataForDataTypes(singletonList(GREEN_SOURCE_MONITORING), 5);

		assertThat(result)
				.as("Read data for green source should have size %d", 2)
				.hasSize(2);

		assertThat(result.get(0).monitoringData())
				.as("Read data for first green source should have correct field values")
				.isInstanceOfSatisfying(GreenSourceMonitoringData.class, data -> {
					assertThat(data.getSuccessRatio()).isEqualTo(0.5);
					assertThat(data.getWeatherPredictionError()).isEqualTo(0.04);
					assertThat(data.getCurrentTraffic()).isEqualTo(15);
				});
		assertThat(result.get(1).monitoringData())
				.as("Read data for first green source should have correct field values")
				.isInstanceOfSatisfying(GreenSourceMonitoringData.class, data -> {
					assertThat(data.getSuccessRatio()).isEqualTo(0.9);
					assertThat(data.getWeatherPredictionError()).isEqualTo(0.02);
					assertThat(data.getCurrentTraffic()).isEqualTo(10);
				});
		assertThat(result.get(0).aid())
				.as("Resulted data has correct aid")
				.isEqualTo("test_data_1");
	}

	@Test
	void shouldCorrectlyReadLastMonitoringDataFieldsForDataType() throws InterruptedException {
		final GreenSourceMonitoringData data1 = ImmutableGreenSourceMonitoringData.builder()
				.weatherPredictionError(0.02)
				.currentTraffic(10)
				.successRatio(0.9)
				.isBeingDisconnected(false)
				.build();
		final GreenSourceMonitoringData data2 = ImmutableGreenSourceMonitoringData.builder()
				.weatherPredictionError(0.04)
				.currentTraffic(15)
				.successRatio(0.5)
				.isBeingDisconnected(true)
				.build();

		database.writeMonitoringData("test_data_1", GREEN_SOURCE_MONITORING, data1);
		TimeUnit.SECONDS.sleep(1);
		database.writeMonitoringData("test_data_1", GREEN_SOURCE_MONITORING, data2);

		var result = database.readLastMonitoringDataForDataTypes(singletonList(GREEN_SOURCE_MONITORING));

		assertThat(result)
				.as("Resulted data for green source should have size 1")
				.hasSize(1);

		assertThat(result.get(0).monitoringData())
				.as("Resulted data for first green source should have correct field values")
				.isInstanceOfSatisfying(GreenSourceMonitoringData.class, data -> {
					assertThat(data.getSuccessRatio()).isEqualTo(0.5);
					assertThat(data.getWeatherPredictionError()).isEqualTo(0.04);
					assertThat(data.getCurrentTraffic()).isEqualTo(15);
					assertThat(data.isBeingDisconnected()).isTrue();
				});
		assertThat(result.get(0).aid())
				.as("Resulted data has correct aid")
				.isEqualTo("test_data_1");
	}

	@Test
	void shouldCorrectlyReadLastMonitoringDataFieldsForDataTypeInPeriod() {
		var inputData = prepareMonitoredDataForTest();
		AtomicInteger i = new AtomicInteger();

		inputData.forEach(data -> {
			final String aid = "test_aid" + i.get();
			database.writeMonitoringData(aid, data.getKey(), data.getValue());
			i.getAndIncrement();
		});

		var resultClient = database.readLastMonitoringDataForDataTypes(singletonList(CLIENT_MONITORING), 500);
		var resultServer = database.readLastMonitoringDataForDataTypes(singletonList(SERVER_MONITORING), 500);

		assertThat(resultClient)
				.as("Resulted data for client should have size 2")
				.hasSize(2)
				.as("Resulted data for client should be of type ClientMonitoringData")
				.allMatch((data) -> data.monitoringData() instanceof ClientMonitoringData)
				.as("Resulted data for client should have correct statuses")
				.allMatch((data) -> List.of(IN_PROGRESS, FINISHED)
						.contains(((ClientMonitoringData) data.monitoringData()).getCurrentJobStatus()));
		assertThat(resultClient.get(0).monitoringData())
				.as("Resulted data for first client should have correct field values")
				.isInstanceOfSatisfying(ClientMonitoringData.class, data -> {
					assertThat(data.getIsFinished()).isFalse();
					assertThat(data.getJobStatusDurationMap()).containsEntry(CREATED, 10L);
				});
		assertThat(resultServer)
				.as("Resulted data for server should have size 1")
				.hasSize(1);
		assertThat(resultServer.get(0).monitoringData())
				.as("Resulted data for server should have correct field values")
				.isInstanceOfSatisfying(ServerMonitoringData.class, data -> {
					assertThat(data.getSuccessRatio()).isEqualTo(0.8);
					assertThat(data.getCurrentBackUpPowerUsage()).isEqualTo(0.7);
				});
	}

	@Test
	void shouldCorrectlyReadMonitoringDataForDataTypeAfterTimeout() throws InterruptedException {
		var oldData = prepareMonitoredDataForTest();
		var newData = ImmutableClientMonitoringData.builder()
				.currentJobStatus(FAILED)
				.isFinished(true)
				.jobStatusDurationMap(Map.of(CREATED, 10L, PROCESSED, 15L))
				.build();

		AtomicInteger i = new AtomicInteger();
		oldData.forEach(data -> {
			final String aid = "test_aid" + i.get();
			database.writeMonitoringData(aid, data.getKey(), data.getValue());
			i.getAndIncrement();
		});

		var resultBefore = database.readLastMonitoringDataForDataTypes(singletonList(CLIENT_MONITORING), 50);

		TimeUnit.SECONDS.sleep(2);
		database.writeMonitoringData("test_new_aid", CLIENT_MONITORING, newData);

		var resultNew = database.readLastMonitoringDataForDataTypes(singletonList(CLIENT_MONITORING), 1);

		assertThat(resultBefore)
				.as("Old data result for client should have size 2")
				.hasSize(2);
		assertThat(resultNew)
				.as("New data result for client should have size 1")
				.hasSize(1);
		assertThat(resultNew.get(0).monitoringData())
				.as("New data result for client should have correct fields")
				.isInstanceOfSatisfying(ClientMonitoringData.class, data -> {
					assertThat(data.getIsFinished()).isTrue();
					assertThat(data.getCurrentJobStatus()).isEqualTo(FAILED);
				});

	}

	@Test
	void shouldCorrectlyReadMonitoringDataForDataTypeAndAIDAfterTimeout() {
		final List<String> aidOfInterest = List.of("test_aid1", "test_aid2");
		var expectedContent = prepareMonitoredDataForAIDTest();

		var result = database.readMonitoringDataForDataTypeAndAID(WEATHER_SHORTAGES, aidOfInterest, 1000);

		assertThat(result)
				.as("Result should have size 3")
				.hasSize(3)
				.as("Result should contain correct data type and aid")
				.allMatch((data) -> data.dataType().equals(WEATHER_SHORTAGES) && aidOfInterest.contains(data.aid()))
				.as("Result should contain correct data")
				.allMatch((data) -> expectedContent.contains(data.monitoringData()));
	}

	@Test
	void shouldCorrectlyReadLatestNRowsMonitoringDataForDataTypeAndAID() {
		final ServerMonitoringData data1 = ImmutableServerMonitoringData.builder()
				.successRatio(0.5)
				.currentBackUpPowerUsage(0.4)
				.currentMaximumCapacity(30)
				.currentTraffic(10)
				.build();
		final ServerMonitoringData data2 = ImmutableServerMonitoringData.builder()
				.successRatio(0.6)
				.currentBackUpPowerUsage(0.5)
				.currentMaximumCapacity(30)
				.currentTraffic(10)
				.build();
		final ServerMonitoringData data3 = ImmutableServerMonitoringData.builder()
				.successRatio(0.7)
				.currentBackUpPowerUsage(0.6)
				.currentMaximumCapacity(30)
				.currentTraffic(10)
				.build();
		final ServerMonitoringData data4 = ImmutableServerMonitoringData.builder()
				.successRatio(0.8)
				.currentBackUpPowerUsage(0.7)
				.currentMaximumCapacity(30)
				.currentTraffic(10)
				.build();
		database.writeMonitoringData("test_aid1", SERVER_MONITORING, data1);
		database.writeMonitoringData("test_aid1", SERVER_MONITORING, data2);
		database.writeMonitoringData("test_aid2", SERVER_MONITORING, data3);
		database.writeMonitoringData("test_aid3", SERVER_MONITORING, data4);

		var result = database.readLatestNRowsMonitoringDataForDataTypeAndAID(SERVER_MONITORING,
				List.of("test_aid1", "test_aid2"),
				1);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).monitoringData()).isEqualTo(data2);
		assertThat(result.get(1).monitoringData()).isEqualTo(data3);
	}


   private List<WeatherShortages> prepareMonitoredDataForAIDTest() {
		final WeatherShortages data1 = new WeatherShortages(1, 1000);
		final WeatherShortages data2 = new WeatherShortages(10, 1000);
		final WeatherShortages data3 = new WeatherShortages(3, 1000);
		final WeatherShortages data4 = new WeatherShortages(5, 1000);
		final WeatherShortages data5 = new WeatherShortages(6, 1000);
		final ClientMonitoringData data6 = ImmutableClientMonitoringData.builder()
				.currentJobStatus(IN_PROGRESS)
				.isFinished(false)
				.jobStatusDurationMap(Map.of(CREATED, 10L, PROCESSED, 15L))
				.build();
		final ClientMonitoringData data7 = ImmutableClientMonitoringData.builder()
				.currentJobStatus(FINISHED)
				.isFinished(true)
				.jobStatusDurationMap(Map.of(CREATED, 10L, PROCESSED, 10L, IN_PROGRESS, 25L))
				.build();

		database.writeMonitoringData("test_aid1", WEATHER_SHORTAGES, data1);
		database.writeMonitoringData("test_aid1", WEATHER_SHORTAGES, data2);
		database.writeMonitoringData("test_aid2", WEATHER_SHORTAGES, data3);
		database.writeMonitoringData("test_aid3", WEATHER_SHORTAGES, data4);
		database.writeMonitoringData("test_aid3", WEATHER_SHORTAGES, data5);
		database.writeMonitoringData("test_aid2", CLIENT_MONITORING, data6);
		database.writeMonitoringData("test_aid1", CLIENT_MONITORING, data7);

		return List.of(data1, data2, data3);
	}

	private List<Map.Entry<DataType, MonitoringData>> prepareMonitoredDataForTest() {
		final ClientMonitoringData data1 = ImmutableClientMonitoringData.builder()
				.currentJobStatus(IN_PROGRESS)
				.isFinished(false)
				.jobStatusDurationMap(Map.of(CREATED, 10L, PROCESSED, 15L))
				.build();
		final ClientMonitoringData data2 = ImmutableClientMonitoringData.builder()
				.currentJobStatus(FINISHED)
				.isFinished(true)
				.jobStatusDurationMap(Map.of(CREATED, 10L, PROCESSED, 10L, IN_PROGRESS, 25L))
				.build();
		final ServerMonitoringData data3 = ImmutableServerMonitoringData.builder()
				.currentMaximumCapacity(100)
				.currentTraffic(0.7)
				.successRatio(0.8)
				.currentBackUpPowerUsage(0.7)
				.build();
		return List.of(
				new AbstractMap.SimpleEntry<>(CLIENT_MONITORING, data1),
				new AbstractMap.SimpleEntry<>(CLIENT_MONITORING, data2),
				new AbstractMap.SimpleEntry<>(SERVER_MONITORING, data3)
		);
	}
}
