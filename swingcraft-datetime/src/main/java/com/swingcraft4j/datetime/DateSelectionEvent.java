package com.swingcraft4j.datetime;

import java.time.LocalDate;
import java.util.EventObject;

/**
 * The selection of a date picker has changed.
 */
public class DateSelectionEvent extends EventObject {

    private final LocalDate date;
    private final DateRange dateRange;

    public DateSelectionEvent(DatePicker source, LocalDate date, DateRange dateRange) {
        super(source);
        this.date = date;
        this.dateRange = dateRange;
    }

    public DatePicker getDatePicker() {
        return (DatePicker) getSource();
    }

    /**
     * @return the selected date, the first date of a range, or null if the selection was cleared
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * @return the selected range, or null if the picker does not select a range or the selection was cleared
     */
    public DateRange getDateRange() {
        return dateRange;
    }
}
