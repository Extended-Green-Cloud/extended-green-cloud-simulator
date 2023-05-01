package com.greencloud.application.mapper;

import static com.greencloud.application.mapper.JobMapper.mapJobToPowerJob;
import static com.greencloud.application.mapper.JobMapper.mapToClientJobRealTime;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.mapper.JobMapper.mapToJobNewStartTime;
import static com.greencloud.application.mapper.JobMapper.mapToJobWithNewTime;
import static com.greencloud.application.mapper.JobMapper.mapToPowerShortageJob;
import static com.greencloud.application.mapper.JobMapper.mapToServerJob;
import static com.greencloud.application.mapper.JobMapper.mapToServerJobRealTime;
import static com.greencloud.application.utils.TimeUtils.setSystemStartTime;
import static java.time.Instant.parse;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobPowerShortageTransfer;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.ImmutableClientJob;
import com.greencloud.commons.domain.job.ImmutablePowerJob;
import com.greencloud.commons.domain.job.ImmutableServerJob;
import com.greencloud.commons.domain.job.PowerJob;
import com.greencloud.commons.domain.job.ServerJob;

import jade.core.AID;

class JobMapperUnitTest {

	private static final ServerJob MOCK_SERVER_JOB = ImmutableServerJob.builder()
			.server(new AID("test_aid", AID.ISGUID))
			.jobId("1")
			.startTime(parse("2022-01-01T09:00:00.000Z"))
			.endTime(parse("2022-01-01T10:00:00.000Z"))
			.deadline(parse("2022-01-01T20:00:00.000Z"))
			.power(10)
			.build();

	private static final ClientJob MOCK_CLIENT_JOB = ImmutableClientJob.builder()
			.clientIdentifier("test_client")
			.jobId("1")
			.startTime(parse("2022-01-01T09:00:00.000Z"))
			.endTime(parse("2022-01-01T10:00:00.000Z"))
			.deadline(parse("2022-01-01T12:00:00.000Z"))
			.power(10)
			.build();

	@Test
	@DisplayName("Test map client job to power job")
	void testMapClientJobToPowerJob() {
		final PowerJob result = mapJobToPowerJob(MOCK_CLIENT_JOB);

		final Instant expectedStart = parse("2022-01-01T09:00:00.000Z");
		final Instant expectedEnd = parse("2022-01-01T10:00:00.000Z");
		final Instant expectedDeadline = parse("2022-01-01T12:00:00.000Z");

		assertThat(result.getDeadline()).isEqualTo(expectedDeadline);
		assertThat(result.getStartTime()).isEqualTo(expectedStart);
		assertThat(result.getEndTime()).isEqualTo(expectedEnd);
		assertThat(result.getPower()).isEqualTo(10);
		assertThat(result.getJobId()).isEqualTo("1");
	}

	@Test
	@DisplayName("Test map to server job with real time")
	void testMapToServerJobRealTime() {
		setSystemStartTime(parse("2022-01-01T08:00:00.000Z"));
		final Instant expectedStart = parse("2022-01-31T08:00:00.000Z");
		final Instant expectedEnd = parse("2022-03-02T08:00:00.000Z");

		final ServerJob result = mapToServerJobRealTime(MOCK_SERVER_JOB);

		assertThat(result.getStartTime()).isEqualTo(expectedStart);
		assertThat(result.getEndTime()).isEqualTo(expectedEnd);
		assertThat(result.getPower()).isEqualTo(10);
		assertThat(result.getServer().getLocalName()).isEqualTo("test_aid");
	}

	@Test
	@DisplayName("Test map to client job with real time")
	void testMapToClientJobRealTime() {
		setSystemStartTime(parse("2022-01-01T08:00:00.000Z"));
		final Instant expectedStart = parse("2022-01-31T08:00:00.000Z");
		final Instant expectedEnd = parse("2022-03-02T08:00:00.000Z");
		final Instant expectedDeadline = parse("2022-05-01T08:00:00.000Z");

		final ClientJob result = mapToClientJobRealTime(MOCK_CLIENT_JOB);

		assertThat(result.getStartTime()).isEqualTo(expectedStart);
		assertThat(result.getEndTime()).isEqualTo(expectedEnd);
		assertThat(result.getDeadline()).isEqualTo(expectedDeadline);
		assertThat(result.getPower()).isEqualTo(10);
		assertThat(result.getPower()).isEqualTo(10);
		assertThat(result.getJobId()).isEqualTo("1");
	}

