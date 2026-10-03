package com.swingcraft4j.modal.internal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;

/**
 * The layer over the content of one window, and the host of the modals that are shown inside the window.
 * It is added to the layered pane of the window above the content pane when the first modal is shown and
 * removed when the last modal is closed. The top modal is the first child.
 * <p>
 * The modals with a window of their own are not in the layer, but they use it too: the layer is the area
 * of the window a modal covers.
 */
final class ModalLayer extends JComponent implements ModalHost {

    private final ModalRoot root;
    private final RootPaneContainer window;
    private final ComponentListener boundsListener;
    private Container contentPane;
    // while painting what is behind a modal: this modal and the modals over it are not painted
    private ModalContainer paintLimit;

    ModalLayer(ModalRoot root, RootPaneContainer window) {
        this.root = root;
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
        // the modals with a window of their own follow the window on the screen, and are shown and hidden with it
        addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
            @Override
            public void ancestorMoved(HierarchyEvent e) {
                root.updateBounds();
            }
        });
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                root.updateBounds();
            }
        });
        registerKeyboardAction(e -> root.escapePressed(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    void install() {
        JLayeredPane layeredPane = window.getLayeredPane();
        contentPane = window.getContentPane();
        layeredPane.addComponentListener(boundsListener);
        contentPane.addComponentListener(boundsListener);
        layeredPane.add(this, JLayeredPane.MODAL_LAYER);
        updateBounds();
    }

    void uninstall() {
        JLayeredPane layeredPane = window.getLayeredPane();
        layeredPane.removeComponentListener(boundsListener);
        contentPane.removeComponentListener(boundsListener);
        layeredPane.remove(this);
        layeredPane.repaint(getX(), getY(), getWidth(), getHeight());
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

    @Override
    public void addContainer(ModalContainer container) {
        add(container, 0);
        container.applyComponentOrientation(root.getComponentOrientation());
        root.containerAdded(container);
        validate();
        repaint();
    }

    @Override
    public void removeContainer(ModalContainer container) {
        remove(container);
        repaint();
        root.containerRemoved(container);
    }

    @Override
    public void updateBounds(ModalContainer container) {
        container.setBounds(container.getArea());
    }

    @Override
    public boolean isWindow() {
        return false;
    }

    @Override
    public Snapshot createBackgroundSnapshot(ModalContainer container) {
        return Snapshot.create(container, 0, 0, container.getWidth(), container.getHeight(), false, g -> paintBehind(container, g));
    }

    /**
     * Paints the window as it is behind the modal: without the modal and the modals over it.
     *
     * @param g the graphics of an image as large as the modal container
     */
    private void paintBehind(ModalContainer container, Graphics2D g) {
        JRootPane rootPane = window.getRootPane();
        Point origin = SwingUtilities.convertPoint(container, 0, 0, rootPane);
        g.translate(-origin.x, -origin.y);
        g.setClip(origin.x, origin.y, container.getWidth(), container.getHeight());
        paintLimit = container;
        try {
            rootPane.paint(g);
        } finally {
            paintLimit = null;
        }
    }

    @Override
    protected void paintChildren(Graphics g) {
        if (paintLimit == null) {
            super.paintChildren(g);
            return;
        }
        // the last modal is at the back
        Component[] components = getComponents();
        for (int i = components.length - 1; i >= 0 && components[i] != paintLimit; i--) {
            Component component = components[i];
            if (component.isVisible()) {
                Graphics g2 = g.create(component.getX(), component.getY(), component.getWidth(), component.getHeight());
                try {
                    component.paint(g2);
                } finally {
                    g2.dispose();
                }
            }
        }
    }

    @Override
    public void doLayout() {
        // the area of every modal of the window is in this layer, also of the modals that are not in it
        root.updateBounds();
    }

    @Override
    public boolean contains(int x, int y) {
        // what is outside the layer is not painted, and must not get the mouse:
        // it would be over the title bar or the menu bar of the window
        if (!super.contains(x, y)) {
            return false;
        }
        for (Component component : getComponents()) {
            if (component.isVisible() && component.contains(x - component.getX(), y - component.getY())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        // the modals overlap
        return false;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        root.updateOutsideClickListener();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // the window disposed, do not keep it alive with the global listener
        root.updateOutsideClickListener();
    }
}
