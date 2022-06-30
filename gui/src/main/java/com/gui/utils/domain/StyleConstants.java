package com.gui.utils.domain;

import java.awt.*;

/**
 * Class stores constants used in styling swing objects
 */
public class StyleConstants {

    /**
     * Frame constants
     */
    public static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    public static final double GUI_FRAME_SIZE_WIDTH = 0.8;
    public static final double GUI_FRAME_SIZE_HEIGHT = 0.85;
    public static final int TITLE_FONT_SIZE_PERCENT = 70;
    public static final int SCROLL_BAR_WIDTH = 10;

    /**
     * Color palette constants
     */
    public static final Color WHITE_COLOR = new Color(255, 255, 255);
    public static final Color TITLE_BACKGROUND_COLOR = new Color(108, 189, 58);
    public static final Color DARK_GREEN_COLOR = new Color(78, 148, 24);
    public static final Color SCROLL_THUMB_COLOR = new Color(204, 204, 204);
    public static final Color VERY_LIGHT_GRAY_COLOR = new Color(245, 245, 245);
    public static final Color LIGHT_GRAY_COLOR = new Color(204, 203, 203);
    public static final Color MEDIUM_GRAY_COLOR = new Color(101, 101, 101);
    public static final Color DARK_GRAY_COLOR = new Color(95, 95, 95);


    /**
     * Font constants
     */
    public static final String FONT_FAMILY = "Dialog";
    public static final Font TITLE_FONT = new Font(FONT_FAMILY, Font.BOLD, (int) SCREEN_SIZE.getWidth() / TITLE_FONT_SIZE_PERCENT);
    public static final Font FIRST_HEADER_FONT = new Font(FONT_FAMILY, Font.BOLD, (int) SCREEN_SIZE.getWidth() / (TITLE_FONT_SIZE_PERCENT + 35));
    public static final Font LIST_VALUE_FONT = new Font(FONT_FAMILY, Font.BOLD, (int) SCREEN_SIZE.getWidth() / (TITLE_FONT_SIZE_PERCENT + 55));
    public static final Font LIST_LABEL_FONT = new Font(FONT_FAMILY, Font.BOLD, (int) SCREEN_SIZE.getWidth() / (TITLE_FONT_SIZE_PERCENT + 60));
    public static final Font PARAGRAPH_FONT = new Font(FONT_FAMILY, Font.PLAIN, (int) SCREEN_SIZE.getWidth() / (TITLE_FONT_SIZE_PERCENT + 80));

    /**
     * CSS class style constants
     */
    public static final String STYLE_FILE = "url(graphStyle.css)";
    public static final String EDGE_MESSAGE_STYLE = "messageFlow";
    public static final String EDGE_HIDDEN_MESSAGE_STYLE = "messageFlowHidden";
    public static final String EDGE_INACTIVE_STYLE = "inactive";
    public static final String EDGE_ACTIVE_STYLE = "active";
    public static final String LABEL_STYLE = "labelStyle";
    public static final String CLIENT_STYLE = "client";
    public static final String CLOUD_NETWORK_STYLE = "cloudNetwork";
    public static final String CLOUD_NETWORK_INACTIVE_STYLE = "cloudNetworkInactive";
    public static final String CLOUD_NETWORK_LOW_STYLE = "cloudNetworkLow";
    public static final String CLOUD_NETWORK_MEDIUM_STYLE = "cloudNetworkMedium";
    public static final String CLOUD_NETWORK_HIGH_STYLE = "cloudNetworkHigh";
    public static final String GREEN_ENERGY_STYLE = "greenEnergy";
    public static final String GREEN_ENERGY_INACTIVE_STYLE = "greenEnergyInactive";
    public static final String GREEN_ENERGY_ACTIVE_STYLE = "greenEnergyActive";
    public static final String MONITORING_STYLE = "monitoring";
    public static final String SERVER_STYLE = "server";
    public static final String SERVER_INACTIVE_STYLE = "serverInactive";
    public static final String SERVER_ACTIVE_STYLE = "serverActive";
}
