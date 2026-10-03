package com.swingcraft4j.toast.option;

/**
 * How the background of a toast uses the color of its type.
 */
public enum BackgroundType {

    /**
     * The background does not use the color of the type.
     */
    DEFAULT,

    /**
     * The background is tinted with the color of the type.
     */
    TINT,

    /**
     * The color of the type fades out from the leading side.
     */
    GRADIENT
}
