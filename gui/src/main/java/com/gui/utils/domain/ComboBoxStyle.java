package com.gui.utils.domain;

import static com.gui.utils.GUIUtils.customizeScrollBar;
import static com.gui.utils.domain.StyleConstants.*;

import javax.swing.*;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

public class ComboBoxStyle extends BasicComboBoxUI {
    public ComboBoxStyle() {
        currentValuePane.setBackground(WHITE_COLOR);
    }

    public static ComboBoxUI createUI(JComponent c) {
        return new ComboBoxStyle();
    }

    @Override
    protected JButton createArrowButton() {
        final JButton button = new BasicArrowButton(BasicArrowButton.SOUTH, DARK_GRAY_COLOR, DARK_GRAY_COLOR, WHITE_COLOR, DARK_GRAY_COLOR);
        button.setBorder(BorderFactory.createLineBorder(DARK_GRAY_COLOR, 2));
        return button;
    }

    @Override
    protected ComboPopup createPopup() {
        return new BasicComboPopup(comboBox) {
            @Override
            protected JScrollPane createScroller() {
                final JScrollPane jScrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                customizeScrollBar(jScrollPane.getVerticalScrollBar());
                return jScrollPane;
            }
        };
    }
}