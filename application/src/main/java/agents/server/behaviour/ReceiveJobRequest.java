package agents.server.behaviour;

import static common.GUIUtils.displayMessageArrow;
import static common.constant.MessageProtocolConstants.CNA_JOB_CFP_PROTOCOL;
import static common.constant.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static jade.lang.acl.ACLMessage.CFP;
import static jade.lang.acl.MessageTemplate.*;
import static mapper.JsonMapper.getMapper;

import agents.server.ServerAgent;
import domain.job.ImmutablePowerJob;
import domain.job.Job;
import domain.job.JobStatusEnum;
import domain.job.PowerJob;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import messages.domain.CallForProposalMessageFactory;
import messages.domain.ReplyMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Behaviour which is responsible for handling upcoming job's call for proposals from cloud network agents
 */
public class ReceiveJobRequest extends CyclicBehaviour {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveJobRequest.class);
    private static final MessageTemplate messageTemplate = and(MatchPerformative(CFP), MatchProtocol(CNA_JOB_CFP_PROTOCOL));

    private ServerAgent myServerAgent;

    /**
     * Method executed at the behaviour's start. It casts the agent to the ServerAgent.
     */
    @Override
    public void onStart() {
        super.onStart();
        myServerAgent = (ServerAgent) myAgent;
    }

    /**
     * Method listens for the upcoming job call for proposals from the Cloud Network Agents.
     * It validates whether the server has enough power to handle the job.
     * If yes, then it sends the call for proposal to owned green sources and starts listening for the responses.
     * If no, then it sends the refuse message to the Cloud Network Agent.
     */
    @Override
    public void action() {
        final ACLMessage message = myAgent.receive(messageTemplate);

        if (Objects.nonNull(message)) {
            try {
                final Job job = getMapper().readValue(message.getContent(), Job.class);
                if (job.getPower() <= myServerAgent.getAvailableCapacity(job.getStartTime(), job.getEndTime())) {
                    logger.info("[{}] Sending call for proposal to Green Source Agents", myAgent.getName());
                    final ACLMessage cfp = preparePowerJobCFP(job);
                    displayMessageArrow(myServerAgent, myServerAgent.getOwnedGreenSources());
                    myServerAgent.getServerJobs().put(job, JobStatusEnum.PROCESSING);
                    myAgent.addBehaviour(new AnnouncePowerRequest(myAgent, cfp, message.createReply()));
                } else {
                    logger.info("[{}] Not enough available power! Sending refuse message to Cloud Network Agent", myAgent.getName());
                    displayMessageArrow(myServerAgent, message.getSender());
                    myAgent.send(ReplyMessageFactory.prepareRefuseReply(message.createReply()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }

    private ACLMessage preparePowerJobCFP(final Job job) {
        final PowerJob powerJob = ImmutablePowerJob.builder()
                .power(job.getPower())
                .startTime(job.getStartTime())
                .endTime(job.getEndTime())
                .jobId(job.getJobId())
                .build();
        return CallForProposalMessageFactory.createCallForProposal(powerJob, myServerAgent.getOwnedGreenSources(), SERVER_JOB_CFP_PROTOCOL);
    }
}
