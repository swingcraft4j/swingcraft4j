package com.swingcraft4j.toast.option;

/**
 * What happens when a toast is shown in a list that shows as many toasts as its limit allows already.
 * See {@link ToastOption#setListMaxVisible(int)}.
 */
public enum ListOverflow {

    /**
     * The new toast waits, and is shown when one of the toasts that are showing closes.
     * No toast is cut short, the toasts are shown in the order they were added.
     */
    WAIT,

    /**
     * The new toast is shown at once, and the oldest toast that is showing is closed.
     */
    CLOSE_OLDEST
}
