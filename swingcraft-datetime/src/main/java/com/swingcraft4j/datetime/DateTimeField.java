package com.swingcraft4j.datetime;

import com.swingcraft4j.datetime.internal.SegmentEditor;
import com.swingcraft4j.datetime.option.FieldOption;

import java.time.LocalDateTime;

/**
 * A field to type a date and a time. It has two buttons: one shows a date picker in a popup and one a time
 * picker. Its default pattern is {@code dd/MM/yyyy hh:mm a}.
 * <p>
 * Its validator gets the date and the time together, so the pickers of the popup do not know it, as each of
 * them has only a part of the value: set what can be selected there with the date option and the time
 * option of the field.
 *
 * @see PickerField
 */
public class DateTimeField extends PickerField<LocalDateTime> {

    public DateTimeField() {
        this(getDefaultOption());
    }

    /**
     * @param option the option, it is copied
     * @throws IllegalArgumentException if the pattern of the option has no date or no time
     */
    public DateTimeField(FieldOption option) {
        super(option);
    }

    @Override
    String getDefaultPattern() {
        return "dd/MM/yyyy hh:mm a";
    }

    @Override
    void checkPattern(SegmentEditor editor) {
        if (!editor.hasDate() || !editor.hasTime()) {
            throw new IllegalArgumentException("the pattern of a date time field has a date and a time: " + editor.getPattern());
        }
    }

    @Override
    LocalDateTime read(SegmentEditor editor) {
        return editor.getDateTime();
    }

    @Override
    void write(SegmentEditor editor, LocalDateTime value) {
        editor.setDateTime(value);
    }

    /**
     * @return the date and the time, or null if the field is empty, filled in part, has a date that
     * does not exist, or its validator has an error
     */
    public LocalDateTime getSelectedDateTime() {
        return getValue();
    }

    /**
     * @param dateTime the date and the time, or null to clear the field
     */
    public void setSelectedDateTime(LocalDateTime dateTime) {
        setValue(dateTime);
    }

    /**
     * Shows the date picker in a popup below the field. Does nothing if the field is not showing or
     * not enabled.
     */
    public void showDatePopup() {
        super.showDatePopup();
    }

    /**
     * Shows the time picker in a popup below the field. Does nothing if the field is not showing or
     * not enabled.
     */
    public void showTimePopup() {
        super.showTimePopup();
    }
}
