package com.greencloud.application.agents.client.management;

import static com.greencloud.application.utils.TimeUtils.useMockTime;
import static com.greencloud.commons.job.JobStatusEnum.CREATED;
import static com.greencloud.commons.job.JobStatusEnum.DELAYED;
import static com.greencloud.commons.job.JobStatusEnum.FINISHED;
import static com.greencloud.commons.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.JobStatusEnum.ON_BACK_UP;
import static com.greencloud.commons.job.JobStatusEnum.ON_HOLD;
import static com.greencloud.commons.job.JobStatusEnum.PROCESSED;
import static com.greencloud.commons.job.JobStatusEnum.SCHEDULED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.quality.Strictness.LENIENT;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.JobPart;
import com.greencloud.commons.job.JobStatusEnum;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ClientStateManagementUnitTest {

	@Mock
	private static ClientAgent mockClient;
	@Mock
	private static ClientStateManagement mockClientManagement;

	static private Stream<Arguments> jobStatusesIncorrect() {
		return Stream.of(
				arguments(SCHEDULED, SCHEDULED, CREATED, CREATED),
				arguments(FINISHED, IN_PROGRESS, FINISHED, IN_PROGRESS),
				arguments(PROCESSED, DELAYED, PROCESSED, IN_PROGRESS),
				arguments(DELAYED, IN_PROGRESS, PROCESSED, IN_PROGRESS),
				arguments(ON_BACK_UP, IN_PROGRESS, ON_HOLD, ON_HOLD),
				arguments(IN_PROGRESS, IN_PROGRESS, ON_BACK_UP, ON_BACK_UP)
		);
	}

	static private Stream<Arguments> jobStatusesCorrect() {
		return Stream.of(
				arguments(SCHEDULED, SCHEDULED, SCHEDULED, CREATED),
				arguments(FINISHED, FINISHED, FINISHED, IN_PROGRESS),
				arguments(PROCESSED, CREATED, PROCESSED, SCHEDULED),
				arguments(DELAYED, PROCESSED, PROCESSED, PROCESSED),
				arguments(ON_BACK_UP, IN_PROGRESS, IN_PROGRESS, IN_PROGRESS),
				arguments(ON_BACK_UP, PROCESSED, PROCESSED, PROCESSED),
				arguments(IN_PROGRESS, PROCESSED, PROCESSED, PROCESSED),
				arguments(IN_PROGRESS, IN_PROGRESS, IN_PROGRESS, PROCESSED),
				arguments(ON_HOLD, IN_PROGRESS, IN_PROGRESS, IN_PROGRESS)
		);
	}

	static private Stream<Arguments> partsCorrectness() {
		return Stream.of(
				arguments(SCHEDULED, SCHEDULED, SCHEDULED, true),
				arguments(FINISHED, FINISHED, SCHEDULED, false)
		);
	}

	@BeforeEach
	void setUp() {
		mockClientManagement = spy(new ClientStateManagement(mockClient));
	}

	@Test
	@DisplayName("Test update job status duration map")
	void testUpdateJobStatusDuration() throws InterruptedException {
		mockClientManagement.setCurrentJobStatus(PROCESSED);
		useMockTime(Instant.parse("2022-01-01T10:00:00.000Z"), ZoneId.of("UTC"));
		mockClientManagement.getTimer().startTimeMeasure();
		useMockTime(Instant.parse("2022-01-01T10:00:02.000Z"), ZoneId.of("UTC"));
		mockClientManagement.updateJobStatusDuration(IN_PROGRESS);

		var result = mockClientManagement.jobStatusDurationMap;
		var notProcessed = result.entrySet().stream()
				.filter(e -> !e.getKey().equals(PROCESSED))
				.map(Map.Entry::getValue).toList();

		assertThat(result).hasSize(10);
		assertThat(result.get(PROCESSED)).isCloseTo(2000, Offset.offset(50L));
		assertThat(notProcessed).are(new Condition<>(val -> val == 0L, "isZero"));
	}

	@ParameterizedTest
	@MethodSource("jobStatusesIncorrect")
	@DisplayName("Test update original job status when update should not happen")
	void testUpdateOriginalJobStatusFail(JobStatusEnum newStatus, JobStatusEnum jobPart1Status,
			JobStatusEnum jobPart2Status, JobStatusEnum currentStatus) {
		mockClientManagement.setCurrentJobStatus(currentStatus);
		doReturn(Map.of(
				"1#1", new JobPart(null, jobPart1Status, null, null, null),
				"1#2", new JobPart(null, jobPart2Status, null, null, null)
		)).when(mockClient).getJobParts();

		mockClientManagement.updateOriginalJobStatus(newStatus);
		assertThat(mockClientManagement.getCurrentJobStatus()).isNotEqualTo(newStatus);
		assertThat(mockClientManagement.getCurrentJobStatus()).isEqualTo(currentStatus);
	}

	@ParameterizedTest
	@MethodSource("jobStatusesCorrect")
	@DisplayName("Test update original job status when update should succeed")
	void testUpdateOriginalJobStatusSucceed(JobStatusEnum newStatus, JobStatusEnum jobPart1Status,
			JobStatusEnum jobPart2Status, JobStatusEnum currentStatus) {
		mockClientManagement.setCurrentJobStatus(currentStatus);
		doReturn(Map.of(
				"1#1", new JobPart(null, jobPart1Status, null, null, null),
				"1#2", new JobPart(null, jobPart2Status, null, null, null)
		)).when(mockClient).getJobParts();

		mockClientManagement.updateOriginalJobStatus(newStatus);
		assertThat(mockClientManagement.getCurrentJobStatus()).isEqualTo(newStatus);
	}

	@ParameterizedTest
	@MethodSource("partsCorrectness")
	@DisplayName("Test check if all parts match given status")
	void testCheckIfAllPartsMatchStatus(JobStatusEnum statusToCheck, JobStatusEnum jobPart1Status,
			JobStatusEnum jobPart2Status, boolean result) {
		doReturn(Map.of(
				"1#1", new JobPart(null, jobPart1Status, null, null, null),
				"1#2", new JobPart(null, jobPart2Status, null, null, null)
		)).when(mockClient).getJobParts();

		assertThat(mockClientManagement.checkIfAllPartsMatchStatus(statusToCheck)).isEqualTo(result);
	}
}
