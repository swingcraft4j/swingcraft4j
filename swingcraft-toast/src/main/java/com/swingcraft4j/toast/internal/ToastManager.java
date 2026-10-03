package com.swingcraft4j.toast.internal;

import com.swingcraft4j.toast.ToastController;
import com.swingcraft4j.toast.ToastType;
import com.swingcraft4j.toast.option.ToastLocation;
import com.swingcraft4j.toast.option.ToastOption;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Shows toasts and keeps track of the windows that have toasts.
 */
public final class ToastManager {

    // weak, so a window disposed with toasts is not kept alive
    private static final Set<ToastLayer> layers = Collections.newSetFromMap(new WeakHashMap<>());
    private static int lastId;
    private static boolean reverseOrder;

    private ToastManager() {
    }

    /**
     * @param message the message of the default toast, not used with a custom component
     * @param custom  the component to show in place of the default content, or null
     */
    public static ToastController show(Component owner, ToastType type, String message, Component custom, ToastOption option) {
        return show(owner, type, message, custom, option, null);
    }

    /**
     * @param cancelAction the action of the cancel button the toast has while it is loading, or null for no button
     */
    static ToastController show(Component owner, ToastType type, String message, Component custom, ToastOption option, Runnable cancelAction) {
        if (type == null) {
            throw new IllegalArgumentException("type must not null");
        }
        if (option == null) {
            throw new IllegalArgumentException("option must not null");
        }
        if (custom != null && custom.getParent() != null) {
            throw new IllegalStateException("component is already showing");
        }
        RootPaneContainer window = getWindow(owner);
        // relative to the window is the same as not relative
        Component relativeOwner = option.isRelativeToOwner() && owner != window ? owner : null;
        // a toast that is loading belongs to its own work, also when another one shows the same message
        if (custom == null && option.isGroupRepeated() && type != ToastType.LOADING) {
            ToastLayer current = ToastLayer.of(window, false);
            ToastPanel repeated = current == null ? null : current.findRepeated(relativeOwner, type, message, option);
            if (repeated != null) {
                repeated.repeat();
                return repeated;
            }
        }
        ToastLayer layer = ToastLayer.of(window, true);
        ToastOption copy = option.copy();
        ToastPanel toast = new ToastPanel(layer, layer.createHost(copy), relativeOwner, type, message, custom, copy, "toast-" + (++lastId), cancelAction);
        toast.open();
        return toast;
    }

    /**
     * @return the open toast with the id, or null
     */
    public static ToastController find(String id) {
        if (id != null) {
            for (ToastPanel toast : getToasts()) {
                // a toast that is closing with an animation is not open any more
                if (toast.isOpen() && id.equals(toast.getId())) {
                    return toast;
                }
            }
        }
        return null;
    }

    /**
     * @return the toast the component is in, or null
     */
    public static ToastController find(Component component) {
        if (component instanceof ToastPanel) {
            return (ToastPanel) component;
        }
        return (ToastPanel) SwingUtilities.getAncestorOfClass(ToastPanel.class, component);
    }

    /**
     * @param location the location of the toasts to close, or null for all toasts
     * @param animate  false to close them without animation, also the ones that are closing already
     */
    public static void closeAll(ToastLocation location, boolean animate) {
        List<ToastPanel> toasts = getToasts();
        // placed one time, and not for each toast that closes without animation
        ToastLayer.batch(() -> {
            for (ToastPanel toast : toasts) {
                if (location == null || toast.getOption().getLocation() == location) {
                    if (animate) {
                        toast.close();
                    } else {
                        toast.closeImmediately();
                    }
                }
            }
        });
    }

    public static boolean isReverseOrder() {
        return reverseOrder;
    }

    public static void setReverseOrder(boolean reverseOrder) {
        if (ToastManager.reverseOrder != reverseOrder) {
            ToastManager.reverseOrder = reverseOrder;
            for (ToastLayer layer : new ArrayList<>(layers)) {
                layer.arrange();
            }
        }
    }

    static void layerInstalled(ToastLayer layer) {
        layers.add(layer);
    }

    static void layerUninstalled(ToastLayer layer) {
        layers.remove(layer);
    }

    private static List<ToastPanel> getToasts() {
        List<ToastPanel> toasts = new ArrayList<>();
        for (ToastLayer layer : layers) {
            toasts.addAll(layer.getAllToasts());
        }
        return toasts;
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
