package com.greencloud.application.agents.greenenergy.management;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.job.ImmutablePowerJob;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.domain.job.PowerJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.greencloud.application.domain.job.JobStatusEnum.ON_HOLD_TRANSFER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class GreenEnergyManagementUnitTest {

    // MOCK OBJECTS
    private static Map<PowerJob, JobStatusEnum> MOCK_POWER_JOBS;

    @Mock
    private static GreenEnergyAgent mockGreenEnergyAgent;

    @Mock
    private static GreenEnergyManagement MOCK_MANAGEMENT;

    @BeforeEach
    void init() {
        MOCK_POWER_JOBS = setUpGreenEnergyJobs();
        setUpGreenEnergyMock();
    }

    // TESTS
    @Test
    @DisplayName("Test power job division - power job during shortage start")
    void testPowerJobDivisionDuringShortageStart() {
        final Instant startTime = Instant.parse("2022-01-01T09:00:00.000Z");
        final PowerJob powerJob = MOCK_POWER_JOBS.keySet().stream()
                .filter(jobKey -> jobKey.getJobId().equals("2")).findFirst()
                .orElse(null);

        mockGreenEnergyAgent.manage().dividePowerJobForPowerShortage(Objects.requireNonNull(powerJob), startTime);

        final Map<PowerJob, JobStatusEnum> updatedJobInstances = mockGreenEnergyAgent.getPowerJobs().entrySet().stream()
                .filter(jobEntry -> jobEntry.getKey().getJobId().equals(powerJob.getJobId()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final Map.Entry<PowerJob, JobStatusEnum> jobOnHold = updatedJobInstances.entrySet().stream()
                .filter(jobEntry -> jobEntry.getValue().equals(ON_HOLD_TRANSFER))
                .findFirst().orElse(null);
        final Map.Entry<PowerJob, JobStatusEnum> jobInProgress = updatedJobInstances.entrySet().stream()
                .filter(jobEntry -> !jobEntry.getValue().equals(ON_HOLD_TRANSFER))
                .findFirst().orElse(null);

        assertThat(mockGreenEnergyAgent.getPowerJobs()).hasSize(6);
        assertFalse(mockGreenEnergyAgent.getPowerJobs().containsKey(powerJob));
        assertThat(updatedJobInstances).hasSize(2);
        assertNotNull(jobOnHold);
        assertNotNull(jobInProgress);
        assertThat(jobOnHold.getKey().getStartTime()).isEqualTo(startTime);
        assertThat(jobInProgress.getKey().getEndTime()).isEqualTo(startTime);
    }

    @Test
    @DisplayName("Test power job division - power job after shortage start")
    void testJobDivisionAfterShortageStart() {
        final Instant startTime = Instant.parse("2022-01-01T09:00:00.000Z");
        final PowerJob powerJob = MOCK_POWER_JOBS.keySet().stream()
                .filter(jobKey -> jobKey.getJobId().equals("5")).findFirst()
                .orElse(null);

        mockGreenEnergyAgent.manage().dividePowerJobForPowerShortage(Objects.requireNonNull(powerJob), startTime);
        final JobStatusEnum statusAfterUpdate = mockGreenEnergyAgent.getPowerJobs().entrySet().stream()
                .filter(jobEntry -> jobEntry.getKey().equals(powerJob))
                .map(Map.Entry::getValue)
                .findFirst().orElse(null);

        assertThat(mockGreenEnergyAgent.getPowerJobs()).hasSameSizeAs(MOCK_POWER_JOBS);
        assertTrue(mockGreenEnergyAgent.getPowerJobs().containsKey(powerJob));
        assertThat(statusAfterUpdate).isEqualTo(ON_HOLD_TRANSFER);
    }

    // PREPARING TEST DATA
    /**
     * Class creates mock green energy power jobs used in test scenarios.
     * The following structure was used:
     *
     * PowerJob1 -> power: 10, time: 08:00 - 10:00, status: IN_PROGRESS,
     * PowerJob2 -> power: 20, time: 07:00 - 11:00, status: IN_PROGRESS
     * PowerJob3 -> power: 50,  time: 06:00 - 15:00, status: ON_HOLD
     * PowerJob4 -> power: 10,  time: 09:00 - 12:00, status: ON_HOLD
     * PowerJob5 -> power: 25, time: 11:00 - 12:00, status: ACCEPTED
     */
    private Map<PowerJob, JobStatusEnum> setUpGreenEnergyJobs() {
        final PowerJob mockJob1 = ImmutablePowerJob.builder()
                .jobId("1")
                .startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
                .endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
                .power(10)
                .build();
        final PowerJob mockJob2 = ImmutablePowerJob.builder()
                .jobId("2")
                .startTime(Instant.parse("2022-01-01T07:00:00.000Z"))
                .endTime(Instant.parse("2022-01-01T11:00:00.000Z"))
                .power(20)
                .build();
        final PowerJob mockJob3 = ImmutablePowerJob.builder()
                .jobId("3")
                .startTime(Instant.parse("2022-01-01T06:00:00.000Z"))
                .endTime(Instant.parse("2022-01-01T15:00:00.000Z"))
                .power(50)
                .build();
        final PowerJob mockJob4 = ImmutablePowerJob.builder()
                .jobId("4")
                .startTime(Instant.parse("2022-01-01T09:00:00.000Z"))
                .endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
                .power(10)
                .build();
        final PowerJob mockJob5 = ImmutablePowerJob.builder()
                .jobId("5")
                .startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
                .endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
                .power(25)
                .build();
        final Map<PowerJob, JobStatusEnum> mockJobMap = new HashMap<>();
        mockJobMap.put(mockJob1, JobStatusEnum.IN_PROGRESS);
        mockJobMap.put(mockJob2, JobStatusEnum.IN_PROGRESS);
        mockJobMap.put(mockJob3, JobStatusEnum.ON_HOLD);
        mockJobMap.put(mockJob4, JobStatusEnum.ON_HOLD);
        mockJobMap.put(mockJob5, JobStatusEnum.ACCEPTED);
        return mockJobMap;
    }

    private void setUpGreenEnergyMock() {
        mockGreenEnergyAgent = spy(GreenEnergyAgent.class);
        mockGreenEnergyAgent.getPowerJobs().putAll(MOCK_POWER_JOBS);
        MOCK_MANAGEMENT = spy(new GreenEnergyManagement(mockGreenEnergyAgent));

        doReturn(MOCK_MANAGEMENT).when(mockGreenEnergyAgent).manage();
        doNothing().when(mockGreenEnergyAgent).addBehaviour(any());
        doNothing().when(mockGreenEnergyAgent).send(any());
    }
}
