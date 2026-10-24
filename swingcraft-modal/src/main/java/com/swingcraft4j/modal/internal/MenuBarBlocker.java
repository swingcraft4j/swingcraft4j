package com.swingcraft4j.modal.internal;

import com.swingcraft4j.modal.option.BackgroundMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Blocks the menu bar of the window while a modal with a blocking background covers the whole window
 * ({@link ModalContainer#blocksMenuBar()}). The {@link ModalLayer} covers only the content pane: the menu bar
 * is outside of it, in a row above it, or over the title bar when FlatLaf embeds it there.
 * <p>
 * The menu bar is blocked in three ways:
 * <ul>
 * <li>the mouse: a cover over the row of the menu bar, painted as the background of the modals. Only the menu bar
 * gets the mouse: what is beside it in the row (the title bar of FlatLaf, when the menu bar is embedded or with
 * the full window content) can still be used</li>
 * <li>the Alt key: FlatLaf selects the first menu when Alt is pressed and released on Windows. A pressed Alt
 * that is consumed does not do that</li>
 * <li>the keys of the menu bar for the whole window (mnemonics, accelerators, F10): see
 * {@link ModalContainer}, they only reach the components of the modal</li>
 * </ul>
 * The window can still be moved, minimized and closed.
 */
final class MenuBarBlocker {

    private final ModalRoot root;
    private final RootPaneContainer window;
    private final Cover cover;
    private final ComponentListener boundsListener;
    private final HierarchyBoundsListener ancestorBoundsListener;
    private final KeyEventDispatcher altKeyDispatcher;
    private JMenuBar menuBar;

    MenuBarBlocker(ModalRoot root, RootPaneContainer window) {
        this.root = root;
        this.window = window;
        cover = new Cover();
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
        ancestorBoundsListener = new HierarchyBoundsAdapter() {
            @Override
            public void ancestorMoved(HierarchyEvent e) {
                updateBounds();
            }

            @Override
            public void ancestorResized(HierarchyEvent e) {
                updateBounds();
            }
        };
        altKeyDispatcher = e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ALT && isBlocked()
                    && getMenuBarOfAltKey(e.getComponent()) == menuBar) {
                e.consume();
            }
            // the event still goes to the focus owner, only the menu bar does not use it
            return false;
        };
    }

    /**
     * Covers the menu bar when a modal that blocks it is in the window, and takes the cover away when there
     * is none. Called when a modal is added or removed.
     */
    void update() {
        boolean install = false;
        for (ModalContainer container : root.getContainers()) {
            if (container.blocksMenuBar()) {
                install = true;
                break;
            }
        }
        JMenuBar newMenuBar = install ? window.getRootPane().getJMenuBar() : null;
        if (newMenuBar == menuBar) {
            return;
        }
        if (menuBar != null) {
            uninstall();
        }
        if (newMenuBar != null) {
            install(newMenuBar);
        }
    }

    private void install(JMenuBar menuBar) {
        this.menuBar = menuBar;
        menuBar.addComponentListener(boundsListener);
        menuBar.addHierarchyBoundsListener(ancestorBoundsListener);
        window.getLayeredPane().add(cover, JLayeredPane.MODAL_LAYER);
        updateBounds();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(altKeyDispatcher);
        // a menu that was open is closed, it would stay over the modal
        MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
        if (path.length > 0 && path[0] == menuBar) {
            MenuSelectionManager.defaultManager().clearSelectedPath();
        }
    }

    void uninstall() {
        if (menuBar == null) {
            return;
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(altKeyDispatcher);
        menuBar.removeComponentListener(boundsListener);
        menuBar.removeHierarchyBoundsListener(ancestorBoundsListener);
        Container parent = cover.getParent();
        if (parent != null) {
            parent.remove(cover);
            parent.repaint(cover.getX(), cover.getY(), cover.getWidth(), cover.getHeight());
        }
        menuBar = null;
    }

    private void updateBounds() {
        JLayeredPane layeredPane = window.getLayeredPane();
        Rectangle menuBarBounds = menuBar.isShowing()
                ? SwingUtilities.convertRectangle(menuBar.getParent(), menuBar.getBounds(), layeredPane)
                : new Rectangle();
        Rectangle bounds = new Rectangle(menuBarBounds);
        if (isInLayeredPane() && !bounds.isEmpty()) {
            // the whole row: the title bar of FlatLaf is beside an embedded menu bar, and the buttons of the title
            // bar are beside it with the full window content. They would be a box that is not covered
            bounds.x = 0;
            bounds.width = layeredPane.getWidth();
        }
        menuBarBounds.translate(-bounds.x, -bounds.y);
        cover.menuBarBounds = menuBarBounds;
        if (!bounds.equals(cover.getBounds())) {
            cover.setBounds(bounds);
            layeredPane.repaint();
        }
    }

    /**
     * @return true if the menu bar is where the root pane puts it, in the layered pane (FlatLaf too, when it embeds
     * it in the title bar). In another component, the cover would be a box over a part of it
     */
    private boolean isInLayeredPane() {
        return menuBar != null && menuBar.getParent() == window.getLayeredPane();
    }

    /**
     * The background of a modal changed, as it fades in and out.
     */
    void repaint() {
        cover.repaint();
    }

    /**
     * @return true if an open modal blocks the menu bar now. A modal that is closing does not, as the
     * modal itself takes no more keys then
     */
    boolean isBlocked() {
        if (menuBar == null) {
            return false;
        }
        for (ModalContainer container : root.getContainers()) {
            if (container.isOpen() && container.isVisible() && container.blocksMenuBar()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if the key is one of the menu bar for the whole window: the mnemonic of a menu, the
     * accelerator of an item, F10
     */
    boolean isMenuBarKey(KeyStroke keyStroke) {
        return menuBar != null && isMenuKey(menuBar, keyStroke);
    }

    private static boolean isMenuKey(MenuElement element, KeyStroke keyStroke) {
        Component component = element.getComponent();
        if (component instanceof JComponent) {
            InputMap inputMap = ((JComponent) component).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            if (inputMap.get(keyStroke) != null) {
                return true;
            }
        }
        for (MenuElement child : element.getSubElements()) {
            if (isMenuKey(child, keyStroke)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the menu bar FlatLaf selects with the Alt key: the one of the root pane of the focus owner, or
     * the one of its frame or dialog (an internal frame has a root pane of its own)
     */
    private static JMenuBar getMenuBarOfAltKey(Component focusOwner) {
        JRootPane rootPane = SwingUtilities.getRootPane(focusOwner);
        JMenuBar menuBar = rootPane != null ? rootPane.getJMenuBar() : null;
        if (menuBar == null) {
            Window window = SwingUtilities.getWindowAncestor(focusOwner);
            if (window instanceof JFrame) {
                menuBar = ((JFrame) window).getJMenuBar();
            } else if (window instanceof JDialog) {
                menuBar = ((JDialog) window).getJMenuBar();
            }
        }
        return menuBar;
    }

    /**
     * Over the menu bar: painted as the background of the modals that block it, and gets the mouse while
     * one of them is open. A click closes the top one if its background closes on click.
     */
    private final class Cover extends JComponent {

        private boolean pressed;
        // where the menu bar is in the cover: only there it gets the mouse, the buttons of the title bar beside it
        // can still be used
        private Rectangle menuBarBounds = new Rectangle();

        Cover() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    pressed = SwingUtilities.isLeftMouseButton(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (pressed && contains(e.getPoint())) {
                        ModalContainer top = getTopContainer();
                        if (top != null && top.getOption().getBackgroundMode() == BackgroundMode.CLOSE_ON_CLICK) {
                            top.close();
                        }
                    }
                    pressed = false;
                }
            });
        }

        /**
         * @return the top modal that blocks the menu bar, or null
         */
        private ModalContainer getTopContainer() {
            for (ModalContainer container : root.getContainers()) {
                if (container.isOpen() && container.isVisible() && container.blocksMenuBar()) {
                    return container;
                }
            }
            return null;
        }

        @Override
        public boolean contains(int x, int y) {
            // while the last modal closes, the menu bar can be used again
            return menuBarBounds.contains(x, y) && isBlocked();
        }

        @Override
        protected void paintComponent(Graphics g) {
            // a menu bar inside another component is only blocked
            if (!isInLayeredPane()) {
                return;
            }
            // the backgrounds over each other, as they are painted over the content: the top modal last
            List<ModalContainer> containers = root.getContainers();
            for (int i = containers.size() - 1; i >= 0; i--) {
                ModalContainer container = containers.get(i);
                if (container.isVisible() && container.blocksMenuBar()) {
                    container.paintBackground(g, 0, 0, getWidth(), getHeight(), 0);
                }
            }
        }
    }
}
