package agents.greenenergy.behaviour.request;

import static common.GUIUtils.displayMessageArrow;
import static common.constant.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static jade.lang.acl.ACLMessage.CFP;
import static jade.lang.acl.MessageTemplate.*;
import static mapper.JsonMapper.getMapper;

import agents.greenenergy.GreenEnergyAgent;
import com.fasterxml.jackson.core.JsonProcessingException;
import domain.job.JobStatusEnum;
import domain.job.PowerJob;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;

/**
 * Behaviour responsible for handling servers' call for proposals for power that is necessary to execute the given job
 */
public class ReceivePowerRequest extends CyclicBehaviour {

    private static final Logger logger = LoggerFactory.getLogger(ReceivePowerRequest.class);
    private static final MessageTemplate messageTemplate = and(MatchPerformative(CFP), MatchProtocol(SERVER_JOB_CFP_PROTOCOL));

    private final GreenEnergyAgent myGreenEnergyAgent;
    private final String guid;

    /**
     * Behaviours constructor.
     *
     * @param myAgent agent which is executing the behaviour
     */
    public ReceivePowerRequest(Agent myAgent) {
        this.myGreenEnergyAgent = (GreenEnergyAgent) myAgent;
        this.guid = myGreenEnergyAgent.getName();
    }

    /**
     * Method which listens for the power call for proposals coming from the servers. It analyzes the request and either
     * rejects it or proceeds with request processing by sending another request to Monitoring Agent for the weather data.
     */
    @Override
    public void action() {
        final ACLMessage cfp = myAgent.receive(messageTemplate);
        if (Objects.nonNull(cfp)) {
            try {
                final PowerJob job = readJob(cfp);
                logger.info("[{}] Sending forecast request to monitoring agent.", guid);
                myGreenEnergyAgent.getPowerJobs().put(job, JobStatusEnum.PROCESSING);
                requestMonitoringData(cfp, job);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }

    private PowerJob readJob(ACLMessage callForProposal) throws NotUnderstoodException {
        try {
            return getMapper().readValue(callForProposal.getContent(), PowerJob.class);
        } catch (JsonProcessingException e) {
            throw new NotUnderstoodException(e.getMessage());
        }
    }

    private void requestMonitoringData(final ACLMessage cfp, final PowerJob job) {
        var sequentialBehaviour = new SequentialBehaviour();
        sequentialBehaviour.addSubBehaviour(new RequestForecastData(myGreenEnergyAgent, cfp.getConversationId(), job));
        sequentialBehaviour.addSubBehaviour(new ReceiveForecastData(myGreenEnergyAgent, cfp, job));
        myAgent.addBehaviour(sequentialBehaviour);
    }
}
