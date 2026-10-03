package com.swingcraft4j.modal.option;

import java.awt.*;

/**
 * The options of a modal: background, layout and border, and the animation in its own
 * {@link AnimationOption}. The setters return the option, so they can be chained.
 * All sizes are unscaled and get scaled with the UI scale factor.
 * <p>
 * The option is copied when the modal is shown, so changing it afterwards
 * does not affect the modals already open.
 */
public class ModalOption {

    // background
    private BackgroundMode backgroundMode = BackgroundMode.CLOSE_ON_CLICK;
    private Color backgroundColor;
    private float backgroundOpacity = 0.4f;
    private boolean closeOnEscape = true;

    // layout
    private Surface surface = Surface.LAYER;
    private Location horizontalLocation = Location.CENTER;
    private Location verticalLocation = Location.CENTER;
    private Insets margin = new Insets(15, 15, 15, 15);
    private Number width = -1;
    private Number height = -1;
    private boolean relativeToOwner;
    private boolean movable;

    private AnimationOption animationOption = new AnimationOption();

    // border
    private int round = 10;
    private Insets shadowSize = Shadow.MEDIUM.getSize();
    private Color shadowColor;
    private float shadowOpacity = -1;
    private int borderWidth = 0;
    private Color borderColor;

    public BackgroundMode getBackgroundMode() {
        return backgroundMode;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public float getBackgroundOpacity() {
        return backgroundOpacity;
    }

    public boolean isCloseOnEscape() {
        return closeOnEscape;
    }

    public Surface getSurface() {
        return surface;
    }

    public Location getHorizontalLocation() {
        return horizontalLocation;
    }

    public Location getVerticalLocation() {
        return verticalLocation;
    }

    public Insets getMargin() {
        return (Insets) margin.clone();
    }

    public Number getWidth() {
        return width;
    }

    public Number getHeight() {
        return height;
    }

    public boolean isRelativeToOwner() {
        return relativeToOwner;
    }

    public boolean isMovable() {
        return movable;
    }

    public AnimationOption getAnimationOption() {
        return animationOption;
    }

    public int getRound() {
        return round;
    }

    public Insets getShadowSize() {
        return (Insets) shadowSize.clone();
    }

    public Color getShadowColor() {
        return shadowColor;
    }

    public float getShadowOpacity() {
        return shadowOpacity;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public ModalOption setBackgroundMode(BackgroundMode backgroundMode) {
        if (backgroundMode == null) {
            throw new IllegalArgumentException("background mode must not null");
        }
        this.backgroundMode = backgroundMode;
        return this;
    }

    /**
     * @param backgroundColor the color painted over the window for the blocking
     *                        background modes, or null for black
     */
    public ModalOption setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    /**
     * @param backgroundOpacity the background opacity from 0 (invisible, but still blocking) to 1
     */
    public ModalOption setBackgroundOpacity(float backgroundOpacity) {
        if (backgroundOpacity < 0 || backgroundOpacity > 1) {
            throw new IllegalArgumentException("background opacity must be 0 to 1");
        }
        this.backgroundOpacity = backgroundOpacity;
        return this;
    }

    public ModalOption setCloseOnEscape(boolean closeOnEscape) {
        this.closeOnEscape = closeOnEscape;
        return this;
    }

    /**
     * @param surface {@link Surface#LAYER} to show the modal inside the window of the owner (default), or
     *                {@link Surface#WINDOW} to show it in a window of its own, where it can be larger than
     *                the window of the owner and is over its heavyweight components
     */
    public ModalOption setSurface(Surface surface) {
        if (surface == null) {
            throw new IllegalArgumentException("surface must not null");
        }
        this.surface = surface;
        return this;
    }

    /**
     * @param horizontal LEADING, LEFT, CENTER, RIGHT or TRAILING
     * @param vertical   TOP, CENTER or BOTTOM
     */
    public ModalOption setLocation(Location horizontal, Location vertical) {
        if (horizontal == null || !horizontal.isHorizontal()) {
            throw new IllegalArgumentException("horizontal location must be LEADING, LEFT, CENTER, RIGHT or TRAILING: " + horizontal);
        }
        if (vertical == null || !vertical.isVertical()) {
            throw new IllegalArgumentException("vertical location must be TOP, CENTER or BOTTOM: " + vertical);
        }
        this.horizontalLocation = horizontal;
        this.verticalLocation = vertical;
        return this;
    }

    public ModalOption setMargin(int margin) {
        return setMargin(margin, margin, margin, margin);
    }

    /**
     * The minimum space between the window edges and the modal (the shadow is not counted).
     */
    public ModalOption setMargin(int top, int left, int bottom, int right) {
        this.margin = new Insets(top, left, bottom, right);
        return this;
    }

    /**
     * Sets the modal size. Each value can be:
     * <ul>
     * <li>{@code -1} to use the preferred size of the modal (default)</li>
     * <li>a {@code float} such as {@code 0.5f} for a fraction of the available space</li>
     * <li>an {@code int} for a fixed size in pixels</li>
     * </ul>
     * The modal never grows larger than the available space. With {@link Surface#WINDOW} it can,
     * a fraction is still of the available space.
     */
    public ModalOption setSize(Number width, Number height) {
        if (width == null || height == null) {
            throw new IllegalArgumentException("size must not null");
        }
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * @param relativeToOwner true to show the modal over the owner component instead of the whole window.
     *                        The background, the location, the size and the margin then apply to the visible
     *                        area of the owner. The modal follows the owner when it moves, hides while
     *                        the owner is hidden and closes when the owner is removed from the window
     */
    public ModalOption setRelativeToOwner(boolean relativeToOwner) {
        this.relativeToOwner = relativeToOwner;
        return this;
    }

    /**
     * @param movable true to let the user drag the modal with the mouse. The modal can be dragged
     *                by any part that does not use the mouse itself, such as the title
     */
    public ModalOption setMovable(boolean movable) {
        this.movable = movable;
        return this;
    }

    /**
     * @param animationOption how the modal is animated, change the current one with {@link #getAnimationOption()}
     */
    public ModalOption setAnimationOption(AnimationOption animationOption) {
        if (animationOption == null) {
            throw new IllegalArgumentException("animation option must not null");
        }
        this.animationOption = animationOption;
        return this;
    }

    /**
     * @param round the corner arc diameter, 0 for square corners
     */
    public ModalOption setRound(int round) {
        if (round < 0) {
            throw new IllegalArgumentException("round must be >= 0");
        }
        this.round = round;
        return this;
    }

    public ModalOption setShadow(Shadow shadow) {
        this.shadowSize = shadow.getSize();
        return this;
    }

    public ModalOption setShadowSize(int shadowSize) {
        return setShadowSize(new Insets(shadowSize, shadowSize, shadowSize, shadowSize));
    }

    /**
     * @param shadowSize the space the shadow takes on each side. Different sizes offset the shadow,
     *                   for example a larger bottom moves it down
     */
    public ModalOption setShadowSize(Insets shadowSize) {
        if (shadowSize.top < 0 || shadowSize.left < 0 || shadowSize.bottom < 0 || shadowSize.right < 0) {
            throw new IllegalArgumentException("shadow size must be >= 0");
        }
        this.shadowSize = (Insets) shadowSize.clone();
        return this;
    }

    /**
     * @param shadowColor the shadow color, or null to use the look and feel popup shadow color
     */
    public ModalOption setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
        return this;
    }

    /**
     * @param shadowOpacity the shadow opacity from 0 to 1, or -1 to use the default: stronger with a dark
     *                      look and feel, where a shadow is hard to see
     */
    public ModalOption setShadowOpacity(float shadowOpacity) {
        this.shadowOpacity = shadowOpacity;
        return this;
    }

    public ModalOption setBorderWidth(int borderWidth) {
        if (borderWidth < 0) {
            throw new IllegalArgumentException("border width must be >= 0");
        }
        this.borderWidth = borderWidth;
        return this;
    }

    /**
     * @param borderColor the outline color, or null to use the look and feel border color
     */
    public ModalOption setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public ModalOption copy() {
        ModalOption option = new ModalOption();
        option.backgroundMode = backgroundMode;
        option.backgroundColor = backgroundColor;
        option.backgroundOpacity = backgroundOpacity;
        option.closeOnEscape = closeOnEscape;
        option.surface = surface;
        option.horizontalLocation = horizontalLocation;
        option.verticalLocation = verticalLocation;
        option.margin = (Insets) margin.clone();
        option.width = width;
        option.height = height;
        option.relativeToOwner = relativeToOwner;
        option.movable = movable;
        option.animationOption = animationOption.copy();
        option.round = round;
        option.shadowSize = (Insets) shadowSize.clone();
        option.shadowColor = shadowColor;
        option.shadowOpacity = shadowOpacity;
        option.borderWidth = borderWidth;
        option.borderColor = borderColor;
        return option;
    }
}
