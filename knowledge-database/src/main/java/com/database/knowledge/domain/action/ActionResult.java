package com.database.knowledge.domain.action;

/**
 * Record that describes action result (i.e. average difference in goal quality after executing the action and number
 * of runs in which given difference in quality was measured)
 */
public record ActionResult(Double diff, Integer runs) {

	public ActionResult() {
		this(0.0, 0);
	}
}
