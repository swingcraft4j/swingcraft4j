package com.swingcraft4j.datetime.internal;

import java.awt.*;
import java.time.Year;

/**
 * A page of years. The pages do not move: a year is always on the same page at the same place.
 */
final class YearGrid extends ValueGrid {

    static final int COLUMNS = 4;
    private static final int ROWS = 5;
    /**
     * How many years a page has.
     */
    static final int YEARS = COLUMNS * ROWS;

    private final CalendarPanel calendar;
    private final int firstYear;

    /**
     * @param year a year of the page
     */
    YearGrid(CalendarPanel calendar, int year) {
        super(COLUMNS, ROWS, new Dimension(60, 40), calendar.getOption().getSize(), calendar.getOption().getStyleOption());
        this.calendar = calendar;
        this.firstYear = getFirstYear(year);
    }

    /**
     * @return the first year of the page the year is on
     */
    static int getFirstYear(int year) {
        return year - Math.floorMod(year, YEARS);
    }

    int getFirstYear() {
        return firstYear;
    }

    @Override
    String getText(int cell) {
        return String.valueOf(firstYear + cell);
    }

    @Override
    boolean isSelected(int cell) {
        return calendar.isSelected(firstYear + cell);
    }

    @Override
    boolean isCurrent(int cell) {
        return firstYear + cell == Year.now().getValue();
    }

    @Override
    boolean isCursor(int cell) {
        return calendar.isCursorShown() && firstYear + cell == calendar.getCursorYear();
    }

    @Override
    void cellClicked(int cell) {
        calendar.cellClicked();
        calendar.yearClicked(firstYear + cell);
    }
}
