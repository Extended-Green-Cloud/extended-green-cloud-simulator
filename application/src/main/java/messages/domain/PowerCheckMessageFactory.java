package messages.domain;

import static common.constant.MessageProtocolConstants.SERVER_JOB_START_CHECK_PROTOCOL;
import static jade.lang.acl.ACLMessage.REQUEST;
import static mapper.JsonMapper.getMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import domain.job.PowerJob;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class PowerCheckMessageFactory {

    /**
     * Method prepares the power check message.
     *
     * @param job           job we want to check
     * @param greenSourceId green source global name
     * @return request ACLMessage
     */
    public static ACLMessage preparePowerCheckMessage(final PowerJob job, final String greenSourceId) {
        final ACLMessage informationMessage = new ACLMessage(REQUEST);
        informationMessage.setProtocol(SERVER_JOB_START_CHECK_PROTOCOL);
        try {
            informationMessage.setContent(getMapper().writeValueAsString(job));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        informationMessage.addReceiver(new AID(greenSourceId, AID.ISGUID));
        return informationMessage;
    }
}
