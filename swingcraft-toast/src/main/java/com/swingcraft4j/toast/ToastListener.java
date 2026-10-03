package com.swingcraft4j.toast;

import java.util.EventListener;

/**
 * Listens to a toast. Add it with {@link com.swingcraft4j.toast.option.ToastOption#addListener(ToastListener)}.
 * The methods do nothing by default, implement the ones that are needed.
 */
public interface ToastListener extends EventListener {

    /**
     * The toast was clicked with the left mouse button. It can be closed with {@link ToastController#close()}.
     */
    default void toastClicked(ToastController toast) {
    }

    /**
     * The toast has been closed and removed.
     */
    default void toastClosed(ToastController toast) {
    }
}
