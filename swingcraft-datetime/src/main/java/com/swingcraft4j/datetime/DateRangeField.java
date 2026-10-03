package com.swingcraft4j.datetime;

import com.swingcraft4j.datetime.internal.SegmentEditor;
import com.swingcraft4j.datetime.option.DateSelectionMode;
import com.swingcraft4j.datetime.option.FieldOption;

import java.time.LocalDate;

/**
 * A field to type a range of dates: its first and its last date, with a separator between them. Its popup
 * has a date picker that selects a range. The pattern of the option is the pattern of one date, its default
 * is {@code dd/MM/yyyy}, and the separator is {@link FieldOption#setRangeSeparator(String)}.
 * <p>
 * The field has a value when both dates are typed and the last date is not before the first.
 * Its validator gets the whole range, so the picker of the popup does not know it: set what can be selected
 * there with the date option of the field.
 *
 * @see PickerField
 */
public class DateRangeField extends PickerField<DateRange> {

    public DateRangeField() {
        this(getDefaultOption());
    }

    /**
     * @param option the option, it is copied
     * @throws IllegalArgumentException if the pattern of the option has a time
     */
    public DateRangeField(FieldOption option) {
        super(option);
    }

    @Override
    String getDefaultPattern() {
        return "dd/MM/yyyy";
    }

    @Override
    void checkPattern(SegmentEditor editor) {
        if (editor.hasTime()) {
            throw new IllegalArgumentException("the pattern of a date range field has no time: " + editor.getPattern());
        }
    }

    @Override
    SegmentEditor createEditor(String pattern, FieldOption option, Runnable listener) {
        return new SegmentEditor(pattern, option.getRangeSeparator(), option.getLocale(), listener);
    }

    @Override
    DateRange read(SegmentEditor editor) {
        LocalDate from = editor.getDate(0);
        LocalDate to = editor.getDate(1);
        // a range that ends before it starts is typed wrong, it is not turned around
        return from != null && to != null && !to.isBefore(from) ? new DateRange(from, to) : null;
    }

    @Override
    void write(SegmentEditor editor, DateRange value) {
        editor.setRange(value != null ? value.getFrom() : null, value != null ? value.getTo() : null);
    }

    @Override
    DateSelectionMode getPopupSelectionMode() {
        return DateSelectionMode.RANGE;
    }

    @Override
    void syncDatePicker(DatePicker datePicker) {
        // the picker shows a range when the field has one, a range that is typed in part is not shown
        DateRange range = read(getEditor());
        if (range != null) {
            datePicker.setSelectedDateRange(range);
        } else {
            datePicker.clearSelection();
        }
    }

    @Override
    void datePicked(DateSelectionEvent event) {
        write(getEditor(), event.getDateRange());
    }

    /**
     * @return the range, or null if the field is empty, filled in part, has a date that does not exist,
     * ends before it starts, or its validator has an error
     */
    public DateRange getSelectedDateRange() {
        return getValue();
    }

    /**
     * @param range the range, or null to clear the field
     */
    public void setSelectedDateRange(DateRange range) {
        setValue(range);
    }

    /**
     * @see #setSelectedDateRange(DateRange)
     */
    public void setSelectedDateRange(LocalDate from, LocalDate to) {
        setSelectedDateRange(new DateRange(from, to));
    }

    /**
     * Shows the date picker in a popup below the field. Does nothing if the field is not showing or
     * not enabled.
     */
    public void showPopup() {
        showDatePopup();
    }
}
