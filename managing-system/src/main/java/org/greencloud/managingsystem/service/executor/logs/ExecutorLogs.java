package org.greencloud.managingsystem.service.executor.logs;

public final class ExecutorLogs {

	public static final String COMPLETED_ACTION_LOG = "Adaptation action id {} on agent {} executed successfully!";
	public static final String ACTION_FAILED_LOG = "Adaptation action id {} on agent {} execution failed!";
	public static final String VERIFY_ACTION_START_LOG = "Verifying {} executed on {} at {}.";
	public static final String VERIFY_ACTION_END_LOG = "Action {} verified with following goal changes: {}";

	private ExecutorLogs() {
	}
}
