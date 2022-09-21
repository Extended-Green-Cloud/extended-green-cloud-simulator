package com.greencloud.application.utils;

import com.greencloud.application.domain.job.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.greencloud.application.domain.job.JobStatusEnum.ON_HOLD_TRANSFER;
import static com.greencloud.application.utils.JobMapUtils.*;
import static java.time.Instant.parse;
import static org.mockito.quality.Strictness.LENIENT;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class JobMapUtilsUnitTest {

    private static final Instant MOCK_NOW = parse("2022-01-01T11:00:00.000Z");
    private static Map<Job, JobStatusEnum> MOCK_JOBS;

    @BeforeAll
    static void setUpAll() {
        TimeUtils.useMockTime(MOCK_NOW, ZoneId.of("UTC"));
        //AbstractAgent.disableGui();
    }

    @BeforeEach
    void init() {
        MOCK_JOBS = setUpJobs();
    }

    private static Stream<Arguments> parametersGetByIdAndStart() {
        return Stream.of(
                Arguments.of(Instant.parse("2022-01-01T07:30:00.000Z"), "2", true),
                Arguments.of(Instant.parse("2022-01-01T04:30:00.000Z"), "1", false));
    }

    private static Stream<Arguments> parametersGetByIdAndStartInstant() {
        return Stream.of(
                Arguments.of(ImmutableJobInstanceIdentifier.builder()
                        .startTime(Instant.parse("2022-01-01T06:00:00.000Z"))
                        .jobId("3")
                        .build(), true),
                Arguments.of(ImmutableJobInstanceIdentifier.builder()
                        .startTime(Instant.parse("2022-01-01T06:00:00.000Z"))
                        .jobId("1")
                        .build(), false));
    }

    private static Stream<Arguments> parametersGetById() {
        return Stream.of(Arguments.of("5", true), Arguments.of("10000", false));
    }

    private static Stream<Arguments> parametersIsJobUnique() {
        return Stream.of(Arguments.of("5", true), Arguments.of("1", false));
    }

    @ParameterizedTest
    @MethodSource("parametersIsJobUnique")
    @DisplayName("Test is job unique by id")
    void testIsJobUnique(final String jobId, final boolean result) {
        final Job jobProcessing = ImmutableJob.builder()
                .jobId("1")
                .clientIdentifier("Client1")
                .startTime(Instant.parse("2022-01-01T10:30:00.000Z"))
                .endTime(Instant.parse("2022-01-01T13:30:00.000Z"))
                .power(10)
                .build();
        MOCK_JOBS.put(jobProcessing, JobStatusEnum.IN_PROGRESS);
        assertThat(isJobUnique(MOCK_JOBS, jobId)).isEqualTo(result);
    }

    @ParameterizedTest
    @MethodSource("parametersGetById")
    @DisplayName("Test getting job by id")
    void testGettingJobById(final String jobId, final boolean result) {
        final Job jobResult = getJobById(MOCK_JOBS, jobId);
        assertThat(Objects.nonNull(jobResult)).isEqualTo(result);
    }

    @ParameterizedTest
    @MethodSource("parametersGetByIdAndStartInstant")
    @DisplayName("Test getting job by id and start time instant")
    void testGettingJobByIdAndStartTimeInstant(final JobInstanceIdentifier jobInstance, final boolean result) {
        final Job jobResult = getJobByIdAndStartDate(MOCK_JOBS, jobInstance);
        assertThat(Objects.nonNull(jobResult)).isEqualTo(result);
    }

    @ParameterizedTest
    @MethodSource("parametersGetByIdAndStart")
    @DisplayName("Test getting job by id and start time")
    void testGettingJobByIdAndStartTime(final Instant startTime, final String jobId, final boolean result) {
        final Job jobResult = getJobByIdAndStartDate(MOCK_JOBS, jobId, startTime);
        assertThat(Objects.nonNull(jobResult)).isEqualTo(result);
    }

    /**
     * Class creates mock jobs used in test scenarios.
     * The following structure was used:
     *
     * Job1 -> power: 10, time: 08:00 - 10:30, status: IN_PROGRESS,
     * Job2 -> power: 12, time: 07:30 - 11:00, status: IN_PROGRESS_BACKUP
     * Job3 -> power: 5,  time: 06:00 - 15:00, status: ON_HOLD_SOURCE_SHORTAGE
     * Job4 -> power: 2,  time: 09:00 - 12:00, status: IN_PROGRESS
     * Job5 -> power: 25, time: 11:00 - 12:00, status: ACCEPTED
     * Job6 -> power: 15, time: 11:30 - 13:00, status: ON_HOLD_TRANSFER
     */
    private Map<Job, JobStatusEnum> setUpJobs() {
        final Job mockJob1 = ImmutableJob.builder()
                .jobId("1")
                .clientIdentifier("Client1")
                .startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
                .endTime(Instant.parse("2022-01-01T10:30:00.000Z"))
                .power(10)
                .build();
        final Job mockJob2 = ImmutableJob.builder()
                .jobId("2")
                .clientIdentifier("Client2")
                .startTime(Instant.parse("2022-01-01T07:30:00.000Z"))
                .endTime(Instant.parse("2022-01-01T11:00:00.000Z"))
                .power(12)
                .build();
        final Job mockJob3 = ImmutableJob.builder()
                .jobId("3")
                .clientIdentifier("Client3")
                .startTime(Instant.parse("2022-01-01T06:00:00.000Z"))
                .endTime(Instant.parse("2022-01-01T15:00:00.000Z"))
                .power(5)
                .build();
        final Job mockJob4 = ImmutableJob.builder()
                .jobId("4")
                .clientIdentifier("Client4")
                .startTime(Instant.parse("2022-01-01T09:00:00.000Z"))
                .endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
                .power(2)
                .build();
        final Job mockJob5 = ImmutableJob.builder()
                .jobId("5")
                .clientIdentifier("Client5")
                .startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
                .endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
                .power(25)
                .build();
        final Job mockJob6 = ImmutableJob.builder()
                .jobId("6")
                .clientIdentifier("Client6")
                .startTime(Instant.parse("2022-01-01T11:30:00.000Z"))
                .endTime(Instant.parse("2022-01-01T13:00:00.000Z"))
                .power(15)
                .build();
        final Map<Job, JobStatusEnum> mockJobMap = new HashMap<>();
        mockJobMap.put(mockJob1, JobStatusEnum.IN_PROGRESS);
        mockJobMap.put(mockJob2, JobStatusEnum.IN_PROGRESS_BACKUP_ENERGY);
        mockJobMap.put(mockJob3, JobStatusEnum.ON_HOLD_SOURCE_SHORTAGE);
        mockJobMap.put(mockJob4, JobStatusEnum.IN_PROGRESS);
        mockJobMap.put(mockJob5, JobStatusEnum.ACCEPTED);
        mockJobMap.put(mockJob6, ON_HOLD_TRANSFER);
        return mockJobMap;
    }
}
