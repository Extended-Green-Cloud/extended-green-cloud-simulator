package com.gui.utils.domain;

import static com.gui.utils.domain.StyleConstants.*;

import javax.swing.*;
import java.awt.*;

public class ComboBoxRenderer extends DefaultListCellRenderer {

    private static final int MARGIN = 3;

    public ComboBoxRenderer() {
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        c.setBackground(WHITE_COLOR);
        c.setForeground(DARK_GRAY_COLOR);
        c.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
        c.setFont(LIST_VALUE_FONT);
        return c;
    }

    @Override
    public Color getBackground() {
        return WHITE_COLOR;
    }

}
