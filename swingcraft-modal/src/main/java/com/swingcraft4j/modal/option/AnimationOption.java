package com.swingcraft4j.modal.option;

import java.awt.*;

/**
 * How a modal is animated when it is shown, closed, pushed and popped. The setters return the option,
 * so they can be chained. Get it from {@link ModalOption#getAnimationOption()}.
 */
public class AnimationOption {

    private boolean enabled = true;
    private int duration = 200;
    private int slideDuration = 300;
    private Point offset = new Point(0, 20);
    private boolean fade = true;
    private float scale;
    private boolean snapshot;

    public boolean isEnabled() {
        return enabled;
    }

    public int getDuration() {
        return duration;
    }

    public int getSlideDuration() {
        return slideDuration;
    }

    public Point getOffset() {
        return new Point(offset);
    }

    public boolean isFade() {
        return fade;
    }

    public float getScale() {
        return scale;
    }

    public boolean isSnapshot() {
        return snapshot;
    }

    /**
     * @param enabled false to show, close, push and pop the modal without animation.
     *                The animation is also off when the system property
     *                {@code flatlaf.animation} is {@code false}
     */
    public AnimationOption setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * @param duration the time in milliseconds to show or close the modal, 0 for no animation
     */
    public AnimationOption setDuration(int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration must be >= 0");
        }
        this.duration = duration;
        return this;
    }

    /**
     * @param slideDuration the time in milliseconds to slide to the pushed or popped modal, 0 for no animation
     */
    public AnimationOption setSlideDuration(int slideDuration) {
        if (slideDuration < 0) {
            throw new IllegalArgumentException("slide duration must be >= 0");
        }
        this.slideDuration = slideDuration;
        return this;
    }

    /**
     * Sets where the modal starts when it is shown, relative to its location. It moves from there to its
     * location, and back when it is closed. The default is 20 below the location. The offset is unscaled
     * and gets scaled with the UI scale factor.
     *
     * @param x the distance to the trailing side, negative for the leading side
     * @param y the distance below, negative for above
     */
    public AnimationOption setOffset(int x, int y) {
        this.offset = new Point(x, y);
        return this;
    }

    /**
     * Fades the modal in when it is shown and out when it is closed.
     * <p>
     * A fade animates an image of the modal: the text of the real components does not look the same when it
     * is painted half visible. The real components are painted again when the animation ends. The text does
     * not move then, but on a screen with a scale that is not a whole number (125%, 150%) the border lines of
     * the components can change a little.
     * <p>
     * Without fade, scale and snapshot the real components of the modal are painted for each frame,
     * and nothing changes when the animation ends.
     *
     * @param fade false to show the modal completely visible from the start (default true)
     */
    public AnimationOption setFade(boolean fade) {
        this.fade = fade;
        return this;
    }

    /**
     * Zooms the modal while it is shown and closed: it grows to its size when it is shown and gets smaller
     * when it is closed. The zoom is added to the offset, set the offset to 0 for a zoom only.
     * <p>
     * A zoom animates an image of the modal, the text of the real components can not be painted in other
     * sizes without moving. The image is not as sharp as the real modal while it is zoomed. For the end of
     * the animation the same applies as for {@link #setFade(boolean)}.
     *
     * @param scale how much smaller the modal starts, from 0 for no zoom (default) to less than 1.
     *              With 0.1 the modal grows from 90% to its size
     */
    public AnimationOption setScale(float scale) {
        if (scale < 0 || scale >= 1) {
            throw new IllegalArgumentException("scale must be 0 to less than 1");
        }
        this.scale = scale;
        return this;
    }

    /**
     * By default the window behind the modal is painted again for each frame while the background fades.
     * A window with many components is slow to animate that way. A modal with {@link Surface#WINDOW} is not
     * painted in the window, so the window behind it is never painted again and the snapshot is not needed.
     *
     * @param snapshot true to paint the window behind the modal once, and animate the image. This is faster
     *                 for a heavy window, but the window behind does not change during the animation. An image
     *                 of the modal is animated too. For the end of the animation the same applies as for
     *                 {@link #setFade(boolean)}
     */
    public AnimationOption setSnapshot(boolean snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    public AnimationOption copy() {
        AnimationOption option = new AnimationOption();
        option.enabled = enabled;
        option.duration = duration;
        option.slideDuration = slideDuration;
        option.offset = new Point(offset);
        option.fade = fade;
        option.scale = scale;
        option.snapshot = snapshot;
        return option;
    }
}
