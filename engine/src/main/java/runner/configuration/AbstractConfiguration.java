package runner.configuration;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Abstract class that contains generic methods used in setting up the configuration
 */
public class AbstractConfiguration {

	protected static String ifNotBlankThenGetOrElse(final String property, final String defaultVal) {
		return isNullOrEmpty(property) ? defaultVal : property;
	}
}
