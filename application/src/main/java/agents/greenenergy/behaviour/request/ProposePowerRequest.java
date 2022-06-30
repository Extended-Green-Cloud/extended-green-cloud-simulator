package agents.greenenergy.behaviour.request;

import static agents.greenenergy.domain.GreenEnergyAgentConstants.MAX_ERROR_IN_JOB_FINISH;
import static common.GUIUtils.displayMessageArrow;
import static common.constant.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static messages.domain.ReplyMessageFactory.prepareStringReply;

import agents.greenenergy.GreenEnergyAgent;
import agents.greenenergy.behaviour.listener.ListenForFinishedJobs;
import agents.greenenergy.behaviour.listener.ListenForStartedJobs;
import agents.greenenergy.behaviour.listener.ListenForUnfinishedJobs;
import domain.job.JobStatusEnum;
import domain.job.PowerJob;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Behaviour which is responsible for sending the proposal with power request to Server Agent and
 * handling the retrieved responses.
 */
public class ProposePowerRequest extends ProposeInitiator {

    private static final Logger logger = LoggerFactory.getLogger(ProposePowerRequest.class);
    private final String guid;
    private final GreenEnergyAgent myGreenEnergyAgent;

    /**
     * Behaviour constructor.
     *
     * @param agent agent which is executing the behaviour
     * @param msg   proposal message that is sent to the Server Agent
     */
    public ProposePowerRequest(final Agent agent, final ACLMessage msg) {
        super(agent, msg);
        this.myGreenEnergyAgent = (GreenEnergyAgent) agent;
        this.guid = myGreenEnergyAgent.getName();
    }

    /**
     * Method handles the accept proposal response from server. It updates the state of the job in
     * green source data and sends the information that the execution of the given job can be started.
     *
     * @param accept_proposal accept proposal response retrieved from the Server Agent
     */
    @Override
    protected void handleAcceptProposal(final ACLMessage accept_proposal) {
        final String jobId = accept_proposal.getContent();
        final PowerJob job = myGreenEnergyAgent.getJobById(jobId);
        logger.info("[{}] Sending information back to server agent.", guid);
        myGreenEnergyAgent.getPowerJobs().replace(job, JobStatusEnum.ACCEPTED);
        var response = prepareStringReply(accept_proposal.createReply(), jobId, INFORM);
        response.setProtocol(SERVER_JOB_CFP_PROTOCOL);
        myAgent.addBehaviour(createJobListeningBehaviours(job));
        displayMessageArrow(myGreenEnergyAgent, accept_proposal.getSender());
        myAgent.send(response);
    }

    /**
     * Method handles the reject proposal response from server. It logs the information to the console.
     *
     * @param reject_proposal reject proposal response retrieved from the Server Agent
     */
    @Override
    protected void handleRejectProposal(final ACLMessage reject_proposal) {
        logger.info("[{}] Server rejected the job proposal", guid);
    }

    private ParallelBehaviour createJobListeningBehaviours(final PowerJob job) {
        final ParallelBehaviour behaviour = new ParallelBehaviour();
        behaviour.addSubBehaviour(new ListenForUnfinishedJobs(myGreenEnergyAgent, calculateJobStartTimeout(job), job.getJobId()));
        behaviour.addSubBehaviour(new ListenForStartedJobs(myGreenEnergyAgent));
        behaviour.addSubBehaviour(new ListenForFinishedJobs(myGreenEnergyAgent));
        return behaviour;
    }

    private Long calculateJobStartTimeout(final PowerJob job) {
        final long hourDifferenceStart = ChronoUnit.HOURS.between(OffsetDateTime.now(), job.getStartTime());
        final long hourDifferenceExecution = ChronoUnit.HOURS.between(job.getStartTime(), job.getEndTime());
        return ((hourDifferenceStart < 0 ? 0 : hourDifferenceStart) + hourDifferenceExecution) * 2 * 1000 + MAX_ERROR_IN_JOB_FINISH;
    }
}
