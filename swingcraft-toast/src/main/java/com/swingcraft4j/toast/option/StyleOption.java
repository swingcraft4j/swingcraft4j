package com.swingcraft4j.toast.option;

import javax.swing.*;
import java.awt.*;

/**
 * How a toast looks: border, background, and which parts it shows. The setters return the option,
 * so they can be chained. Get it from {@link ToastOption#getStyleOption()}.
 * All sizes are unscaled and get scaled with the UI scale factor.
 */
public class StyleOption {

    // border
    private BorderType borderType = BorderType.DEFAULT;
    private int lineSize = 3;
    private int round = 10;
    private Insets shadowSize = Shadow.MEDIUM.getSize();
    private Color shadowColor;
    private float shadowOpacity = -1;

    // background
    private BackgroundType backgroundType = BackgroundType.DEFAULT;
    private Color color;

    // content
    private Insets padding = new Insets(5, 5, 5, 5);
    private int maxWidth;
    private boolean showIcon = true;
    private Icon icon;
    private boolean iconSeparateLine;
    private boolean showLabel;
    private String label;
    private boolean showCloseButton = true;
    private boolean paintTextColor;
    private boolean showProgressLine;
    private int progressLineSize = 2;
    private ProgressLinePosition progressLinePosition = ProgressLinePosition.BOTTOM;
    private Color progressLineColor;

    public BorderType getBorderType() {
        return borderType;
    }

    public int getLineSize() {
        return lineSize;
    }

    public int getRound() {
        return round;
    }

    public Insets getShadowSize() {
        return (Insets) shadowSize.clone();
    }

    public Color getShadowColor() {
        return shadowColor;
    }

    public float getShadowOpacity() {
        return shadowOpacity;
    }

    public BackgroundType getBackgroundType() {
        return backgroundType;
    }

    public Color getColor() {
        return color;
    }

    public Insets getPadding() {
        return (Insets) padding.clone();
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public boolean isShowIcon() {
        return showIcon;
    }

    public Icon getIcon() {
        return icon;
    }

    public boolean isIconSeparateLine() {
        return iconSeparateLine;
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public String getLabel() {
        return label;
    }

    public boolean isShowCloseButton() {
        return showCloseButton;
    }

    public boolean isPaintTextColor() {
        return paintTextColor;
    }

    public boolean isShowProgressLine() {
        return showProgressLine;
    }

    public int getProgressLineSize() {
        return progressLineSize;
    }

    public ProgressLinePosition getProgressLinePosition() {
        return progressLinePosition;
    }

    public Color getProgressLineColor() {
        return progressLineColor;
    }

    public StyleOption setBorderType(BorderType borderType) {
        if (borderType == null) {
            throw new IllegalArgumentException("border type must not null");
        }
        this.borderType = borderType;
        return this;
    }

    /**
     * @param lineSize the width of the line of the border type
     */
    public StyleOption setLineSize(int lineSize) {
        if (lineSize < 0) {
            throw new IllegalArgumentException("line size must be >= 0");
        }
        this.lineSize = lineSize;
        return this;
    }

    /**
     * @param round the corner arc diameter, 0 for square corners
     */
    public StyleOption setRound(int round) {
        if (round < 0) {
            throw new IllegalArgumentException("round must be >= 0");
        }
        this.round = round;
        return this;
    }

    public StyleOption setShadow(Shadow shadow) {
        this.shadowSize = shadow.getSize();
        return this;
    }

    public StyleOption setShadowSize(int shadowSize) {
        return setShadowSize(new Insets(shadowSize, shadowSize, shadowSize, shadowSize));
    }

    /**
     * @param shadowSize the space the shadow takes on each side. Different sizes offset the shadow,
     *                   for example a larger bottom moves it down
     */
    public StyleOption setShadowSize(Insets shadowSize) {
        if (shadowSize.top < 0 || shadowSize.left < 0 || shadowSize.bottom < 0 || shadowSize.right < 0) {
            throw new IllegalArgumentException("shadow size must be >= 0");
        }
        this.shadowSize = (Insets) shadowSize.clone();
        return this;
    }

    /**
     * @param shadowColor the shadow color, or null to use the look and feel popup shadow color
     */
    public StyleOption setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
        return this;
    }

    /**
     * @param shadowOpacity the shadow opacity from 0 to 1, or -1 to use the default: stronger with a dark
     *                      look and feel, where a shadow is hard to see
     */
    public StyleOption setShadowOpacity(float shadowOpacity) {
        this.shadowOpacity = shadowOpacity;
        return this;
    }

    public StyleOption setBackgroundType(BackgroundType backgroundType) {
        if (backgroundType == null) {
            throw new IllegalArgumentException("background type must not null");
        }
        this.backgroundType = backgroundType;
        return this;
    }

    /**
     * @param color the color of the toast in place of the color of its type, or null to use the type
     */
    public StyleOption setColor(Color color) {
        this.color = color;
        return this;
    }

    public StyleOption setPadding(int padding) {
        return setPadding(new Insets(padding, padding, padding, padding));
    }

    /**
     * @param padding the space between the edge of the toast and its content, a custom component too
     */
    public StyleOption setPadding(Insets padding) {
        if (padding.top < 0 || padding.left < 0 || padding.bottom < 0 || padding.right < 0) {
            throw new IllegalArgumentException("padding must be >= 0");
        }
        this.padding = (Insets) padding.clone();
        return this;
    }

    /**
     * @param maxWidth the largest width of a toast with a message, a message that is longer goes on at
     *                 the next line. 0 (default) for no limit: the toast is as wide as its message, if the
     *                 window is wide enough. Not used for a {@link ToastLayoutType#BANNER} and for a toast
     *                 with a custom component
     */
    public StyleOption setMaxWidth(int maxWidth) {
        if (maxWidth < 0) {
            throw new IllegalArgumentException("max width must be >= 0");
        }
        this.maxWidth = maxWidth;
        return this;
    }

    public StyleOption setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
        return this;
    }

