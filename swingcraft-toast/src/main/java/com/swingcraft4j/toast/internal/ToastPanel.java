package com.swingcraft4j.toast.internal;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.Animator;
import com.formdev.flatlaf.util.ColorFunctions;
import com.formdev.flatlaf.util.UIScale;
import com.swingcraft4j.toast.ToastController;
import com.swingcraft4j.toast.ToastListener;
import com.swingcraft4j.toast.ToastType;
import com.swingcraft4j.toast.option.AnimationOption;
import com.swingcraft4j.toast.option.BackgroundType;
import com.swingcraft4j.toast.option.ListOverflow;
import com.swingcraft4j.toast.option.ProgressLinePosition;
import com.swingcraft4j.toast.option.StyleOption;
import com.swingcraft4j.toast.option.ToastLayoutType;
import com.swingcraft4j.toast.option.ToastOption;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.util.Objects;

/**
 * One toast: its border and shadow, and inside either the default content (icon, label, message and
 * close button) or a custom component.
 * <p>
 * The toast is shown and closed with an animation: the progress goes from 0 to 1 while it is shown and back
 * to 0 while it closes. The {@link ToastArranger} of its layout type places it by the progress, and tells it
 * how it is presented: how visible, how small and how much of it is hidden behind another toast.
 */
public final class ToastPanel extends JPanel implements ToastController {

    // a message is not made narrower than this, the rest of a toast in a very small area is cut off
    private static final int MINIMUM_TEXT_WIDTH = 40;
    // how often the icon of a toast that is loading is painted
    private static final int LOADING_INTERVAL = 30;
    // how often the progress line is painted
    private static final int PROGRESS_INTERVAL = 16;

    private enum State {
        CREATED, WAITING, OPENING, OPENED, CLOSING, CLOSED
    }

    private final ToastLayer layer;
    private final ToastHost host;
    private final Component owner;
    private ToastType type;
    private final ToastOption option;
    private final String id;
    private final ToastBorder border;
    private final ProgressAnimation animation = new ProgressAnimation();
    private State state = State.CREATED;
    private float progress;
    private Timer closeTimer;
    private boolean paused;

    // the progress line: when the delay to close the toast started, and how much of it is left, from 1 to 0
    private Timer progressTimer;
    private long closeTimerStart;
    private float remaining = 1;

    // the default content: the message, and how often it was shown. No message with a custom component
    private String message;
    private final boolean custom;
    private ToastText text;
    private JLabel labelCount;
    private int count = 1;

    // loading: the icon that turns, and what the cancel button does. No button without action
    private JLabel labelIcon;
    // true if the icon is the one that turns: it is painted over the image of the toast, and not in it
    private boolean turningIcon;
    private boolean paintingSnapshot;
    private Timer loadingTimer;
    private final Runnable cancelAction;

    // how the toast is presented, set by the arranger
    private float alpha = 1;
    private float scale = 1;
    private int hidden;
    private float contentAlpha = 1;

    // where the toast is in the layer, with its shadow. Its own bounds are that only when the layer is its host
    private Rectangle layerBounds = new Rectangle();

    // images of the toast, painted in place of it while it is not presented as it is
    private Snapshot snapshot;
    private Snapshot borderSnapshot;
    // the image the content is painted through in a transparent window
    private Snapshot buffer;

    private ComponentListener ownerBoundsListener;
    private HierarchyBoundsListener ownerAncestorBoundsListener;
    private HierarchyListener ownerHierarchyListener;

