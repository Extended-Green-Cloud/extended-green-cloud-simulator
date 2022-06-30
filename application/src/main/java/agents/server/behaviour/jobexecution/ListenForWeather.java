package agents.server.behaviour.jobexecution;

import static common.constant.MessageProtocolConstants.SERVER_JOB_START_CHECK_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;

import agents.server.ServerAgent;
import domain.job.Job;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListenForWeather extends CyclicBehaviour {

    private static final Logger logger = LoggerFactory.getLogger(CheckWeatherBeforeJobExecution.class);
    private static final MessageTemplate messageTemplate = and(MatchPerformative(INFORM),
        MatchProtocol(SERVER_JOB_START_CHECK_PROTOCOL));

    private final ServerAgent myServerAgent;
    private final Job jobToExecute;

    /**
     * Behaviour constructor.
     *
     * @param agent   agent that is executing the behaviour
     * @param job     job that is to be executed
     */
    public ListenForWeather(Agent agent, Job job) {
        myServerAgent =  (ServerAgent) agent;
        jobToExecute = job;
    }

    @Override
    public void action() {
        final ACLMessage message = myAgent.receive(messageTemplate);
        if (Objects.nonNull(message)) {
            try {
                logger.info("[{}] Starting job execution!.", myServerAgent.getName());
                myAgent.addBehaviour(new StartJobExecution(myServerAgent, jobToExecute));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }
}
