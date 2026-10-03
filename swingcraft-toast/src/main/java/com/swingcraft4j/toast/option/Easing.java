package com.swingcraft4j.toast.option;

import com.formdev.flatlaf.util.CubicBezierEasing;

/**
 * How the speed of an animation changes over its time. Use one of the constants,
 * {@link #cubicBezier(float, float, float, float)} or an own implementation.
 */
public interface Easing {

    /**
     * The same speed all the time.
     */
    Easing LINEAR = fraction -> fraction;

    /**
     * Starts fast and ends slow, the default.
     */
    Easing STANDARD = CubicBezierEasing.STANDARD_EASING::interpolate;

    /**
     * Starts slow.
     */
    Easing EASE_IN = CubicBezierEasing.EASE_IN::interpolate;

    /**
     * Ends slow.
     */
    Easing EASE_OUT = CubicBezierEasing.EASE_OUT::interpolate;

    /**
     * Starts and ends slow.
     */
    Easing EASE_IN_OUT = CubicBezierEasing.EASE_IN_OUT::interpolate;

    /**
     * @param fraction how much of the time of the animation has passed, from 0 to 1
     * @return how far the animation is, from 0 to 1. A value outside is limited to that range
     */
    float interpolate(float fraction);

    /**
     * @return the easing of a cubic bezier curve from (0, 0) to (1, 1) with the two control points,
     * as {@code cubic-bezier()} in CSS
     * @throws IllegalArgumentException if {@code x1} or {@code x2} is not from 0 to 1
     */
    static Easing cubicBezier(float x1, float y1, float x2, float y2) {
        return new CubicBezierEasing(x1, y1, x2, y2)::interpolate;
    }
}
