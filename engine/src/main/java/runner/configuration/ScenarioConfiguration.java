package runner.configuration;

import static java.io.File.separator;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static runner.configuration.ResourceRequirementConfiguration.readJobsRequirements;
import static runner.configuration.enums.ClientGeneratorTypeEnum.FROM_EVENTS;
import static runner.configuration.enums.ClientGeneratorTypeEnum.FROM_SAMPLE;
import static runner.constants.EngineConstants.PROPERTIES_DIR;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import org.greencloud.commons.exception.InvalidPropertiesException;

import runner.EngineRunner;
import runner.configuration.enums.ClientGeneratorTypeEnum;

/**
 * Constants used to set up the scenario run in the system
 * The configuration is by default injected from .properties files
 */
public class ScenarioConfiguration extends AbstractConfiguration {

	private static final String SCENARIOS_DIR = "scenarios";
	private static final String SAMPLE_DIR = "samples";
	private static final String SCENARIO_PROPERTIES_FILE = "scenario.properties";

	/**
	 * Type of workload generation
	 */
	public static ClientGeneratorTypeEnum generatorType;

	/**
	 * Path to the file containing scenario structure
	 */
	public static String scenarioFilePath;

	/**
	 * Optional path to the file containing scenario events
	 */
	public static Optional<String> eventFilePath;

	/**
	 * Optional path to the file containing jobs sample
	 */
	public static Optional<String> jobsSampleFilePath;

	/**
	 * Number of clients run by default at the given scenario
	 */
	public static long clientNumber;

	/**
	 * Number of different job types
	 */
	public static int jobTypesNumber;


	/**
	 * Method reads the properties set for the given scenario execution
	 */
	public static void readScenarioProperties() {
		final Properties props = new Properties();
		try {
			final String pathToScenarioProps = buildResourceFilePath(PROPERTIES_DIR, SCENARIO_PROPERTIES_FILE);
			props.load(EngineRunner.class.getClassLoader().getResourceAsStream(pathToScenarioProps));

			generatorType = ClientGeneratorTypeEnum.valueOf(props.getProperty("scenario.generator"));

			final boolean useSubDirectory = parseBoolean(props.getProperty("scenario.usesubdirectory"));

			final String scenarioPath = useSubDirectory ? retrieveScenarioSubDirectory(props) : SCENARIOS_DIR;

			scenarioFilePath = buildResourceFilePath(scenarioPath, props.getProperty("scenario.structure"));
			eventFilePath = generatorType.equals(FROM_EVENTS)
					? Optional.of(buildResourceFilePath(scenarioPath, props.getProperty("scenario.events")))
					: Optional.empty();
			jobsSampleFilePath = generatorType.equals(FROM_SAMPLE)
					? Optional.of(buildResourceFilePath(SAMPLE_DIR, props.getProperty("scenario.jobssample")))
					: Optional.empty();
			clientNumber = parseInt(ifNotBlankThenGetOrElse(props.getProperty("scenario.clients.number"), "0"));
			jobTypesNumber = parseInt(ifNotBlankThenGetOrElse(props.getProperty("scenario.jobs.typesnumber"), "0"));
			readJobsRequirements(props);

		} catch (final IOException e) {
			throw new InvalidPropertiesException("Could not read properties file:", e);
		}
	}

	/**
	 * Method retrieves the directory containing the scenario used in the system run
	 */
	protected static String retrieveScenarioSubDirectory(final Properties props) {
		final String subDirectory = props.getProperty("scenario.subdirectory").replace(".", separator);
		return buildResourceFilePath(SCENARIOS_DIR, subDirectory);
	}
}
