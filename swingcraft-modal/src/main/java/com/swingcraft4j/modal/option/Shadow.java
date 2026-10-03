package com.swingcraft4j.modal.option;

import java.awt.*;

/**
 * Shadow size presets for {@link ModalOption#setShadow(Shadow)}.
 * <p>
 * A size is the space the shadow takes at each side. The space at the sides sets how soft the shadow is,
 * and the bottom is larger than the top by twice the distance the shadow is moved down. A shadow is soft
 * when the sides are much larger than that distance.
 */
public enum Shadow {

    NONE(0, 0, 0, 0),
    SMALL(3, 6, 9, 6),
    MEDIUM(5, 10, 15, 10),
    LARGE(8, 16, 24, 16),
    EXTRA_LARGE(12, 24, 36, 24);

    private final Insets size;

    Shadow(int top, int left, int bottom, int right) {
        this.size = new Insets(top, left, bottom, right);
    }

    public Insets getSize() {
        return (Insets) size.clone();
    }
}
