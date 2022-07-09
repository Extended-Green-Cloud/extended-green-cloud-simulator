package agents.server.behaviour;

import static mapper.JsonMapper.getMapper;
import static messages.MessagingUtils.rejectJobOffers;
import static messages.MessagingUtils.retrieveProposals;
import static messages.MessagingUtils.retrieveValidMessages;
import static messages.domain.JobOfferMessageFactory.makeServerJobOffer;

import agents.server.ServerAgent;
import com.fasterxml.jackson.core.JsonProcessingException;
import common.constant.InvalidJobIdConstant;
import domain.GreenSourceData;
import domain.job.Job;
import domain.job.JobStatusEnum;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import java.util.List;
import java.util.Vector;
import messages.domain.ReplyMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Behaviours responsible for passing the power request to green sources and choosing one to provide the power
 */
public class AnnouncePowerRequest extends ContractNetInitiator {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncePowerRequest.class);

    private final ACLMessage replyMessage;
    private final ServerAgent myServerAgent;
    private final Job job;

    /**
     * Behaviour constructor
     *
     * @param agent        agent which executed the behaviour
     * @param powerRequest call for proposal containing the details regarding power needed to execute the job
     * @param replyMessage reply message sent to cloud network after retreiving the green sources' responses
     */
    public AnnouncePowerRequest(final Agent agent, final ACLMessage powerRequest, final ACLMessage replyMessage, final Job job) {
        super(agent, powerRequest);
        this.replyMessage = replyMessage;
        this.job = job;
        this.myServerAgent = (ServerAgent) myAgent;
    }

    /**
     * Method which waits for all Green Source Agent responses. It is responsible for analyzing the received proposals,
     * choosing the Green Source Agent for power job execution and rejecting the remaining Green Source Agents.
     *
     * @param responses   retrieved responses from Green Source Agents
     * @param acceptances vector containing accept proposal message sent back to the chosen green source (not used)
     */
    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        final List<ACLMessage> proposals = retrieveProposals(responses);

        if (responses.isEmpty()) {
            logger.info("[{}] No responses were retrieved", myAgent.getName());
        } else if (proposals.isEmpty()) {
            logger.info("[{}] No Green Sources available - sending refuse message to Cloud Network Agent", myAgent);
            myAgent.send(ReplyMessageFactory.prepareRefuseReply(replyMessage));
        } else if (myServerAgent.manage().getAvailableCapacity(job.getStartTime(), job.getEndTime())*0.5 <= job.getPower()) {
            logger.info("[{}] No enough capacity - sending refuse message to Cloud Network Agent", myAgent);
            myAgent.send(ReplyMessageFactory.prepareRefuseReply(replyMessage));
        } else {
            final List<ACLMessage> validProposals = retrieveValidMessages(proposals, GreenSourceData.class);
            if (!validProposals.isEmpty()) {
                myServerAgent.getServerJobs().replace(myServerAgent.manage().getJobById(job.getJobId()), JobStatusEnum.ACCEPTED);
                final ACLMessage chosenGreenSourceOffer = myServerAgent.chooseGreenSourceToExecuteJob(validProposals);
                final GreenSourceData chosenGreenSourceData = readMessageContent(chosenGreenSourceOffer);
                final String jobId = chosenGreenSourceData.getJobId();
                logger.info("[{}] Chosen Green Source for the job with id {} : {}", myAgent.getName(), jobId, chosenGreenSourceOffer.getSender().getLocalName());

                final double servicePrice = myServerAgent.manage().calculateServicePrice(chosenGreenSourceData);
                final ACLMessage proposalMessage = makeServerJobOffer(myServerAgent, servicePrice, jobId, replyMessage);
                myServerAgent.getGreenSourceForJobMap().put(jobId, chosenGreenSourceOffer.getSender());

                logger.info("[{}] Sending job volunteering offer to Cloud Network Agent", myAgent.getName());
                myServerAgent.addBehaviour(new VolunteerForJob(myAgent, proposalMessage, chosenGreenSourceOffer.createReply()));
                rejectJobOffers(myServerAgent, jobId, chosenGreenSourceOffer, proposals);
            } else {
                handleInvalidProposals(proposals);
            }
        }
    }

    private void handleInvalidProposals(final List<ACLMessage> proposals) {
        logger.info("I didn't understand any proposal from Green Energy Agents");
        rejectJobOffers(myServerAgent, InvalidJobIdConstant.INVALID_JOB_ID, null, proposals);
        myAgent.send(ReplyMessageFactory.prepareRefuseReply(replyMessage));
    }

    private GreenSourceData readMessageContent(final ACLMessage message) {
        try {
            return getMapper().readValue(message.getContent(), GreenSourceData.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
