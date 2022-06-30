package agents.server.behaviour.jobexecution;

import static messages.domain.PowerCheckMessageFactory.preparePowerCheckMessage;

import agents.server.ServerAgent;
import domain.job.ImmutablePowerJob;
import domain.job.Job;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckWeatherBeforeJobExecution extends WakerBehaviour {

    private static final Logger logger = LoggerFactory.getLogger(CheckWeatherBeforeJobExecution.class);
    private final ServerAgent myServerAgent;
    private final Job jobToExecute;

    /**
     * Behaviour constructor.
     *
     * @param agent   agent that is executing the behaviour
     * @param timeOut time after which the job will be executed
     * @param job     job that is to be executed
     */
    private CheckWeatherBeforeJobExecution(Agent agent, long timeOut, final Job job) {
        super(agent, timeOut);
        this.jobToExecute = job;
        myServerAgent = (ServerAgent) agent;
    }

    /**
     * Method which is responsible for creating the behaviour. It calculates the time after which
     * the job execution will start. For testing purposes 1h = 2s. If the provided time is later than
     * the current time then the job execution will start immediately
     *
     * @param serverAgent  agent that will execute the behaviour
     * @param jobToExecute job that will be executed
     * @return behaviour to be run
     */
    public static CheckWeatherBeforeJobExecution createFor(final ServerAgent serverAgent, final Job jobToExecute) {
        final long hourDifference = ChronoUnit.HOURS.between(OffsetDateTime.now(), jobToExecute.getStartTime());
        final long timeOut = hourDifference < 0 ? 0 : hourDifference * 2 * 1000;
        return new CheckWeatherBeforeJobExecution(serverAgent, timeOut, jobToExecute);
    }

    @Override
    protected void onWake() {
        logger.info("[{}] Checking weather before the job execution {}", myAgent.getName(), jobToExecute.getClientIdentifier());
        myAgent.send(getPowerCheckMessage(jobToExecute));
        myAgent.addBehaviour(new ListenForWeather(myServerAgent, jobToExecute));
    }

    private ACLMessage getPowerCheckMessage(final Job job) {
        var powerJob = ImmutablePowerJob.builder()
            .power(job.getPower())
            .startTime(job.getStartTime())
            .endTime(job.getEndTime())
            .jobId(job.getJobId())
            .build();

        return preparePowerCheckMessage(powerJob, myServerAgent.getGreenSourceForJobMap().get(job.getJobId()).getName());
    }
}
