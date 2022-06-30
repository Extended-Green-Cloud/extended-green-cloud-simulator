package com.gui.domain.types;

import static com.gui.utils.GUIUtils.formatToHTML;

/**
 * Enum storing all possible labels describing the agent node
 */
public enum AgentNodeLabelEnum implements LabelEnum {

    // Common for job executors labels:
    IS_ACTIVE_LABEL("CURRENT STATE:"),
    MAXIMUM_CAPACITY_LABEL("MAXIMUM CAPACITY:"),
    TRAFFIC_LABEL("CURRENT TRAFFIC:"),
    TOTAL_NUMBER_OF_CLIENTS_LABEL("TOTAL NUMBER OF CLIENTS:"),
    NUMBER_OF_EXECUTED_JOBS_LABEL("NUMBER OF EXECUTED JOBS:"),

    // Cloud network labels:
    SERVERS_NUMBER_LABEL("NUMBER OF CONNECTED SERVERS:"),

    // Green source labels:
    GREEN_SOURCE_NUMBER_LABEL("NUMBER OF CONNECTED SERVERS:"),
    LOCATION_LATITUDE_LABEL("LOCATION LATITUDE:"),
    LOCATION_LONGITUDE_LABEL("LOCATION LONGITUDE:"),

    // Clients labels
    JOB_ID_LABEL("JOB IDENTIFIER:"),
    JOB_START_LABEL("EXECUTION START DATE:"),
    JOB_END_LABEL("EXECUTION END DATE:"),
    JOB_STATUS("JOB STATUS:");

    private final String label;

    AgentNodeLabelEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return formatToHTML(label);
    }
}
