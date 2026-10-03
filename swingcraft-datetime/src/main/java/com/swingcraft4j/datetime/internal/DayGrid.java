package com.swingcraft4j.datetime.internal;

import com.formdev.flatlaf.util.ColorFunctions;
import com.swingcraft4j.datetime.DateRange;
import com.swingcraft4j.datetime.option.PickerSize;

import java.awt.*;
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
    // the space around the shape of a selected day
    private static final float CELL_PADDING = 2;
    // the line around today
    private static final float OUTLINE_WIDTH = 1.3f;
    // from the edge of the shape to the line when today is selected
    private static final float OUTLINE_GAP = 2.2f;
    private static final Dimension CELL_SIZE = new Dimension(38, 34);

    private final CalendarPanel calendar;
    private final YearMonth month;
    private final LocalDate firstDate;
    private final DayOfWeek firstDay;
    private final String[] dayNames = new String[DAYS];
    // if each day can be selected, found when it is asked for the first time: the option can ask
    // a validator, and the grid asks for each day each time it is painted and the mouse moves
    private final Boolean[] selectable = new Boolean[DAYS * WEEKS];
    // today, the same for all the days of one paint
    private LocalDate today = LocalDate.now();

    DayGrid(CalendarPanel calendar, YearMonth month) {
        super(DAYS, WEEKS + 1, CELL_SIZE, calendar.getOption().getSize(), calendar.getOption().getStyleOption());
        this.calendar = calendar;
        this.month = month;
        firstDay = calendar.getOption().getFirstDayOfWeek();
        LocalDate first = month.atDay(1);
        firstDate = first.minusDays((first.getDayOfWeek().getValue() - firstDay.getValue() + DAYS) % DAYS);
        for (int i = 0; i < DAYS; i++) {
            dayNames[i] = firstDay.plus(i).getDisplayName(TextStyle.SHORT, calendar.getOption().getLocale());
        }
    }

    /**
     * @return the preferred size of the days of a month, the same for every month
     */
    static Dimension getPreferredSize(PickerSize size) {
        return getPreferredSize(DAYS, WEEKS + 1, CELL_SIZE, size);
    }

    private LocalDate getDate(int cell) {
        return firstDate.plusDays(cell - DAYS);
    }

    private boolean isSelectable(int cell) {
        int day = cell - DAYS;
        if (selectable[day] == null) {
            selectable[day] = calendar.getOption().isSelectable(getDate(cell));
        }
        return selectable[day];
    }

    @Override
    protected void paintComponent(Graphics g) {
        today = LocalDate.now();
        super.paintComponent(g);
    }

    // false for a day of the month before or after, if the style leaves them out
    private boolean isShown(LocalDate date) {
        return getStyle().isShowOutsideDays() || YearMonth.from(date).equals(month);
    }

    private static boolean isWeekend(DayOfWeek day) {
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    @Override
    boolean isCellEnabled(int cell) {
        // the first row has the names of the days
        if (cell < DAYS) {
            return false;
        }
        return isShown(getDate(cell)) && isSelectable(cell);
    }

    @Override
    void cellClicked(int cell) {
        calendar.cellClicked();
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
        Color weekendColor = getStyle().getWeekendColor();
        if (cell < DAYS) {
            boolean weekend = weekendColor != null && isEnabled() && isWeekend(firstDay.plus(cell));
            g.setColor(weekend ? weekendColor : PickerUtils.disabledForeground());
            PickerUtils.paintText(g, dayNames[cell], bounds);
            return;
        }
        LocalDate date = getDate(cell);
        if (!isShown(date)) {
            return;
        }
        DateSelection selection = calendar.getSelection();
        boolean selected = selection.isSelectedDate(date);
        RoundRectangle2D.Float shape = getSelectionShape(bounds);
        Color background = selected ? getSelectedBackground(hover, pressed) : getCellBackground(hover, pressed);
        if (background != null) {
            g.setColor(background);
            g.fill(shape);
        }
        if (getStyle().isShowToday() && date.equals(today)) {
            PickerUtils.paintCurrentMark(g, shape, selected, scale(OUTLINE_WIDTH), scale(OUTLINE_GAP), getAccentColor());
        }
        if (calendar.isCursorShown() && date.equals(calendar.getCursorDate())) {
            PickerUtils.paintCursor(g, shape, scale(1));
        }
        if (selected) {
            g.setColor(PickerUtils.accentForeground(getAccentColor()));
        } else if (!isEnabled() || !YearMonth.from(date).equals(month) || !isSelectable(cell)) {
            g.setColor(PickerUtils.disabledForeground());
        } else if (weekendColor != null && isWeekend(date.getDayOfWeek())) {
            g.setColor(weekendColor);
        } else {
            g.setColor(PickerUtils.foreground());
        }
        PickerUtils.paintText(g, String.valueOf(date.getDayOfMonth()), bounds);
    }

    // the shape of a selected day: a square in the middle of the cell, with the round corners of the style
    private RoundRectangle2D.Float getSelectionShape(Rectangle2D.Float bounds) {
        float size = Math.min(bounds.width, bounds.height) - scale(CELL_PADDING) * 2;
        float arc = getSelectionArc(size);
        return new RoundRectangle2D.Float((float) bounds.getCenterX() - size / 2, (float) bounds.getCenterY() - size / 2, size, size, arc, arc);
    }

    // the band behind the days of a range: one for each week, with the shape of a selected day at its two
    // ends. A band is one shape, the bands of single cells would show a line between them on a scaled screen
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
                : ColorFunctions.mix(getAccentColor(), background, 0.2f));
        for (int week = 1; week <= WEEKS; week++) {
            int first = -1;
            int last = -1;
            for (int cell = week * DAYS; cell < (week + 1) * DAYS; cell++) {
                LocalDate date = getDate(cell);
                if (range.contains(date) && isShown(date)) {
                    first = first == -1 ? cell : first;
                    last = cell;
                }
            }
            if (first != -1) {
                RoundRectangle2D.Float start = getSelectionShape(getCellBounds(first));
                Rectangle2D band = start.getBounds2D().createUnion(getSelectionShape(getCellBounds(last)).getBounds2D());
                g.fill(new RoundRectangle2D.Double(band.getX(), band.getY(), band.getWidth(), band.getHeight(),
                        start.arcwidth, start.archeight));
            }
        }
    }
}
