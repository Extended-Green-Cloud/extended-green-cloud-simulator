package com.gui.gui.utils;

import static com.gui.gui.utils.domain.GUIStyleConstants.GRAY_2_COLOR;
import static com.gui.gui.utils.domain.GUIStyleConstants.GRAY_3_COLOR;
import static com.gui.gui.utils.domain.GUIStyleConstants.GRAY_4_COLOR;
import static com.gui.gui.utils.domain.GUIStyleConstants.GRAY_5_COLOR;
import static com.gui.gui.utils.domain.GUIStyleConstants.LABEL_FONT;
import static com.gui.gui.utils.domain.GUIStyleConstants.SECOND_HEADER_FONT;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import com.gui.agents.AbstractAgentNode;
import com.gui.gui.utils.domain.customrenderer.ComboBoxRenderer;
import com.gui.gui.utils.domain.customui.ComboBoxUI;
import com.gui.gui.utils.domain.customui.ScrollBarUI;

/**
 * Class provides set of utilities connected with styling and creating custom GUI components
 */
public class GUIComponentUtils {

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
	 * Method adds the customized styling to scroll bars
	 *
	 * @param scrollBar scroll bar to style
	 */
	public static void customizeScrollBar(final JScrollBar scrollBar) {
		scrollBar.setUI(new ScrollBarUI());
		scrollBar.setBackground(Color.WHITE);
	}

	/**
	 * Method creates the comboBox and initialize it with given values
	 *
	 * @param comboBoxValues values of the dropdown
	 * @param actionListener action computed on value selection
	 * @return JComboBox
	 */
	public static JComboBox createDefaultComboBox(final String[] comboBoxValues, final ActionListener actionListener) {
		final JComboBox jComboBox = new JComboBox(new DefaultComboBoxModel(comboBoxValues));
		jComboBox.setRenderer(new ComboBoxRenderer());
		jComboBox.setUI(ComboBoxUI.createUI());
		final Border comboBoxBorder = BorderFactory.createLineBorder(GRAY_5_COLOR, 2);
		jComboBox.setBorder(comboBoxBorder);
		jComboBox.setForeground(GRAY_5_COLOR);
		jComboBox.addActionListener(actionListener);
		return jComboBox;
	}

	/**
	 * Method creates the comboBox and initialize it with given agent nodes
	 *
	 * @param agents         com.greencloud.application.agents to be put in the combobox
	 * @param actionListener action computed on value selection
	 * @param type           type of com.greencloud.application.agents to select from
	 * @return JComboBox
	 */
	public static JComboBox createDefaultAgentComboBox(final List<AbstractAgentNode> agents,
			final ActionListener actionListener, final String type) {
		final JComboBox jComboBox = createDefaultComboBox(new String[] {}, actionListener);
		createDefaultAgentComboBoxModel(agents, jComboBox,type);
		return jComboBox;
	}

	/**
	 * Method creates and assigns to the combobox a default model computed based on a given agent list
	 *
	 * @param agents   com.greencloud.application.agents to be included in the model
	 * @param comboBox combo box for which the model is assigned
	 * @param type     type of com.greencloud.application.agents to select from
	 */
	public static void createDefaultAgentComboBoxModel(final List<AbstractAgentNode> agents, final JComboBox comboBox,
			final String type) {
		final List<String> agentNames = new java.util.ArrayList<>(
				agents.stream().map(AbstractAgentNode::getAgentName).toList());
		agentNames.add(0, String.format("Please select the %s", type));
		comboBox.setModel(new DefaultComboBoxModel(agentNames.toArray(new String[0])));
	}

	/**
	 * Method creates the numeric input field
	 *
	 * @param title input field title
	 * @return numeric input field
	 */
	public static JFormattedTextField createNumericTextField(final String title) {
		final JFormattedTextField jFormattedTextField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		jFormattedTextField.setForeground(GRAY_5_COLOR);
		jFormattedTextField.setFont(LABEL_FONT);
		jFormattedTextField.setText("0");
		jFormattedTextField.setMargin(new Insets(0, 10, 0, 10));
		final Border border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(GRAY_4_COLOR, 2), title,
				TitledBorder.LEFT, TitledBorder.TOP, LABEL_FONT);
		jFormattedTextField.setBorder(border);
		return jFormattedTextField;
	}

	/**
	 * Method creates a default button
	 *
	 * @param title          button text
	 * @param actionListener button action listeners
	 * @return JButton
	 */
	public static JButton createButton(final String title, final ActionListener actionListener) {
		final JButton jButton = new JButton();
		jButton.addActionListener(actionListener);
		addStyleToButton(jButton, title, false);
		return jButton;
	}

	/**
	 * Method styles the button
	 *
	 * @param jButton     button to style
	 * @param disableText text to be placed on the button
	 * @param isDisabled  flag indicating if the button is disabled
	 */
	public static void addStyleToButton(final JButton jButton, final String disableText, final boolean isDisabled) {
		final Color mainColor = isDisabled ? GRAY_3_COLOR : GRAY_5_COLOR;
		final Color onPressColor = isDisabled ? GRAY_3_COLOR : GRAY_2_COLOR;
		jButton.setBorder(BorderFactory.createLineBorder(mainColor));
		jButton.setFont(SECOND_HEADER_FONT);
		jButton.setBackground(mainColor);
		jButton.setForeground(GRAY_2_COLOR);
		jButton.setText(disableText);
		jButton.setUI(new BasicButtonUI() {
			@Override
			protected void paintButtonPressed(Graphics g, AbstractButton b) {
				if (b.isContentAreaFilled()) {
					Dimension size = b.getSize();
					g.setColor(onPressColor);
					g.fillRect(0, 0, size.width, size.height);
				}
			}
		});
	}
}
