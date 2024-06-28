package org.greencloud.commons.constants;

/**
 * Class with constants describing available fact types
 */
public class EGCSFactTypeConstants {

	// FACTS
	public static final String INITIAL_FACTS = "initial-facts";

	// TRANSFER FACTS
	public static final String TRANSFER_INSTANT = "transfer-instant";

	// ALLOCATION FACTS
	public static final String ALLOCATION = "allocation";
	public static final String ALLOCATION_TIMER = "allocation-timer";

	// JOB FACTS
	public static final String JOB = "job";
	public static final String JOB_ACCEPTED = "job-accepted";
	public static final String JOB_REFUSED = "job-refused";
	public static final String JOB_PRIORITY_FACTS = "job-priority-facts";
	public static final String JOB_DIVIDED = "job-divided";
	public static final String JOB_PREVIOUS = "job-previous";
	public static final String JOB_IS_STARTED = "job-is-started";
	public static final String JOB_IS_PRESENT = "job-is-present";
	public static final String JOBS = "jobs";
	public static final String JOB_ID = "job-id";
	public static final String JOB_TIME = "job-time";
	public static final String JOB_START_INFORM = "job-start-inform";
	public static final String JOB_FINISH_INFORM = "job-finish-inform";
	public static final String JOB_MANUAL_FINISH_INFORM = "job-manual-finish-inform";

	// MESSAGES FACTS
	public static final String OFFER = "offer";
	public static final String BEST_PROPOSAL = "best-proposal";
	public static final String NEW_PROPOSAL = "new-proposal";
	public static final String BEST_PROPOSAL_CONTENT = "best-proposal-content";
	public static final String NEW_PROPOSAL_CONTENT = "new-proposal-content";

	// RESOURCE FACTS
	public static final String PREVIOUS_RESOURCES =	"previous-resources";
	public static final String RESOURCES_SUFFICIENCY =	"resources-sufficiency";

	// ENERGY FACTS
	public static final String ENERGY_TYPE = "energy-type";

	// BEHAVIOUR FACTS
	public static final String INITIATE_CFP = "initiate-cfp";

	// PRICE FACTS
	public static final String COMPUTE_FINAL_PRICE = "compute-final-price";
}
