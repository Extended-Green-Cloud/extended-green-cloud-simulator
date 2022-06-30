package agents.server.behaviour.jobexecution;

import static common.GUIUtils.displayMessageArrow;
import static common.GUIUtils.updateServerState;
import static messages.domain.JobStatusMessageFactory.prepareJobStartedMessage;

import agents.server.ServerAgent;
import domain.job.Job;
import domain.job.JobStatusEnum;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartJobExecution extends OneShotBehaviour {

    private static final Logger logger = LoggerFactory.getLogger(StartJobExecution.class);
    private final ServerAgent myServerAgent;
    private final Job jobToExecute;

    /**
     * Behaviour constructor.
     *
     * @param agent   agent that is executing the behaviour
     * @param job     job that is to be executed
     */
    public StartJobExecution(Agent agent, final Job job) {
        this.jobToExecute = job;
        myServerAgent = (ServerAgent) agent;
    }

    /**
     * Method starts the execution of the job. It updates the server state, then sends the information that the execution has started to the
     * Green Source Agent and the Cloud Network. Finally, it starts the behaviour responsible for informing about job
     * execution finish.
     */
    @Override
    public void action() {
        logger.info("[{}] Start actual execution the job for {}", myAgent.getName(), jobToExecute.getClientIdentifier());
        myServerAgent.getServerJobs().replace(jobToExecute, JobStatusEnum.IN_PROGRESS);
        updateServerState(myServerAgent, false);
        myAgent.addBehaviour(FinishJobExecution.createFor(myServerAgent, jobToExecute));
    }
}
