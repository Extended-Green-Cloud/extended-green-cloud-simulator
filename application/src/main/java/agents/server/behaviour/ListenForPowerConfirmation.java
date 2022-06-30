package agents.server.behaviour;

import static common.GUIUtils.announceBookedJob;
import static common.constant.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static mapper.JsonMapper.getMapper;

import agents.server.ServerAgent;
import agents.server.behaviour.jobexecution.CheckWeatherBeforeJobExecution;
import domain.job.Job;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Behaviour responsible for listening for confirmation message from Green Energy Source regarding power delivery
 */
public class ListenForPowerConfirmation extends CyclicBehaviour {

    private static final Logger logger = LoggerFactory.getLogger(ListenForPowerConfirmation.class);
    private static final MessageTemplate messageTemplate = and(MatchPerformative(INFORM), MatchProtocol(SERVER_JOB_CFP_PROTOCOL));

    private ServerAgent myServerAgent;

    /**
     * Method runs at the start of the behaviour. It casts the abstract agent to agent of type Server Agent
     */
    @Override
    public void onStart() {
        super.onStart();
        myServerAgent = (ServerAgent) myAgent;
    }

    /**
     * Method listens for the confirmation message coming from Green Energy Source. When the confirmation is received,
     * it schedules the start of job execution
     */
    @Override
    public void action() {
        final ACLMessage inform = myAgent.receive(messageTemplate);

        if (Objects.nonNull(inform)) {
            try {
                final String jobId = getMapper().readValue(inform.getContent(), String.class);
                final Job job = myServerAgent.getJobById(jobId);
                logger.info("[{}] Scheduling the execution of the job", myAgent.getName());
                announceBookedJob(myServerAgent, jobId);
                myAgent.addBehaviour(CheckWeatherBeforeJobExecution.createFor(myServerAgent, job));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }
}
