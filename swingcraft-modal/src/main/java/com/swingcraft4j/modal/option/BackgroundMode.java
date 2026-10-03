package com.swingcraft4j.modal.option;

/**
 * What the area around the modal does.
 */
public enum BackgroundMode {

    /**
     * The background is covered and blocked. Clicking it closes the modal.
     */
    CLOSE_ON_CLICK,

    /**
     * The background is covered and blocked. Clicking it does nothing.
     */
    BLOCK,

    /**
     * No background. The components behind the modal stay usable and the modal stays open.
     */
    NONE,

    /**
     * No background, like a popup. Clicking outside the modal closes it and the click still
     * reaches the component behind. Unlike a popup, the modal stays open when the window loses focus.
     */
    POPUP;

    /**
     * @return true if the background is painted and blocks the components behind the modal
     */
    public boolean isBlocking() {
        return this == CLOSE_ON_CLICK || this == BLOCK;
    }
}
