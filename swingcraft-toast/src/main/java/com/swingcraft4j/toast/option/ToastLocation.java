package com.swingcraft4j.toast.option;

/**
 * Where the toasts are placed in the window.
 * Leading and trailing follow the component orientation.
 */
public enum ToastLocation {

    TOP_LEADING(true, 0f),
    TOP_CENTER(true, 0.5f),
    TOP_TRAILING(true, 1f),
    BOTTOM_LEADING(false, 0f),
    BOTTOM_CENTER(false, 0.5f),
    BOTTOM_TRAILING(false, 1f);

    private final boolean top;
    private final float alignment;

    ToastLocation(boolean top, float alignment) {
        this.top = top;
        this.alignment = alignment;
    }

    public boolean isTop() {
        return top;
    }

    public boolean isCenter() {
        return alignment == 0.5f;
    }

    /**
     * @return the horizontal alignment from 0 (left) to 1 (right)
     */
    public float getAlignment(boolean leftToRight) {
        return leftToRight ? alignment : 1f - alignment;
    }
}
