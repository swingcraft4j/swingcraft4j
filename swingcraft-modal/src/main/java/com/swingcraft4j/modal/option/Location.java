package com.swingcraft4j.modal.option;

/**
 * Where the modal is placed inside the window.
 * {@link #LEADING} and {@link #TRAILING} follow the component orientation.
 */
public enum Location {

    TOP(0f, false, true),
    BOTTOM(1f, false, true),
    LEFT(0f, true, false),
    RIGHT(1f, true, false),
    LEADING(0f, true, false),
    TRAILING(1f, true, false),
    CENTER(0.5f, true, true);

    private final float value;
    private final boolean horizontal;
    private final boolean vertical;

    Location(float value, boolean horizontal, boolean vertical) {
        this.value = value;
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public boolean isVertical() {
        return vertical;
    }

    /**
     * @return the alignment from 0 (start) to 1 (end)
     */
    public float getValue(boolean leftToRight) {
        if (!leftToRight && (this == LEADING || this == TRAILING)) {
            return 1f - value;
        }
        return value;
    }
}