    /**
     * @param icon the icon in place of the icon of the type, or null to use the type
     */
    public StyleOption setIcon(Icon icon) {
        this.icon = icon;
        return this;
    }

    /**
     * @param iconSeparateLine true to show a line between the icon and the text. A toast without icon
     *                         or without text has no line
     */
    public StyleOption setIconSeparateLine(boolean iconSeparateLine) {
        this.iconSeparateLine = iconSeparateLine;
        return this;
    }

    /**
     * @param showLabel true to show a label above the message, the name of the type by default
     */
    public StyleOption setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
        return this;
    }

    /**
     * @param label the text of the label in place of the name of the type, or null to use the type
     */
    public StyleOption setLabel(String label) {
        this.label = label;
        return this;
    }

    public StyleOption setShowCloseButton(boolean showCloseButton) {
        this.showCloseButton = showCloseButton;
        return this;
    }

    /**
     * @param paintTextColor true to paint the message in the color of the type
     */
    public StyleOption setPaintTextColor(boolean paintTextColor) {
        this.paintTextColor = paintTextColor;
        return this;
    }

    /**
     * @param showProgressLine true to show a line at an edge of a toast that closes by itself: it gets
     *                         shorter as the delay passes. The line is full again while the mouse is over
     *                         the toast, as the delay starts again then
     */
    public StyleOption setShowProgressLine(boolean showProgressLine) {
        this.showProgressLine = showProgressLine;
        return this;
    }

    /**
     * @param progressLineSize the height of the progress line
     */
    public StyleOption setProgressLineSize(int progressLineSize) {
        if (progressLineSize < 0) {
            throw new IllegalArgumentException("progress line size must be >= 0");
        }
        this.progressLineSize = progressLineSize;
        return this;
    }

    /**
     * @param progressLinePosition the edge of the toast the progress line is at, the bottom by default
     */
    public StyleOption setProgressLinePosition(ProgressLinePosition progressLinePosition) {
        if (progressLinePosition == null) {
            throw new IllegalArgumentException("progress line position must not null");
        }
        this.progressLinePosition = progressLinePosition;
        return this;
    }

    /**
     * @param progressLineColor the color of the progress line, or null to use the color of the toast
     */
    public StyleOption setProgressLineColor(Color progressLineColor) {
        this.progressLineColor = progressLineColor;
        return this;
    }

    public StyleOption copy() {
        StyleOption option = new StyleOption();
        option.borderType = borderType;
        option.lineSize = lineSize;
        option.round = round;
        option.shadowSize = (Insets) shadowSize.clone();
        option.shadowColor = shadowColor;
        option.shadowOpacity = shadowOpacity;
        option.backgroundType = backgroundType;
        option.color = color;
        option.padding = (Insets) padding.clone();
        option.maxWidth = maxWidth;
        option.showIcon = showIcon;
        option.icon = icon;
        option.iconSeparateLine = iconSeparateLine;
        option.showLabel = showLabel;
        option.label = label;
        option.showCloseButton = showCloseButton;
        option.paintTextColor = paintTextColor;
        option.showProgressLine = showProgressLine;
        option.progressLineSize = progressLineSize;
        option.progressLinePosition = progressLinePosition;
        option.progressLineColor = progressLineColor;
        return option;
    }
}
