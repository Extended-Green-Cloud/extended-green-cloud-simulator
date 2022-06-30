package agents.greenenergy.behaviour.listener;

import static common.GUIUtils.displayMessageArrow;
import static common.GUIUtils.updateGreenSourceState;
import static messages.domain.JobStatusMessageFactory.prepareManualFinishMessageForServer;

import agents.greenenergy.GreenEnergyAgent;
import domain.job.PowerJob;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Behaviour responsible for verifying if the job was finished at the correct time.
 */
public class ListenForUnfinishedJobs extends WakerBehaviour {

    private static final Logger logger = LoggerFactory.getLogger(ListenForUnfinishedJobs.class);

    private final String jobId;
    private final GreenEnergyAgent myGreenEnergyAgent;

    /**
     * Behaviour constructor.
     *
     * @param agent   agent which is executing the behaviour
     * @param timeout timeout after which the job should finish
     * @param jobId   unique job identifier
     */
    public ListenForUnfinishedJobs(Agent agent, long timeout, String jobId) {
        super(agent, timeout);
        this.myGreenEnergyAgent = (GreenEnergyAgent) agent;
        this.jobId = jobId;
    }

    /**
     * Method verifies if the job execution finished correctly. If there was no information about
     * job finish execution but the timout passed - the Green Source finishes the power supply manually and sends
     * the warning to the Server Agent.
     */
    @Override
    protected void onWake() {
        final PowerJob job = myGreenEnergyAgent.getJobById(jobId);
        if (Objects.nonNull(job)) {
            logger.error("[{}] The power delivery should be finished! Finishing power delivery by hand.", myAgent.getName());
            myGreenEnergyAgent.getPowerJobs().remove(job);
            updateGreenSourceState(myGreenEnergyAgent, true);
            displayMessageArrow(myGreenEnergyAgent, myGreenEnergyAgent.getOwnerServer());
            myAgent.send(prepareManualFinishMessageForServer(jobId, myGreenEnergyAgent.getOwnerServer()));
        }
    }
}
