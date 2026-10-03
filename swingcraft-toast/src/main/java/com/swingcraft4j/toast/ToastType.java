package com.swingcraft4j.toast;

import java.awt.*;

/**
 * The kind of message a toast shows. It sets the color, the icon and the default label of the toast.
 */
public enum ToastType {

    DEFAULT("Message", new Color(0x64748B)),
    SUCCESS("Success", new Color(0x1EA97C)),
    INFO("Info", new Color(0x3B82F6)),
    WARNING("Warning", new Color(0xCC8925)),
    ERROR("Error", new Color(0xFF5757)),

    /**
     * Something is running, and the toast waits for it: its icon turns, and the toast does not close by
     * itself. When the work is done, {@link ToastController#update(ToastType, String)} shows the result
     * in the toast. {@link JToast#showTask} and {@link JToast#showFuture} do all of that.
     */
    LOADING("Loading", new Color(0x64748B));

    private final String label;
    private final Color color;

    ToastType(String label, Color color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public Color getColor() {
        return color;
    }
}
