package agents.greenenergy;

import static common.constant.DFServiceConstants.GS_SERVICE_NAME;
import static common.constant.DFServiceConstants.GS_SERVICE_TYPE;
import static domain.job.JobStatusEnum.ACCEPTED;
import static domain.job.JobStatusEnum.IN_PROGRESS;
import static java.util.stream.Collectors.toMap;
import static yellowpages.YellowPagesService.register;

import agents.greenenergy.behaviour.query.ReceivePowerCheckRequest;
import agents.greenenergy.behaviour.request.ReceivePowerRequest;
import agents.greenenergy.domain.EnergyTypeEnum;
import agents.greenenergy.domain.GreenPower;
import behaviours.ReceiveGUIController;
import domain.MonitoringData;
import domain.WeatherData;
import domain.job.PowerJob;
import domain.location.ImmutableLocation;
import jade.core.AID;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent representing the Green Energy Source Agent that produces the power for the Servers
 */
public class GreenEnergyAgent extends AbstractGreenEnergyAgent {

    private static final Logger logger = LoggerFactory.getLogger(GreenEnergyAgent.class);

    /**
     * Method run at the agent's start. In initialize the Green Source Agent based on the given by the user arguments,
     * registers it in the DF and then runs the starting behaviours - listening for the power requests and listening for
     * the finish power request information.
     */
    @Override
    protected void setup() {
        super.setup();
        final Object[] args = getArguments();
        initializeAgent(args);
        register(this, GS_SERVICE_TYPE, GS_SERVICE_NAME, ownerServer.getName());
        addBehaviour(new ReceivePowerRequest(this));
        addBehaviour(new ReceivePowerCheckRequest(this));
        addBehaviour(new ReceiveGUIController(this, List.of(new ReceivePowerRequest(this))));
    }

    @Override
    protected void takeDown() {
        getGuiController().removeAgentNodeFromGraph(getAgentNode());
        super.takeDown();
    }

    private void initializeAgent(final Object[] args) {
        if (Objects.nonNull(args) && args.length == 7) {
            this.powerJobs = new HashMap<>();
            this.monitoringAgent = new AID(args[0].toString(), AID.ISLOCALNAME);
            this.ownerServer = new AID(args[1].toString(), AID.ISLOCALNAME);
            try {
                this.greenPower = new GreenPower(Integer.parseInt(args[2].toString()), this);
                this.pricePerPowerUnit = Double.parseDouble(args[3].toString());
                this.location = ImmutableLocation.builder()
                        .latitude(Double.parseDouble(args[4].toString()))
                        .longitude(Double.parseDouble(args[5].toString()))
                        .build();
                this.energyType = (EnergyTypeEnum) args[6];
            } catch (NumberFormatException e) {
                logger.info("Incorrect argument: please check arguments in the documentation");
                doDelete();
            }
        } else {
            logger.info("Incorrect arguments: some parameters for green source agent are missing - check the parameters in the documentation");
            doDelete();
        }
    }

    /**
     * computes average power available during computation of the job being processed
     *
     * @param powerJob job being processed (not yet accepted!)
     * @param weather  monitoring data with forecast for requested timetable
     * @return optional of available average power of Optional.empty() if there is no available power at some moemnt
     */
    public Optional<Double> getAverageAvailablePower(final PowerJob powerJob, final MonitoringData weather) {
        var powerChart = getPowerChart(powerJob, weather);
        var availablePower = powerChart.values().stream().mapToDouble(a -> a).average().getAsDouble();
        logger.info("[{}] Calculated available {} average power {} between {} and {}", this.getName(), energyType,
            String.format("%.2f", availablePower), powerJob.getStartTime(), powerJob.getEndTime());
        if(powerChart.values().stream().anyMatch(value -> value <= 0)) {
            return Optional.empty();
        }
        return Optional.of(availablePower);
    }

    /**
     * computes power available during at a timestamp
     *
     * @param weather   monitoring data with weather for requested time
     * @return available power
     */
    public Double getAvailablePower(final WeatherData weather) {
        var timestamp = weather.getTime();
        final int powerInUse = powerJobs.keySet().stream()
            .filter(job -> job.isExecutedAtTime(timestamp))
            .filter(job -> powerJobs.get(job).equals(ACCEPTED) || powerJobs.get(job).equals(IN_PROGRESS))
            .mapToInt(PowerJob::getPower).sum();
        double availablePower = getCapacity(weather, timestamp) - powerInUse;
        logger.info("[{}] Calculated available {} power {} at {} for {}", this.getName(), energyType,
            String.format("%.2f", availablePower), timestamp, weather);
        return availablePower;
    }

    private Map<Instant, Double> getPowerChart(PowerJob powerJob, final MonitoringData weather) {
        var start = powerJob.getStartTime().toInstant();
        var end = powerJob.getEndTime().toInstant();
        var timetable = getJobsTimetable(powerJob).stream()
            .filter(time -> (time.isAfter(start) && time.isBefore(end)) || time.equals(start) || time.equals(end))
            .toList();
        var powerJobs = getPowerJobs().keySet().stream()
            .filter(job -> getPowerJobs().get(job).equals(ACCEPTED) || getPowerJobs().get(job).equals(IN_PROGRESS))
            .toList();

        if(powerJobs.isEmpty()) {
            return timetable.stream()
                .collect(toMap(Function.identity(), time -> getCapacity(weather, time)));
        }

        return timetable.stream()
            .collect(toMap(Function.identity(), time -> powerJobs.stream()
                .filter(j -> j.isExecutedAtTime(time))
                .map(PowerJob::getPower)
                .map(power -> getCapacity(weather, time) - power)
                .mapToDouble(a -> a)
                .average()
                .orElse(0)));
    }

    /**
     * Finds distinct start and end times of taken {@link PowerJob}s including the candidate job
     *
     * @param candidateJob job defining the search time window
     * @return list of all start and end times
     */
    public List<Instant> getJobsTimetable(PowerJob candidateJob) {
        return Stream.concat(Stream.of(candidateJob.getStartTime(), candidateJob.getEndTime()),
                Stream.concat(
                    powerJobs.keySet().stream().map(PowerJob::getStartTime),
                    powerJobs.keySet().stream().map(PowerJob::getEndTime)))
            .map(OffsetDateTime::toInstant)
            .distinct()
            .toList();
    }
}