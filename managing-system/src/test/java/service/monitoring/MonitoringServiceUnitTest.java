package service.monitoring;

import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.monitoring.BackUpPowerUsageService;
import org.greencloud.managingsystem.service.monitoring.JobSuccessRatioService;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.greencloud.managingsystem.service.monitoring.TrafficDistributionService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import com.database.knowledge.domain.goal.AdaptationGoal;
import com.database.knowledge.domain.goal.GoalEnum;
import com.database.knowledge.exception.InvalidGoalIdentifierException;

class MonitoringServiceUnitTest {

	private static AdaptationGoal SUCCESS_RATIO;
	private static AdaptationGoal BACK_UP_POWER;
	private static AdaptationGoal TRAFFIC;

	@Mock
	private static ManagingAgent mockManagingAgent;
	@Mock
	private static JobSuccessRatioService mockJobSuccessRatioService;
	@Mock
	private static BackUpPowerUsageService mockBackUpPowerUsageService;
	@Mock
	private static TrafficDistributionService mockTrafficDistributionService;

	private static MonitoringService monitoringService;

	private static Stream<Arguments> parametersForGetAdaptationGoal() {
		return Stream.of(
				Arguments.of(MAXIMIZE_JOB_SUCCESS_RATIO, SUCCESS_RATIO),
				Arguments.of(MINIMIZE_USED_BACKUP_POWER, BACK_UP_POWER),
				Arguments.of(DISTRIBUTE_TRAFFIC_EVENLY, TRAFFIC)
		);
	}

	private static Stream<Arguments> parametersForSuccessRatioMaximized() {
		return Stream.of(
				Arguments.of(true, false, false),
				Arguments.of(true, true, true),
				Arguments.of(true, false, false)
		);
	}

	@BeforeAll
	static void init() {
		SUCCESS_RATIO = new AdaptationGoal(1, "Maximize job success ratio", 0.5, true, 0.7);
		BACK_UP_POWER = new AdaptationGoal(2, "Minimize used backup power", 0.6, true, 0.1);
		TRAFFIC = new AdaptationGoal(3, "Distribute traffic evenly", 0.7, false, 0.2);

		mockManagingAgent = mock(ManagingAgent.class);
		mockJobSuccessRatioService = mock(JobSuccessRatioService.class);
		mockBackUpPowerUsageService = mock(BackUpPowerUsageService.class);
		mockTrafficDistributionService = mock(TrafficDistributionService.class);

		monitoringService = new MonitoringService(mockManagingAgent);
		monitoringService.setJobSuccessRatioService(mockJobSuccessRatioService);
		monitoringService.setBackUpPowerUsageService(mockBackUpPowerUsageService);
		monitoringService.setTrafficDistributionService(mockTrafficDistributionService);

		doReturn(List.of(SUCCESS_RATIO, BACK_UP_POWER, TRAFFIC)).when(mockManagingAgent).getAdaptationGoalList();
	}

	@ParameterizedTest
	@MethodSource("parametersForGetAdaptationGoal")
	@DisplayName("Test get adaptation goal for valid goal type")
	void testGetAdaptationGoal(final GoalEnum type, final AdaptationGoal expectedResult) {
		assertThat(monitoringService.getAdaptationGoal(type)).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Test get adaptation goal for invalid goal type")
	void testGetAdaptationGoalForInvalid() {
		final GoalEnum mockEnum = mock(GoalEnum.class);
		doReturn(14).when(mockEnum).getAdaptationGoalId();

		assertThatThrownBy(() -> monitoringService.getAdaptationGoal(mockEnum))
				.isInstanceOf(InvalidGoalIdentifierException.class)
				.hasMessage("Goal not found: Goal with identifier 14 was not found");
	}

	@ParameterizedTest
	@MethodSource("parametersForSuccessRatioMaximized")
	@DisplayName("Test is success ratio maximized")
	void testIsSuccessRatioMaximized(final boolean clientRatioBoolean, final boolean componentRatioBoolean,
			final boolean expectedResult) {
		doReturn(clientRatioBoolean).when(mockJobSuccessRatioService).evaluateClientJobSuccessRatio();
		doReturn(componentRatioBoolean).when(mockJobSuccessRatioService).evaluateComponentSuccessRatio();

		assertThat(monitoringService.isSuccessRatioMaximized()).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Test compute system indicator")
	void testComputeSystemIndicator() {
		doReturn(0.8).when(mockJobSuccessRatioService).getJobSuccessRatio();
		doReturn(0.7).when(mockTrafficDistributionService).getAverageTrafficDistribution();
		doReturn(0.5).when(mockBackUpPowerUsageService).getBackUpPowerUsage();

		assertThat(monitoringService.computeSystemIndicator()).isEqualTo(0.67);
	}

	@Test
	@DisplayName("Test getting current goal qualities")
	void testGetCurrentGoalQualities() {
		var expectedResult = Map.of(
				MAXIMIZE_JOB_SUCCESS_RATIO, 0.8,
				MINIMIZE_USED_BACKUP_POWER, 0.5,
				DISTRIBUTE_TRAFFIC_EVENLY, 0.7
		);

		doReturn(0.8).when(mockJobSuccessRatioService).getJobSuccessRatio();
		doReturn(0.7).when(mockTrafficDistributionService).getAverageTrafficDistribution();
		doReturn(0.5).when(mockBackUpPowerUsageService).getBackUpPowerUsage();

		assertThat(monitoringService.getCurrentGoalQualities())
				.as("Map should contain 3 goals")
				.hasSize(3)
				.as("Data of the goals should equal to the expected result")
				.containsExactlyInAnyOrderEntriesOf(expectedResult);
	}
}
