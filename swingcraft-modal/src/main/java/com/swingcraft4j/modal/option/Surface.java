package com.swingcraft4j.modal.option;

/**
 * What the modal is shown on.
 */
public enum Surface {

    /**
     * The modal is shown inside the window of the owner, in a layer over its content (lightweight).
     * It can not be larger than the window, and it is behind the heavyweight components of the window,
     * such as an embedded browser or a video canvas.
     */
    LAYER,

    /**
     * The modal is shown in a window of its own, over the window of the owner (heavyweight).
     * It is over the heavyweight components too, and it can be larger than the window of the owner:
     * the location, the margin and the background still apply to the window of the owner (or to the
     * owner, when the modal is relative to it), but the size is only limited by the screen.
     * <p>
     * The window is over everything in the window of the owner, also over the modals in a {@link #LAYER}.
     * So while it is open, every modal shown in the window of the owner or from inside this modal gets a
     * window of its own too, whatever its option is: the modal that is shown last is the top one.
     * <p>
     * A modal that is larger than the window of the owner stays on the screen.
     * <p>
     * The window is transparent around the modal. Where the system can not show a transparent window,
     * the modal is shown in a {@link #LAYER}.
     */
    WINDOW
}
