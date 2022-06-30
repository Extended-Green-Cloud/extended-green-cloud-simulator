package com.gui.domain.types;

/**
 * Enum storing available job statuses displayed in GUI
 */
public enum JobStatusEnum {

    CREATED("BEFORE SEND"),
    PROCESSING("PROCESSING"),
    IN_PROGRESS("IN PROGRESS"),
    DELAYED("DELAYED"),
    FINISHED("FINISHED"),
    REJECTED("REJECTED");

    private final String status;

    JobStatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
