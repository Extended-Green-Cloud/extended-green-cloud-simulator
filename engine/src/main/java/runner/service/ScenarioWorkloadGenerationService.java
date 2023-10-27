package runner.service;

import static com.greencloud.factory.constants.AgentControllerConstants.RUN_CLIENT_AGENT_DELAY;
import static java.lang.Math.floorDiv;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.shuffle;
import static java.util.stream.LongStream.rangeClosed;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.MEMORY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.STORAGE;
import static org.greencloud.commons.enums.agent.ClientTimeTypeEnum.REAL_TIME;
import static org.greencloud.commons.mapper.JobMapper.mapSyntheticArgoJobToJob;
import static org.greencloud.commons.mapper.JsonMapper.getMapper;
import static runner.configuration.ResourceRequirementConfiguration.cpuRange;
import static runner.configuration.ResourceRequirementConfiguration.deadlineRange;
import static runner.configuration.ResourceRequirementConfiguration.durationRange;
import static runner.configuration.ResourceRequirementConfiguration.memoryRange;
import static runner.configuration.ResourceRequirementConfiguration.stepsNumberRange;
import static runner.configuration.ResourceRequirementConfiguration.storageRange;
import static runner.configuration.ScenarioConfiguration.clientNumber;
import static runner.configuration.ScenarioConfiguration.generatorType;
import static runner.configuration.ScenarioConfiguration.jobTypesNumber;
import static runner.configuration.ScenarioConfiguration.jobsSampleFilePath;
import static runner.utils.FileReader.readFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import org.greencloud.commons.args.agent.client.factory.ClientArgs;
import org.greencloud.commons.args.job.ImmutableSyntheticJobArgs;
import org.greencloud.commons.args.job.ImmutableSyntheticJobStepArgs;
import org.greencloud.commons.args.job.JobArgs;
import org.greencloud.commons.args.job.SyntheticJobArgs;
import org.greencloud.commons.exception.InvalidScenarioException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.greencloud.factory.AgentFactory;
import com.greencloud.factory.AgentFactoryImpl;

import jade.wrapper.AgentController;

/**
 * Service containing methods responsible for generating workload for the simulation
 */
public class ScenarioWorkloadGenerationService {

	final ThreadLocalRandom random = ThreadLocalRandom.current();
	private final AgentFactory agentFactory;
	private final ScenarioEventService eventService;
	private final AbstractScenarioService scenarioService;

	public ScenarioWorkloadGenerationService(final AbstractScenarioService scenarioService) {
		this.agentFactory = new AgentFactoryImpl();
		this.eventService = new ScenarioEventService(scenarioService);
		this.scenarioService = scenarioService;
	}

	/**
	 * Method generates workload for the simulation based on the selected by user generator type.
	 */
	public void generateWorkloadForSimulation() {
		switch (generatorType) {
			case RANDOM -> runClientAgents(emptyList());
			case FROM_SAMPLE -> runJobsDrawnFromSample();
			case FROM_EVENTS -> eventService.runScenarioEvents();
		}
	}

	private void runJobsDrawnFromSample() {
		if (jobsSampleFilePath.isPresent()) {
			final File jobSamplesFile = readFile(jobsSampleFilePath.get());
			final List<SyntheticJobArgs> sampleJobs = parseJobSamples(jobSamplesFile);
			final List<SyntheticJobArgs> selectedJobs = pickFromJobSamples(sampleJobs);
			runClientAgents(selectedJobs);
		}
	}

	private List<SyntheticJobArgs> pickFromJobSamples(final List<SyntheticJobArgs> sampleJobs) {
		final int sampleSize = sampleJobs.size();

		if (clientNumber <= sampleSize) {
			return pickRandomJobs(sampleJobs, (int) clientNumber);
		} else {
			final int samplingNo = (int) floorDiv(clientNumber, sampleSize);
			final int samplingReminder = (int) (clientNumber % sampleSize);

			final List<SyntheticJobArgs> jobs = new ArrayList<>(rangeClosed(1, samplingNo)
					.mapToObj(it -> pickRandomJobs(sampleJobs, sampleSize))
					.flatMap(Collection::stream)
					.toList());
			jobs.addAll(pickRandomJobs(sampleJobs, samplingReminder));
			return jobs;
		}
	}

