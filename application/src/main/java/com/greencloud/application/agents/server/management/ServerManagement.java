package com.greencloud.application.agents.server.management;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.jobexecution.handler.HandleJobFinish;
import com.greencloud.application.agents.server.behaviour.jobexecution.handler.HandleJobStart;
import com.greencloud.application.domain.GreenSourceData;
import com.greencloud.application.domain.job.Job;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.mapper.JobMapper;
import com.greencloud.application.messages.domain.factory.JobStatusMessageFactory;
import com.greencloud.application.utils.TimeUtils;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.greencloud.application.agents.server.domain.ServerPowerSourceType.BACK_UP_POWER;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS_BACKUP_ENERGY;
import static com.greencloud.application.domain.job.JobStatusEnum.ON_HOLD_SOURCE_SHORTAGE;
import static com.greencloud.application.utils.GUIUtils.displayMessageArrow;
import static com.greencloud.application.utils.JobMapUtils.getJobById;
import static com.greencloud.application.utils.JobMapUtils.isJobUnique;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.utils.TimeUtils.isWithinTimeStamp;

public class ServerManagement {

    private static final Logger logger = LoggerFactory.getLogger(ServerStateManagement.class);

    private final ServerAgent serverAgent;

    public ServerManagement(ServerAgent serverAgent) {
        this.serverAgent = serverAgent;
    }

    /**
     * Method creates new instances for given job which will be affected by the power shortage.
     * If the power shortage will begin after the start of job execution -> job will be divided into 2
     *
     * Example:
     * Job1 (start: 08:00, finish: 10:00)
     * Power shortage start: 09:00
     *
     * Job1Instance1: (start: 08:00, finish: 09:00) <- job not affected by power shortage
     * Job1Instance2: (start: 09:00, finish: 10:00) <- job affected by power shortage
     *
     * @param job                affected job
     * @param powerShortageStart time when power shortage starts
     */
    public Job divideJobForPowerShortage(final Job job, final Instant powerShortageStart) {
        if (powerShortageStart.isAfter(job.getStartTime()) && !powerShortageStart.equals(job.getStartTime())) {
            final Job affectedJobInstance = JobMapper.mapToJobNewStartTime(job, powerShortageStart);
            final Job notAffectedJobInstance = JobMapper.mapToJobNewEndTime(job, powerShortageStart);
            final JobStatusEnum currentJobStatus = serverAgent.getServerJobs().get(job);

            serverAgent.getServerJobs().remove(job);
            serverAgent.getServerJobs().put(affectedJobInstance, JobStatusEnum.ON_HOLD_TRANSFER);
            serverAgent.getServerJobs().put(notAffectedJobInstance, currentJobStatus);

            serverAgent.addBehaviour(HandleJobStart.createFor(serverAgent, affectedJobInstance, false, true));
            serverAgent.addBehaviour(HandleJobFinish.createFor(serverAgent, notAffectedJobInstance, false));

            if (getCurrentTime().isBefore(notAffectedJobInstance.getStartTime())) {
                serverAgent.addBehaviour(
                        HandleJobStart.createFor(serverAgent, notAffectedJobInstance, true, false));
            }

            return affectedJobInstance;
        } else {
            serverAgent.getServerJobs().replace(job, JobStatusEnum.ON_HOLD_TRANSFER);
            serverAgent.manageState().updateServerGUI();
            return job;
        }
    }

    /**
     * Method performs job finishing action
     *
     * @param jobToFinish job to be finished
     * @param informCNA   flag indicating whether cloud network should be informed about the job finish
     */
    public void finishJobExecution(final Job jobToFinish, final boolean informCNA) {
        final JobStatusEnum jobStatusEnum = serverAgent.getServerJobs().get(jobToFinish);

        sendFinishInformation(jobToFinish, informCNA);
        updateStateAfterJobFinish(jobToFinish);

        if (jobStatusEnum.equals(IN_PROGRESS_BACKUP_ENERGY)) {
            final Map<Job, JobStatusEnum> jobsWithinTimeStamp = serverAgent.getServerJobs().entrySet().stream()
                    .filter(job -> isWithinTimeStamp(job.getKey().getStartTime(), job.getKey().getEndTime(),
                            getCurrentTime()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            supplyJobsWithBackupPower(jobsWithinTimeStamp);
        }
    }

    /**
     * Method calculates the price for executing the job by given green source and server
     *
     * @param greenSourceData green source executing the job
     * @return full price
     */
    public double calculateServicePrice(final GreenSourceData greenSourceData) {
        var job = getJobById(serverAgent.getServerJobs(), greenSourceData.getJobId());
        var powerCost = job.getPower() * greenSourceData.getPricePerPowerUnit();
        var computingCost = TimeUtils.differenceInHours(job.getStartTime(), job.getEndTime()) * serverAgent.getPricePerHour();
        return powerCost + computingCost;
    }

    private void sendFinishInformation(final Job jobToFinish, final boolean informCNA) {
        final List<AID> receivers = informCNA ?
                List.of(serverAgent.getGreenSourceForJobMap().get(jobToFinish.getJobId()),
                        serverAgent.getOwnerCloudNetworkAgent()) :
                Collections.singletonList(serverAgent.getGreenSourceForJobMap().get(jobToFinish.getJobId()));
        final ACLMessage finishJobMessage = JobStatusMessageFactory.prepareFinishMessage(jobToFinish.getJobId(), jobToFinish.getStartTime(),
                receivers);

        displayMessageArrow(serverAgent, receivers);
        serverAgent.send(finishJobMessage);
    }

    private void updateStateAfterJobFinish(final Job jobToFinish) {
        serverAgent.manageState().incrementFinishedJobs(jobToFinish.getJobId());
        if (isJobUnique(serverAgent.getServerJobs(), jobToFinish.getJobId())) {
            serverAgent.getGreenSourceForJobMap().remove(jobToFinish.getJobId());
            serverAgent.manageState().updateClientNumberGUI();
        }
        serverAgent.getServerJobs().remove(jobToFinish);
    }

    private void supplyJobsWithBackupPower(final Map<Job, JobStatusEnum> jobEntries) {
        jobEntries.entrySet().stream()
                .filter(job -> job.getValue().equals(ON_HOLD_SOURCE_SHORTAGE))
                .forEach(jobEntry -> {
                    final Job job = jobEntry.getKey();
                    if (serverAgent.manageState().getAvailableCapacity(job.getStartTime(), job.getEndTime(), JobMapper.mapToJobInstanceId(job),
                            BACK_UP_POWER) >= job.getPower()) {
                        MDC.put(MDC_JOB_ID, job.getJobId());
                        logger.info("Supplying job {} with back up power", job.getJobId());
                        serverAgent.getServerJobs().replace(job, IN_PROGRESS_BACKUP_ENERGY);
                        serverAgent.manageState().updateServerGUI();
                    }
                });
    }
}
