package com.swingcraft4j.datetime;

/**
 * What a {@link FieldValidator} says about the value of a field: how serious it is, and a message for the user.
 * The field shows it with the color of its border and an icon, the message is the tool tip of the icon.
 */
public final class ValidationResult {

    public enum Severity {
        /**
         * The value is not allowed. The field has no value while it shows an error.
         */
        ERROR,
        /**
         * The value is allowed, but the user should look at it again.
         */
        WARNING,
        /**
         * The value is allowed, and the field shows that it is.
         */
        SUCCESS
    }

    private final Severity severity;
    private final String message;

    private ValidationResult(Severity severity, String message) {
        this.severity = severity;
        this.message = message;
    }

    /**
     * @param message the message for the user, or null for none
     */
    public static ValidationResult error(String message) {
        return new ValidationResult(Severity.ERROR, message);
    }

    /**
     * @param message the message for the user, or null for none
     */
    public static ValidationResult warning(String message) {
        return new ValidationResult(Severity.WARNING, message);
    }

    /**
     * @param message the message for the user, or null for none
     */
    public static ValidationResult success(String message) {
        return new ValidationResult(Severity.SUCCESS, message);
    }

    public Severity getSeverity() {
        return severity;
    }

    /**
     * @return the message for the user, or null if there is none
     */
    public String getMessage() {
        return message;
    }

    public boolean isError() {
        return severity == Severity.ERROR;
    }

    @Override
    public String toString() {
        return severity + (message != null ? ": " + message : "");
    }
}
