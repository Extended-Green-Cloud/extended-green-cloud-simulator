package com.greencloud.application.mapper;

import static com.greencloud.application.mapper.JobMapper.mapServerJobToPowerJob;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.mapper.JobMapper.mapToServerJob;
import static com.greencloud.application.mapper.JobMapper.mapToServerJobRealTime;
import static com.greencloud.application.utils.TimeUtils.setSystemStartTimeMock;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.commons.job.ImmutablePowerJob;
import com.greencloud.commons.job.ImmutableServerJob;
import com.greencloud.commons.job.PowerJob;
import com.greencloud.commons.job.ServerJob;

import jade.core.AID;

class JobMapperUnitTest {

	private static final ServerJob MOCK_SERVER_JOB = ImmutableServerJob.builder()
			.server(new AID("test_aid", AID.ISGUID))
			.jobId("1")
			.startTime(Instant.parse("2022-01-01T09:00:00.000Z"))
			.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
			.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
			.power(10)
			.build();

	@Test
	@DisplayName("Test map to server job with real time")
	void testMapToServerJobRealTime() {
		setSystemStartTimeMock(Instant.parse("2022-01-01T08:00:00.000Z"));
		final Instant expectedStart = Instant.parse("2022-01-31T08:00:00.000Z");
		final Instant expectedEnd = Instant.parse("2022-03-02T08:00:00.000Z");

		final ServerJob result = mapToServerJobRealTime(MOCK_SERVER_JOB);

		assertThat(result.getStartTime()).isEqualTo(expectedStart);
		assertThat(result.getEndTime()).isEqualTo(expectedEnd);
		assertThat(result.getPower()).isEqualTo(10);
		assertThat(result.getServer().getLocalName()).isEqualTo("test_aid");
	}

	@Test
	@DisplayName("Test map to server job from power job and server AID")
	void testMapToServer() {
		final AID testAID = new AID("test_AID", AID.ISGUID);
		final PowerJob testJob = ImmutablePowerJob.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T09:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10)
				.build();

		final ServerJob result = mapToServerJob(testJob, testAID);

		assertThat(result.getServer().getLocalName()).isEqualTo("test_AID");
		assertThat(result.getPower()).isEqualTo(10);
		assertThat(result.getDeadline()).isEqualTo(Instant.parse("2022-01-01T20:00:00.000Z"));
	}

	@Test
	@DisplayName("Test map to power job from server job")
	void testMapServerJobToPowerJob() {
		final PowerJob expectedJob = ImmutablePowerJob.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T09:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10)
				.build();

		final PowerJob result = mapServerJobToPowerJob(MOCK_SERVER_JOB);

		assertThat(result).isEqualTo(expectedJob);
	}

	@Test
	@DisplayName("Test map to job instance id from power job")
	void testMapToJobInstanceIdFromPowerJob() {
		final JobInstanceIdentifier result = mapToJobInstanceId(MOCK_SERVER_JOB);

		assertThat(result.getStartTime()).isEqualTo(Instant.parse("2022-01-01T09:00:00.000Z"));
		assertThat(result.getJobId()).isEqualTo("1");
	}

	@Test
	@DisplayName("Test map to job instance id")
	void testMapToJobInstanceId() {
		final Instant mockStart = Instant.parse("2022-01-01T08:00:00.000Z");
		final JobInstanceIdentifier result = mapToJobInstanceId("1", mockStart);

		assertThat(result.getStartTime()).isEqualTo(Instant.parse("2022-01-01T08:00:00.000Z"));
		assertThat(result.getJobId()).isEqualTo("1");
	}
}
