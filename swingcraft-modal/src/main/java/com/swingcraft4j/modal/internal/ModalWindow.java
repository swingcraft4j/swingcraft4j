package com.swingcraft4j.modal.internal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeListener;

/**
 * A window of its own for one modal, and the host of that modal. It is owned by the window the modal is
 * shown in, so it stays over that window and is hidden with it.
 * <p>
 * The window is transparent, and the {@link ModalContainer} paints in it what it paints in the layer: the
 * background over the area of the modal, and the modal with its shadow. The window is as large as all of
 * it, see {@link ModalContainer#getReach}: the modal can be outside its area.
 */
final class ModalWindow extends JWindow implements ModalHost {

    private final ModalRoot root;
    private final WindowFocusListener ownerFocusListener;
    private final PropertyChangeListener focusOwnerListener;
    private ModalContainer container;
    // while the window is placed: placing it does its layout, and its layout places it
    private boolean updating;
    // while an owned window gets its own background
    private boolean settingOwnedBackground;

    /**
     * @return true if a window owned by the owner can be transparent
     */
    static boolean isSupported(Window owner) {
        GraphicsConfiguration configuration = owner.getGraphicsConfiguration();
        return configuration != null
                && configuration.getDevice().isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
    }

    ModalWindow(ModalRoot root, Window owner) {
        super(owner);
        this.root = root;
        // macOS gives a transparent window a shadow in the shape of what was painted first
        getRootPane().putClientProperty("Window.shadow", Boolean.FALSE);
        ContentPane contentPane = new ContentPane();
        // the layer has the escape key of the owner window, but this window has the focus. Not for the focused
        // window as in the layer: such a key is not registered for a window that is not shown yet
        contentPane.registerKeyboardAction(e -> root.escapePressed(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        setContentPane(contentPane);
        setGlassPane(new GlassPane());
        getGlassPane().setVisible(true);
        setBackground(new Color(0, true));
        ownerFocusListener = new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                ownerFocused();
            }
        };
        focusOwnerListener = e -> focusOwnerChanged(e.getOldValue(), e.getNewValue());
    }

    ModalRoot getRoot() {
        return root;
    }

    /**
     * A window has the background of its owner until it gets one of its own. So a window owned by this window
     * (the popup of a combo box, a tooltip, a dialog) would start with a transparent background without being
     * made for it: it is transparent for the system, but its panes still fill it. And when it is then set to
     * transparent, as FlatLaf does for the window that paints the shadow of a popup, nothing changes because
     * it already is: the shadow is painted on a filled window.
     * <p>
     * Only a window without a background of its own asks for this one. It gets a background that is not
     * transparent before it can use the one of this window.
     */
    @Override
    public Color getBackground() {
        Color background = super.getBackground();
        if (settingOwnedBackground || background == null || background.getAlpha() == 255) {
            return settingOwnedBackground ? getOwnedBackground() : background;
        }
        boolean found = false;
        for (Window window : getOwnedWindows()) {
            if (!window.isBackgroundSet()) {
                found = true;
                settingOwnedBackground = true;
                try {
                    window.setBackground(getOwnedBackground());
                } finally {
                    settingOwnedBackground = false;
                }
            }
        }
        return found ? getOwnedBackground() : background;
    }

    private static Color getOwnedBackground() {
        Color color = UIManager.getColor("Panel.background");
        // not a color of the look and feel: those are replaced when the look and feel changes
        return color != null ? new Color(color.getRGB()) : Color.WHITE;
    }

    /**
     * The background of a blocking modal keeps the mouse away from the owner window, but the owner window
     * still gets the focus: with its title bar, or when a dialog over it is closed. The keyboard would then
     * use the components behind the modal.
     */
    private void ownerFocused() {
        if (isVisible() && root.isTopBlocking(container)) {
            Component focusOwner = getMostRecentFocusOwner();
            (focusOwner != null ? focusOwner : this).requestFocus();
        }
    }

