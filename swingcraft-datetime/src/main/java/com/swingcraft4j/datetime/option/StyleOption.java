package com.swingcraft4j.datetime.option;

import java.awt.*;

/**
 * How a picker looks: its colors, the shape of its selection and its space. The setters return the option,
 * so they can be chained. Get it from {@link DateOption#getStyleOption()} or
 * {@link TimeOption#getStyleOption()}, the same style can be given to both. A picker does not use the
 * options that are for the other picker only.
 * <p>
 * All sizes are unscaled and get scaled with the UI scale factor.
 */
public class StyleOption {

    // colors
    private Color color;
    private Color background;
    private Color clockBackground;
    private Color weekendColor;
    // shape and space
    private int selectionRound = 999;
    private Insets padding = new Insets(10, 10, 10, 10);
    // calendar
    private boolean showOutsideDays = true;
    private boolean showToday = true;

    public Color getColor() {
        return color;
    }

    public Color getBackground() {
        return background;
    }

    public Color getClockBackground() {
        return clockBackground;
    }

    public Color getWeekendColor() {
        return weekendColor;
    }

    public int getSelectionRound() {
        return selectionRound;
    }

    public Insets getPadding() {
        return (Insets) padding.clone();
    }

    public boolean isShowOutsideDays() {
        return showOutsideDays;
    }

    public boolean isShowToday() {
        return showToday;
    }

    /**
     * @param color the color of what is selected: the selected day and the band of a range, the outline of
     *              today, the hand of the clock and the selected hour and minute. Null (default) to use the
     *              accent color of the look and feel
     */
    public StyleOption setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * @param background the background of the picker, or null (default) to use the one of the look and feel
     */
    public StyleOption setBackground(Color background) {
        this.background = background;
        return this;
    }

    /**
     * @param clockBackground the color of the face of the clock of a time picker, or null (default) for
     *                        a shade of the background
     */
    public StyleOption setClockBackground(Color clockBackground) {
        this.clockBackground = clockBackground;
        return this;
    }

    /**
     * @param weekendColor the color of the Saturdays and the Sundays of a date picker and of their names,
     *                     or null (default) to show them as the other days
     */
    public StyleOption setWeekendColor(Color weekendColor) {
        this.weekendColor = weekendColor;
        return this;
    }

    /**
     * @param selectionRound the corner arc diameter of the selected day, month and year of a date picker:
     *                       0 for square corners, a large value (default 999) for a circle
     */
    public StyleOption setSelectionRound(int selectionRound) {
        if (selectionRound < 0) {
            throw new IllegalArgumentException("selection round must be >= 0");
        }
        this.selectionRound = selectionRound;
        return this;
    }

    public StyleOption setPadding(int padding) {
        return setPadding(new Insets(padding, padding, padding, padding));
    }

    /**
     * @param padding the space between the edge of the picker and its content, 10 by default
     */
    public StyleOption setPadding(Insets padding) {
        if (padding.top < 0 || padding.left < 0 || padding.bottom < 0 || padding.right < 0) {
            throw new IllegalArgumentException("padding must be >= 0");
        }
        this.padding = (Insets) padding.clone();
        return this;
    }

    /**
     * @param showOutsideDays false to leave the days of the months before and after out of the calendar of
     *                        a date picker, their places stay empty (default true)
     */
    public StyleOption setShowOutsideDays(boolean showOutsideDays) {
        this.showOutsideDays = showOutsideDays;
        return this;
    }

    /**
     * @param showToday false to show today, this month and this year as all the others in a date picker,
     *                  without the outline (default true)
     */
    public StyleOption setShowToday(boolean showToday) {
        this.showToday = showToday;
        return this;
    }

    public StyleOption copy() {
        StyleOption option = new StyleOption();
        option.color = color;
        option.background = background;
        option.clockBackground = clockBackground;
        option.weekendColor = weekendColor;
        option.selectionRound = selectionRound;
        option.padding = (Insets) padding.clone();
        option.showOutsideDays = showOutsideDays;
        option.showToday = showToday;
        return option;
    }
}