	private List<SyntheticJobArgs> pickRandomJobs(final List<SyntheticJobArgs> sampleJobs, final int sampleSize) {
		final List<SyntheticJobArgs> jobsCopy = new ArrayList<>(sampleJobs);
		shuffle(jobsCopy);
		return jobsCopy.subList(0, sampleSize);
	}

	private void runClientAgents(final List<SyntheticJobArgs> clientJobs) {
		rangeClosed(1, clientNumber).forEach(idx -> {
			final int jobId = scenarioService.timescaleDatabase.getNextClientId();
			final String clientName = format("Client%d", jobId);
			final SyntheticJobArgs syntheticJob = clientJobs.isEmpty() ?
					generateRandomJob() :
					clientJobs.get((int) idx - 1);
			final JobArgs job = mapSyntheticArgoJobToJob(syntheticJob);

			final ClientArgs clientArgs = agentFactory.createClientAgent(
					clientName, valueOf(jobId), REAL_TIME, job);
			final AgentController agentController = scenarioService.factory.createAgentController(clientArgs,
					scenarioService.scenario);
			scenarioService.factory.runAgentController(agentController, RUN_CLIENT_AGENT_DELAY);
		});
	}

	private SyntheticJobArgs generateRandomJob() {
		final int typeId = random.nextInt(1, jobTypesNumber + 1);
		final String jobType = format("JobType%d", typeId);

		final long randomCPU = random.nextLong(cpuRange.lowerEndpoint(), cpuRange.upperEndpoint());
		final long randomMemory = random.nextLong(memoryRange.lowerEndpoint(), memoryRange.upperEndpoint());
		final long randomStorage = random.nextLong(storageRange.lowerEndpoint(), storageRange.upperEndpoint());
		final long randomDuration = random.nextLong(durationRange.lowerEndpoint(), durationRange.upperEndpoint());
		final long randomDeadline = deadlineRange.upperEndpoint() == 0 ? 0 : random.nextLong(
				randomDuration + deadlineRange.lowerEndpoint(),
				randomDuration + deadlineRange.upperEndpoint());

		final int stepsNumber = random.nextInt(stepsNumberRange.lowerEndpoint(), stepsNumberRange.upperEndpoint());
		final AtomicLong cpuForSteps = new AtomicLong(randomCPU - stepsNumber);
		final AtomicLong memoryForSteps = new AtomicLong(randomMemory - stepsNumber);
		final AtomicLong durationForSteps = new AtomicLong(randomDuration - stepsNumber);

		final List<ImmutableSyntheticJobStepArgs> randomSteps = rangeClosed(1, stepsNumber)
				.mapToObj(idx -> {
					final int stepId = random.nextInt(1, stepsNumber + 1);
					final String stepName = format("JobStep%d", stepId);

					final long randomStepCPU = random.nextLong(1, cpuForSteps.get());
					final long randomStepMemory = random.nextLong(1, memoryForSteps.get());
					final long randomStepDuration = random.nextLong(1, durationForSteps.get());

					cpuForSteps.updateAndGet(cpu -> cpu - randomStepCPU);
					memoryForSteps.updateAndGet(cpu -> cpu - randomStepMemory);
					durationForSteps.updateAndGet(cpu -> cpu - randomStepDuration);

					return ImmutableSyntheticJobStepArgs.builder()
							.name(stepName)
							.putResources(CPU, randomCPU)
							.putResources(MEMORY, randomMemory)
							.duration(randomStepDuration)
							.build();
				}).toList();

		return ImmutableSyntheticJobArgs.builder()
				.processType(jobType)
				.putResources(CPU, randomCPU)
				.putResources(MEMORY, randomMemory)
				.putResources(STORAGE, randomStorage)
				.duration(randomDuration)
				.deadline(randomDeadline)
				.jobSteps(randomSteps)
				.build();
	}

	private List<SyntheticJobArgs> parseJobSamples(final File jobsSampleFile) {
		try {
			return getMapper().readValue(jobsSampleFile, new TypeReference<>() {
			});
		} catch (IOException e) {
			throw new InvalidScenarioException(format("Failed to parse jobs example file \"%s\"", jobsSampleFile), e);
		}
	}
}
