package com.swingcraft4j.toast.option;

/**
 * The direction a toast moves in when it is shown. It moves back when it is closed.
 * Leading and trailing follow the component orientation.
 */
public enum ToastDirection {

    /**
     * From the edge of the window the toast is at: from the top or the bottom for a location in the center,
     * from the side for a leading or trailing location. In a stack always from the top or the bottom.
     */
    AUTO,
    TOP_TO_BOTTOM,
    BOTTOM_TO_TOP,
    LEADING_TO_TRAILING,
    TRAILING_TO_LEADING,

    /**
     * The toast does not move.
     */
    NONE
}
