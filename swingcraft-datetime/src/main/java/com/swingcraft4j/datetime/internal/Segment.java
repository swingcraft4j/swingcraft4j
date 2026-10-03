package com.swingcraft4j.datetime.internal;

/**
 * One segment of a field: its type, the letters of the pattern it was made from, and its value.
 */
final class Segment {

    /**
     * The value of a segment that has no value yet, its placeholder is shown.
     */
    static final int EMPTY = -1;

    // a year that is typed with one or two digits is a year of this century
    private static final int CENTURY = 2000;

    private final SegmentType type;
    private final String pattern;
    private final int group;
    private int value = EMPTY;
    // how many digits were typed since the segment was selected, the next digit goes after them
    private int typed;

    /**
     * @param pattern the letters of the pattern, as "dd". The text itself for a literal
     * @param group   0, or 1 for a segment of the second date of a range
     */
    Segment(SegmentType type, String pattern, int group) {
        this.type = type;
        this.pattern = pattern;
        this.group = group;
    }

    int getGroup() {
        return group;
    }

    SegmentType getType() {
        return type;
    }

    String getPattern() {
        return pattern;
    }

    boolean isEditable() {
        return type.isEditable();
    }

    boolean isEmpty() {
        return value == EMPTY;
    }

    int getValue() {
        return value;
    }

    void setValue(int value) {
        this.value = value;
        typed = 0;
    }

    void clear() {
        setValue(EMPTY);
    }

    /**
     * @return true for a month that is shown with its name, and not as a number
     */
    boolean isName() {
        return type == SegmentType.MONTH && pattern.length() >= 3;
    }

    /**
     * @return true for a year that is shown with its last two digits
     */
    boolean isShortYear() {
        return type == SegmentType.YEAR && pattern.length() == 2;
    }

    /**
     * @return how many digits can be typed
     */
    int getDigits() {
        return isShortYear() ? 2 : type.getDigits();
    }

    // the number that is typed and shown: the last two digits of a short year, the value for all others
    private int getNumber() {
        return isShortYear() ? value % 100 : value;
    }

    private void setNumber(int number) {
        value = isShortYear() ? CENTURY + number : number;
    }

    private int getLargestNumber() {
        return isShortYear() ? 99 : type.getMaximum();
    }

    /**
     * The user has typed a digit. It goes after the digits that were typed before, or starts the number again
     * if the segment was just selected or is full.
     *
     * @return true if no more digit can follow, so the next segment is selected
     */
    boolean type(int digit) {
        int digits = getDigits();
        if (digits == 0) {
            return false;
        }
        int number = typed == 0 || typed >= digits ? digit : getNumber() * 10 + digit;
        if (number > getLargestNumber()) {
            // the digit does not fit after the others, it starts a new number
            number = digit;
            typed = 0;
        }
        if (typed >= digits) {
            typed = 0;
        }
        setNumber(number);
        typed++;
        return typed >= digits || number * 10 > getLargestNumber();
    }

    /**
     * @return true if a digit was typed since the segment was selected
     */
    boolean isTyping() {
        return typed > 0;
    }

    /**
     * The segment is not edited any more: a value that can not stay is fixed or removed.
     */
    void commit() {
        typed = 0;
        if (value == EMPTY) {
            return;
        }
        if (type == SegmentType.YEAR && value < 100) {
            // 26 is 2026
            value += CENTURY;
        }
        if (value < type.getMinimum() || value > type.getMaximum()) {
            value = EMPTY;
        }
    }

    /**
     * @return the digits of the value, with zeros in front to the length of the pattern
     */
    String getNumberText() {
        StringBuilder text = new StringBuilder(String.valueOf(getNumber()));
        while (text.length() < pattern.length()) {
            text.insert(0, '0');
        }
        return text.toString();
    }
}
