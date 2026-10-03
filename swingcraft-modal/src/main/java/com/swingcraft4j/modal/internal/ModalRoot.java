package com.swingcraft4j.modal.internal;

import com.swingcraft4j.modal.option.BackgroundMode;
import com.swingcraft4j.modal.option.ModalOption;
import com.swingcraft4j.modal.option.Surface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * All open modals of one window, whatever they are shown on. It is created when the first modal is shown
 * and removed when the last modal is closed.
 * <p>
 * Each modal is placed on the screen by a {@link ModalHost}. The root has the {@link ModalLayer} of the
 * window, that is the host of the modals shown inside the window, and creates a {@link ModalWindow} for
 * each modal that is shown in a window of its own. What does not depend on the host is done here:
 * the order of the modals, the escape key and the click outside a popup modal.
 */
final class ModalRoot {

    private static final String CLIENT_PROPERTY = "JModal.root";

    private final RootPaneContainer window;
    private final ModalLayer layer;
    // the top modal first. The modals with a window of their own are over the modals in the layer
    private final List<ModalContainer> containers = new ArrayList<>();
    private final AWTEventListener outsideClickListener;
    private boolean outsideClickInstalled;

    /**
     * @return the root of the window, it is created if the window has no open modal
     */
    static ModalRoot of(RootPaneContainer window) {
        ModalRoot root = (ModalRoot) window.getRootPane().getClientProperty(CLIENT_PROPERTY);
        if (root == null) {
            root = new ModalRoot(window);
            root.install();
        }
        return root;
    }

    private ModalRoot(RootPaneContainer window) {
        this.window = window;
        layer = new ModalLayer(this, window);
        outsideClickListener = event -> {
            if (event.getID() == MouseEvent.MOUSE_PRESSED) {
                mousePressed((MouseEvent) event);
            }
        };
    }

    private void install() {
        layer.install();
        window.getRootPane().putClientProperty(CLIENT_PROPERTY, this);
        ModalManager.rootInstalled(this);
    }

    private void uninstall() {
        layer.uninstall();
        window.getRootPane().putClientProperty(CLIENT_PROPERTY, null);
        ModalManager.rootUninstalled(this);
    }

    /**
     * The layer is in the window as long as a modal is open, also when all of them have a window of their own:
     * it is where the area of each modal is measured, and it has the escape key of the window.
     */
    ModalLayer getLayer() {
        return layer;
    }

    ComponentOrientation getComponentOrientation() {
        return window.getRootPane().getComponentOrientation();
    }

    /**
     * @return the window of the screen the modals are shown in, or null if it is not in a window
     */
    Window getOwnerWindow() {
        return window instanceof Window ? (Window) window : SwingUtilities.getWindowAncestor(window.getRootPane());
    }

    /**
     * @return true if a modal with a window of its own is open. A modal in the layer would be behind it
     */
    boolean hasWindowModal() {
        for (ModalContainer container : containers) {
            if (container.isOpen() && container.getHost().isWindow()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return what shows a modal with the option
     */
    ModalHost createHost(ModalOption option) {
        if (option.getSurface() == Surface.WINDOW) {
            Window owner = getOwnerWindow();
            if (owner != null && ModalWindow.isSupported(owner)) {
                return new ModalWindow(this, owner);
            }
        }
        return layer;
    }

    /**
     * Called by the host when it has shown the container.
     */
    void containerAdded(ModalContainer container) {
        int index = 0;
        if (!container.getHost().isWindow()) {
            while (index < containers.size() && containers.get(index).getHost().isWindow()) {
                index++;
            }
        }
        containers.add(index, container);
        updateOutsideClickListener();
    }

    /**
     * Called by the host when it has removed the container.
     */
    void containerRemoved(ModalContainer container) {
        containers.remove(container);
        updateOutsideClickListener();
        if (containers.isEmpty()) {
            uninstall();
        }
    }

    /**
     * @return the open modals, the top one first
     */
    List<ModalContainer> getContainers() {
        return new ArrayList<>(containers);
    }

    /**
     * The window was moved, resized, shown or hidden: all modals are placed again.
     */
    void updateBounds() {
        for (ModalContainer container : getContainers()) {
            container.getHost().updateBounds(container);
        }
    }

    /**
     * @return true if the container is the top one of the modals that block the window. The keyboard must
     * stay in it, as the mouse does
     */
    boolean isTopBlocking(ModalContainer container) {
        for (ModalContainer other : containers) {
            if (other.isVisible() && other.isOpen() && other.getOption().getBackgroundMode().isBlocking()) {
                return other == container;
            }
        }
        return false;
    }

    void escapePressed() {
        for (ModalContainer container : getContainers()) {
            // hidden with its owner, or already closing
            if (!container.isVisible() || !container.isOpen()) {
                continue;
            }
            if (container.getOption().isCloseOnEscape()) {
                container.close();
                return;
            }
            // do not close the modal behind a blocking modal
            if (container.getOption().getBackgroundMode().isBlocking()) {
                return;
            }
        }
    }

    /**
     * The popup modals do not get the mouse event of the background,
     * so the clicks outside are watched with a global listener while one of them is open.
     */
    void updateOutsideClickListener() {
        boolean install = false;
        if (layer.isDisplayable()) {
            for (ModalContainer container : containers) {
                if (container.getOption().getBackgroundMode() == BackgroundMode.POPUP) {
                    install = true;
                    break;
                }
            }
        }
        if (install != outsideClickInstalled) {
            outsideClickInstalled = install;
            if (install) {
                Toolkit.getDefaultToolkit().addAWTEventListener(outsideClickListener, AWTEvent.MOUSE_EVENT_MASK);
            } else {
                Toolkit.getDefaultToolkit().removeAWTEventListener(outsideClickListener);
            }
        }
    }

    private void mousePressed(MouseEvent e) {
        Component source = e.getComponent();
        if (source == null || !layer.isShowing()) {
            return;
        }
        // only clicks in the window of this root, and in the windows of its modals, close the modal
        Window sourceWindow = source instanceof Window ? (Window) source : SwingUtilities.getWindowAncestor(source);
        if (sourceWindow == null || !isOwnWindow(sourceWindow)) {
            return;
        }
        // clicks in a popup shown over the modal (combo box, popup menu) are not outside clicks
        Component target = SwingUtilities.getDeepestComponentAt(source, e.getX(), e.getY());
        if (target instanceof JPopupMenu || SwingUtilities.getAncestorOfClass(JPopupMenu.class, target) != null) {
            return;
        }
        Point point = e.getLocationOnScreen();
        for (ModalContainer container : getContainers()) {
            if (!container.isVisible()) {
                continue;
            }
            // the click is on this modal, the modals behind it are covered
            if (container.containsScreenPoint(point)) {
                return;
            }
            if (container.getOption().getBackgroundMode() == BackgroundMode.POPUP) {
                container.close();
            }
        }
    }

    private boolean isOwnWindow(Window sourceWindow) {
        if (sourceWindow == SwingUtilities.getWindowAncestor(layer)) {
            return true;
        }
        return sourceWindow instanceof ModalWindow && ((ModalWindow) sourceWindow).getRoot() == this;
    }
}
