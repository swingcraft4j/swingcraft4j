package com.swingcraft4j.datetime.internal;

/**
 * What a segment of a field is: a part of the date or the time that is edited, or a text between them.
 */
enum SegmentType {

    DAY(1, 31, 2),
    MONTH(1, 12, 2),
    YEAR(1, 9999, 4),
    HOUR_24(0, 23, 2),
    HOUR_12(1, 12, 2),
    MINUTE(0, 59, 2),
    SECOND(0, 59, 2),
    /**
     * 0 is AM and 1 is PM. It is not typed with digits.
     */
    AM_PM(0, 1, 0),
    /**
     * The name of the day of the date. It is shown and not edited.
     */
    WEEKDAY(0, 0, 0),
    /**
     * A text of the pattern, as the line between the day and the month. It is shown and not edited.
     */
    LITERAL(0, 0, 0);

    private final int minimum;
    private final int maximum;
    private final int digits;

    SegmentType(int minimum, int maximum, int digits) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.digits = digits;
    }

    int getMinimum() {
        return minimum;
    }

    int getMaximum() {
        return maximum;
    }

    /**
     * @return how many digits the value has at most, 0 if it is not typed with digits
     */
    int getDigits() {
        return digits;
    }

    boolean isEditable() {
        return this != WEEKDAY && this != LITERAL;
    }

    boolean isDate() {
        return this == DAY || this == MONTH || this == YEAR;
    }

    boolean isTime() {
        return this == HOUR_24 || this == HOUR_12 || this == MINUTE || this == SECOND || this == AM_PM;
    }
}
