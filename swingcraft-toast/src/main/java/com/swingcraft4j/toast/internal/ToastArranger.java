package com.swingcraft4j.toast.internal;

import com.formdev.flatlaf.util.UIScale;
import com.swingcraft4j.toast.option.ToastDirection;
import com.swingcraft4j.toast.option.ToastLayoutType;
import com.swingcraft4j.toast.option.ToastLocation;

import java.awt.*;
import java.util.List;

/**
 * Arranges the toasts that are shown at the same location with the same {@link ToastLayoutType}.
 * It sets the bounds of each toast and how it is presented, by how far each toast is shown: it is called
 * for every step of the animation of a toast, so the other toasts move with the one that is shown or closed.
 * <p>
 * A new layout type is a new subclass, returned by {@link #of(ToastLayoutType)}.
 */
abstract class ToastArranger {

    private static final ToastArranger LIST = new ListArranger(false);
    private static final ToastArranger BANNER = new ListArranger(true);
    private static final ToastArranger STACK = new StackArranger();
    private static final ToastArranger REPLACE = new ReplaceArranger();

    // how much of its width a toast moves when it is shown from the side
    private static final float HORIZONTAL_DISTANCE = 0.7f;

    static ToastArranger of(ToastLayoutType layoutType) {
        switch (layoutType) {
            case STACK:
                return STACK;
            case REPLACE:
                return REPLACE;
            case BANNER:
                return BANNER;
            default:
                return LIST;
        }
    }

    /**
     * @param area   the area of the layer the toasts are placed in
     * @param toasts the toasts, the newest first. The options of the group are taken from the newest
     * @param expand how far the group is expanded by the mouse over it, from 0 to 1. Not every layout type
     *               can be expanded
     */
    abstract void arrange(Rectangle area, List<ToastPanel> toasts, boolean leftToRight, float expand);

    /**
     * @return the margin of the group, with the left and the right side as they are on the screen
     */
    static Insets getMargin(ToastPanel newest, boolean leftToRight) {
        Insets margin = UIScale.scale(newest.getOption().getMargin());
        if (!leftToRight) {
            int left = margin.left;
            margin.left = margin.right;
            margin.right = left;
        }
        return margin;
    }

    /**
     * @return the preferred size of the toast, not wider than the space there is: a message that is too long
     * takes more lines
     */
    static Dimension getSize(ToastPanel toast, int availableWidth) {
        return toast.getToastSize(Math.max(availableWidth, 0));
    }

    /**
     * @param x where the toast starts in its area
     * @return x, or where a toast that is wider than its area starts to stay on the screen. Only a toast with
     * a window of its own can be wider than its area
     */
    static int keepOnScreen(ToastPanel toast, Rectangle area, int x, int width) {
        Rectangle screen = toast.getScreenLimit();
        if (screen == null) {
            return x;
        }
        // inside the area it is not moved, also when the area itself is not on the screen
        int left = Math.min(area.x, screen.x);
        int right = Math.max(area.x + area.width, screen.x + screen.width);
        return Math.max(Math.min(x, right - width), left);
    }

    /**
     * @param verticalOnly true if the toast can not come from the side, as in a stack
     * @return how far the toast is from its place while it is shown or closed
     */
    static Point getOffset(ToastPanel toast, Dimension size, boolean leftToRight, boolean verticalOnly) {
        ToastLocation location = toast.getOption().getLocation();
        ToastDirection direction = toast.getOption().getAnimationOption().getDirection();
        if (direction == ToastDirection.AUTO) {
            if (location.isCenter() || verticalOnly) {
                direction = location.isTop() ? ToastDirection.TOP_TO_BOTTOM : ToastDirection.BOTTOM_TO_TOP;
            } else {
                boolean left = location.getAlignment(leftToRight) == 0;
                direction = left == leftToRight ? ToastDirection.LEADING_TO_TRAILING : ToastDirection.TRAILING_TO_LEADING;
            }
        }
        float distance = 1f - toast.getProgress();
        switch (direction) {
            case TOP_TO_BOTTOM:
                return new Point(0, -Math.round(size.height * distance));
            case BOTTOM_TO_TOP:
                return new Point(0, Math.round(size.height * distance));
            case LEADING_TO_TRAILING:
                return new Point(Math.round(size.width * HORIZONTAL_DISTANCE * distance) * (leftToRight ? -1 : 1), 0);
            case TRAILING_TO_LEADING:
                return new Point(Math.round(size.width * HORIZONTAL_DISTANCE * distance) * (leftToRight ? 1 : -1), 0);
            default:
                return new Point();
        }
    }

    /**
     * @return how visible the toast is by its own animation
     */
    static float getAlpha(ToastPanel toast) {
        return toast.getOption().getAnimationOption().isFade() ? toast.getProgress() : 1;
    }
}