    /**
     * @param layer   arranges the toast with the other toasts of the window
     * @param host    puts the toast on the screen: the layer, or a window of its own
     * @param owner   the component the toast is relative to, or null for the whole window
     * @param message the message of the default content, not used with a custom component
     * @param custom  the component to show in place of the default content, or null
     * @param cancelAction the action of the cancel button the toast has while it is loading, or null
     */
    ToastPanel(ToastLayer layer, ToastHost host, Component owner, ToastType type, String message, Component custom, ToastOption option, String id, Runnable cancelAction) {
        this.cancelAction = cancelAction;
        this.layer = layer;
        this.host = host;
        this.owner = owner;
        this.type = type;
        this.option = option;
        this.id = id;
        this.message = message;
        this.custom = custom != null;
        this.border = new ToastBorder(this);
        setOpaque(false);
        setBorder(border);
        if (custom != null) {
            // the padding of the style is in the border
            setLayout(new MigLayout("fill,insets 0"));
            add(custom, "grow");
        } else {
            installContent();
        }
        installMouseListener(this);
    }

    private void installContent() {
        StyleOption style = option.getStyleOption();
        Icon icon = null;
        if (style.isShowIcon()) {
            icon = style.getIcon() != null ? style.getIcon() : ToastIcon.of(type, getColor());
        }
        // the line is between the icon and the text, so only when there are both
        boolean hasText = style.isShowLabel() || (message != null && !message.isEmpty());
        boolean separateLine = style.isIconSeparateLine() && icon != null && hasText;
        // the line has its own column after the icon, an empty column would still take a gap
        int textColumn = separateLine ? 2 : 1;

        // one cell for each part. The label and the message are in the same cell, and so are the count
        // and the close button: the count in a column of its own would take a gap while it is hidden.
        // The padding of the style is in the border
        setLayout(new MigLayout("fill,insets 0,hidemode 3", separateLine ? "[][][grow,fill][]" : "[][grow,fill][]", "[center]"));

        labelIcon = null;
        turningIcon = icon != null && style.getIcon() == null && type == ToastType.LOADING;
        if (icon != null) {
            labelIcon = new JLabel(icon) {
                @Override
                public void paint(Graphics g) {
                    // an image of the toast would show the icon as it was when the image was made
                    if (!(paintingSnapshot && turningIcon)) {
                        super.paint(g);
                    }
                }
            };
            installMouseListener(labelIcon);
            add(labelIcon, "cell 0 0");
        }
        if (separateLine) {
            // as high as the content
            add(new JSeparator(JSeparator.VERTICAL), "cell 1 0,growy");
        }
        if (style.isShowLabel()) {
            JLabel label = new JLabel(style.getLabel() != null ? style.getLabel() : type.getLabel());
            label.putClientProperty(FlatClientProperties.STYLE, "font:bold");
            label.setForeground(getColor());
            installMouseListener(label);
            // close to the message, as lines of one text
            add(label, "cell " + textColumn + " 0,flowy,gapbottom 0");
        }
        text = new ToastText(message);
        if (style.isPaintTextColor()) {
            text.setForeground(getColor());
        }
        installMouseListener(text);
        add(text, "cell " + textColumn + " 0");

        // how often the message was shown, visible from the second time
        labelCount = new JLabel();
        labelCount.putClientProperty(FlatClientProperties.STYLE, "font:bold");
        labelCount.setForeground(getColor());
        labelCount.setVisible(false);
        installMouseListener(labelCount);
        add(labelCount, "cell " + (textColumn + 1) + " 0");

        if (cancelAction != null && type == ToastType.LOADING) {
            // as a link, a button would make the toast higher
            String text = UIManager.getString("OptionPane.cancelButtonText");
            JButton buttonCancel = new JButton(text != null ? text : "Cancel");
            buttonCancel.setFocusable(false);
            buttonCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            buttonCancel.putClientProperty(FlatClientProperties.STYLE, "" +
                    "margin:1,5,1,5;" +
                    "borderWidth:0;" +
                    "focusWidth:0;" +
                    "innerFocusWidth:0;" +
                    "background:null;" +
                    "foreground:$Component.accentColor;");
            buttonCancel.addActionListener(e -> {
                if (isOpen() && this.type == ToastType.LOADING) {
                    cancelAction.run();
                }
            });
            add(buttonCancel, "cell " + (textColumn + 1) + " 0");
        }

        if (style.isShowCloseButton()) {
            JButton buttonClose = new JButton(ToastIcon.close());
            buttonClose.setFocusable(false);
            buttonClose.putClientProperty(FlatClientProperties.STYLE, "" +
                    "arc:999;" +
                    "margin:3,3,3,3;" +
                    "borderWidth:0;" +
                    "focusWidth:0;" +
                    "innerFocusWidth:0;" +
                    "background:null;");
            buttonClose.addActionListener(e -> close());
            add(buttonClose, "cell " + (textColumn + 1) + " 0");
        }
    }

