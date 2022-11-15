package com.greencloud.application.mapper;


import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceIdWithRealTime;
import static com.greencloud.application.mapper.JobMapper.mapToPowerJobRealTime;
import static com.greencloud.application.utils.TimeUtils.setSystemStartTimeMock;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.commons.job.ImmutablePowerJob;
import com.greencloud.commons.job.PowerJob;

class JobMapperTest {

	private static final PowerJob MOCK_POWER_JOB = ImmutablePowerJob.builder()
			.jobId("1")
			.startTime(Instant.parse("2022-01-01T09:00:00.000Z"))
			.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
			.deadline(Instant.parse("2022-01-01T10:30:00.000Z"))
			.power(10)
			.build();
	private static final JobInstanceIdentifier MOCK_JOB_INSTANCE = ImmutableJobInstanceIdentifier.builder()
			.jobId("1")
			.startTime(Instant.parse("2022-01-01T09:00:00.000Z"))
			.build();

	@Test
	@DisplayName("Test map to power job with real time")
	void testMapToPowerJobRealTime() {
		setSystemStartTimeMock(Instant.parse("2022-01-01T08:00:00.000Z"));
		final Instant expectedStart = Instant.parse("2022-01-31T08:00:00.000Z");
		final Instant expectedEnd = Instant.parse("2022-03-02T08:00:00.000Z");

		final PowerJob result = mapToPowerJobRealTime(MOCK_POWER_JOB);

		assertThat(result.getStartTime()).isEqualTo(expectedStart);
		assertThat(result.getEndTime()).isEqualTo(expectedEnd);
		assertThat(result.getPower()).isEqualTo(10);
	}

	@Test
	@DisplayName("Test map to job instance id from power job")
	void testMapToJobInstanceIdFromPowerJob() {
		final JobInstanceIdentifier result = mapToJobInstanceId(MOCK_POWER_JOB);

		assertThat(result.getStartTime()).isEqualTo(Instant.parse("2022-01-01T09:00:00.000Z"));
		assertThat(result.getJobId()).isEqualTo("1");
	}

	@Test
	@DisplayName("Test map to job instance id with real time")
	void testMapToJobInstanceIdWithRealTime() {
		setSystemStartTimeMock(Instant.parse("2022-01-01T08:00:00.000Z"));
		final JobInstanceIdentifier result = mapToJobInstanceIdWithRealTime(MOCK_JOB_INSTANCE);

		assertThat(result.getStartTime()).isEqualTo(Instant.parse("2022-01-31T08:00:00.000Z"));
		assertThat(result.getJobId()).isEqualTo("1");
	}
}
