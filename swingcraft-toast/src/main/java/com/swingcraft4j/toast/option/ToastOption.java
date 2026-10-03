package com.swingcraft4j.toast.option;

import com.swingcraft4j.toast.ToastListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The options of a toast: where it is placed, how the toasts are arranged and when it closes. The animation
 * and the style are in their own {@link AnimationOption} and {@link StyleOption}. The setters return the
 * option, so they can be chained. All sizes are unscaled and get scaled with the UI scale factor.
 * <p>
 * The option is copied when the toast is shown, so changing it afterwards
 * does not affect the toasts already showing.
 * <p>
 * The toasts with the same location and layout type are arranged together. They should use the same
 * margin, gap and stack options: these are taken from the newest toast.
 */
public class ToastOption {

    // layout
    private Surface surface = Surface.LAYER;
    private ToastLocation location = ToastLocation.TOP_CENTER;
    private Insets margin = new Insets(10, 10, 10, 10);
    private ToastLayoutType layoutType = ToastLayoutType.LIST;
    private int gap = 10;
    private int listMaxVisible;
    private ListOverflow listOverflow = ListOverflow.WAIT;
    private int stackOffset = 10;
    private int stackMaxVisible = 3;
    private boolean stackExpandOnHover;
    private boolean relativeToOwner;

    // closing
    private boolean autoClose = true;
    private int delay = 3000;
    private boolean pauseOnHover = true;
    private boolean closeOnClick;
    private boolean groupRepeated;
    private boolean cancellable;
    private List<ToastListener> listeners = new ArrayList<>();

    private AnimationOption animationOption = new AnimationOption();
    private StyleOption styleOption = new StyleOption();

    public Surface getSurface() {
        return surface;
    }

    public ToastLocation getLocation() {
        return location;
    }

    public Insets getMargin() {
        return (Insets) margin.clone();
    }

    public ToastLayoutType getLayoutType() {
        return layoutType;
    }

    public int getGap() {
        return gap;
    }

    public int getListMaxVisible() {
        return listMaxVisible;
    }

    public ListOverflow getListOverflow() {
        return listOverflow;
    }

    public int getStackOffset() {
        return stackOffset;
    }

    public int getStackMaxVisible() {
        return stackMaxVisible;
    }

    public boolean isStackExpandOnHover() {
        return stackExpandOnHover;
    }

    public boolean isRelativeToOwner() {
        return relativeToOwner;
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    public int getDelay() {
        return delay;
    }

    public boolean isPauseOnHover() {
        return pauseOnHover;
    }

    public boolean isCloseOnClick() {
        return closeOnClick;
    }

    public boolean isGroupRepeated() {
        return groupRepeated;
    }

    public boolean isCancellable() {
        return cancellable;
    }

    public List<ToastListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public AnimationOption getAnimationOption() {
        return animationOption;
    }

    public StyleOption getStyleOption() {
        return styleOption;
    }

    /**
     * @param surface {@link Surface#LAYER} to show the toast inside the window of the owner (default), or
     *                {@link Surface#WINDOW} to show it in a window of its own, where it is not limited by
     *                the window of the owner and is over its heavyweight components
     */
    public ToastOption setSurface(Surface surface) {
        if (surface == null) {
            throw new IllegalArgumentException("surface must not null");
        }
        this.surface = surface;
        return this;
    }

    public ToastOption setLocation(ToastLocation location) {
        if (location == null) {
            throw new IllegalArgumentException("location must not null");
        }
        this.location = location;
        return this;
    }

    public ToastOption setMargin(int margin) {
        return setMargin(margin, margin, margin, margin);
    }

    /**
     * The space between the window edges and the toasts (the shadow is not counted).
     */
    public ToastOption setMargin(int top, int left, int bottom, int right) {
        this.margin = new Insets(top, left, bottom, right);
        return this;
    }

    public ToastOption setLayoutType(ToastLayoutType layoutType) {
        if (layoutType == null) {
            throw new IllegalArgumentException("layout type must not null");
        }
        this.layoutType = layoutType;
        return this;
    }

    /**
     * @param gap the space between the toasts of a {@link ToastLayoutType#LIST}, and of an expanded stack
     */
    public ToastOption setGap(int gap) {
        this.gap = gap;
        return this;
    }

    /**
     * @param listMaxVisible how many toasts of a {@link ToastLayoutType#LIST} or {@link ToastLayoutType#BANNER}
     *                       are shown at the same time, 0 for no limit (default). What happens with a toast
     *                       over the limit is set with {@link #setListOverflow(ListOverflow)}
     */
    public ToastOption setListMaxVisible(int listMaxVisible) {
        if (listMaxVisible < 0) {
            throw new IllegalArgumentException("list max visible must be >= 0");
        }
        this.listMaxVisible = listMaxVisible;
        return this;
    }

    /**
     * @param listOverflow what happens with a toast over the limit of its list: it waits (default) and is
     *                     shown when a toast closes, its delay starts when it is shown. Or it is shown at once
     *                     and the oldest toast is closed
     */
    public ToastOption setListOverflow(ListOverflow listOverflow) {
        if (listOverflow == null) {
            throw new IllegalArgumentException("list overflow must not null");
        }
        this.listOverflow = listOverflow;
        return this;
    }

    /**
     * @param stackOffset how far the edge of each older toast of a {@link ToastLayoutType#STACK} is visible
     *                    behind the toast in front of it
     */
    public ToastOption setStackOffset(int stackOffset) {
        if (stackOffset < 0) {
            throw new IllegalArgumentException("stack offset must be >= 0");
        }
        this.stackOffset = stackOffset;
        return this;
    }

    /**
     * @param stackMaxVisible how many toasts of a {@link ToastLayoutType#STACK} are visible. The older
     *                        ones are hidden until the toasts in front of them close
     */
    public ToastOption setStackMaxVisible(int stackMaxVisible) {
        if (stackMaxVisible < 1) {
            throw new IllegalArgumentException("stack max visible must be >= 1");
        }
        this.stackMaxVisible = stackMaxVisible;
        return this;
    }

    /**
     * @param stackExpandOnHover true to show the toasts of a {@link ToastLayoutType#STACK} one after the other
     *                           while the mouse is over the stack, with the gap between them, and all of them:
     *                           also the ones that are hidden behind the maximum. The stack closes again when
     *                           the mouse leaves it
     */
    public ToastOption setStackExpandOnHover(boolean stackExpandOnHover) {
        this.stackExpandOnHover = stackExpandOnHover;
        return this;
    }

    /**
     * @param relativeToOwner true to place the toast in the owner component instead of the whole window.
     *                        The toast follows the owner when it moves, hides while the owner is hidden
     *                        and closes when the owner is removed from the window
     */
    public ToastOption setRelativeToOwner(boolean relativeToOwner) {
        this.relativeToOwner = relativeToOwner;
        return this;
    }

    /**
     * @param autoClose false to keep the toast until it is closed by the user or the application
     */
    public ToastOption setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
        return this;
    }

