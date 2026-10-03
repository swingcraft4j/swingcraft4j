package com.swingcraft4j.toast.internal;

import com.formdev.flatlaf.util.Animator;
import com.swingcraft4j.toast.ToastType;
import com.swingcraft4j.toast.option.Surface;
import com.swingcraft4j.toast.option.ToastLayoutType;
import com.swingcraft4j.toast.option.ToastOption;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The layer of the toasts of one window. It is added to the layered pane of the window, above the
 * content pane and the modals, when the first toast is shown and removed when the last toast is closed.
 * <p>
 * It arranges all toasts of the window, in its own coordinates: the toasts with the same owner, location
 * and layout type are a group, arranged by a {@link ToastArranger}. Each toast is put on the screen by its
 * {@link ToastHost}. The layer is the host of the toasts that are shown inside the window, they are its
 * children with the newest first. A toast with a window of its own has a {@link ToastWindow} as host, that
 * is placed where the toast would be in the layer.
 */
final class ToastLayer extends JComponent implements ToastHost {

    private static final String CLIENT_PROPERTY = "JToast.layer";
    // above the modal layer, below the popup layer
    private static final Integer LAYER = JLayeredPane.MODAL_LAYER + 50;
    // how often it is checked if the mouse is over a toast
    private static final int HOVER_INTERVAL = 100;

    // how many batches are running, and the layers to arrange when the last one ends
    private static int batches;
    private static final Set<ToastLayer> batchLayers = new LinkedHashSet<>();

    private final RootPaneContainer window;
    private final ComponentListener boundsListener;
    private final Timer hoverTimer;
    // the location of the mouse in this layer, or null. Replaced by the tests
    Supplier<Point> mouseLocation = this::getMouseLocation;
    private Container contentPane;
    // the toasts that are shown, on every host. The newest first
    private final List<ToastPanel> toasts = new ArrayList<>();
    // the part of the screen a toast with a window of its own stays in, in the layer. Null if not known
    private Rectangle screenLimit;
    // the toasts over the limit of their list, shown when a toast closes. The oldest first
    private final List<ToastPanel> waiting = new ArrayList<>();
    // how far each group is expanded by the mouse over it, by the key of the group
    private final Map<List<Object>, Expansion> expansions = new HashMap<>();

    /**
     * How far a group is expanded: 0 is not, 1 is completely. It changes with an animation.
     */
    private static final class Expansion {

        private final ProgressAnimation animation = new ProgressAnimation();
        private boolean expanded;
        private float progress;
    }

    /**
     * @return the layer of the window, or null if the window has no toast and {@code create} is false
     */
    static ToastLayer of(RootPaneContainer window, boolean create) {
        ToastLayer layer = (ToastLayer) window.getRootPane().getClientProperty(CLIENT_PROPERTY);
        if (layer == null && create) {
            layer = new ToastLayer(window);
            layer.install();
        }
        return layer;
    }

