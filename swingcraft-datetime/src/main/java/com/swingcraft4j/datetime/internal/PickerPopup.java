package com.swingcraft4j.datetime.internal;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;

/**
 * Shows a picker in a popup at a component. The date picker and the time picker have one each.
 */
public final class PickerPopup {

    // the space between the component and the popup
    private static final int GAP = 2;

    private final JComponent picker;
    private JPopupMenu popupMenu;
    private LookAndFeel lookAndFeel;

    public PickerPopup(JComponent picker) {
        this.picker = picker;
    }

    /**
     * Shows the popup below the component, or above it if there is no space below.
     */
    public void show(Component invoker) {
        if (invoker == null || !invoker.isShowing()) {
            throw new IllegalArgumentException("the component of the popup is not showing");
        }
        if (popupMenu == null) {
            popupMenu = new JPopupMenu();
            popupMenu.putClientProperty(FlatClientProperties.STYLE, "borderInsets:1,1,1,1");
            popupMenu.add(picker);
        } else if (picker.getParent() != popupMenu) {
            // the picker was added to another container after the popup was closed
            popupMenu.add(picker);
        }
        // the components of a popup that is not showing are not updated when the look and feel changes
        if (lookAndFeel != UIManager.getLookAndFeel()) {
            if (lookAndFeel != null) {
                SwingUtilities.updateComponentTreeUI(popupMenu);
            }
            lookAndFeel = UIManager.getLookAndFeel();
        }
        Point location = getLocation(invoker);
        popupMenu.show(invoker, location.x, location.y);
    }

    public void close() {
        if (popupMenu != null) {
            popupMenu.setVisible(false);
        }
    }

    public boolean isVisible() {
        return popupMenu != null && popupMenu.isVisible();
    }

    // relative to the invoker
    private Point getLocation(Component invoker) {
        Dimension size = popupMenu.getPreferredSize();
        int gap = UIScale.scale(GAP);
        // at the leading edge of the invoker
        int x = invoker.getComponentOrientation().isLeftToRight() ? 0 : invoker.getWidth() - size.width;
        int y = invoker.getHeight() + gap;
        GraphicsConfiguration configuration = invoker.getGraphicsConfiguration();
        if (configuration != null) {
            Rectangle screen = configuration.getBounds();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
            int bottom = screen.y + screen.height - insets.bottom;
            int top = screen.y + insets.top;
            Point onScreen = invoker.getLocationOnScreen();
            boolean fitsBelow = onScreen.y + y + size.height <= bottom;
            boolean fitsAbove = onScreen.y - gap - size.height >= top;
            if (!fitsBelow && fitsAbove) {
                y = -gap - size.height;
            }
        }
        return new Point(x, y);
    }
}
