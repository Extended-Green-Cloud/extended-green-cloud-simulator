package agents.server.behaviour;

import static common.GUIUtils.displayMessageArrow;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static mapper.JsonMapper.getMapper;

import agents.server.ServerAgent;
import domain.job.Job;
import domain.job.JobInstanceIdentifier;
import domain.job.JobStatusEnum;
import domain.job.JobWithProtocol;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;
import messages.domain.ReplyMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Behaviours responsible for sending volunteering offer to Cloud Network Agent and handling
 * recieved responses.
 */
public class VolunteerForJob extends ProposeInitiator {

  private static final Logger logger = LoggerFactory.getLogger(VolunteerForJob.class);

  private final ServerAgent myServerAgent;
  private final ACLMessage replyMessage;

  /**
   * Behaviour constructor.
   *
   * @param agent agent executing the behaviour
   * @param msg proposal message that has to be sent to Cloud Network
   * @param replyMessage reply message sent to green source after retreiving the cloud network
   *     response
   */
  public VolunteerForJob(final Agent agent, final ACLMessage msg, final ACLMessage replyMessage) {
    super(agent, msg);
    this.replyMessage = replyMessage;
    this.myServerAgent = (ServerAgent) myAgent;
  }

  /**
   * Method handles the accept proposal response received from the Cloud Network Agents. It starts
   * the job execution behaviour, updates the network data and forwards the accept proposal to the
   * chosen Green Source Agent
   *
   * @param accept_proposal accept proposal message retrieved from the Cloud Network
   */
  @Override
  protected void handleAcceptProposal(final ACLMessage accept_proposal) {
    try {
      logger.info("[{}] Sending ACCEPT_PROPOSAL to Green Source Agent", myAgent.getName());
      final JobWithProtocol jobWithProtocol =
          getMapper().readValue(accept_proposal.getContent(), JobWithProtocol.class);
      final JobInstanceIdentifier jobInstanceId = jobWithProtocol.getJobInstanceIdentifier();
      myServerAgent
          .getServerJobs()
          .replace(myServerAgent.manage().getJobByIdAndStartDate(jobInstanceId), JobStatusEnum.ACCEPTED);
      myServerAgent.manage().updateClientNumber();
      displayMessageArrow(myServerAgent, replyMessage.getAllReceiver());
      myAgent.send(
          ReplyMessageFactory.prepareAcceptReplyWithProtocol(
              replyMessage, jobInstanceId, jobWithProtocol.getReplyProtocol()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Method handles the reject proposal response received from the Cloud Network Agents. It forwards
   * the reject proposal to the Green Source Agent
   *
   * @param reject_proposal reject proposal message retrieved from the Cloud Network
   */
  @Override
  protected void handleRejectProposal(final ACLMessage reject_proposal) {
    try {
      logger.info(
          "[{}] Cloud Network {} rejected the job volunteering offer",
          myAgent.getName(),
          reject_proposal.getSender().getLocalName());
      final String jobId = reject_proposal.getContent();
      final Job job = myServerAgent.manage().getJobById(jobId);
      myServerAgent.getGreenSourceForJobMap().remove(jobId);
      var jobs = myServerAgent.getServerJobs().keySet().stream()
          .filter(key -> key.getJobId().equals(jobId))
          .toList();
      jobs.forEach(myServerAgent.getServerJobs()::remove);
      displayMessageArrow(myServerAgent, replyMessage.getAllReceiver());
      myServerAgent.send(
          ReplyMessageFactory.prepareStringReply(replyMessage, jobId, REJECT_PROPOSAL));
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
