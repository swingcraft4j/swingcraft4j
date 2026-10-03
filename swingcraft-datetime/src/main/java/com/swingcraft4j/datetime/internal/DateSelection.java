package com.swingcraft4j.datetime.internal;

import com.swingcraft4j.datetime.DateRange;
import com.swingcraft4j.datetime.option.DateSelectionMode;

import java.time.LocalDate;

/**
 * The dates that are selected in a date picker: one date, or a range that is selected with two clicks.
 * Between the two clicks the range has a start and no end, and follows the mouse.
 */
public final class DateSelection {

    private DateSelectionMode mode = DateSelectionMode.SINGLE;
    // the date of a single selection, or the first click of a range
    private LocalDate start;
    // the second click of a range, it can be before the start
    private LocalDate end;
    // the date under the mouse while a range has no end
    private LocalDate hover;

    public DateSelectionMode getMode() {
        return mode;
    }

    /**
     * Changes the mode, the selection is cleared if the mode is another one.
     */
    public void setMode(DateSelectionMode mode) {
        if (this.mode != mode) {
            this.mode = mode;
            clear();
        }
    }

    public void clear() {
        start = null;
        end = null;
        hover = null;
    }

    public void setDate(LocalDate date) {
        start = date;
        end = mode == DateSelectionMode.RANGE ? date : null;
        hover = null;
    }

    public void setRange(DateRange range) {
        start = range.getFrom();
        end = range.getTo();
        hover = null;
    }

    /**
     * The user has clicked the date.
     */
    public void click(LocalDate date) {
        if (mode == DateSelectionMode.SINGLE || start == null || end != null) {
            start = date;
            end = null;
            hover = mode == DateSelectionMode.RANGE ? date : null;
        } else {
            end = date;
            hover = null;
        }
    }

    /**
     * @return true if the range that is shown has changed
     */
    public boolean setHover(LocalDate date) {
        if (!isSelecting() || date.equals(hover)) {
            return false;
        }
        hover = date;
        return true;
    }

    /**
     * @return true if a range has a start and waits for its end
     */
    public boolean isSelecting() {
        return mode == DateSelectionMode.RANGE && start != null && end == null;
    }

    /**
     * @return true if a date, or a range with its end, is selected
     */
    public boolean isSelected() {
        return start != null && (mode == DateSelectionMode.SINGLE || end != null);
    }

    /**
     * @return the selected date, the first date of a range, or null if nothing is selected
     */
    public LocalDate getDate() {
        if (!isSelected()) {
            return null;
        }
        return mode == DateSelectionMode.RANGE ? getRange().getFrom() : start;
    }

    /**
     * @return the selected range, or null if the mode is not range or the range has no end yet
     */
    public DateRange getRange() {
        return mode == DateSelectionMode.RANGE && isSelected() ? new DateRange(start, end) : null;
    }

    /**
     * @return the range that is shown: the selected one, or from the start to the date under the mouse.
     * Null if there is none
     */
    DateRange getShownRange() {
        if (mode != DateSelectionMode.RANGE || start == null) {
            return null;
        }
        LocalDate last = end != null ? end : hover;
        return last != null ? new DateRange(start, last) : null;
    }

    /**
     * @return true if the date is selected, or the start or the end of a range
     */
    boolean isSelectedDate(LocalDate date) {
        return date.equals(start) || date.equals(end);
    }

    /**
     * @return the second click of a range, or null if the range waits for it or one date is selected
     */
    LocalDate getEnd() {
        return end;
    }

    /**
     * @return a date to show when the picker opens: the selected date or the start of a range, or null
     */
    public LocalDate getAnchor() {
        return start;
    }
}
