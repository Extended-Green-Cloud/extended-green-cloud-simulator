package com.greencloud.application.agents.scheduler.managment;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.greencloud.application.agents.scheduler.managment.logs.SchedulerManagementLog.INCREASE_DEADLINE_WEIGHT_LOG;
import static com.greencloud.application.agents.scheduler.managment.logs.SchedulerManagementLog.INCREASE_POWER_WEIGHT_LOG;


/**
* Set of utilities used to manage the adaptations of scheduler agent
*/
public class SchedulerAdaptationManagement {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerAdaptationManagement.class);

    private final SchedulerAgent schedulerAgent;

    /**
     * Default constructor
     *
     * @param schedulerAgent parent scheduler agent
     */
    public SchedulerAdaptationManagement(final SchedulerAgent schedulerAgent) {
        this.schedulerAgent = schedulerAgent;
    }

    /**
     * Method executes adaptation action that increases
     * the current weight of deadline priority for Scheduler Agent
     */
    public boolean executeIncreaseDeadlineWeightAction() {
        final int currentDeadlineWeight = schedulerAgent.config().getDeadlineWeightPriority();
        schedulerAgent.config().increaseDeadlineWeightPriority();
        logger.info(INCREASE_DEADLINE_WEIGHT_LOG, currentDeadlineWeight, schedulerAgent.config().getDeadlineWeightPriority());
        return true;
    }

    public boolean executeIncreasePowerWeightAction() {
        final int currentPowerWeight = schedulerAgent.config().getPowerWeightPriority();
        schedulerAgent.config().increasePowerWeightPriority();
        logger.info(INCREASE_POWER_WEIGHT_LOG, currentPowerWeight, schedulerAgent.config().getPowerWeightPriority());
        return true;
    }
}
