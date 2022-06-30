package agents.cloudnetwork.behaviour;

import static common.GUIUtils.announceFinishedJob;
import static common.constant.MessageProtocolConstants.FINISH_JOB_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.*;
import static messages.domain.JobStatusMessageFactory.prepareFinishMessageForClient;

import agents.cloudnetwork.CloudNetworkAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Behaviour responsible for returning to the client the information that the job execution has finished
 */
public class ReturnCompletedJob extends CyclicBehaviour {

    private static final Logger logger = LoggerFactory.getLogger(ReturnCompletedJob.class);
    private static final MessageTemplate messageTemplate = and(MatchPerformative(INFORM), MatchProtocol(FINISH_JOB_PROTOCOL));

    private CloudNetworkAgent myCloudNetworkAgent;

    /**
     * Method runs at the behaviour start. It casts the abstract agent to the agent of type CloudNetworkAgent
     */
    @Override
    public void onStart() {
        super.onStart();
        this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
    }

    /**
     * Method which listens for the information that some job execution has finished. It finds the corresponding job
     * in network data, updates the network state and passes the finish job message to the appropriate client.
     */
    @Override
    public void action() {
        final ACLMessage message = myAgent.receive(messageTemplate);

        if (Objects.nonNull(message)) {
            logger.info("[{}] Sending information that the job execution is finished", myAgent.getName());
            final String jobId = message.getContent();
            final String clientId = myCloudNetworkAgent.getJobById(jobId).getClientIdentifier();
            updateNetworkInformation(jobId);
            myAgent.send(prepareFinishMessageForClient(jobId, clientId));
        } else {
            block();
        }
    }

    private void updateNetworkInformation(final String jobId) {
        myCloudNetworkAgent.getNetworkJobs().remove(myCloudNetworkAgent.getJobById(jobId));
        myCloudNetworkAgent.getServerForJobMap().remove(jobId);
        announceFinishedJob(myCloudNetworkAgent, jobId);
    }
}
