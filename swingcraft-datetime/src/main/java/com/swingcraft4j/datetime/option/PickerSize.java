package com.swingcraft4j.datetime.option;

/**
 * How large a picker is: its cells, its clock and its text.
 */
public enum PickerSize {

    SMALL(0.76f, -1),
    /**
     * The size of a picker if no other size is set. Its text has the size of the font of the look and feel.
     */
    DEFAULT(0.88f, 0),
    MEDIUM(1f, 0),
    LARGE(1.15f, 1);

    private final float scale;
    private final int fontOffset;

    PickerSize(float scale, int fontOffset) {
        this.scale = scale;
        this.fontOffset = fontOffset;
    }

    /**
     * @return how large the parts of the picker are, 1 for {@link #MEDIUM}
     */
    public float getScale() {
        return scale;
    }

    /**
     * @return what is added to the size of the font of the look and feel
     */
    public int getFontOffset() {
        return fontOffset;
    }
}
