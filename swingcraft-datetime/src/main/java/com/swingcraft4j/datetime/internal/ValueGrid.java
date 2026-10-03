package com.swingcraft4j.datetime.internal;

import com.swingcraft4j.datetime.option.PickerSize;
import com.swingcraft4j.datetime.option.StyleOption;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * A grid of values with a text each, as the months of a year and the years. A selected value has a
 * round background.
 */
abstract class ValueGrid extends CellGrid {

    // the height of the round background, and the space at its sides
    private static final float SELECTION_HEIGHT = 30;
    private static final float SELECTION_GAP = 4;

    ValueGrid(int columns, int rows, Dimension cellSize, PickerSize size, StyleOption style) {
        super(columns, rows, cellSize, size, style);
    }

    abstract String getText(int cell);

    abstract boolean isSelected(int cell);

    /**
     * @return true for the value of today: this month or this year. It has a line around it
     */
    abstract boolean isCurrent(int cell);

    /**
     * @return true if the keyboard is at the value
     */
    abstract boolean isCursor(int cell);

    @Override
    void paintCell(Graphics2D g, int cell, Rectangle2D.Float bounds, boolean hover, boolean pressed) {
        boolean selected = isSelected(cell);
        float height = Math.min(scale(SELECTION_HEIGHT), bounds.height);
        float gap = scale(SELECTION_GAP);
        float arc = getSelectionArc(height);
        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(
                bounds.x + gap, (float) bounds.getCenterY() - height / 2, bounds.width - gap * 2, height, arc, arc);
        Color background = selected ? getSelectedBackground(hover, pressed) : getCellBackground(hover, pressed);
        if (background != null) {
            g.setColor(background);
            g.fill(shape);
        }
        if (getStyle().isShowToday() && isCurrent(cell)) {
            PickerUtils.paintCurrentMark(g, shape, selected, scale(1.3f), scale(2.2f), getAccentColor());
        }
        if (isCursor(cell)) {
            PickerUtils.paintCursor(g, shape, scale(1));
        }
        if (selected) {
            g.setColor(PickerUtils.accentForeground(getAccentColor()));
        } else {
            g.setColor(isEnabled() ? PickerUtils.foreground() : PickerUtils.disabledForeground());
        }
        PickerUtils.paintText(g, getText(cell), bounds);
    }
}
