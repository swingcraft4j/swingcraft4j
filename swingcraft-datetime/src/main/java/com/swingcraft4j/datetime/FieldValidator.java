package com.swingcraft4j.datetime;

/**
 * Checks the value of a field. Set it with {@code setValidator} of the field.
 *
 * @param <T> the value of the field: a date, a time, or a date and a time
 */
@FunctionalInterface
public interface FieldValidator<T> {

    /**
     * Checks the value. It is called each time the field has another value, on the event dispatch thread.
     *
     * @param value the value of the field. It is null only when {@link PickerField#validateInput()} is
     *              called for a field that has no value, so a field that must be filled in can be told
     * @return what to show at the field, or null if the value is allowed and nothing is shown
     */
    ValidationResult validate(T value);
}
