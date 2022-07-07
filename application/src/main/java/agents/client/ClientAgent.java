package agents.client;

import static common.TimeUtils.convertToSimulationTime;
import static common.TimeUtils.getCurrentTime;

import agents.client.behaviour.df.FindCloudNetworkAgents;
import agents.client.behaviour.RequestJobExecution;
import agents.client.behaviour.listener.ListenForJobUpdate;
import behaviours.ReceiveGUIController;
import common.TimeUtils;
import domain.job.ImmutableJob;
import domain.job.Job;
import exception.IncorrectTaskDateException;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Agent representing the Client that wants to have the job executed by the Cloud Network
 */
public class ClientAgent extends AbstractClientAgent {

    private static final Logger logger = LoggerFactory.getLogger(ClientAgent.class);

    /**
     * Method run at the agent's start. In initialize the Client Agent based on the given by the user arguments and
     * runs the starting behaviours - requesting the job execution and listening for job result information
     */
    @Override
    protected void setup() {
        super.setup();
        final Object[] args = getArguments();

        if (Objects.nonNull(args) && args.length == 4) {
            initializeAgent();

            // TODO to be removed (added for testing purposes)
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            final Job jobToBeExecuted = initializeAgentJob(args);
            addBehaviour(new ReceiveGUIController(this, prepareStartingBehaviour(jobToBeExecuted)));
        } else {
            logger.error("Incorrect arguments: some parameters for client's job are missing - check the parameters in the documentation");
            doDelete();
        }
    }

    /**
     * Method run before the Client is being deleted. It logs the Client's finish information.
     */
    @Override
    protected void takeDown() {
        logger.info("I'm finished. Bye!");
        if (Objects.nonNull(getGuiController())) {
            getGuiController().removeAgentNodeFromGraph(getAgentNode());
        }
        super.takeDown();
    }

    private void initializeAgent() {
        this.chosenCloudNetworkAgent = null;
    }

    private Job initializeAgentJob(final Object[] arguments) {
        try {
            final OffsetDateTime startTime = TimeUtils.convertToOffsetDateTime(arguments[0].toString());
            final OffsetDateTime endTime = TimeUtils.convertToOffsetDateTime(arguments[1].toString());
            final OffsetDateTime currentTime = TimeUtils.getCurrentTimeMinusError();
            if (startTime.isBefore(currentTime) || endTime.isBefore(currentTime)) {
                logger.error("The job execution dates cannot be before current time!");
                doDelete();
            }
            if (endTime.isBefore(startTime)) {
                logger.error("The job execution end date cannot be before job execution start date!");
                doDelete();
            }
            prepareSimulatedTimes(startTime, endTime);
            logger.info("[{}] Job simulation time: from {} to {}", this.getName(), simulatedJobStart, simulatedJobEnd);
            return ImmutableJob.builder()
                    .clientIdentifier(getAID().getName())
                    .startTime(getSimulatedJobStart())
                    .endTime(getSimulatedJobEnd())
                    .power(Integer.parseInt(arguments[2].toString()))
                    .jobId(arguments[3].toString())
                    .build();
        } catch (IncorrectTaskDateException e) {
            logger.error(e.getMessage());
            doDelete();
        } catch (NumberFormatException e) {
            logger.error("The given power is not a number!");
            doDelete();
        }
        return null;
    }

    private void prepareSimulatedTimes(final OffsetDateTime startTime, final OffsetDateTime endTime) {
        final OffsetDateTime currentTime = getCurrentTime();
        final long expectedJobStart = convertToSimulationTime(ChronoUnit.SECONDS.between(currentTime, startTime));
        final long expectedJobEnd = convertToSimulationTime(ChronoUnit.SECONDS.between(currentTime, endTime));
        setSimulatedJobStart(currentTime.plus(expectedJobStart, ChronoUnit.MILLIS));
        setSimulatedJobEnd(currentTime.plus(expectedJobEnd, ChronoUnit.MILLIS));
    }

    private List<Behaviour> prepareStartingBehaviour(final Job job) {
        var parallelBehaviour = new ParallelBehaviour();
        parallelBehaviour.addSubBehaviour(new ListenForJobUpdate(this));
        var startingBehaviour = new SequentialBehaviour(this);
        startingBehaviour.addSubBehaviour(new FindCloudNetworkAgents());
        startingBehaviour.addSubBehaviour(new RequestJobExecution(this, null, job));
        parallelBehaviour.addSubBehaviour(startingBehaviour);
        return Collections.singletonList(parallelBehaviour);
    }
}
