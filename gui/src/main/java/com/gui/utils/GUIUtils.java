package com.gui.utils;

import static com.gui.utils.domain.StyleConstants.*;
import static javax.swing.BorderFactory.createTitledBorder;

import com.gui.domain.types.LabelEnum;
import com.gui.utils.domain.ComboBoxRenderer;
import com.gui.utils.domain.ComboBoxStyle;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.border.DropShadowBorder;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Class provides set of utilities connected with GUI objects' styling or creation
 */
public class GUIUtils {

    /**
     * Method concatenates multiple CSS classes
     *
     * @param style list of css classes
     * @return concatenated classes
     */
    public static String concatenateStyles(final List<String> style) {
        return String.join(", ", style);
    }

    /**
     * Method formats the input string into a string enclosed inside html tags
     *
     * @param string string to be formatted
     * @return string enclosed by html tags
     */
    public static String formatToHTML(final String string) {
        return String.format("<html><span>%s</span></html>", string);
    }

    /**
     * Method creates the horizontal separator
     *
     * @param color color of the separator
     * @return separator being JSeparator object
     */
    public static JSeparator createSeparator(final Color color) {
        final JSeparator separator = new JSeparator();
        separator.setBackground(color);
        separator.setForeground(color);
        return separator;
    }

    /**
     * Method creates the shadow to be added to some JPanel
     *
     * @return border being a shadow
     */
    public static Border createCardShadow() {
        final DropShadowBorder shadow = new DropShadowBorder();
        shadow.setShadowColor(Color.GRAY);
        shadow.setShowLeftShadow(true);
        shadow.setShowRightShadow(true);
        shadow.setShowBottomShadow(true);
        shadow.setShowTopShadow(true);
        return shadow;
    }

    /**
     * Method creates JLabel with given font style and foreground color
     *
     * @param font       font style for JLabel
     * @param foreground font color for JLabel
     * @param text       JLabel text
     * @return styled JLabel
     */
    public static JLabel createJLabel(final Font font, final Color foreground, final String text) {
        final JLabel jLabel = new JLabel(formatToHTML(text));
        jLabel.setFont(font);
        jLabel.setForeground(foreground);
        return jLabel;
    }

    /**
     * Method creates list element which is JLabel
     *
     * @param text JLabel text
     * @return list element JLabel
     */
    public static JLabel createListLabel(final String text) {
        return createJLabel(LIST_VALUE_FONT, WHITE_COLOR, formatToHTML(text));
    }

    /**
     * Method creates a paragraph which is a JLabel
     *
     * @param text JLabel text
     * @return JLabel being a paragraph
     */
    public static JLabel createParagraph(final String text) {
        return createJLabel(PARAGRAPH_FONT, DARK_GRAY_COLOR, formatToHTML(text));
    }

    /**
     * Method creates title label for main frame
     *
     * @param title text of the label
     * @return JLabel being title label
     */
    public static JLabel createTitleLabel(final String title) {
        final JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(WHITE_COLOR);
        titleLabel.setBackground(TITLE_BACKGROUND_COLOR);
        titleLabel.setOpaque(true);
        titleLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return titleLabel;
    }

    /**
     * Method adds the header for the panel
     *
     * @param text  header text
     * @param panel panel to which the header is to be added
     * @return JLabel being the header
     */
    public static void addPanelHeader(final String text, final JPanel panel) {
        panel.add(createJLabel(FIRST_HEADER_FONT, MEDIUM_GRAY_COLOR, text), new CC().spanX().gapY("5px", "5px"));
    }

    /**
     * Method adds the customized styling to scroll bars
     *
     * @param scrollBar scroll bar to style
     */
    public static void customizeScrollBar(final JScrollBar scrollBar) {
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = SCROLL_THUMB_COLOR;
                this.trackColor = VERY_LIGHT_GRAY_COLOR;
                this.scrollBarWidth = SCROLL_BAR_WIDTH;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton jbutton = new JButton();
                jbutton.setPreferredSize(new Dimension(0, 0));
                jbutton.setMinimumSize(new Dimension(0, 0));
                jbutton.setMaximumSize(new Dimension(0, 0));
                return jbutton;
            }

        });
        scrollBar.setBackground(Color.WHITE);
    }

    /**
     * Method creates panel with shadow
     *
     * @param layout    layout of the panel
     * @return styled JPanel
     */
    public static JPanel createShadowPanel(final LayoutManager layout) {
        final JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(layout);
        panel.setBorder(createCardShadow());
        return panel;
    }

    /**
     * Method creates panel with top and bottom border
     *
     * @param layout    layout of the panel
     * @return styled JPanel
     */
    public static JPanel createBorderPanel(final LayoutManager layout) {
        final JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(layout);
        panel.setBorder(new MatteBorder(5, 0, 5, 0, DARK_GREEN_COLOR));
        return panel;
    }

    /**
     * Method creates the panel displaying a list of labels together with their descriptions
     *
     * @param labelMap map containing set of values as JLabels together with their String descriptions
     * @return JPanel being the list panel
     */
    public static JPanel createLabelListPanel(final Map<LabelEnum, JLabel> labelMap) {
        final MigLayout layout = new MigLayout(new LC().fillX().gridGapX("15px"));
        final JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(layout);
        labelMap.forEach((labelType, labelValue) -> {
            final JPanel labelContainer = new JPanel(new MigLayout(new LC().wrapAfter(2).fill()));
            labelContainer.setBackground(MEDIUM_GRAY_COLOR);
            final JLabel headerLabel = createJLabel(LIST_LABEL_FONT, LIGHT_GRAY_COLOR, labelType.getLabel());
            headerLabel.setBorder(new EmptyBorder(1, 1, 1, 1));
            labelContainer.add(headerLabel, new CC().width("75%").grow());
            labelContainer.add(labelValue, new CC().width("25%").grow());
            panel.add(labelContainer, new CC().grow().spanX());
        });
        return panel;
    }

    /**
     * Method creates the comboBox and initialize it with given values
     *
     * @param comboBoxValues values of the dropdown
     * @return JComboBox
     */
    public static JComboBox createDefaultComboBox(final String[] comboBoxValues) {
        final JComboBox jComboBox = new JComboBox(new DefaultComboBoxModel(comboBoxValues));
        jComboBox.setRenderer(new ComboBoxRenderer());
        jComboBox.setUI(ComboBoxStyle.createUI(jComboBox));
        final Border comboBoxBorder = BorderFactory.createLineBorder(DARK_GRAY_COLOR, 2);
        jComboBox.setBorder(comboBoxBorder);
        jComboBox.setForeground(DARK_GRAY_COLOR);
        return jComboBox;
    }

    /**
     * Method creates the scroll pane
     *
     * @param panel panel inside scroll pane
     * @return JScrollPane
     */
    public static JScrollPane createDefaultScrollPane(final JPanel panel) {
        final JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        customizeScrollBar(scrollPane.getVerticalScrollBar());
        customizeScrollBar(scrollPane.getHorizontalScrollBar());
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        return scrollPane;
    }
}
