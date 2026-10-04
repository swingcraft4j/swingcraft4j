package com.swingcraft4j.datetime;

import com.swingcraft4j.datetime.internal.SegmentEditor;
import com.swingcraft4j.datetime.option.FieldOption;

import java.time.LocalDate;
import java.util.function.Predicate;

/**
 * A field to type a date, with a date picker in its popup. Its default pattern is {@code dd/MM/yyyy}.
 * A date its validator has an error for can not be selected in the popup.
 *
 * @see JPickerField
 */
public class JDateField extends JPickerField<LocalDate> {

    public JDateField() {
        this(getDefaultOption());
    }

    /**
     * @param option the option, it is copied
     * @throws IllegalArgumentException if the pattern of the option has a time
     */
    public JDateField(FieldOption option) {
        super(option);
    }

    @Override
    String getDefaultPattern() {
        return "dd/MM/yyyy";
    }

    @Override
    void checkPattern(SegmentEditor editor) {
        if (editor.hasTime()) {
            throw new IllegalArgumentException("the pattern of a date field has no time: " + editor.getPattern());
        }
    }

    @Override
    LocalDate read(SegmentEditor editor) {
        return editor.getDate();
    }

    @Override
    void write(SegmentEditor editor, LocalDate value) {
        editor.setDate(value);
    }

    @Override
    Predicate<LocalDate> getPopupDates() {
        FieldValidator<LocalDate> validator = getValidator();
        return validator != null ? date -> isAllowed(validator.validate(date)) : null;
    }

    /**
     * @return the date, or null if the field is empty, filled in part, has a date that does not exist, or
     * its validator has an error
     */
    public LocalDate getSelectedDate() {
        return getValue();
    }

    /**
     * @param date the date, or null to clear the field
     */
    public void setSelectedDate(LocalDate date) {
        setValue(date);
    }

    /**
     * Shows the date picker in a popup below the field. Does nothing if the field is not showing or
     * not enabled.
     */
    public void showPopup() {
        showDatePopup();
    }
}
