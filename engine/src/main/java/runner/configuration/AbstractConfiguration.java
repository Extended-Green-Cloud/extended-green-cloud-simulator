package runner.configuration;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.io.File.separator;

import runner.EngineRunner;

/**
 * Abstract class that contains generic methods used in setting up the configuration
 */
public class AbstractConfiguration {

	/**
	 * Method verifies if the system was started from the JAR or IDE
	 *
	 * @return boolean indicating if the system was started from jar
	 */
	protected static boolean isLoadedInJar() {
		final String classPath = EngineRunner.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		return classPath.endsWith(".jar");
	}

	/**
	 * Method builds correct path of the given resource file
	 *
	 * @param pathElements elements of the path
	 * @return String path to the resource
	 */
	protected static String buildResourceFilePath(final String... pathElements) {
		if (isLoadedInJar()) {
			return String.join("/", pathElements);
		}
		return String.join(separator, pathElements);
	}

	protected static String ifNotBlankThenGetOrElse(final String property, final String defaultVal) {
		return isNullOrEmpty(property) ? defaultVal : property;
	}
}
