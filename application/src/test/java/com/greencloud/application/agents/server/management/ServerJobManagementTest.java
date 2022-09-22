package com.greencloud.application.agents.server.management;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.ImmutableJob;
import com.greencloud.application.domain.job.Job;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.utils.TimeUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS_BACKUP_ENERGY;
import static com.greencloud.application.domain.job.JobStatusEnum.ON_HOLD_TRANSFER;
import static java.time.Instant.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

public class ServerJobManagementTest {

    //MOCKED OBJECTS

    private static final Instant MOCK_NOW = parse("2022-01-01T11:00:00.000Z");
    private static final int MOCK_CAPACITY = 200;
    private static final double MOCK_PRICE = 10;
    private static ServerStateManagement MOCK_STATE_MANAGEMENT;

    private static ServerJobManagement MOCK_JOB_MANAGEMENT;
    private static Map<Job, JobStatusEnum> MOCK_JOBS;

    @Mock
    private static ServerAgent serverAgent;

    // TEST SET-UP

    @BeforeAll
    static void setUpAll() {
        TimeUtils.useMockTime(MOCK_NOW, ZoneId.of("UTC"));
        //AbstractAgent.disableGui();
    }

    @BeforeEach
    void init() {
        MOCK_JOBS = setUpServerJobs();
        setUpServerMock();
    }


    /**
     * Class creates mock server jobs used in test scenarios.
     * The following structure was used:
     *
     * Job1 -> power: 10, time: 08:00 - 10:30, status: IN_PROGRESS,
     * Job2 -> power: 12, time: 07:30 - 11:00, status: IN_PROGRESS_BACKUP
     * Job3 -> power: 5,  time: 06:00 - 15:00, status: ON_HOLD_SOURCE_SHORTAGE
     * Job4 -> power: 2,  time: 09:00 - 12:00, status: IN_PROGRESS
     * Job5 -> power: 25, time: 11:00 - 12:00, status: ACCEPTED
     * Job6 -> power: 15, time: 11:30 - 13:00, status: ON_HOLD_TRANSFER
     */
    private Map<Job, JobStatusEnum> setUpServerJobs() {
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

    private void setUpServerMock() {
        serverAgent = spy(ServerAgent.class);
        serverAgent.getServerJobs().putAll(MOCK_JOBS);
        serverAgent.setCurrentMaximumCapacity(MOCK_CAPACITY);

        final ServerStateManagement stateManagement = new ServerStateManagement(serverAgent);
        MOCK_STATE_MANAGEMENT = spy(stateManagement);
        final ServerJobManagement jobManagement = new ServerJobManagement(serverAgent);
        MOCK_JOB_MANAGEMENT = spy(jobManagement);

        doReturn(MOCK_PRICE).when(serverAgent).getPricePerHour();
        doReturn(MOCK_CAPACITY).when(serverAgent).getInitialMaximumCapacity();
        doReturn(MOCK_STATE_MANAGEMENT).when(serverAgent).manageState();
        doReturn(MOCK_JOB_MANAGEMENT).when(serverAgent).manageJobs();
        doNothing().when(serverAgent).addBehaviour(any());
        doNothing().when(serverAgent).send(any());
    }
}
