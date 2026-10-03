package com.swingcraft4j.datetime;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A range of dates, from the first to the last date, both are in the range.
 */
public final class DateRange {

    private final LocalDate from;
    private final LocalDate to;

    /**
     * @param from the first date
     * @param to   the last date. If it is before the first date, the two are swapped
     */
    public DateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("date must not null");
        }
        boolean swap = to.isBefore(from);
        this.from = swap ? to : from;
        this.to = swap ? from : to;
    }

    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getTo() {
        return to;
    }

    /**
     * @return true if the date is in the range, the first and the last date too
     */
    public boolean contains(LocalDate date) {
        return date != null && !date.isBefore(from) && !date.isAfter(to);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DateRange)) {
            return false;
        }
        DateRange range = (DateRange) object;
        return from.equals(range.from) && to.equals(range.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return from + " - " + to;
    }
}
