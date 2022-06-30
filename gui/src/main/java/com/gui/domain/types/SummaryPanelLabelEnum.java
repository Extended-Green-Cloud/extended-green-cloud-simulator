package com.gui.domain.types;

import static com.gui.utils.GUIUtils.formatToHTML;

/**
 * Enum storing all available summary panel label types
 */
public enum SummaryPanelLabelEnum implements LabelEnum {

    CLIENT_COUNT_LABEL("NUMBER OF CLIENTS:"),
    ACTIVE_JOBS_LABEL("NUMBER OF CURRENTLY RUNNING JOBS:"),
    ALL_JOBS_COUNT("NUMBER OF ALL PLANNED JOBS:");

    private final String label;

    SummaryPanelLabelEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return formatToHTML(label);
    }
}