    private ToastLayer(RootPaneContainer window) {
        this.window = window;
        boundsListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateBounds();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                updateBounds();
            }
        };
        hoverTimer = new Timer(HOVER_INTERVAL, e -> updateHover(mouseLocation.get()));
        // the toasts with a window of their own follow the window on the screen, and are shown and hidden with it
        addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
            @Override
            public void ancestorMoved(HierarchyEvent e) {
                if (hasWindowToast()) {
                    arrange();
                }
            }
        });
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && hasWindowToast()) {
                arrange();
            }
        });
    }

    private boolean hasWindowToast() {
        for (ToastPanel toast : toasts) {
            if (toast.getHost().isWindow()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the window of the screen the toasts are shown in, or null if the layer is not in a window
     */
    private Window getOwnerWindow() {
        return window instanceof Window ? (Window) window : SwingUtilities.getWindowAncestor(window.getRootPane());
    }

    /**
     * @return what shows a toast with the option
     */
    ToastHost createHost(ToastOption option) {
        if (option.getSurface() == Surface.WINDOW) {
            Window owner = getOwnerWindow();
            if (owner != null && ToastWindow.isSupported(owner)) {
                return new ToastWindow(this, owner);
            }
        }
        return this;
    }

    @Override
    public void attach(ToastPanel toast) {
        add(toast, 0);
    }

    @Override
    public void detach(ToastPanel toast) {
        Rectangle bounds = toast.getBounds();
        remove(toast);
        repaint(bounds);
    }

    @Override
    public void place(ToastPanel toast, Rectangle bounds) {
        toast.setBounds(bounds);
    }

    @Override
    public void updateVisible(ToastPanel toast) {
        // a child of the layer is seen when it is visible
    }

    @Override
    public void toFront(ToastPanel toast) {
        setComponentZOrder(toast, 0);
    }

    @Override
    public boolean isWindow() {
        return false;
    }

    private void install() {
        JLayeredPane layeredPane = window.getLayeredPane();
        contentPane = window.getContentPane();
        layeredPane.addComponentListener(boundsListener);
        contentPane.addComponentListener(boundsListener);
        layeredPane.add(this, LAYER);
        updateBounds();
        window.getRootPane().putClientProperty(CLIENT_PROPERTY, this);
        ToastManager.layerInstalled(this);
    }

    private void uninstall() {
        JLayeredPane layeredPane = window.getLayeredPane();
        layeredPane.removeComponentListener(boundsListener);
        contentPane.removeComponentListener(boundsListener);
        layeredPane.remove(this);
        layeredPane.repaint(getX(), getY(), getWidth(), getHeight());
        window.getRootPane().putClientProperty(CLIENT_PROPERTY, null);
        ToastManager.layerUninstalled(this);
    }

    /**
     * The layer covers the content pane, but not the menu bar and the title bar.
     */
    private void updateBounds() {
        JLayeredPane layeredPane = window.getLayeredPane();
        Rectangle bounds = contentPane.getParent() == layeredPane
                ? contentPane.getBounds()
                : new Rectangle(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
        if (!bounds.equals(getBounds())) {
            setBounds(bounds);
            revalidate();
            repaint();
        }
    }

    void addToast(ToastPanel toast) {
        List<ToastPanel> replaced = new ArrayList<>();
        if (toast.getOption().getLayoutType() == ToastLayoutType.REPLACE) {
            for (ToastPanel other : getToasts()) {
                if (other.isOpen() && getKey(other).equals(getKey(toast))) {
                    replaced.add(other);
                }
            }
        }
        toasts.add(0, toast);
        toast.getHost().attach(toast);
        toast.applyComponentOrientation(window.getRootPane().getComponentOrientation());
        arrange();
        updateHoverTimer();
        // after the new toast is added: the layer is removed when it has no toast
        for (ToastPanel other : replaced) {
            other.close();
        }
    }

    /**
     * @return true if the list the toast belongs to shows as many toasts as its limit allows,
     * or other toasts wait for a place already
     */
    boolean isFull(ToastPanel toast) {
        int maxVisible = toast.getOption().getListMaxVisible();
        ToastLayoutType layoutType = toast.getOption().getLayoutType();
        if ((layoutType != ToastLayoutType.LIST && layoutType != ToastLayoutType.BANNER) || maxVisible <= 0) {
            return false;
        }
        List<Object> key = getKey(toast);
        for (ToastPanel other : waiting) {
            if (getKey(other).equals(key)) {
                return true;
            }
        }
        return countOpen(key) >= maxVisible;
    }

    /**
     * @return how many toasts of the group are shown and not closing
     */
    private int countOpen(List<Object> key) {
        int count = 0;
        for (ToastPanel other : getToasts()) {
            if (other.isOpen() && getKey(other).equals(key)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Closes the oldest toasts of the list of the toast, until the list is not over its limit any more.
     */
    void closeOldest(ToastPanel toast) {
        List<Object> key = getKey(toast);
        int maxVisible = toast.getOption().getListMaxVisible();
        List<ToastPanel> toasts = getToasts();
        for (int i = toasts.size() - 1; i >= 0 && countOpen(key) > maxVisible; i--) {
            ToastPanel other = toasts.get(i);
            if (other != toast && other.isOpen() && getKey(other).equals(key)) {
                other.close();
            }
        }
    }

    /**
     * @return the toast that shows the message already, at the same place, or null
     */
    ToastPanel findRepeated(Component owner, ToastType type, String message, ToastOption option) {
        List<Object> key = Arrays.asList(owner, option.getLocation(), option.getLayoutType());
        for (ToastPanel toast : getAllToasts()) {
            if (toast.isOpen() && getKey(toast).equals(key) && toast.isRepeatOf(type, message)) {
                return toast;
            }
        }
        return null;
    }

    /**
     * Makes the toast the newest of the window, so it is in front of the toasts of its group.
     *
     * @return false if no toast of its group was in front of it: nothing has changed
     */
    boolean bringToFront(ToastPanel toast) {
        List<Object> key = getKey(toast);
        boolean behind = false;
        for (ToastPanel other : toasts) {
            if (other == toast) {
                break;
            }
            // a toast that is closing goes away by itself
            if (other.isOpen() && getKey(other).equals(key)) {
                behind = true;
                break;
            }
        }
        if (!behind) {
            return false;
        }
        toasts.remove(toast);
        toasts.add(0, toast);
        toast.getHost().toFront(toast);
        return true;
    }

    void addWaiting(ToastPanel toast) {
        waiting.add(toast);
    }

    void removeWaiting(ToastPanel toast) {
        waiting.remove(toast);
    }

    /**
     * A toast starts to close: the oldest toast that waits for a place in its list is shown.
     */
    void toastClosing(ToastPanel toast) {
        List<Object> key = getKey(toast);
        for (ToastPanel next : waiting) {
            if (getKey(next).equals(key)) {
                if (countOpen(key) < next.getOption().getListMaxVisible()) {
                    waiting.remove(next);
                    next.start();
                }
                return;
            }
        }
    }

    /**
     * @return what makes the group of a toast: the toasts with the same key are arranged together
     */
    private static List<Object> getKey(ToastPanel toast) {
        return Arrays.asList(toast.getOwner(), toast.getOption().getLocation(), toast.getOption().getLayoutType());
    }

    void removeToast(ToastPanel toast) {
        toasts.remove(toast);
        toast.getHost().detach(toast);
        if (toasts.isEmpty() && waiting.isEmpty()) {
            uninstall();
        } else {
            arrange();
        }
        updateHoverTimer();
    }

    /**
     * @return the toasts that are shown, the newest first
     */
    List<ToastPanel> getToasts() {
        return new ArrayList<>(toasts);
    }

    /**
     * @return the toasts that are shown and the ones that wait to be shown
     */
    List<ToastPanel> getAllToasts() {
        List<ToastPanel> toasts = getToasts();
        toasts.addAll(waiting);
        return toasts;
    }

    /**
     * @return the toasts that are arranged together, the hidden ones are not in a group
     */
    private Map<List<Object>, List<ToastPanel>> getGroups() {
        Map<List<Object>, List<ToastPanel>> groups = new LinkedHashMap<>();
        for (ToastPanel toast : getToasts()) {
            if (toast.isVisible()) {
                List<Object> key = getKey(toast);
                List<ToastPanel> group = groups.get(key);
                if (group == null) {
                    group = new ArrayList<>();
                    groups.put(key, group);
                }
                group.add(toast);
            }
        }
        return groups;
    }

    /**
     * Places the toasts again, for example for the next step of an animation.
     */
    void arrange() {
        if (batches > 0) {
            // placed when the batch ends
            batchLayers.add(this);
            return;
        }
        invalidate();
        validate();
    }

    /**
     * Runs what changes many toasts at the same time, as a frame of their animations. The toasts are not
     * placed for each change, but one time when all of it is done: to place the toasts of a large group
     * takes time, and nobody sees the steps between.
     */
    static void batch(Runnable changes) {
        batches++;
        try {
            changes.run();
        } finally {
            if (--batches == 0 && !batchLayers.isEmpty()) {
                List<ToastLayer> changed = new ArrayList<>(batchLayers);
                batchLayers.clear();
                for (ToastLayer layer : changed) {
                    // not a layer that was removed with its last toast
                    if (!layer.toasts.isEmpty()) {
                        layer.arrange();
                    }
                }
            }
        }
    }

    @Override
    public void doLayout() {
        // of the window: the layer is created later and does not get the orientation that was set before
        boolean leftToRight = window.getRootPane().getComponentOrientation().isLeftToRight();
        screenLimit = hasWindowToast() ? findScreenLimit() : null;
        Map<List<Object>, List<ToastPanel>> groups = getGroups();
        // forget the groups that have no toast any more
        expansions.keySet().retainAll(groups.keySet());
        for (Map.Entry<List<Object>, List<ToastPanel>> entry : groups.entrySet()) {
            List<ToastPanel> group = entry.getValue();
            ToastPanel newest = group.get(0);
            Expansion expansion = expansions.get(entry.getKey());
            ToastArranger.of(newest.getOption().getLayoutType()).arrange(
                    getArea(newest.getOwner()), group, leftToRight, expansion == null ? 0 : expansion.progress);
        }
        // when every toast is placed and knows how it is presented: a window is painted the moment it is shown
        for (ToastPanel toast : getToasts()) {
            toast.getHost().updateVisible(toast);
        }
    }

    /**
     * @return the screen the window is on, without the task bar, in the coordinates of this layer.
     * Null while the layer is not on a screen
     */
    private Rectangle findScreenLimit() {
        GraphicsConfiguration configuration = getGraphicsConfiguration();
        if (configuration == null || !isShowing()) {
            return null;
        }
        Rectangle bounds = configuration.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
        Point location = getLocationOnScreen();
        return new Rectangle(bounds.x + insets.left - location.x, bounds.y + insets.top - location.y,
                bounds.width - (insets.left + insets.right), bounds.height - (insets.top + insets.bottom));
    }

    /**
     * @return the part of the screen a toast with a window of its own stays in, in this layer, or null
     */
    Rectangle getScreenLimit() {
        return screenLimit;
    }

    /**
     * @return the area of the layer for the toasts of the owner: the visible part of the owner, or the whole layer
     */
    private Rectangle getArea(Component owner) {
        Rectangle layerArea = new Rectangle(0, 0, getWidth(), getHeight());
        if (owner == null) {
            return layerArea;
        }
        Rectangle ownerArea = owner instanceof JComponent
                ? ((JComponent) owner).getVisibleRect()
                : new Rectangle(0, 0, owner.getWidth(), owner.getHeight());
        Rectangle area = SwingUtilities.convertRectangle(owner, ownerArea, this).intersection(layerArea);
        area.width = Math.max(area.width, 0);
        area.height = Math.max(area.height, 0);
        return area;
    }

    /**
     * The mouse is checked with a timer and not with mouse events: a toast with a custom component
     * does not get the events of the components inside it, and a toast can move under a mouse that does not.
     */
    private void updateHoverTimer() {
        boolean run = isDisplayable() && !toasts.isEmpty();
        if (run && !hoverTimer.isRunning()) {
            hoverTimer.start();
        } else if (!run && hoverTimer.isRunning()) {
            hoverTimer.stop();
        }
    }

    /**
     * @return the location of the mouse in this layer, or null if it is not known
     */
    private Point getMouseLocation() {
        if (!isShowing()) {
            return null;
        }
        try {
            PointerInfo pointer = MouseInfo.getPointerInfo();
            if (pointer == null) {
                return null;
            }
            Point point = pointer.getLocation();
            SwingUtilities.convertPointFromScreen(point, this);
            return point;
        } catch (HeadlessException | SecurityException e) {
            return null;
        }
    }

    /**
     * Pauses the toasts the mouse is over. In a stack the mouse over one toast pauses all toasts of the stack,
     * and expands the stack if it has that option.
     *
     * @param mouse the location of the mouse in this layer, or null if it is not over the window
     */
    private void updateHover(Point mouse) {
        for (Map.Entry<List<Object>, List<ToastPanel>> entry : getGroups().entrySet()) {
            List<ToastPanel> group = entry.getValue();
            ToastOption option = group.get(0).getOption();
            boolean stack = option.getLayoutType() == ToastLayoutType.STACK;
            boolean anyHover = false;
            boolean[] hover = new boolean[group.size()];
            // the toasts and what is between them: the mouse in the gap of an expanded stack is still over the stack
            Rectangle groupBounds = null;
            for (int i = 0; i < hover.length; i++) {
                ToastPanel toast = group.get(i);
                // where the toast is in the layer, also when it has a window of its own
                Rectangle bounds = toast.getLayerBounds();
                hover[i] = mouse != null && toast.isOver(mouse.x - bounds.x, mouse.y - bounds.y);
                anyHover |= hover[i];
                if (toast.getAlpha() > 0) {
                    groupBounds = groupBounds == null ? bounds : groupBounds.union(bounds);
                }
            }
            if (stack && option.isStackExpandOnHover()) {
                Expansion expansion = expansions.get(entry.getKey());
                if (expansion != null && expansion.progress > 0) {
                    anyHover = mouse != null && groupBounds != null && groupBounds.contains(mouse);
                }
                setExpanded(entry.getKey(), option, anyHover);
            }
            for (int i = 0; i < hover.length; i++) {
                ToastPanel toast = group.get(i);
                toast.setPaused(toast.getOption().isPauseOnHover() && (stack ? anyHover : hover[i]));
            }
        }
    }

    /**
     * Expands or closes the group, with the animation of the toasts.
     */
    private void setExpanded(List<Object> key, ToastOption option, boolean expanded) {
        Expansion expansion = expansions.get(key);
        if (expansion == null) {
            if (!expanded) {
                return;
            }
            expansion = new Expansion();
            expansions.put(key, expansion);
        }
        if (expansion.expanded != expanded) {
            expansion.expanded = expanded;
            Expansion animated = expansion;
            float to = expanded ? 1 : 0;
            boolean animate = option.getAnimationOption().isEnabled() && Animator.useAnimation() && isShowing();
            int duration = animate ? Math.round(option.getAnimationOption().getDuration() * Math.abs(to - expansion.progress)) : 0;
            expansion.animation.start(duration, expansion.progress, to, option.getAnimationOption().getEasing(), progress -> {
                animated.progress = progress;
                arrange();
            }, () -> {
            });
        }
    }

    @Override
    public boolean contains(int x, int y) {
        // a toast can be outside the layer when there are more toasts than fit. It is not painted there,
        // and it must not get the mouse: it would be over the title bar or the menu bar of the window
        if (!super.contains(x, y)) {
            return false;
        }
        // only the toasts get the mouse, the components behind the rest of the layer stay usable
        for (Component component : getComponents()) {
            if (component.isVisible() && component.contains(x - component.getX(), y - component.getY())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        // the toasts of a stack overlap
        return false;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        updateHoverTimer();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // the window disposed, do not keep it alive with the timer
        updateHoverTimer();
    }
}
