package org.greencloud.managingsystem.service.analyzer.logs;

/**
 * Class storing log messages used in analyzer services of managing system
 */
public class ManagingAgentAnalyzerLog {

	// ANALYZER SERVICE LOG MESSAGES
	public static final String SYSTEM_QUALITY_INDICATOR_VIOLATED_LOG =
			"Overall system quality has dropped below a desired threshold! Analyzing system to increase "
					+ "overall quality...";
	public static final String SYSTEM_QUALITY_INDICATOR_NOT_VIOLATED_LOG =
			"System quality fulfills the desired threshold. Analyzing system to optimize overall quality...";

	public static final String GOAL_QUALITY_BELOW_THRESHOLD_LOG =
			"Goal quality has violated given threshold! Analyzing adaptation actions.";
	public static final String GOAL_QUALITY_ABOVE_THRESHOLD_LOG =
			"Goal quality comply with the given threshold. Performing the trend analysis.";
	public static final String GOAL_TREND_FOUND_LOG =
			"The negative trend was found in goal quality. Adapting the system in advance.";
	public static final String GOAL_TREND_NOT_FOUND_LOG =
			"The goal is not tending to violate the threshold. Finishing analysis";
	public static final String NO_ACTIONS_AVAILABLE_LOG =
			"There are no adaptation actions available! Adaptation not possible. Consider adding adaptation actions";
	public static final String COMPUTE_ADAPTATION_ACTION_QUALITY_LOG =
			"Computing qualities of available adaptation actions...";
}