    /**
     * @param delay the time in milliseconds the toast is shown before it closes itself
     */
    public ToastOption setDelay(int delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay must be >= 0");
        }
        this.delay = delay;
        return this;
    }

    /**
     * @param pauseOnHover true to not close the toast while the mouse is over it, the delay starts again
     *                     when the mouse leaves. In a stack the mouse over one toast pauses all of them
     */
    public ToastOption setPauseOnHover(boolean pauseOnHover) {
        this.pauseOnHover = pauseOnHover;
        return this;
    }

    public ToastOption setCloseOnClick(boolean closeOnClick) {
        this.closeOnClick = closeOnClick;
        return this;
    }

    /**
     * @param cancellable true to give the toast of a task a cancel button while the task runs. It cancels the
     *                    task and closes the toast, see {@link com.swingcraft4j.toast.JToast#showTask} and
     *                    {@link com.swingcraft4j.toast.JToast#showFuture}. Other toasts do not use it
     */
    public ToastOption setCancellable(boolean cancellable) {
        this.cancellable = cancellable;
        return this;
    }

    /**
     * @param groupRepeated true to not show a toast again that is showing already: a toast with the same type
     *                      and message, at the same location with the same layout type. The toast that is
     *                      showing gets a count of how often it was shown, and its delay starts again.
     *                      Not for a toast with a custom component
     */
    public ToastOption setGroupRepeated(boolean groupRepeated) {
        this.groupRepeated = groupRepeated;
        return this;
    }

    public ToastOption addListener(ToastListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not null");
        }
        listeners.add(listener);
        return this;
    }

    public ToastOption removeListener(ToastListener listener) {
        listeners.remove(listener);
        return this;
    }

    /**
     * @param animationOption how the toast is animated, change the current one with {@link #getAnimationOption()}
     */
    public ToastOption setAnimationOption(AnimationOption animationOption) {
        if (animationOption == null) {
            throw new IllegalArgumentException("animation option must not null");
        }
        this.animationOption = animationOption;
        return this;
    }

    /**
     * @param styleOption how the toast looks, change the current one with {@link #getStyleOption()}
     */
    public ToastOption setStyleOption(StyleOption styleOption) {
        if (styleOption == null) {
            throw new IllegalArgumentException("style option must not null");
        }
        this.styleOption = styleOption;
        return this;
    }

    public ToastOption copy() {
        ToastOption option = new ToastOption();
        option.surface = surface;
        option.location = location;
        option.margin = (Insets) margin.clone();
        option.layoutType = layoutType;
        option.gap = gap;
        option.listMaxVisible = listMaxVisible;
        option.listOverflow = listOverflow;
        option.stackOffset = stackOffset;
        option.stackMaxVisible = stackMaxVisible;
        option.stackExpandOnHover = stackExpandOnHover;
        option.relativeToOwner = relativeToOwner;
        option.autoClose = autoClose;
        option.delay = delay;
        option.pauseOnHover = pauseOnHover;
        option.closeOnClick = closeOnClick;
        option.groupRepeated = groupRepeated;
        option.cancellable = cancellable;
        option.listeners = new ArrayList<>(listeners);
        option.animationOption = animationOption.copy();
        option.styleOption = styleOption.copy();
        return option;
    }
}
