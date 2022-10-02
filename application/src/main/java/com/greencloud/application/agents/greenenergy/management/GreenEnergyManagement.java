package com.greencloud.application.agents.greenenergy.management;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.powersupply.handler.HandleManualPowerSupplyFinish;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.domain.job.PowerJob;
import com.greencloud.application.mapper.JobMapper;

import java.time.Instant;
import java.util.Date;

import static com.greencloud.application.agents.greenenergy.domain.GreenEnergyAgentConstants.MAX_ERROR_IN_JOB_FINISH;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;

/**
 *  Set of methods used in the Green Energy Agent's behaviours
 */
public class GreenEnergyManagement {
    public GreenEnergyAgent greenEnergyAgent;

    public GreenEnergyManagement(GreenEnergyAgent greenEnergyAgent) {
        this.greenEnergyAgent = greenEnergyAgent;
    }

    /**
     * Method creates new instances for given power job which will be affected by the power shortage.
     * If the power shortage will begin after the start of job execution -> job will be divided into 2*
     * Example:
     * Job1 (start: 08:00, finish: 10:00)
     * Power shortage start: 09:00
     * Job1Instance1: (start: 08:00, finish: 09:00) <- job not affected by power shortage
     * Job1Instance2: (start: 09:00, finish: 10:00) <- job affected by power shortage
     *
     * @param powerJob           affected power job
     * @param powerShortageStart time when power shortage starts
     */
    public PowerJob dividePowerJobForPowerShortage(final PowerJob powerJob, final Instant powerShortageStart) {
        if (powerShortageStart.isAfter(powerJob.getStartTime())) {
            final PowerJob affectedPowerJobInstance = JobMapper.mapToJobNewStartTime(powerJob, powerShortageStart);
            final PowerJob notAffectedPowerJobInstance = JobMapper.mapToJobNewEndTime(powerJob, powerShortageStart);
            final JobStatusEnum currentJobStatus = greenEnergyAgent.getPowerJobs().get(powerJob);

            greenEnergyAgent.getPowerJobs().remove(powerJob);
            greenEnergyAgent.getPowerJobs().put(affectedPowerJobInstance, JobStatusEnum.ON_HOLD_TRANSFER);
            greenEnergyAgent.getPowerJobs().put(notAffectedPowerJobInstance, currentJobStatus);
            final Date endDate = Date.from(affectedPowerJobInstance.getEndTime().plusMillis(MAX_ERROR_IN_JOB_FINISH));
            greenEnergyAgent.addBehaviour(new HandleManualPowerSupplyFinish(greenEnergyAgent, endDate,
                    mapToJobInstanceId(affectedPowerJobInstance)));
            greenEnergyAgent.manageState().updateGreenSourceGUI();
            return affectedPowerJobInstance;
        } else {
            greenEnergyAgent.getPowerJobs().replace(powerJob, JobStatusEnum.ON_HOLD_TRANSFER);
            greenEnergyAgent.manageState().updateGreenSourceGUI();
            return powerJob;
        }
    }
}
