package com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request;

import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request.RequestWeatherData.getJobsTimetable;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.commons.job.ExecutionJobStatusEnum;
import com.greencloud.commons.job.ImmutableServerJob;
import com.greencloud.commons.job.ServerJob;

import jade.core.AID;

class RequestWeatherDataUnitTest {

	@Test
	@DisplayName("Test get jobs timetable with repeatable time instances")
	void testGetJobsTimetableRepeatableInstances() {
		final ServerJob mockCandidatePowerJob = ImmutableServerJob.builder()
				.server(mock(AID.class))
				.jobId("6")
				.power(30)
				.startTime(Instant.parse("2022-01-01T13:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.build();
		final List<Instant> result = getJobsTimetable(mockCandidatePowerJob, setUpMockJobs());

		assertThat(result).hasSize(8)
				.contains(convertToRealTime(Instant.parse("2022-01-01T13:00:00.000Z")))
				.contains(convertToRealTime(Instant.parse("2022-01-01T12:00:00.000Z")));
	}

	@Test
	@DisplayName("Test get jobs timetable with job in processing")
	void testGetJobsTimetableJobInProcessing() {
		final ServerJob mockCandidatePowerJob = ImmutableServerJob.builder().jobId("6").power(30)
				.server(mock(AID.class))
				.startTime(Instant.parse("2022-01-01T13:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.build();
		final ServerJob jobProcessing = ImmutableServerJob.builder().jobId("10")
				.server(mock(AID.class))
				.startTime(Instant.parse("2022-01-01T10:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final Map<ServerJob, ExecutionJobStatusEnum> testJobs = setUpMockJobs();
		testJobs.put(jobProcessing, ExecutionJobStatusEnum.PROCESSING);
		final List<Instant> result = getJobsTimetable(mockCandidatePowerJob, testJobs);

		assertThat(result).hasSize(8)
				.contains(convertToRealTime(Instant.parse("2022-01-01T13:00:00.000Z")))
				.contains(convertToRealTime(Instant.parse("2022-01-01T12:00:00.000Z")))
				.doesNotContain(convertToRealTime(Instant.parse("2022-01-01T13:30:00.000Z")));
	}

	// MOCK DATA

	/**
	 * Class creates mock jobs used in test scenarios.
	 * The following structure was used:
	 *
	 * ServerJob1 -> power: 10, time: 08:00 - 10:00, status: IN_PROGRESS,
	 * ServerJob2 -> power: 50,  time: 06:00 - 15:00, status: ON_HOLD
	 * ServerJob3 -> power: 25, time: 11:00 - 12:00, status: ACCEPTED
	 */
	private Map<ServerJob, ExecutionJobStatusEnum> setUpMockJobs() {
		final ServerJob mockJob1 = ImmutableServerJob.builder().jobId("1")
				.server(mock(AID.class))
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final ServerJob mockJob2 = ImmutableServerJob.builder().jobId("2")
				.server(mock(AID.class))
				.startTime(Instant.parse("2022-01-01T06:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T15:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(50).build();
		final ServerJob mockJob3 = ImmutableServerJob.builder().jobId("3")
				.server(mock(AID.class))
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(25).build();
		final Map<ServerJob, ExecutionJobStatusEnum> mockJobMap = new HashMap<>();
		mockJobMap.put(mockJob1, ExecutionJobStatusEnum.IN_PROGRESS);
		mockJobMap.put(mockJob2, ExecutionJobStatusEnum.ON_HOLD_PLANNED);
		mockJobMap.put(mockJob3, ExecutionJobStatusEnum.ACCEPTED);
		return mockJobMap;
	}
}
