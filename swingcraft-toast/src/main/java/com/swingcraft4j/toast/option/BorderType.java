package com.swingcraft4j.toast.option;

/**
 * The line painted on a toast in the color of its type.
 * Leading and trailing follow the component orientation.
 */
public enum BorderType {

    /**
     * No line.
     */
    DEFAULT,

    /**
     * A line around the toast.
     */
    OUTLINE,
    LEADING_LINE,
    TRAILING_LINE,
    TOP_LINE,
    BOTTOM_LINE
}
