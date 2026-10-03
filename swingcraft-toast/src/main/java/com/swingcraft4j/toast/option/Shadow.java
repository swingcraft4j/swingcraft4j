package com.swingcraft4j.toast.option;

import java.awt.*;

/**
 * Shadow size presets for {@link StyleOption#setShadow(Shadow)}.
 * <p>
 * A size is the space the shadow takes at each side. The space at the sides sets how soft the shadow is,
 * and the bottom is larger than the top by twice the distance the shadow is moved down. A shadow is soft
 * when the sides are much larger than that distance.
 */
public enum Shadow {

    NONE(0, 0, 0, 0),
    SMALL(2, 4, 6, 4),
    MEDIUM(5, 8, 11, 8),
    LARGE(7, 12, 17, 12),
    EXTRA_LARGE(11, 18, 25, 18);

    private final Insets size;

    Shadow(int top, int left, int bottom, int right) {
        this.size = new Insets(top, left, bottom, right);
    }

    public Insets getSize() {
        return (Insets) size.clone();
    }
}
