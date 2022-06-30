package com.gui.domain.guielements;

import static com.gui.domain.types.SummaryPanelLabelEnum.*;
import static com.gui.utils.GUIUtils.*;

import com.gui.domain.types.LabelEnum;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class represents the summary panel of the GUI
 * <p>
 * {@value TITLE_LABEL}         title of the summary panel
 */
public class SummaryPanel {

    private static final String TITLE_LABEL = "NETWORK STATISTICS";

    private final JPanel mainPanel;
    private final Map<LabelEnum, JLabel> labelMap;

    private int clientsCount;
    private int activeJobsCount;
    private int allJobsCount;


    /**
     * Default constructor
     */
    public SummaryPanel() {
        this.clientsCount = 0;
        this.activeJobsCount = 0;
        this.allJobsCount = 0;
        this.labelMap = initializeLabels();
        this.mainPanel = initializeSummaryPanel();
    }

    /**
     * Function updates the clients number by given value
     *
     * @param value value to be added to the clients number
     */
    public synchronized void updateClientsCountByValue(final int value) {
        clientsCount += value;
        labelMap.get(CLIENT_COUNT_LABEL).setText(formatToHTML(formatToHTML(String.valueOf(clientsCount))));
    }

    /**
     * Function updates the active jobs number by given value
     *
     * @param value value to be added to the active jobs number
     */
    public synchronized void updateActiveJobsCountByValue(final int value) {
        activeJobsCount += value;
        labelMap.get(ACTIVE_JOBS_LABEL).setText(formatToHTML(String.valueOf(activeJobsCount)));
    }

    /**
     * Function updates the all jobs number by given value
     *
     * @param value value to be added to the all jobs number
     */
    public synchronized void updateAllJobsCountByValue(final int value) {
        allJobsCount += value;
        labelMap.get(ALL_JOBS_COUNT).setText(formatToHTML(String.valueOf(allJobsCount)));
    }

    /**
     * @return summary panel
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JPanel initializeSummaryPanel() {
        final MigLayout panelLayout = new MigLayout(new LC().fillX().gridGapX("15px"));
        final JPanel summaryPanel = createBorderPanel(panelLayout);
        addPanelHeader(TITLE_LABEL, summaryPanel);
        summaryPanel.add(createLabelListPanel(labelMap), new CC().grow());
        return summaryPanel;
    }

    private Map<LabelEnum, JLabel> initializeLabels() {
        final Map<LabelEnum, JLabel> jLabelMap = new LinkedHashMap<>();
        jLabelMap.put(CLIENT_COUNT_LABEL, createListLabel(String.valueOf(clientsCount)));
        jLabelMap.put(ACTIVE_JOBS_LABEL, createListLabel(String.valueOf(activeJobsCount)));
        jLabelMap.put(ALL_JOBS_COUNT, createListLabel(String.valueOf(allJobsCount)));
        return jLabelMap;
    }
}
