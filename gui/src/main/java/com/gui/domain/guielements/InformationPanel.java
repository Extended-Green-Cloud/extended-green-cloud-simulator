package com.gui.domain.guielements;

import static com.gui.utils.GUIUtils.*;
import static com.gui.utils.domain.StyleConstants.LIGHT_GRAY_COLOR;

import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.math3.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Class represents the information panel of the GUI
 * <p>
 * {@value TITLE_LABEL}  title of the information panel
 */
public class InformationPanel {

    private static final String TITLE_LABEL = "LATEST NEWS";
    private static final CC ROW_ATTRIBUTES = new CC().spanX().growX();

    private final JPanel informationBoxPanel;
    private final JPanel mainPanel;
    private final List<Pair<String, String>> informationList;
    private final JScrollPane informationBoxScroll;

    /**
     * Default constructor
     */
    public InformationPanel() {
        this.informationList = new ArrayList<>();
        this.informationBoxPanel = initializeInformationBoxPanel();
        this.informationBoxScroll = initializeInformationBoxScroll();
        this.mainPanel = initializeMainPanel();
    }

    /**
     * Function adds new information to information box panel
     *
     * @param information information that is to be added
     */
    public synchronized void addNewInformation(final String information) {
        if (informationList.size() >= 50) {
            informationList.remove(0);
        }
        informationList.add(new Pair<>(information, getCurrentTime()));
        createBodySection();
    }

    /**
     * @return information panel
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }


    private JPanel initializeInformationBoxPanel() {
        final JPanel infoBoxPanel = new JPanel();
        final MigLayout informationBoxLayout = new MigLayout(new LC().bottomToTop().fillX().flowY());
        infoBoxPanel.setBackground(Color.WHITE);
        infoBoxPanel.setLayout(informationBoxLayout);
        return infoBoxPanel;
    }

    private JScrollPane initializeInformationBoxScroll() {
        final JScrollPane infoBoxScroll = createDefaultScrollPane(informationBoxPanel);
        infoBoxScroll.getVerticalScrollBar().setValue(infoBoxScroll.getVerticalScrollBar().getMaximum());
        return infoBoxScroll;
    }

    private JPanel initializeMainPanel() {
        final MigLayout panelLayout = new MigLayout(new LC().fillX());
        final JPanel informationMainPanel = createBorderPanel(panelLayout);
        addPanelHeader(TITLE_LABEL, informationMainPanel);
        createBodySection();
        informationMainPanel.add(informationBoxScroll, new CC().height("100%").span().grow().wrap().gapY("5px", "0px"));
        return informationMainPanel;
    }

    private void createBodySection() {
        informationBoxPanel.removeAll();
        final int lastIdx = informationList.size();

        if (!informationList.isEmpty()) {
            IntStream.range(0, lastIdx).forEach(idx -> {
                final Pair<String, String> information = informationList.get(lastIdx - 1 - idx);
                informationBoxPanel.add(createSeparator(LIGHT_GRAY_COLOR), new CC().spanX().growX());
                informationBoxPanel.add(createInformationLabel(Optional.ofNullable(information)), ROW_ATTRIBUTES);
            });
        } else {
            informationBoxPanel.add(createInformationLabel(Optional.empty()), ROW_ATTRIBUTES);
        }
        informationBoxScroll.getVerticalScrollBar().setValue(informationBoxScroll.getVerticalScrollBar().getMaximum());
    }

    private JLabel createInformationLabel(final Optional<Pair<String, String>> information) {
        final String labelText =
                information.map(info -> String.format("[%s]: %s", info.getSecond(), info.getFirst()))
                        .orElse(String.format("[%s]: %s", getCurrentTime(), "There are no latest news"));
        return createParagraph(labelText);
    }

    private String getCurrentTime() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return formatter.format(LocalDateTime.now());
    }
}
