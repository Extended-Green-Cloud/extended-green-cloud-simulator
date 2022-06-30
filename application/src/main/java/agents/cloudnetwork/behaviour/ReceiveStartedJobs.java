package agents.cloudnetwork.behaviour;

import static common.GUIUtils.announceStartedJob;
import static common.constant.MessageProtocolConstants.STARTED_JOB_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.*;
import static messages.domain.ReplyMessageFactory.prepareConfirmationReply;

import agents.cloudnetwork.CloudNetworkAgent;
import domain.job.JobStatusEnum;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Behaviour responsible for receiving information that the execution of the job has started
 */
public class ReceiveStartedJobs extends CyclicBehaviour {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveStartedJobs.class);
    private static final MessageTemplate messageTemplate = and(MatchPerformative(INFORM), MatchProtocol(STARTED_JOB_PROTOCOL));

    private final ACLMessage replyMessage;
    private final CloudNetworkAgent myCloudNetworkAgent;

    /**
     * Behaviour constructor.
     *
     * @param agent        agent which is executing the behaviour
     * @param replyMessage reply inside which the information about the job execution status should be sent
     */
    public ReceiveStartedJobs(Agent agent, ACLMessage replyMessage) {
        super(agent);
        this.replyMessage = replyMessage;
        this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
    }

    /**
     * Method which listens for the information that some job execution has started. It finds the corresponding job
     * in network data, updates the network state and passes the started job message to the appropriate client.
     */
    @Override
    public void action() {
        final ACLMessage message = myAgent.receive(messageTemplate);

        if (Objects.nonNull(message)) {
            logger.info("[{}] Sending information that the job execution has started", myAgent.getName());
            final String jobId = message.getContent();
            myCloudNetworkAgent.getNetworkJobs().replace(myCloudNetworkAgent.getJobById(jobId), JobStatusEnum.IN_PROGRESS);
            announceStartedJob(myCloudNetworkAgent);
            myAgent.send(prepareConfirmationReply(jobId, replyMessage));
        } else {
            block();
        }
    }
}
