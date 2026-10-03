package com.swingcraft4j.toast.option;

/**
 * How a toast is animated when it is shown and closed. The other toasts at the same location move with it.
 * The setters return the option, so they can be chained. Get it from {@link ToastOption#getAnimationOption()}.
 */
public class AnimationOption {

    private boolean enabled = true;
    private int duration = 350;
    private int openDuration = -1;
    private int closeDuration = -1;
    private Easing easing = Easing.STANDARD;
    private boolean fade = true;
    private ToastDirection direction = ToastDirection.AUTO;

    public boolean isEnabled() {
        return enabled;
    }

    public int getDuration() {
        return duration;
    }

    /**
     * @return the time in milliseconds to show the toast: the duration, if no other time is set
     */
    public int getOpenDuration() {
        return openDuration >= 0 ? openDuration : duration;
    }

    /**
     * @return the time in milliseconds to close the toast: the duration, if no other time is set
     */
    public int getCloseDuration() {
        return closeDuration >= 0 ? closeDuration : duration;
    }

    public Easing getEasing() {
        return easing;
    }

    public boolean isFade() {
        return fade;
    }

    public ToastDirection getDirection() {
        return direction;
    }

    /**
     * @param enabled false to show and close the toast without animation.
     *                The animation is also off when the system property
     *                {@code flatlaf.animation} is {@code false}
     */
    public AnimationOption setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * @param duration the time in milliseconds to show or close the toast and to expand a stack,
     *                 0 for no animation
     */
    public AnimationOption setDuration(int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration must be >= 0");
        }
        this.duration = duration;
        return this;
    }

    /**
     * @param openDuration the time in milliseconds to show the toast, 0 for no animation,
     *                     or -1 (default) to use the duration
     */
    public AnimationOption setOpenDuration(int openDuration) {
        if (openDuration < -1) {
            throw new IllegalArgumentException("open duration must be >= -1");
        }
        this.openDuration = openDuration;
        return this;
    }

    /**
     * @param closeDuration the time in milliseconds to close the toast, 0 for no animation,
     *                      or -1 (default) to use the duration
     */
    public AnimationOption setCloseDuration(int closeDuration) {
        if (closeDuration < -1) {
            throw new IllegalArgumentException("close duration must be >= -1");
        }
        this.closeDuration = closeDuration;
        return this;
    }

    /**
     * @param easing how the speed of the animation changes over its time
     */
    public AnimationOption setEasing(Easing easing) {
        if (easing == null) {
            throw new IllegalArgumentException("easing must not null");
        }
        this.easing = easing;
        return this;
    }

    /**
     * Fades the toast in when it is shown and out when it is closed.
     * <p>
     * A fade animates an image of the toast: the text of the real components does not look the same when it
     * is painted half visible. The real components are painted again when the animation ends.
     *
     * @param fade false to show the toast completely visible from the start (default true)
     */
    public AnimationOption setFade(boolean fade) {
        this.fade = fade;
        return this;
    }

    /**
     * @param direction the direction the toast moves in when it is shown
     */
    public AnimationOption setDirection(ToastDirection direction) {
        if (direction == null) {
            throw new IllegalArgumentException("direction must not null");
        }
        this.direction = direction;
        return this;
    }

    public AnimationOption copy() {
        AnimationOption option = new AnimationOption();
        option.enabled = enabled;
        option.duration = duration;
        option.openDuration = openDuration;
        option.closeDuration = closeDuration;
        option.easing = easing;
        option.fade = fade;
        option.direction = direction;
        return option;
    }
}
