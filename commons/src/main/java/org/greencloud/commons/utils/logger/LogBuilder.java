package org.greencloud.commons.utils.logger;

import static java.lang.String.valueOf;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_AGENT_NAME;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * Builder allowing to construct and configure log messages.
 */
public class LogBuilder {

	private final Logger logger;

	/**
	 * Default constructor.
	 *
	 * @param loggingClass instance of the class that is responsible for logging the message.
	 */
	private LogBuilder(final Class<?> loggingClass) {
		this.logger = getLogger(loggingClass);
	}

	/**
	 * Method used to initialize log builder.
	 *
	 * @param loggingClass instance of the class that is responsible for logging the message.
	 * @return initialized builder object
	 */
	public static LogBuilder constructLogBuilder(final Class<?> loggingClass) {
		return new LogBuilder(loggingClass);
	}

	/**
	 * Method used to put job identifier into the context.
	 *
	 * @param jobId job identifier
	 * @return current builder object
	 */
	public LogBuilder withJobId(final String jobId) {
		MDC.put(MDC_JOB_ID, jobId);
		return this;
	}

	/**
	 * Method used to put job identifier into the context.
	 *
	 * @param jobId job identifier
	 * @return current builder object
	 */
	public LogBuilder withJobId(final Integer jobId) {
		MDC.put(MDC_JOB_ID, valueOf(jobId));
		return this;
	}

	/**
	 * Method used to put job identifier into the context.
	 *
	 * @param jobId job identifier
	 * @return current builder object
	 */
	public LogBuilder withJobId(final Double jobId) {
		MDC.put(MDC_JOB_ID, valueOf(jobId));
		return this;
	}

	/**
	 * Method used to put ruleset identifier into the context.
	 *
	 * @param ruleSetId ruleset identifier
	 * @return current builder object
	 */
	public LogBuilder withRuleSetId(final String ruleSetId) {
		MDC.put(MDC_RULE_SET_ID, ruleSetId);
		return this;
	}

	/**
	 * Method used to put ruleset identifier into the context.
	 *
	 * @param ruleSetId ruleset identifier
	 * @return current builder object
	 */
	public LogBuilder withRuleSetId(final Integer ruleSetId) {
		MDC.put(MDC_RULE_SET_ID, valueOf(ruleSetId));
		return this;
	}

	/**
	 * Method used to put ruleset identifier into the context.
	 *
	 * @param ruleSetId ruleset identifier
	 * @return current builder object
	 */
	public LogBuilder withRuleSetId(final Double ruleSetId) {
		MDC.put(MDC_RULE_SET_ID, valueOf(ruleSetId));
		return this;
	}

	/**
	 * Method used to put agent name into the context.
	 *
	 * @param agentName name of the agent
	 * @return current builder object
	 */
	public LogBuilder withAgentName(final String agentName) {
		MDC.put(MDC_AGENT_NAME, agentName);
		return this;
	}

	/**
	 * Method used to clear the context of displayed log.
	 *
	 * @return current builder object
	 */
	public LogBuilder clearContext() {
		MDC.clear();
		return this;
	}

	/**
	 * Method used to log information message.
	 *
	 * @param content message text
	 * @param args    parameters passed to the message text
	 */
	public void messageInfo(final String content, final Object... args) {
		this.logger.info(content, args);
	}

	/**
	 * Method used to log error message.
	 *
	 * @param content message text
	 * @param args    parameters passed to the message text
	 */
	public void messageError(final String content, final Object... args) {
		this.logger.error(content, args);
	}

	/**
	 * Method used to log warning message.
	 *
	 * @param content message text
	 * @param args    parameters passed to the message text
	 */
	public void messageWarn(final String content, final Object... args) {
		this.logger.warn(content, args);
	}

	/**
	 * Method used to log debug message.
	 *
	 * @param content message text
	 * @param args    parameters passed to the message text
	 */
	public void messageDebug(final String content, final Object... args) {
		this.logger.debug(content, args);
	}
}
