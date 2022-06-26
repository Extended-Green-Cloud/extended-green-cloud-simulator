package agents.server.behaviour;

import static java.time.temporal.ChronoUnit.HOURS;
import static mapper.JsonMapper.getMapper;
import static messages.MessagingUtils.rejectJobOffers;
import static messages.MessagingUtils.retrieveProposals;
import static messages.domain.JobOfferMessageFactory.makeServerJobOffer;

import agents.server.ServerAgent;
import com.fasterxml.jackson.core.JsonProcessingException;
import domain.GreenSourceData;
import exception.IncorrectGreenSourceOfferException;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import messages.domain.ReplyMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 * Behaviours responsible for passing the power request to green sources and choosing one to provide the power
 */
public class AnnouncePowerRequest extends ContractNetInitiator {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncePowerRequest.class);
    private final ACLMessage replyMessage;
    private final ServerAgent myServerAgent;

    /**
     * Behaviour constructor
     *
     * @param agent        agent which executed the behaviour
     * @param powerRequest call for proposal containing the details regarding power needed to execute the job
     * @param replyMessage reply message sent to cloud network after retreiving the green sources' responses
     */
    public AnnouncePowerRequest(final Agent agent, final ACLMessage powerRequest, final ACLMessage replyMessage) {
        super(agent, powerRequest);
        this.replyMessage = replyMessage;
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
        } else {
            final ACLMessage chosenGreenSourceOffer = chooseGreenSourceToExecuteJob(proposals);
            logger.info("[{}] Chosen Green Source for the job: {}", myAgent.getName(), chosenGreenSourceOffer.getSender().getLocalName());

            GreenSourceData chosenGreenSourceData;
            try {
                chosenGreenSourceData = getMapper().readValue(chosenGreenSourceOffer.getContent(), GreenSourceData.class);
            } catch (JsonProcessingException e) {
                throw new IncorrectGreenSourceOfferException();
            }
            final String jobId = chosenGreenSourceData.getJobId();
            final double servicePrice = calculateServicePrice(chosenGreenSourceData);
            final ACLMessage proposalMessage = makeServerJobOffer(myServerAgent, servicePrice, jobId, replyMessage);

            myServerAgent.getGreenSourceForJobMap().put(jobId, chosenGreenSourceOffer.getSender());
            logger.info("[{}] Sending job volunteering offer to Cloud Network Agent", myAgent.getName());
            myAgent.addBehaviour(new VolunteerForJob(myAgent, proposalMessage, chosenGreenSourceOffer.createReply()));
            rejectJobOffers(myAgent, jobId, chosenGreenSourceOffer, proposals);
        }
    }

    private double calculateServicePrice(final GreenSourceData greenSourceData) {
        var job = myServerAgent.getJobById(greenSourceData.getJobId());
        var powerCost = job.getPower() * greenSourceData.getPricePerPowerUnit();
        var computingCost =
                HOURS.between(job.getEndTime(), job.getStartTime())
                        * myServerAgent.getPricePerHour();
        return powerCost + computingCost;
    }

    private ACLMessage chooseGreenSourceToExecuteJob(final List<ACLMessage> greenSourceOffers) {
        final Comparator<ACLMessage> compareGreenSources =
                Comparator.comparingDouble(greenSource -> {
                    try {
                        return getMapper().readValue(greenSource.getContent(), GreenSourceData.class).getAvailablePowerInTime();
                    } catch (final JsonProcessingException e) {
                        throw new IncorrectGreenSourceOfferException();
                    }
                });
        return greenSourceOffers.stream().min(compareGreenSources).orElseThrow();
    }
}
