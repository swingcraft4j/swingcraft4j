package com.swingcraft4j.toast.internal;

import javax.swing.*;
import java.awt.*;

/**
 * A window of its own for one toast, and the host of that toast. It is owned by the window the toast is
 * shown in, so it stays over that window and is hidden with it.
 * <p>
 * The window is transparent and as large as the toast with its shadow. The layer places it where the toast
 * would be in the layer, so it moves with every step of an animation. It is small on purpose: a toast is
 * painted again all the time while its progress line runs, and a transparent window is painted as a whole.
 * <p>
 * The window never gets the focus: a toast must not take it away from what the user is doing.
 */
final class ToastWindow extends JWindow implements ToastHost {

    private final ToastLayer layer;
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

    ToastWindow(ToastLayer layer, Window owner) {
        super(owner);
        this.layer = layer;
        setFocusableWindowState(false);
        // macOS gives a transparent window a shadow in the shape of what was painted first
        getRootPane().putClientProperty("Window.shadow", Boolean.FALSE);
        JComponent contentPane = new JComponent() {
        };
        setContentPane(contentPane);
        setGlassPane(new GlassPane());
        getGlassPane().setVisible(true);
        setBackground(new Color(0, true));
    }

    /**
     * A window has the background of its owner until it gets one of its own. So a window owned by this window
     * (a tooltip, the popup of a component in a custom toast) would start with a transparent background
     * without being made for it: it is transparent for the system, but its panes still fill it. And when it
     * is then set to transparent, as FlatLaf does for the window that paints the shadow of a popup, nothing
     * changes because it already is: the shadow is painted on a filled window.
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

    @Override
    public void attach(ToastPanel toast) {
        getContentPane().add(toast);
        // not shown yet: the toast is not placed and does not know how it is presented. It can be laid out
        // and painted to an image when the window is on a screen
        addNotify();
    }

    @Override
    public void detach(ToastPanel toast) {
        dispose();
        getContentPane().remove(toast);
    }

    @Override
    public void place(ToastPanel toast, Rectangle bounds) {
        // the toast fills the window
        toast.setBounds(0, 0, bounds.width, bounds.height);
        if (!layer.isShowing()) {
            return;
        }
        Point location = layer.getLocationOnScreen();
        Rectangle windowBounds = new Rectangle(location.x + bounds.x, location.y + bounds.y, bounds.width, bounds.height);
        if (!windowBounds.equals(getBounds())) {
            setBounds(windowBounds);
            validate();
        }
    }

    @Override
    public void updateVisible(ToastPanel toast) {
        boolean visible = toast.isVisible() && layer.isShowing() && getWidth() > 0 && getHeight() > 0;
        if (visible != isVisible()) {
            setVisible(visible);
        }
    }

    @Override
    public void toFront(ToastPanel toast) {
        if (isVisible()) {
            toFront();
        }
    }

    @Override
    public boolean isWindow() {
        return true;
    }

    /**
     * A popup that fits in this window (a tooltip) would be shown inside of it and painted on the transparent
     * window, where its text does not look the same. FlatLaf shows every popup in a window of its own while
     * the glass pane of the window is visible. That is all this glass pane is for: it paints nothing, and the
     * mouse does not see it.
     */
    private static final class GlassPane extends JComponent {

        @Override
        public boolean contains(int x, int y) {
            return false;
        }
    }
}