    /**
     * The owner window stays the active window when the focus comes to this window. So the component that had
     * the focus there is not painted again when it has lost the focus for good, and still looks focused.
     * Only that component is painted: painting the whole owner window makes the animation of the modal wait.
     */
    private void focusOwnerChanged(Object oldOwner, Object newOwner) {
        if (newOwner instanceof Component && SwingUtilities.isDescendingFrom((Component) newOwner, this)) {
            Component previous = oldOwner instanceof Component ? (Component) oldOwner : getOwner().getMostRecentFocusOwner();
            if (previous != null && !SwingUtilities.isDescendingFrom(previous, this)) {
                previous.repaint();
            }
        }
    }

    @Override
    public void addContainer(ModalContainer container) {
        this.container = container;
        getContentPane().add(container);
        container.applyComponentOrientation(root.getComponentOrientation());
        getOwner().addWindowFocusListener(ownerFocusListener);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("permanentFocusOwner", focusOwnerListener);
        root.containerAdded(container);
        // not shown yet: the container is not ready for its first frame. It can be laid out and painted to an
        // image when the window is on a screen
        addNotify();
        if (isContainerShowing()) {
            placeWindow();
            validate();
        }
    }

    @Override
    public void removeContainer(ModalContainer container) {
        getOwner().removeWindowFocusListener(ownerFocusListener);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener("permanentFocusOwner", focusOwnerListener);
        dispose();
        getContentPane().remove(container);
        this.container = null;
        root.containerRemoved(container);
    }

    @Override
    public void updateBounds(ModalContainer container) {
        if (updating || this.container == null) {
            return;
        }
        updating = true;
        try {
            boolean visible = isContainerShowing();
            if (visible) {
                placeWindow();
                validate();
            }
            if (visible != isVisible()) {
                setVisible(visible);
            }
        } finally {
            updating = false;
        }
    }

    /**
     * @return true if the modal can be seen: it is not hidden with its owner, and its area is on the screen
     */
    private boolean isContainerShowing() {
        return container.isVisible() && root.getLayer().isShowing() && !container.getArea().isEmpty();
    }

    /**
     * @return true if the window was moved or resized
     */
    private boolean placeWindow() {
        Rectangle area = container.getArea();
        Point location = root.getLayer().getLocationOnScreen();
        location.translate(area.x, area.y);
        Rectangle screen = getScreenBounds();
        screen.translate(-location.x, -location.y);
        Rectangle reach = container.getReach(area.width, area.height, screen);
        Rectangle bounds = new Rectangle(location.x + reach.x, location.y + reach.y, reach.width, reach.height);
        // the area is at the same place of the screen, wherever the window starts
        container.setLayoutArea(new Rectangle(-reach.x, -reach.y, area.width, area.height), screen);
        if (bounds.equals(getBounds())) {
            return false;
        }
        setBounds(bounds);
        return true;
    }

    /**
     * @return the screen the owner window is on, without the task bar
     */
    private Rectangle getScreenBounds() {
        GraphicsConfiguration configuration = root.getLayer().getGraphicsConfiguration();
        Rectangle bounds = configuration.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
        return new Rectangle(bounds.x + insets.left, bounds.y + insets.top,
                bounds.width - (insets.left + insets.right), bounds.height - (insets.top + insets.bottom));
    }

    @Override
    public boolean isWindow() {
        return true;
    }

    @Override
    public Snapshot createBackgroundSnapshot(ModalContainer container) {
        // the window of the owner is behind, and it is not painted again when this window is
        return null;
    }

    /**
     * A popup that fits in this window (the list of a combo box, a tooltip, a popup menu) would be shown inside
     * of it and painted on the transparent window, where its text does not look the same. FlatLaf shows every
     * popup in a window of its own while the glass pane of the window is visible. That is all this glass pane
     * is for: it paints nothing, and the mouse does not see it.
     */
    private static final class GlassPane extends JComponent {

        @Override
        public boolean contains(int x, int y) {
            return false;
        }
    }

    private final class ContentPane extends JComponent {

        @Override
        public void doLayout() {
            // the size of the modal may have changed
            if (!updating && container != null && isContainerShowing()) {
                updating = true;
                try {
                    if (placeWindow()) {
                        // this pane gets its new size now, and not after the window was painted
                        ModalWindow.this.validate();
                    }
                } finally {
                    updating = false;
                }
            }
            for (Component component : getComponents()) {
                component.setBounds(0, 0, getWidth(), getHeight());
            }
        }
    }
}