	@Test
	@DisplayName("Test map to client job with new start time")
	void testMapToJobNewStartTime() {
		final Instant newStart = parse("2022-01-01T07:00:00.000Z");

		final ClientJob result = mapToJobNewStartTime(MOCK_CLIENT_JOB, newStart);

		final Instant expectedEnd = parse("2022-01-01T10:00:00.000Z");
		final Instant expectedDeadline = parse("2022-01-01T12:00:00.000Z");

		assertThat(result.getDeadline()).isEqualTo(expectedDeadline);
		assertThat(result.getStartTime()).isEqualTo(newStart);
		assertThat(result.getEndTime()).isEqualTo(expectedEnd);
		assertThat(result.getPower()).isEqualTo(10);
		assertThat(result.getJobId()).isEqualTo("1");
		assertThat(result.getClientIdentifier()).isEqualTo("test_client");
	}

	@Test
	@DisplayName("Test map to client job with new time frames")
	void testMapToJobNewTime() {
		final Instant newStart = parse("2022-01-01T07:00:00.000Z");
		final Instant newEnd = parse("2022-01-01T11:00:00.000Z");

		final ClientJob result = mapToJobWithNewTime(MOCK_CLIENT_JOB, newStart, newEnd);
		final Instant expectedDeadline = parse("2022-01-01T12:00:00.000Z");

		assertThat(result.getDeadline()).isEqualTo(expectedDeadline);
		assertThat(result.getStartTime()).isEqualTo(newStart);
		assertThat(result.getEndTime()).isEqualTo(newEnd);
		assertThat(result.getPower()).isEqualTo(10);
		assertThat(result.getJobId()).isEqualTo("1");
		assertThat(result.getClientIdentifier()).isEqualTo("test_client");
	}

	@Test
	@DisplayName("Test map to server job from power job and server AID")
	void testMapToServer() {
		final AID testAID = new AID("test_AID", AID.ISGUID);
		final PowerJob testJob = ImmutablePowerJob.builder()
				.jobId("1")
				.startTime(parse("2022-01-01T09:00:00.000Z"))
				.endTime(parse("2022-01-01T10:00:00.000Z"))
				.deadline(parse("2022-01-01T20:00:00.000Z"))
				.power(10)
				.build();

		final ServerJob result = mapToServerJob(testJob, testAID);

		assertThat(result.getServer().getLocalName()).isEqualTo("test_AID");
		assertThat(result.getPower()).isEqualTo(10);
		assertThat(result.getDeadline()).isEqualTo(parse("2022-01-01T20:00:00.000Z"));
	}

	@Test
	@DisplayName("Test map to job instance id from power job")
	void testMapToJobInstanceIdFromPowerJob() {
		final JobInstanceIdentifier result = mapToJobInstanceId(MOCK_SERVER_JOB);

		assertThat(result.getStartTime()).isEqualTo(parse("2022-01-01T09:00:00.000Z"));
		assertThat(result.getJobId()).isEqualTo("1");
	}

	@Test
	@DisplayName("Test map client job to job instance id")
	void testMapClientJobToJobInstanceId() {
		final JobInstanceIdentifier result = mapToJobInstanceId(MOCK_CLIENT_JOB);

		assertThat(result.getStartTime()).isEqualTo(parse("2022-01-01T09:00:00.000Z"));
		assertThat(result.getJobId()).isEqualTo("1");
	}

	@Test
	@DisplayName("Test to job instance id with new start")
	void testMapToJobInstanceIdWithStartTime() {
		final Instant startTime = parse("2022-01-01T07:00:00.000Z");
		final JobInstanceIdentifier jobInstance = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(parse("2022-01-01T09:00:00.000Z"))
				.jobInstanceId("40f")
				.build();

		final JobInstanceIdentifier result = mapToJobInstanceId(jobInstance, startTime);

		assertThat(result.getStartTime()).isEqualTo(startTime);
		assertThat(result.getJobId()).isEqualTo("1");
	}
}
