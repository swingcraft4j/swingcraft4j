package com.swingcraft4j.datetime;

import com.swingcraft4j.datetime.internal.SegmentEditor;
import com.swingcraft4j.datetime.option.FieldOption;

import java.time.LocalTime;
import java.util.function.Predicate;

/**
 * A field to type a time, with a time picker in its popup. Its default pattern is {@code hh:mm a}, a pattern
 * with {@code HH} gives a time and a clock with 24 hours. A time its validator has an error for can not be
 * selected in the popup.
 *
 * @see PickerField
 */
public class TimeField extends PickerField<LocalTime> {

    public TimeField() {
        this(getDefaultOption());
    }

    /**
     * @param option the option, it is copied
     * @throws IllegalArgumentException if the pattern of the option has a date
     */
    public TimeField(FieldOption option) {
        super(option);
    }

    @Override
    String getDefaultPattern() {
        return "hh:mm a";
    }

    @Override
    void checkPattern(SegmentEditor editor) {
        if (editor.hasDate()) {
            throw new IllegalArgumentException("the pattern of a time field has no date: " + editor.getPattern());
        }
    }

    @Override
    LocalTime read(SegmentEditor editor) {
        return editor.getTime();
    }

    @Override
    void write(SegmentEditor editor, LocalTime value) {
        editor.setTime(value);
    }

    @Override
    Predicate<LocalTime> getPopupTimes() {
        FieldValidator<LocalTime> validator = getValidator();
        return validator != null ? time -> isAllowed(validator.validate(time)) : null;
    }

    /**
     * @return the time, or null if the field is empty, filled in part, or its validator has an error
     */
    public LocalTime getSelectedTime() {
        return getValue();
    }

    /**
     * @param time the time, or null to clear the field
     */
    public void setSelectedTime(LocalTime time) {
        setValue(time);
    }

    /**
     * Shows the time picker in a popup below the field. Does nothing if the field is not showing or
     * not enabled.
     */
    public void showPopup() {
        showTimePopup();
    }
}
