package com.greencloud.commons.job;

/**
 * Enum storing available job statuses displayed in GUI
 */
public enum ClientJobStatusEnum {

	CREATED("BEFORE SEND"),
	SCHEDULED("SCHEDULED"),
	PROCESSED("PROCESSED"),
	IN_PROGRESS("IN PROGRESS"),
	DELAYED("DELAYED"),
	FINISHED("FINISHED"),
	ON_BACK_UP("ON BACK UP"),
    ON_HOLD("ON_HOLD"),
	FAILED("FAILED"),
	REJECTED("REJECTED");

	private final String status;

	ClientJobStatusEnum(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
}
