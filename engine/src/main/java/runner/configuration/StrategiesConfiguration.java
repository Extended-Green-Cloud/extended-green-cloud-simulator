package runner.configuration;

import static java.lang.Integer.parseInt;
import static org.jrba.rulesengine.constants.RuleSetTypeConstants.DEFAULT_RULE_SET;
import static org.jrba.utils.file.FileReader.buildResourceFilePath;

import java.io.IOException;
import java.util.Properties;

import org.greencloud.commons.exception.InvalidPropertiesException;

import runner.EngineRunner;

/**
 * Constants used to set up strategies parsing.
 */
public class StrategiesConfiguration extends AbstractConfiguration {

	public static final String RULE_SETS_DIR = "rulesets";
	public static final String STRATEGIES_DIR = "strategy";
	private static final String STRATEGY_PROPERTIES_FILE = "strategy.properties";

	/**
	 * Port on which the RuleSetRestAPI is to be run.
	 */
	public static int ruleSetApiPort;

	/**
	 * Name of the default resource allocation strategy that is to be run.
	 */
	public static String allocationStrategyName;

	/**
	 * Name of the default tasks prioritization strategy that is to be run.
	 */
	public static String prioritizationStrategyName;

	/**
	 * Number of resource allocation steps.
	 */
	public static Integer allocationStepsNumber;

	/**
	 * Method reads the properties set for the given strategies execution
	 */
	public static void readScenarioProperties() {
		final Properties props = new Properties();
		try {
			final String pathToStrategy = buildResourceFilePath(STRATEGIES_DIR, STRATEGY_PROPERTIES_FILE);
			props.load(EngineRunner.class.getClassLoader().getResourceAsStream(pathToStrategy));

			ruleSetApiPort = parseInt(ifNotBlankThenGetOrElse(props.getProperty("api.port"), "5000"));
			allocationStepsNumber = parseInt(ifNotBlankThenGetOrElse(props.getProperty("strategy.steps"), "1"));
			prioritizationStrategyName = ifNotBlankThenGetOrElse(props.getProperty("strategy.prioritization"),
					DEFAULT_RULE_SET);
			allocationStrategyName = ifNotBlankThenGetOrElse(props.getProperty("strategy.allocation"),
					DEFAULT_RULE_SET);

		} catch (final IOException e) {
			throw new InvalidPropertiesException("Could not read properties file:", e);
		}
	}
}
