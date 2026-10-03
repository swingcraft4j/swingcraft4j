package com.swingcraft4j.datetime.internal;

import java.awt.*;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;

/**
 * The twelve months of a year.
 */
final class MonthGrid extends ValueGrid {

    private final CalendarPanel calendar;
    private final int year;

    MonthGrid(CalendarPanel calendar, int year) {
        super(3, 4, new Dimension(80, 50), calendar.getOption().getSize());
        this.calendar = calendar;
        this.year = year;
    }

    private YearMonth getMonth(int cell) {
        return YearMonth.of(year, cell + 1);
    }

    @Override
    String getText(int cell) {
        return Month.of(cell + 1).getDisplayName(TextStyle.SHORT, calendar.getOption().getLocale());
    }

    @Override
    boolean isSelected(int cell) {
        return calendar.isSelected(getMonth(cell));
    }

    @Override
    boolean isCurrent(int cell) {
        return getMonth(cell).equals(YearMonth.now());
    }

    @Override
    void cellClicked(int cell) {
        calendar.monthClicked(getMonth(cell));
    }
}
