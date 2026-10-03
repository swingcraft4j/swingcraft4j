package com.swingcraft4j.toast.option;

/**
 * What the toast is shown on.
 */
public enum Surface {

    /**
     * The toast is shown inside the window of the owner, in a layer over its content (lightweight).
     * It is not wider than the window, the toasts that do not fit in the window are cut off, and it is behind
     * the heavyweight components of the window, such as an embedded browser or a video canvas.
     */
    LAYER,

    /**
     * The toast is shown in a window of its own, over the window of the owner (heavyweight).
     * It is over the heavyweight components too, and it is not limited by the window of the owner: the
     * location and the margin still apply to the window of the owner (or to the owner, when the toast is
     * relative to it), but a toast can be wider than that window, and the toasts that do not fit in it go on
     * outside of it. A toast that is wider than the window of the owner stays on the screen.
     * <p>
     * The window of a toast never gets the focus, so a component in a custom toast can be used with the
     * mouse but not with the keyboard.
     * <p>
     * The toasts at the same location are arranged together whatever they are shown on, but a toast in a
     * window is always over a toast in a {@link #LAYER}. They should use the same surface.
     * <p>
     * The window is transparent around the toast. Where the system can not show a transparent window,
     * the toast is shown in a {@link #LAYER}.
     */
    WINDOW
}
