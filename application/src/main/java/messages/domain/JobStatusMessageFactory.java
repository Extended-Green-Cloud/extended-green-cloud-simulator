package messages.domain;

import static common.constant.MessageProtocolConstants.*;
import static jade.lang.acl.ACLMessage.INFORM;
import static mapper.JsonMapper.getMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import domain.job.ImmutableJobInstanceIdentifier;
import domain.job.JobInstanceIdentifier;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Class storing methods used in creating messages informing that the job execution has finished
 */
public class JobStatusMessageFactory {

    /**
     * Method prepares the information message about the job execution start which is to be sent
     * to the client
     *
     * @param clientId client global name
     * @return inform ACLMessage
     */
    public static ACLMessage prepareStartMessageForClient(final String clientId) {
        final ACLMessage informationMessage = new ACLMessage(INFORM);
        informationMessage.setProtocol(STARTED_JOB_PROTOCOL);
        informationMessage.setContent(STARTED_JOB_PROTOCOL);
        informationMessage.addReceiver(new AID(clientId, AID.ISGUID));
        return informationMessage;
    }

    /**
     * Method prepares the information message about the job execution finish which is to be sent
     * to the client
     *
     * @param clientId client global name
     * @return inform ACLMessage
     */
    public static ACLMessage prepareFinishMessageForClient(final String clientId) {
        final ACLMessage informationMessage = new ACLMessage(INFORM);
        informationMessage.setProtocol(FINISH_JOB_PROTOCOL);
        informationMessage.setContent(FINISH_JOB_PROTOCOL);
        informationMessage.addReceiver(new AID(clientId, AID.ISGUID));
        return informationMessage;
    }

    /**
     * Method prepares the information message about the job execution finish which is to be sent
     * to list of receivers
     *
     * @param jobId        unique identifier of the kob of interest
     * @param jobStartTime time when the job execution started
     * @param receivers    list of AID addresses of the message receivers
     * @return inform ACLMessage
     */
    public static ACLMessage prepareFinishMessage(final String jobId, final OffsetDateTime jobStartTime, final List<AID> receivers) {
        final ACLMessage informationMessage = new ACLMessage(INFORM);
        final JobInstanceIdentifier jobInstanceId = ImmutableJobInstanceIdentifier.builder().jobId(jobId).startTime(jobStartTime).build();
        try {
            informationMessage.setContent(getMapper().writeValueAsString(jobInstanceId));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        informationMessage.setProtocol(FINISH_JOB_PROTOCOL);
        receivers.forEach(informationMessage::addReceiver);
        return informationMessage;
    }

    /**
     * Method prepares the information message about finishing the power delivery by hand by the Green Source.
     *
     * @param jobInstanceId identifier of the job instance
     * @param serverAddress server address
     * @return inform ACLMessage
     */
    public static ACLMessage prepareManualFinishMessageForServer(final JobInstanceIdentifier jobInstanceId, final AID serverAddress) {
        final ACLMessage informationMessage = new ACLMessage(INFORM);
        informationMessage.setProtocol(MANUAL_JOB_FINISH_PROTOCOL);
        try {
            informationMessage.setContent(getMapper().writeValueAsString(jobInstanceId));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        informationMessage.addReceiver(serverAddress);
        return informationMessage;
    }

    /**
     * Method prepares the information message stating that the job execution has started
     *
     * @param jobId        unique identifier of the kob of interest
     * @param jobStartTime time when the job execution started
     * @param receivers    list of AID addresses of the message receivers
     * @return inform ACLMessage
     */
    public static ACLMessage prepareJobStartedMessage(final String jobId, final OffsetDateTime jobStartTime, final List<AID> receivers) {
        final ACLMessage informationMessage = new ACLMessage(INFORM);
        informationMessage.setProtocol(STARTED_JOB_PROTOCOL);
        final JobInstanceIdentifier jobInstanceId = ImmutableJobInstanceIdentifier.builder().jobId(jobId).startTime(jobStartTime).build();
        try {
            informationMessage.setContent(getMapper().writeValueAsString(jobInstanceId));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        receivers.forEach(informationMessage::addReceiver);
        return informationMessage;
    }

    /**
     * Method prepares the information message about the job execution delay which is to be sent
     * to the client
     *
     * @param clientId client global name
     * @return inform ACLMessage
     */
    public static ACLMessage prepareDelayMessageForClient(final String clientId) {
        final ACLMessage informationMessage = new ACLMessage(INFORM);
        informationMessage.setProtocol(DELAYED_JOB_PROTOCOL);
        informationMessage.setContent(DELAYED_JOB_PROTOCOL);
        informationMessage.addReceiver(new AID(clientId, AID.ISGUID));
        return informationMessage;
    }

    /**
     * Method prepares the message informing the client that there is a power shortage and that the job will be executed
     * using the backup power
     *
     * @param clientId client global name
     * @return inform ACLMessage
     */
    public static ACLMessage preparePowerShortageMessageForClient(final String clientId) {
        final ACLMessage informationMessage = new ACLMessage(INFORM);
        informationMessage.setProtocol(BACK_UP_POWER_JOB_PROTOCOL);
        informationMessage.setContent(BACK_UP_POWER_JOB_PROTOCOL);
        informationMessage.addReceiver(new AID(clientId, AID.ISGUID));
        return informationMessage;
    }
}