    /**
     * The click on the toast. The parts of the default content that get the mouse get the listener too,
     * a custom component gets it for every part of it that does not use the mouse itself.
     */
    private void installMouseListener(Component component) {
        component.addMouseListener(new MouseAdapter() {

            private boolean pressed;

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = SwingUtilities.isLeftMouseButton(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), ToastPanel.this);
                if (pressed && isOpen() && contains(point)) {
                    clicked();
                }
                pressed = false;
            }
        });
    }

    private void clicked() {
        for (ToastListener listener : option.getListeners()) {
            listener.toastClicked(this);
        }
        if (option.isCloseOnClick()) {
            close();
        }
    }

    private void installOwnerListener() {
        ownerBoundsListener = new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                layer.revalidate();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                layer.revalidate();
            }
        };
        ownerAncestorBoundsListener = new HierarchyBoundsAdapter() {
            @Override
            public void ancestorMoved(HierarchyEvent e) {
                layer.revalidate();
            }

            @Override
            public void ancestorResized(HierarchyEvent e) {
                layer.revalidate();
            }
        };
        ownerHierarchyListener = e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                setVisible(owner.isShowing());
                layer.revalidate();
                // nobody sees a toast that is hidden with its owner, its delay starts when it is shown again
                updateCloseTimer();
            }
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !owner.isDisplayable()) {
                // later, the owner may only be moving to another parent
                SwingUtilities.invokeLater(() -> {
                    if (!owner.isDisplayable()) {
                        close();
                    }
                });
            }
        };
        owner.addComponentListener(ownerBoundsListener);
        owner.addHierarchyBoundsListener(ownerAncestorBoundsListener);
        owner.addHierarchyListener(ownerHierarchyListener);
        setVisible(owner.isShowing());
    }

    private void uninstallOwnerListener() {
        owner.removeComponentListener(ownerBoundsListener);
        owner.removeHierarchyBoundsListener(ownerAncestorBoundsListener);
        owner.removeHierarchyListener(ownerHierarchyListener);
    }

    /**
     * @param open true for the time to show the toast, false for the time to close it
     * @return the time for the animation, 0 when the toast is not animated or nobody can see it
     */
    private int getDuration(boolean open) {
        AnimationOption animationOption = option.getAnimationOption();
        if (!animationOption.isEnabled() || !Animator.useAnimation() || !isShowing()) {
            return 0;
        }
        return open ? animationOption.getOpenDuration() : animationOption.getCloseDuration();
    }

    void open() {
        if (owner != null) {
            installOwnerListener();
        }
        if (!layer.isFull(this)) {
            start();
        } else if (option.getListOverflow() == ListOverflow.CLOSE_OLDEST) {
            start();
            layer.closeOldest(this);
        } else {
            // shown later, when a toast that is showing closes
            state = State.WAITING;
            layer.addWaiting(this);
        }
    }

    /**
     * Shows the toast now.
     */
    void start() {
        state = State.OPENING;
        layer.addToast(this);
        updateLoadingTimer();
        animation.start(getDuration(true), 0, 1, option.getAnimationOption().getEasing(), this::setProgress, this::opened);
    }

    private void opened() {
        state = State.OPENED;
        updateCloseTimer();
    }

    private void setProgress(float progress) {
        this.progress = progress;
        layer.arrange();
    }

    @Override
    public void close() {
        close(true);
    }

    @Override
    public void closeImmediately() {
        if (state == State.CLOSING) {
            animation.finish();
        } else {
            close(false);
        }
    }

    private void close(boolean animate) {
        if (!isOpen()) {
            return;
        }
        if (state == State.WAITING) {
            // it was never shown
            layer.removeWaiting(this);
            released();
            return;
        }
        state = State.CLOSING;
        updateCloseTimer();
        updateLoadingTimer();
        // its place is free for a toast that waits
        layer.toastClosing(this);
        // if the toast is still opening, it goes back from where it is
        int duration = animate ? Math.round(getDuration(false) * progress) : 0;
        animation.start(duration, progress, 0, option.getAnimationOption().getEasing(), this::setProgress, this::closed);
    }

    private void closed() {
        layer.removeToast(this);
        endSnapshot();
        released();
    }

    private void released() {
        state = State.CLOSED;
        updateLoadingTimer();
        if (owner != null) {
            uninstallOwnerListener();
        }
        // release a custom component, so it can be shown again
        removeAll();
        for (ToastListener listener : option.getListeners()) {
            listener.toastClosed(this);
        }
    }

    /**
     * Jumps to the end of the animation that is running.
     */
    void finishAnimation() {
        animation.finish();
    }

    /**
     * @param paused true to not close the toast by itself, the delay starts again when it is not paused any more
     */
    void setPaused(boolean paused) {
        if (this.paused != paused) {
            this.paused = paused;
            updateCloseTimer();
        }
    }

    /**
     * The timer that closes the toast runs while the toast is completely shown, not paused and not hidden
     * with its owner.
     */
    private void updateCloseTimer() {
        // a toast that is loading waits for its result
        boolean run = state == State.OPENED && option.isAutoClose() && !paused && type != ToastType.LOADING && isVisible();
        if (run && closeTimer == null) {
            closeTimer = new Timer(option.getDelay(), e -> close());
            closeTimer.setRepeats(false);
            closeTimer.start();
            closeTimerStarted();
            if (option.getStyleOption().isShowProgressLine()) {
                progressTimer = new Timer(PROGRESS_INTERVAL, e -> updateRemaining());
                progressTimer.start();
            }
        } else if (!run && closeTimer != null) {
            closeTimer.stop();
            closeTimer = null;
            if (progressTimer != null) {
                progressTimer.stop();
                progressTimer = null;
            }
            if (isOpen()) {
                // paused: the delay starts again later
                setRemaining(1);
            } else {
                // closing: the line stays where it is
                updateRemaining();
            }
        }
    }

    /**
     * The delay to close the toast starts again, if it is running.
     */
    private void restartCloseTimer() {
        if (closeTimer != null) {
            closeTimer.restart();
            closeTimerStarted();
        }
    }

    private void closeTimerStarted() {
        closeTimerStart = System.nanoTime();
        setRemaining(1);
    }

    /**
     * Sets how much of the delay is left by the time that has passed.
     */
    private void updateRemaining() {
        float delay = option.getDelay();
        float elapsed = (System.nanoTime() - closeTimerStart) / 1000000f;
        setRemaining(delay <= 0 ? 0 : Math.max(1 - elapsed / delay, 0));
    }

    private void setRemaining(float remaining) {
        if (this.remaining != remaining) {
            this.remaining = remaining;
            if (hasProgressLine()) {
                // only the line
                Insets shadow = getShadowInsets();
                int lineHeight = UIScale.scale(option.getStyleOption().getProgressLineSize()) + 2;
                boolean top = option.getStyleOption().getProgressLinePosition() == ProgressLinePosition.TOP;
                repaint(0, top ? shadow.top : getHeight() - shadow.bottom - lineHeight, getWidth(), lineHeight);
            }
        }
    }

    private boolean hasProgressLine() {
        return option.getStyleOption().isShowProgressLine() && option.isAutoClose() && type != ToastType.LOADING;
    }

    /**
     * The icon that turns is not in the image of the toast, so it also turns while the toast is animated:
     * with a long animation it would stand still for all that time.
     */
    private void paintTurningIcon(Graphics g) {
        if (turningIcon && labelIcon != null && labelIcon.isVisible()) {
            Icon icon = labelIcon.getIcon();
            int x = labelIcon.getX() + (labelIcon.getWidth() - icon.getIconWidth()) / 2;
            int y = labelIcon.getY() + (labelIcon.getHeight() - icon.getIconHeight()) / 2;
            icon.paintIcon(this, g, x, y);
        }
    }

    /**
     * The icon of a toast that is loading turns while the toast is shown: it is painted again and again.
     */
    private void updateLoadingTimer() {
        boolean run = type == ToastType.LOADING && (state == State.OPENING || state == State.OPENED) && isDisplayable();
        if (run && loadingTimer == null) {
            loadingTimer = new Timer(LOADING_INTERVAL, e -> {
                if (labelIcon != null) {
                    labelIcon.repaint();
                }
            });
            loadingTimer.start();
        } else if (!run && loadingTimer != null) {
            loadingTimer.stop();
            loadingTimer = null;
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        updateLoadingTimer();
    }

    private void paintProgressLine(Graphics g) {
        if (hasProgressLine() && remaining > 0) {
            border.paintProgressLine(this, g, remaining);
        }
    }

    /**
     * @return true if a toast with the type and the message would show the same as this toast
     */
    boolean isRepeatOf(ToastType type, String message) {
        return !custom && this.type == type && Objects.equals(this.message, message);
    }

    /**
     * The message was shown again: counts it, and the delay starts again.
     */
    void repeat() {
        count++;
        labelCount.setText("\u00d7" + count);
        labelCount.setVisible(true);
        // the toast is wider with the count, and its images are old
        endSnapshot();
        revalidate();
        if (state != State.WAITING) {
            layer.arrange();
        }
        restartCloseTimer();
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        update(type, message);
    }

    @Override
    public void setType(ToastType type) {
        update(type, message);
    }

    @Override
    public void update(ToastType type, String message) {
        if (type == null) {
            throw new IllegalArgumentException("type must not null");
        }
        if (custom) {
            // the message is not shown
            message = null;
        }
        if (!isOpen() || (this.type == type && Objects.equals(this.message, message))) {
            return;
        }
        boolean typeChanged = this.type != type;
        this.type = type;
        this.message = message;
        if (!custom) {
            // another message, it was not shown before. The parts of the content depend on the type:
            // a type has an icon or not
            count = 1;
            removeAll();
            installContent();
        }
        // the toast has another size and other colors, and its images are old
        endSnapshot();
        revalidate();
        repaint();
        if (typeChanged) {
            showAgainInFront();
        }
        if (state != State.WAITING) {
            layer.arrange();
        }
        // a toast that was loading starts its delay now, and one that is loading now has none
        updateCloseTimer();
        updateLoadingTimer();
        restartCloseTimer();
    }

    /**
     * In a stack only the toast in front shows its content. A toast behind another that gets another type, as
     * the toast of a task that is done, would change without anybody seeing it. So it comes to the front,
     * shown again as a new toast is: the toasts that were in front of it go one step back.
     * <p>
     * Not for another message only: a task that tells each of its steps would come to the front all the time.
     */
    private void showAgainInFront() {
        if (option.getLayoutType() != ToastLayoutType.STACK || (state != State.OPENING && state != State.OPENED)) {
            return;
        }
        if (layer.bringToFront(this)) {
            state = State.OPENING;
            // from the start, before it is placed again
            progress = 0;
            animation.start(getDuration(true), 0, 1, option.getAnimationOption().getEasing(), this::setProgress, this::opened);
        }
    }

    @Override
    public boolean isOpen() {
        return state == State.WAITING || state == State.OPENING || state == State.OPENED;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ToastType getType() {
        return type;
    }

    ToastOption getOption() {
        return option;
    }

    ToastHost getHost() {
        return host;
    }

    /**
     * @return where the toast is in the layer, with its shadow
     */
    Rectangle getLayerBounds() {
        return new Rectangle(layerBounds);
    }

    /**
     * @return the part of the screen the toast stays in when it is wider than its area, in the layer.
     * Null if the toast is in the layer, or the screen is not known
     */
    Rectangle getScreenLimit() {
        return host.isWindow() ? layer.getScreenLimit() : null;
    }

    Component getOwner() {
        return owner;
    }

    /**
     * @return how far the toast is shown: 0 is not at all, 1 is completely
     */
    float getProgress() {
        return progress;
    }

    /**
     * @return the color of the toast: the color of its type, or the color of the style
     */
    Color getColor() {
        Color color = option.getStyleOption().getColor();
        return color != null ? color : type.getColor();
    }

    Insets getShadowInsets() {
        return border.getShadowInsets();
    }

    /**
     * @param availableWidth the width there is for the visible part of the toast
     * @return the preferred size of the visible part of the toast, without the shadow, and not wider than
     * the width there is and the maximum width of the style. A message that does not fit goes on at the
     * next line: the toast is higher, and its other parts, as the close button, stay inside it
     */
    Dimension getToastSize(int availableWidth) {
        Insets shadow = getShadowInsets();
        int shadowWidth = shadow.left + shadow.right;
        int maxWidth = option.getStyleOption().getMaxWidth();
        // a banner is as wide as the area, and a custom component can not go on at the next line
        Rectangle screen = getScreenLimit();
        if (screen != null && option.getLayoutType() != ToastLayoutType.BANNER) {
            // in a window of its own the toast is not limited by its area, but by the screen
            Insets margin = UIScale.scale(option.getMargin());
            availableWidth = Math.max(availableWidth, screen.width - (margin.left + margin.right));
        }
        if (maxWidth > 0 && text != null && option.getLayoutType() != ToastLayoutType.BANNER) {
            availableWidth = Math.min(availableWidth, UIScale.scale(maxWidth));
        }
        if (text != null) {
            text.setWidthLimit(0);
            // the layout keeps the sizes of the components until it is invalid
            invalidate();
        }
        Dimension size = getPreferredSize();
        int tooWide = size.width - shadowWidth - availableWidth;
        if (text != null && tooWide > 0) {
            text.setWidthLimit(Math.max(text.getPreferredSize().width - tooWide, MINIMUM_TEXT_WIDTH));
            invalidate();
            size = getPreferredSize();
        }
        int width = Math.max(Math.min(size.width - shadowWidth, availableWidth), 0);
        return new Dimension(width, size.height - (shadow.top + shadow.bottom));
    }

    /**
     * Sets the bounds of the visible part of the toast, the shadow is placed around it.
     */
    void setToastBounds(int x, int y, int width, int height) {
        Insets shadow = getShadowInsets();
        layerBounds = new Rectangle(x - shadow.left, y - shadow.top, width + shadow.left + shadow.right, height + shadow.top + shadow.bottom);
        host.place(this, layerBounds);
    }

    /**
     * Sets how the toast is painted in its bounds.
     *
     * @param alpha        how visible the toast is, from 0 to 1
     * @param scale        the size of the toast, 1 is its size and less is smaller. A smaller toast keeps
     *                     its far edge: the bottom at a top location, the top at a bottom location
     * @param hidden       how much of the visible part is not painted at the near edge, because it would be
     *                     outside the toast in front
     * @param contentAlpha how visible the content of the toast is, from 0 to 1. Of a toast behind another
     *                     only the background is visible
     */
    void setPresentation(float alpha, float scale, int hidden, float contentAlpha) {
        alpha = Math.max(0, Math.min(alpha, 1));
        contentAlpha = Math.max(0, Math.min(contentAlpha, 1));
        if (this.alpha != alpha || this.scale != scale || this.hidden != hidden || this.contentAlpha != contentAlpha) {
            this.alpha = alpha;
            this.scale = scale;
            this.hidden = hidden;
            this.contentAlpha = contentAlpha;
            repaint();
        }
        if (isPresentedAsItIs()) {
            endSnapshot();
        } else {
            startSnapshot();
        }
    }

    float getAlpha() {
        return alpha;
    }

    float getScale() {
        return scale;
    }

    float getContentAlpha() {
        return contentAlpha;
    }

    private boolean isPresentedAsItIs() {
        return alpha >= 1 && scale == 1 && hidden <= 0 && contentAlpha >= 1;
    }

    /**
     * Paints the toast to images. The text of the real components does not look the same half visible,
     * and it can not be painted smaller without moving.
     * <p>
     * On a screen with a scale such as 125% the images are made again when the toast has moved to another
     * part of a pixel: the text and the shadow are rounded from there, and the old images would show them
     * one pixel off. They would jump when the animation is done and the real components are painted.
     */
    private void startSnapshot() {
        Rectangle content = getContentArea();
        if (snapshot != null && snapshot.isFor(this, content.x, content.y, content.width, content.height)
                && borderSnapshot != null && borderSnapshot.isFor(this, 0, 0, getWidth(), getHeight())) {
            return;
        }
        endSnapshot();
        if (!isShowing()) {
            return;
        }
        // the content has the size of the toast before it is painted
        validate();
        Rectangle area = getContentArea();
        paintingSnapshot = true;
        try {
            snapshot = Snapshot.create(this, area.x, area.y, area.width, area.height, false, g -> {
                g.translate(-area.x, -area.y);
                // the background of the content, it can be a gradient
                paintBorder(g);
                super.paintChildren(g);
            });
        } finally {
            paintingSnapshot = false;
        }
        borderSnapshot = Snapshot.create(this, 0, 0, getWidth(), getHeight(), true, this::paintBorder);
        if (snapshot == null || borderSnapshot == null) {
            endSnapshot();
        }
    }

    private void endSnapshot() {
        if (snapshot != null) {
            snapshot.flush();
            snapshot = null;
        }
        if (borderSnapshot != null) {
            borderSnapshot.flush();
            borderSnapshot = null;
        }
    }

    /**
     * @return the area inside the border, where the content is
     */
    private Rectangle getContentArea() {
        Insets insets = getInsets();
        return new Rectangle(insets.left, insets.top,
                Math.max(getWidth() - (insets.left + insets.right), 0),
                Math.max(getHeight() - (insets.top + insets.bottom), 0));
    }

    /**
     * @return the part of the toast that is painted: without the shadow and what is hidden
     */
    private Rectangle getPaintedArea() {
        Insets shadow = getShadowInsets();
        int height = Math.max(getHeight() - (shadow.top + shadow.bottom) - hidden, 0);
        int y = option.getLocation().isTop() ? shadow.top + hidden : shadow.top;
        return new Rectangle(shadow.left, y, Math.max(getWidth() - (shadow.left + shadow.right), 0), height);
    }

    @Override
    public void paint(Graphics g) {
        if (isPresentedAsItIs()) {
            super.paint(g);
            paintProgressLine(g);
            return;
        }
        if (alpha <= 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            if (hidden > 0) {
                // with the shadow at the sides and the far edge
                Rectangle painted = getPaintedArea();
                boolean top = option.getLocation().isTop();
                g2.clipRect(0, top ? painted.y : 0, getWidth(), top ? getHeight() - painted.y : painted.y + painted.height);
            }
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            if (snapshot == null) {
                // no image, for example while the window is not on a screen
                super.paint(g2);
                g2.setComposite(AlphaComposite.SrcOver.derive(alpha * contentAlpha));
            } else if (scale == 1) {
                Rectangle area = getContentArea();
                paintBorderSnapshot(g2, area, false);
                g2.setComposite(AlphaComposite.SrcOver.derive(alpha * contentAlpha));
                snapshot.paint(g2, area.x, area.y);
            } else {
                // smaller around the middle of the far edge
                Rectangle area = getContentArea();
                Insets shadow = getShadowInsets();
                double anchorX = shadow.left + (getWidth() - (shadow.left + shadow.right)) / 2.0;
                double anchorY = option.getLocation().isTop() ? getHeight() - shadow.bottom : shadow.top;
                g2.translate(anchorX, anchorY);
                g2.scale(scale, scale);
                g2.translate(-anchorX, -anchorY);
                paintBorderSnapshot(g2, area, true);
                g2.setComposite(AlphaComposite.SrcOver.derive(alpha * contentAlpha));
                snapshot.paintScaled(g2, area.x, area.y);
            }
            // with the content, and not in the images: they change all the time
            if (snapshot != null) {
                paintTurningIcon(g2);
            }
            paintProgressLine(g2);
        } finally {
            g2.dispose();
        }
    }

    /**
     * Text painted on a transparent window does not look the same as the text of a window that is not
     * transparent. So in a window of its own the content is painted to an image that is not transparent, with
     * its background, as in a snapshot. The image is only as large as what is painted, and it is used again
     * for the next time.
     */
    @Override
    protected void paintChildren(Graphics g) {
        Rectangle area = getContentArea();
        Rectangle clip = g.getClipBounds();
        clip = clip == null ? area : clip.intersection(area);
        Snapshot painted = null;
        if (host.isWindow() && !clip.isEmpty()) {
            Rectangle paintedClip = clip;
            painted = Snapshot.create(this, clip.x, clip.y, clip.width, clip.height, false, g2 -> {
                g2.translate(-paintedClip.x, -paintedClip.y);
                g2.setClip(paintedClip);
                paintBorder(g2);
                super.paintChildren(g2);
            }, buffer);
        }
        if (painted != null) {
            buffer = painted;
            painted.paint(g, clip.x, clip.y);
        } else {
            super.paintChildren(g);
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // the window disposed, do not keep it alive with the timer
        updateLoadingTimer();
        if (buffer != null) {
            buffer.flush();
            buffer = null;
        }
    }

    /**
     * Paints the image of the border, with the shadow and the background. The image of the content has the
     * background too, so a toast that is half visible would show its background two times where the content
     * is: the content would be more visible than the rest of the toast, as a box in it. So the border is not
     * painted behind the content, unless the content is less visible than the toast: then the background is
     * seen through it.
     */
    private void paintBorderSnapshot(Graphics2D g, Rectangle area, boolean scaled) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            if (contentAlpha >= 1) {
                // the image of the content is cut to the same area, so they meet without a line between them
                Area outside = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
                outside.subtract(new Area(area));
                g2.clip(outside);
            }
            if (scaled) {
                borderSnapshot.paintScaled(g2, 0, 0);
            } else {
                borderSnapshot.paint(g2, 0, 0);
            }
        } finally {
            g2.dispose();
        }
    }

    @Override
    protected boolean isPaintingOrigin() {
        // when a component of the toast repaints itself, paint the image and not that component
        return !isPresentedAsItIs() || host.isWindow();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        // the images have the colors of the old look and feel
        if (snapshot != null) {
            endSnapshot();
            revalidate();
        }
    }

    /**
     * The background is not set, it follows the look and feel and the color of the toast.
     */
    @Override
    public Color getBackground() {
        Color base = UIManager.getColor("TextArea.background");
        if (base == null) {
            return super.getBackground();
        }
        if (option == null || option.getStyleOption().getBackgroundType() != BackgroundType.TINT) {
            // a little different from the window behind
            return ColorFunctions.mix(FlatLaf.isLafDark() ? Color.WHITE : Color.BLACK, base, 0.03f);
        }
        return ColorFunctions.mix(getColor(), base, 0.1f);
    }

    /**
     * @return true if the point is over what is painted of the toast: not the shadow and not what is hidden
     */
    boolean isOver(int x, int y) {
        return alpha > 0 && getPaintedArea().contains(x, y);
    }

    @Override
    public boolean contains(int x, int y) {
        // a toast behind another has no content to click, the mouse goes to what is behind it
        return contentAlpha > 0 && isOver(x, y);
    }
}
