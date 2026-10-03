package com.swingcraft4j.datetime.internal;

import com.formdev.flatlaf.util.ColorFunctions;
import com.swingcraft4j.datetime.DateRange;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;

/**
 * The days of a month: a row with the names of the days, and six weeks. The first and the last week are
 * filled with the days of the months before and after.
 */
final class DayGrid extends CellGrid {

    private static final int DAYS = 7;
    private static final int WEEKS = 6;
    // the space around the circle of a selected day
    private static final float CELL_PADDING = 2;
    // the line around today
    private static final float OUTLINE_WIDTH = 1.3f;
    // from the edge of the circle to the line when today is selected
    private static final float OUTLINE_GAP = 2.2f;

    private final CalendarPanel calendar;
    private final YearMonth month;
    private final LocalDate firstDate;
    private final String[] dayNames = new String[DAYS];

    DayGrid(CalendarPanel calendar, YearMonth month) {
        super(DAYS, WEEKS + 1, new Dimension(38, 34), calendar.getOption().getSize());
        this.calendar = calendar;
        this.month = month;
        DayOfWeek firstDay = calendar.getOption().getFirstDayOfWeek();
        LocalDate first = month.atDay(1);
        firstDate = first.minusDays((first.getDayOfWeek().getValue() - firstDay.getValue() + DAYS) % DAYS);
        for (int i = 0; i < DAYS; i++) {
            dayNames[i] = firstDay.plus(i).getDisplayName(TextStyle.SHORT, calendar.getOption().getLocale());
        }
    }

    YearMonth getMonth() {
        return month;
    }

    private LocalDate getDate(int cell) {
        return firstDate.plusDays(cell - DAYS);
    }

    @Override
    boolean isCellEnabled(int cell) {
        // the first row has the names of the days
        return cell >= DAYS && calendar.getOption().isSelectable(getDate(cell));
    }

    @Override
    void cellClicked(int cell) {
        calendar.dateClicked(getDate(cell));
    }

    @Override
    void cellHovered(int cell) {
        // a range that waits for its end follows the mouse
        if (calendar.getSelection().setHover(getDate(cell))) {
            repaint();
        }
    }

    @Override
    void paintCell(Graphics2D g, int cell, Rectangle2D.Float bounds, boolean hover, boolean pressed) {
        if (cell < DAYS) {
            g.setColor(PickerUtils.disabledForeground());
            PickerUtils.paintText(g, dayNames[cell], bounds);
            return;
        }
        LocalDate date = getDate(cell);
        DateSelection selection = calendar.getSelection();
        boolean selected = selection.isSelectedDate(date);
        Ellipse2D.Float circle = getCircle(bounds);
        Color background = selected ? getSelectedBackground(hover, pressed) : getCellBackground(hover, pressed);
        if (background != null) {
            g.setColor(background);
            g.fill(circle);
        }
        if (date.equals(LocalDate.now())) {
            PickerUtils.paintCurrentMark(g, circle, selected, scale(OUTLINE_WIDTH), scale(OUTLINE_GAP));
        }
        if (selected) {
            g.setColor(PickerUtils.accentForeground());
        } else if (!isEnabled() || !YearMonth.from(date).equals(month) || !calendar.getOption().isSelectable(date)) {
            g.setColor(PickerUtils.disabledForeground());
        } else {
            g.setColor(PickerUtils.foreground());
        }
        PickerUtils.paintText(g, String.valueOf(date.getDayOfMonth()), bounds);
    }

    // the circle of a selected day
    private Ellipse2D.Float getCircle(Rectangle2D.Float bounds) {
        float size = Math.min(bounds.width, bounds.height) - scale(CELL_PADDING) * 2;
        return new Ellipse2D.Float((float) bounds.getCenterX() - size / 2, (float) bounds.getCenterY() - size / 2, size, size);
    }

    // the band behind the days of a range: one for each week, round at its two ends. A band is one shape,
    // the bands of single cells would show a line between them on a scaled screen
    @Override
    void paintBackground(Graphics2D g) {
        DateSelection selection = calendar.getSelection();
        DateRange range = selection.getShownRange();
        if (range == null || range.getFrom().equals(range.getTo())) {
            return;
        }
        Color background = PickerUtils.background(this);
        // a range that waits for its end is shown lighter than a selected one
        g.setColor(selection.isSelecting()
                ? PickerUtils.shade(background, 0.05f)
                : ColorFunctions.mix(PickerUtils.accentColor(), background, 0.2f));
        for (int week = 1; week <= WEEKS; week++) {
            int first = -1;
            int last = -1;
            for (int cell = week * DAYS; cell < (week + 1) * DAYS; cell++) {
                if (range.contains(getDate(cell))) {
                    first = first == -1 ? cell : first;
                    last = cell;
                }
            }
            if (first != -1) {
                Rectangle2D shape = getCircle(getCellBounds(first)).getBounds2D().createUnion(getCircle(getCellBounds(last)).getBounds2D());
                g.fill(new RoundRectangle2D.Double(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight(), shape.getHeight(), shape.getHeight()));
            }
        }
    }
}
