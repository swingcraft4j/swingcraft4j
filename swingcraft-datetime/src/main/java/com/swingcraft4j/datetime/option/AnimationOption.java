package com.swingcraft4j.datetime.option;

/**
 * How a picker is animated: the slide to another month of a date picker, and the hand of the clock of a
 * time picker. The setters return the option, so they can be chained. Get it from
 * {@link DateOption#getAnimationOption()} or {@link TimeOption#getAnimationOption()}.
 */
public class AnimationOption {

    private boolean enabled = true;
    private int duration = 300;

    public boolean isEnabled() {
        return enabled;
    }

    public int getDuration() {
        return duration;
    }

    /**
     * @param enabled false to change the picker without animation (default true).
     *                The animation is also off when the system property
     *                {@code flatlaf.animation} is {@code false}
     */
    public AnimationOption setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * @param duration the time in milliseconds of an animation, 0 for no animation
     */
    public AnimationOption setDuration(int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration must be >= 0");
        }
        this.duration = duration;
        return this;
    }

    public AnimationOption copy() {
        AnimationOption option = new AnimationOption();
        option.enabled = enabled;
        option.duration = duration;
        return option;
    }
}
