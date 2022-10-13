package com.greencloud.commons.job;

/**
 * Enum storing available job statuses displayed in GUI
 */
public enum JobStatusEnum {

	CREATED("BEFORE SEND"),
	IN_PROGRESS("IN PROGRESS"),
	DELAYED("DELAYED"),
	FINISHED("FINISHED"),
	ON_BACK_UP("ON BACK UP"),
    ON_HOLD("ON_HOLD"),
	FAILED("FAILED"),
	REJECTED("REJECTED");

	private final String status;

	JobStatusEnum(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
}
