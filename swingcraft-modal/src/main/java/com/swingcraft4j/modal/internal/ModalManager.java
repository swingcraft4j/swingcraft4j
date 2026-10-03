package com.swingcraft4j.modal.internal;

import com.swingcraft4j.modal.Modal;
import com.swingcraft4j.modal.ModalController;
import com.swingcraft4j.modal.option.ModalOption;
import com.swingcraft4j.modal.option.Surface;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Shows modals and keeps track of the windows that have open modals.
 */
public final class ModalManager {

    // weak, so a window disposed with open modals is not kept alive
    private static final Set<ModalRoot> roots = Collections.newSetFromMap(new WeakHashMap<>());

    private ModalManager() {
    }

    public static ModalController show(Component owner, Modal modal, ModalOption option, String id) {
        if (modal == null) {
            throw new IllegalArgumentException("modal must not null");
        }
        if (option == null) {
            throw new IllegalArgumentException("option must not null");
        }
        ModalContainer.release(modal);
        if (modal.getParent() != null) {
            throw new IllegalStateException("modal is already showing");
        }
        if (id != null && find(id) != null) {
            throw new IllegalArgumentException("id '" + id + "' already exist");
        }
        ModalOption copy = option.copy();
        RootPaneContainer window = getWindow(owner);
        // relative to the window is the same as not relative
        Component relativeOwner = copy.isRelativeToOwner() && owner != window ? owner : null;
        while (window instanceof ModalWindow) {
            // the owner is in a modal that has a window of its own. That window is not the window of the user,
            // and a layer of the window of the user is behind it
            window = getWindow(((ModalWindow) window).getOwner());
            copy.setSurface(Surface.WINDOW);
        }
        ModalRoot root = ModalRoot.of(window);
        if (root.hasWindowModal()) {
            // a window is over the layer, and the modal that is shown last is the top one
            copy.setSurface(Surface.WINDOW);
        }
        ModalContainer container = new ModalContainer(root, root.createHost(copy), relativeOwner, modal, copy, id);
        container.open();
        return container;
    }

    /**
     * @return the open modal with the id, or null
     */
    public static ModalController find(String id) {
        if (id != null) {
            for (ModalContainer container : getContainers()) {
                // a modal that is closing with an animation is not open any more
                if (container.isOpen() && id.equals(container.getId())) {
                    return container;
                }
            }
        }
        return null;
    }

    /**
     * @param animate false to close the modals without animation, also the ones that are closing already
     */
    public static void closeAll(boolean animate) {
        for (ModalContainer container : getContainers()) {
            if (animate) {
                container.close();
            } else {
                container.closeImmediately();
            }
        }
    }

    static void rootInstalled(ModalRoot root) {
        roots.add(root);
    }

    static void rootUninstalled(ModalRoot root) {
        roots.remove(root);
    }

    private static List<ModalContainer> getContainers() {
        List<ModalContainer> containers = new ArrayList<>();
        for (ModalRoot root : roots) {
            containers.addAll(root.getContainers());
        }
        return containers;
    }

    private static RootPaneContainer getWindow(Component owner) {
        for (Component component = owner; component != null; component = component.getParent()) {
            if (component instanceof RootPaneContainer) {
                return (RootPaneContainer) component;
            }
        }
        throw new IllegalArgumentException("owner must be in a window");
    }
}
