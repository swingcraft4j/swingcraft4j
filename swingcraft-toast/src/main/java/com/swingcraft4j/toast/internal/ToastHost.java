package com.swingcraft4j.toast.internal;

import java.awt.*;

/**
 * What a toast is shown on: the {@link ToastLayer} inside the window, or a {@link ToastWindow} of its own.
 * <p>
 * A host only puts the {@link ToastPanel} on the screen. Where a toast is, and how it is presented, is the
 * same on every host: the layer arranges all toasts of the window, in its own coordinates.
 */
interface ToastHost {

    /**
     * Adds the toast to the host. It is not seen before it has been placed and {@link #updateVisible} is called.
     */
    void attach(ToastPanel toast);

    void detach(ToastPanel toast);

    /**
     * @param bounds the bounds of the toast with its shadow, in the layer
     */
    void place(ToastPanel toast, Rectangle bounds);

    /**
     * Shows or hides the toast. Called when all toasts are placed and know how they are presented.
     */
    void updateVisible(ToastPanel toast);

    /**
     * Puts the toast over the other toasts.
     */
    void toFront(ToastPanel toast);

    /**
     * @return true if the toast has a window of its own: it is not limited by the window of the owner,
     * and it is painted on a transparent window
     */
    boolean isWindow();
}
