package com.swingcraft4j.modal.internal;

import com.formdev.flatlaf.util.Animator;
import com.swingcraft4j.modal.Modal;
import com.swingcraft4j.modal.ModalController;
import com.swingcraft4j.modal.option.BackgroundMode;
import com.swingcraft4j.modal.option.ModalOption;

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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * One open modal: the background that covers the window (or the owner) and the modal panel on top of it.
 * The panel shows the top modal of the stack, the modals below were covered by {@link #push(Modal)}.
 * <p>
 * It is placed on the screen by its {@link ModalHost}: as a child of the layer of the window, or as the
 * content of a window of its own. All it does is the same on both.
 * <p>
 * The modal is shown and closed with an animation: the progress goes from 0 to 1 while it opens and back
 * to 0 while it closes. The background fades with the progress and the layout moves the panel with it.
 */
public final class ModalContainer extends JComponent implements ModalController {

    private enum State {
        CREATED, OPENING, OPENED, CLOSING, CLOSED
    }

    // can not be seen, but enough for the window to get the mouse
    private static final float WINDOW_MINIMUM_OPACITY = 0.01f;

    private final ModalRoot root;
    private final ModalHost host;
    private final Component owner;
    private final ModalOption option;
    private final String id;
    private final ModalPanel panel;
    private final ModalLayout layout;
    private final Deque<Modal> modals = new ArrayDeque<>();
    private final ProgressAnimation showAnimation = new ProgressAnimation();
    private final ProgressAnimation slideAnimation = new ProgressAnimation();
    private State state = State.CREATED;
    private float progress;
    private JComponent inputBlocker;
    // an image of what is behind this modal, painted in place of the real components while animating
    private Snapshot backgroundSnapshot;
    private Component previousFocusOwner;
    private ComponentListener ownerBoundsListener;
    private HierarchyBoundsListener ownerAncestorBoundsListener;
    private HierarchyListener ownerHierarchyListener;

    /**
     * @param owner the component the modal is relative to, or null for the whole window
     */
    ModalContainer(ModalRoot root, ModalHost host, Component owner, Modal modal, ModalOption option, String id) {
        this.root = root;
        this.host = host;
        this.owner = owner;
        this.option = option;
        this.id = id;
        panel = new ModalPanel(option, host.isWindow());
        layout = new ModalLayout(panel, option, host.isWindow());
        setLayout(layout);
        add(panel);
        modals.push(modal);
        panel.setModal(modal);
        if (option.getBackgroundMode().isBlocking()) {
            installBackgroundListener();
        }
        if (option.isMovable()) {
            installMoveListener();
        }
    }

    /**
     * A modal can only be in one place. If it is still in a modal that is closing with an animation,
     * the closing is finished now, so the modal can be shown again without waiting.
     */
    static void release(Modal modal) {
        ModalContainer container = (ModalContainer) SwingUtilities.getAncestorOfClass(ModalContainer.class, modal);
        if (container != null && container.state == State.CLOSING) {
            container.slideAnimation.finish();
            container.showAnimation.finish();
        }
    }

    private void installBackgroundListener() {
        boolean closeOnClick = option.getBackgroundMode() == BackgroundMode.CLOSE_ON_CLICK;
        // the listener blocks the mouse event from the components behind, even if it does nothing
        addMouseListener(new MouseAdapter() {

            private boolean pressed;

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = SwingUtilities.isLeftMouseButton(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (pressed && closeOnClick && getComponentAt(e.getPoint()) == ModalContainer.this) {
                    close();
                }
                pressed = false;
            }
        });
    }

    /**
     * The panel gets the mouse event of every part of the modal that does not use the mouse itself.
     */
    private void installMoveListener() {
        MouseAdapter listener = new MouseAdapter() {

            private Point pressed;
            private Point pressedOffset;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    pressed = e.getLocationOnScreen();
                    pressedOffset = layout.getOffset();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (pressed != null) {
                    Point point = e.getLocationOnScreen();
                    layout.setOffset(pressedOffset.x + point.x - pressed.x, pressedOffset.y + point.y - pressed.y);
                    host.updateBounds(ModalContainer.this);
                    doLayout();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = null;
            }
        };
        panel.addMouseListener(listener);
        panel.addMouseMotionListener(listener);
    }

    private void installOwnerListener() {
        ownerBoundsListener = new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                root.getLayer().revalidate();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                root.getLayer().revalidate();
            }
        };
        ownerAncestorBoundsListener = new HierarchyBoundsAdapter() {
            @Override
            public void ancestorMoved(HierarchyEvent e) {
                root.getLayer().revalidate();
            }

            @Override
            public void ancestorResized(HierarchyEvent e) {
                root.getLayer().revalidate();
            }
        };
        ownerHierarchyListener = e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                setVisible(owner.isShowing());
                host.updateBounds(this);
                if (owner.isShowing()) {
                    focusShownAgain();
                }
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

    /**
     * The modal was hidden with its owner and is shown again: the focus comes back to it. The focus has left
     * the modal when it was hidden, and what shows the owner again, as a tabbed pane, gives it to a component
     * of the owner: the keyboard would use what is behind the blocking background.
     * <p>
     * A window of its own gets the focus back by itself.
     */
    private void focusShownAgain() {
        if (host.isWindow() || !option.getBackgroundMode().isBlocking()) {
            return;
        }
        // later, after the focus was given to the component of the owner
        SwingUtilities.invokeLater(() -> {
            if (state != State.OPENED || !isShowing()) {
                return;
            }
            Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, this)) {
                return;
            }
            Component focus = panel.getFocusTraversalPolicy().getDefaultComponent(panel);
            if (focus != null) {
                focus.requestFocusInWindow();
            }
        });
    }

    private void uninstallOwnerListener() {
        owner.removeComponentListener(ownerBoundsListener);
        owner.removeHierarchyBoundsListener(ownerAncestorBoundsListener);
        owner.removeHierarchyListener(ownerHierarchyListener);
    }

    /**
     * @return the area this modal covers, in the layer of the window: the visible part of the owner, or the
     * whole layer. In a window of its own the modal covers the same area, the window is placed over it
     */
    Rectangle getArea() {
        ModalLayer layer = root.getLayer();
        Rectangle layerArea = new Rectangle(0, 0, layer.getWidth(), layer.getHeight());
        if (owner == null) {
            return layerArea;
        }
        Rectangle ownerArea = owner instanceof JComponent
                ? ((JComponent) owner).getVisibleRect()
                : new Rectangle(0, 0, owner.getWidth(), owner.getHeight());
        Rectangle area = SwingUtilities.convertRectangle(owner, ownerArea, layer).intersection(layerArea);
        area.width = Math.max(area.width, 0);
        area.height = Math.max(area.height, 0);
        return area;
    }

    /**
     * @return the time for an animation, 0 when the modal is not animated or nobody can see it
     */
    private int getDuration(int duration) {
        // not if this component is showing: a window of its own is shown after the modal is ready for the first frame
        boolean seen = isVisible() && root.getLayer().isShowing();
        return option.getAnimationOption().isEnabled() && Animator.useAnimation() && seen ? duration : 0;
    }

    /**
     * Sets how far the modal is shown: 0 is not at all, 1 is completely.
     */
    private void setProgress(float progress) {
        this.progress = progress;
        layout.setAnimationProgress(progress);
        // only used while an image of the modal is painted
        panel.setSnapshotState(option.getAnimationOption().isFade() ? progress : 1, 1 - option.getAnimationOption().getScale() * (1 - progress));
        doLayout();
        if (option.getBackgroundMode().isBlocking()) {
            // the background fades
            repaint();
        } else {
            // nothing but the modal changes, do not paint the components behind the rest of the area
            panel.repaint();
        }
    }

    /**
     * Keeps the mouse away from the modal while it is closing or sliding to another modal.
     * Otherwise a second click on a button runs its action again.
     */
    private void setInputBlocked(boolean blocked) {
        if (blocked == (inputBlocker != null)) {
            return;
        }
        if (blocked) {
            inputBlocker = new JComponent() {
            };
            inputBlocker.addMouseListener(new MouseAdapter() {
            });
            add(inputBlocker, 0);
            inputBlocker.setBounds(panel.getBounds());
        } else {
            remove(inputBlocker);
            inputBlocker = null;
        }
    }

    /**
     * @return true if images are animated in place of the real components
     */
    private boolean usesSnapshot() {
        return option.getAnimationOption().isSnapshot() || option.getAnimationOption().isFade() || option.getAnimationOption().getScale() > 0;
    }

    /**
     * Paints the modal and what is behind it to images. Until {@link #endSnapshot()} the images are painted,
     * so each frame of the animation does not paint all the real components again.
     */
    private void startSnapshot() {
        panel.startSnapshot();
        // a fade and a zoom need the image of the modal only
        if (option.getAnimationOption().isSnapshot() && backgroundSnapshot == null && option.getBackgroundMode().isBlocking()) {
            backgroundSnapshot = host.createBackgroundSnapshot(this);
        }
    }

    private void endSnapshot() {
        panel.endSnapshot();
        if (backgroundSnapshot != null) {
            backgroundSnapshot.flush();
            backgroundSnapshot = null;
            repaint();
        }
    }

    /**
     * @return the image of what is behind, or null if there is none or it can not be used any more
     */
    private Snapshot getBackgroundSnapshot() {
        if (backgroundSnapshot != null && !backgroundSnapshot.hasSize(getWidth(), getHeight())) {
            // the window was resized, go on with the real components
            backgroundSnapshot.flush();
            backgroundSnapshot = null;
        }
        return backgroundSnapshot;
    }

    /**
     * With the image this component paints its whole area, so the components behind are not painted.
     */
    @Override
    public boolean isOpaque() {
        return getBackgroundSnapshot() != null;
    }

    @Override
    public void doLayout() {
        super.doLayout();
        if (inputBlocker != null) {
            inputBlocker.setBounds(panel.getBounds());
        }
    }

    void open() {
        state = State.OPENING;
        previousFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (owner != null) {
            installOwnerListener();
        }
        setProgress(0);
        host.addContainer(this);
        int duration = getDuration(option.getAnimationOption().getDuration());
        if (duration > 0 && usesSnapshot()) {
            // the image of the modal is made for the location where the modal ends, see Snapshot
            setProgress(1);
            startSnapshot();
            setProgress(0);
        } else if (duration == 0) {
            setProgress(1);
        }
        // a window of its own is painted the moment it is shown, so it is shown now: with what is seen first
        host.updateBounds(this);
        showAnimation.start(duration, 0, 1, this::setProgress, this::opened);
    }

    private void opened() {
        state = State.OPENED;
        endSnapshot();
        modalShown(modals.peek());
    }

    private void modalShown(Modal modal) {
        Component focus = panel.getFocusTraversalPolicy().getDefaultComponent(panel);
        if (focus != null) {
            focus.requestFocusInWindow();
        }
        modal.modalOpened();
    }

    /**
     * Shows the top modal of the stack in place of the modal that is showing.
     *
     * @param forward true to slide to a pushed modal, false to slide back to the modal before
     * @param popped  the modal that was removed from the stack, or null
     */
    private void slideToTopModal(boolean forward, Modal popped) {
        Modal modal = modals.peek();
        modal.applyComponentOrientation(getComponentOrientation());
        // a modal that is not completely open yet is changed without animation
        int duration = state == State.OPENED ? getDuration(option.getAnimationOption().getSlideDuration()) : 0;
        setInputBlocked(true);
        panel.startSlide(modal, forward);
        // a window of its own is as large as both modals need
        host.updateBounds(this);
        slideAnimation.start(duration, 0, 1, this::setSlideProgress, () -> {
            panel.endSlide();
            // the size of the panel follows the modal
            host.updateBounds(this);
            validate();
            panel.repaint();
            if (state != State.CLOSING) {
                setInputBlocked(false);
            }
            if (popped != null) {
                popped.modalClosed();
            }
            // modalClosed may have closed everything
            if (state == State.OPENED) {
                modalShown(modal);
            }
        });
    }

    private void setSlideProgress(float slideProgress) {
        panel.setSlideProgress(slideProgress);
        validate();
        panel.repaint();
    }

    @Override
    public void push(Modal modal) {
        if (modal == null) {
            throw new IllegalArgumentException("modal must not null");
        }
        if (!isOpen()) {
            throw new IllegalStateException("modal is closed");
        }
        // a slide that is still running jumps to its end
        slideAnimation.finish();
        release(modal);
        if (modal.getParent() != null || modals.contains(modal)) {
            throw new IllegalStateException("modal is already showing");
        }
        modals.push(modal);
        slideToTopModal(true, null);
    }

    @Override
    public void pop() {
        slideAnimation.finish();
        if (!canPop()) {
            return;
        }
        Modal modal = modals.pop();
        slideToTopModal(false, modal);
    }

    @Override
    public boolean canPop() {
        return isOpen() && modals.size() > 1;
    }

    @Override
    public void close() {
        close(true);
    }

    @Override
    public void closeImmediately() {
        if (state == State.CLOSING) {
            slideAnimation.finish();
            showAnimation.finish();
        } else {
            close(false);
        }
    }

    private void close(boolean animate) {
        if (!isOpen()) {
            return;
        }
        slideAnimation.finish();
        // the callback of the slide may have closed the modal
        if (!isOpen()) {
            return;
        }
        state = State.CLOSING;
        setInputBlocked(true);
        restoreFocus();
        int duration = animate ? Math.round(getDuration(option.getAnimationOption().getDuration()) * progress) : 0;
        if (duration > 0 && usesSnapshot()) {
            startSnapshot();
        }
        // if the modal is still opening, it goes back from where it is
        showAnimation.start(duration, progress, 0, this::setProgress, this::closed);
    }

    /**
     * The focus leaves the modal when it starts to close, so the keyboard can not use it any more.
     */
    private void restoreFocus() {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        boolean hasFocus = focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, this);
        if (hasFocus) {
            boolean hasPrevious = previousFocusOwner != null && previousFocusOwner.isShowing();
            if (host.isWindow()) {
                // the focus goes to another window
                Component focus = hasPrevious ? previousFocusOwner : root.getOwnerWindow();
                if (focus != null) {
                    focus.requestFocus();
                }
            } else if (hasPrevious) {
                previousFocusOwner.requestFocusInWindow();
            }
        }
        previousFocusOwner = null;
    }

    private void closed() {
        state = State.CLOSED;
        if (owner != null) {
            uninstallOwnerListener();
        }
        host.removeContainer(this);
        setInputBlocked(false);
        endSnapshot();
        // release the modals, so they can be shown again
        panel.setModal(null);
        List<Modal> closedModals = new ArrayList<>(modals);
        modals.clear();
        for (Modal modal : closedModals) {
            modal.modalClosed();
        }
    }

    @Override
    public boolean isOpen() {
        return state == State.OPENING || state == State.OPENED;
    }

    @Override
    public String getId() {
        return id;
    }

    ModalOption getOption() {
        return option;
    }

    ModalHost getHost() {
        return host;
    }

    /**
     * @return the bounds this component needs to show the background and the modal in every place of its
     * animations, relative to its area of the given size. Only larger than the area, or outside of it, when
     * the host lets the modal out of its area
     *
     * @param screen the part of the screen the modal stays in when it leaves its area, relative to the area
     */
    Rectangle getReach(int areaWidth, int areaHeight, Rectangle screen) {
        Rectangle reach = layout.getReach(this, areaWidth, areaHeight, screen);
        if (option.getBackgroundMode().isBlocking()) {
            reach = reach.union(new Rectangle(0, 0, areaWidth, areaHeight));
        }
        return reach;
    }

    /**
     * @param area   where the area of the modal is in this component. By default the whole component
     * @param screen the part of the screen the modal stays in when it leaves its area, relative to the area
     */
    void setLayoutArea(Rectangle area, Rectangle screen) {
        if (layout.setArea(area, screen)) {
            invalidate();
            repaint();
        }
    }

    /**
     * @return true if the point of the screen is on this modal, or on its blocking background
     */
    boolean containsScreenPoint(Point screenPoint) {
        if (!isShowing()) {
            return false;
        }
        Point point = new Point(screenPoint);
        SwingUtilities.convertPointFromScreen(point, this);
        return contains(point.x, point.y);
    }

    /**
     * Without a blocking background only the modal itself is part of this component,
     * so the mouse (event and cursor) works on the components behind as if nothing is there.
     */
    @Override
    public boolean contains(int x, int y) {
        // only inside this component: a part of the modal outside of it is not painted
        if (!super.contains(x, y)) {
            return false;
        }
        if (option.getBackgroundMode().isBlocking() && layout.getArea(this).contains(x, y)) {
            return true;
        }
        return panel.contains(x - panel.getX(), y - panel.getY());
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        // the input blocker is over the panel
        return inputBlocker == null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Snapshot background = getBackgroundSnapshot();
        if (background != null) {
            background.paint(g, 0, 0);
        }
        float opacity = option.getBackgroundOpacity() * progress;
        if (host.isWindow()) {
            // the mouse goes through the parts of a transparent window where nothing is painted
            opacity = Math.max(opacity, WINDOW_MINIMUM_OPACITY);
        }
        if (option.getBackgroundMode().isBlocking() && opacity > 0) {
            Graphics2D g2 = (Graphics2D) g.create();
            Color color = option.getBackgroundColor();
            Rectangle area = layout.getArea(this);
            g2.setColor(color != null ? color : Color.BLACK);
            g2.setComposite(AlphaComposite.SrcOver.derive(Math.min(opacity, 1f)));
            g2.fillRect(area.x, area.y, area.width, area.height);
            g2.dispose();
        }
    }
}
