package com.swingcraft4j.toast.option;

/**
 * How the toasts at the same location are arranged.
 */
public enum ToastLayoutType {

    /**
     * The toasts are shown one after the other, with a gap between them.
     */
    LIST,

    /**
     * The toasts are shown over each other: the newest is in front, the older ones are behind it,
     * a little smaller, and only their edge is visible.
     */
    STACK,

    /**
     * One toast at a time: a new toast takes the place of the one that is showing, which is closed.
     */
    REPLACE,

    /**
     * As a list, but each toast is as wide as the window, without the margin. For a toast from edge to edge
     * set the margin and the round of the style to 0.
     */
    BANNER
}
