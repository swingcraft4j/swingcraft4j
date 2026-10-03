package com.swingcraft4j.modal;

import com.swingcraft4j.modal.internal.ModalManager;
import com.swingcraft4j.modal.option.ModalOption;

import java.awt.*;

/**
 * Shows modals inside a window. All methods must be called on the event dispatch thread.
 */
public final class JModal {

    private static ModalOption defaultOption = new ModalOption();

    private JModal() {
    }

    public static ModalController show(Component owner, Modal modal) {
        return show(owner, modal, defaultOption, null);
    }

    public static ModalController show(Component owner, Modal modal, String id) {
        return show(owner, modal, defaultOption, id);
    }

    public static ModalController show(Component owner, Modal modal, ModalOption option) {
        return show(owner, modal, option, null);
    }

    /**
     * Shows the modal in the window of the owner.
     *
     * @param owner  the window to show the modal in, or any component inside it. With
     *               {@link ModalOption#setRelativeToOwner(boolean)} the modal is shown over this component
     * @param modal  the modal to show
     * @param option the option, it is copied
     * @param id     an id to find the modal with {@link #close(String)} and {@link #isOpen(String)}, or null
     * @return the controller to close the modal
     * @throws IllegalArgumentException if the owner is not in a window or the id is already used by an open modal
     * @throws IllegalStateException    if the modal is already showing
     */
    public static ModalController show(Component owner, Modal modal, ModalOption option, String id) {
        return ModalManager.show(owner, modal, option, id);
    }

    /**
     * Closes the modal with the id. Does nothing if no modal with the id is open.
     */
    public static void close(String id) {
        ModalController controller = ModalManager.find(id);
        if (controller != null) {
            controller.close();
        }
    }

    /**
     * Closes the modal with the id without animation. Does nothing if no modal with the id is open.
     */
    public static void closeImmediately(String id) {
        ModalController controller = ModalManager.find(id);
        if (controller != null) {
            controller.closeImmediately();
        }
    }

    public static void closeAll() {
        ModalManager.closeAll(true);
    }

    /**
     * Closes all modals without animation, also the ones that are closing with an animation.
     */
    public static void closeAllImmediately() {
        ModalManager.closeAll(false);
    }

    /**
     * Shows another modal in place of the open modal with the id.
     *
     * @throws IllegalArgumentException if no modal with the id is open
     * @see ModalController#push(Modal)
     */
    public static void push(String id, Modal modal) {
        ModalController controller = ModalManager.find(id);
        if (controller == null) {
            throw new IllegalArgumentException("id '" + id + "' not found");
        }
        controller.push(modal);
    }

    /**
     * Goes back to the modal before the last push. Does nothing if no modal with the id is open.
     *
     * @see ModalController#pop()
     */
    public static void pop(String id) {
        ModalController controller = ModalManager.find(id);
        if (controller != null) {
            controller.pop();
        }
    }

    public static boolean isOpen(String id) {
        return ModalManager.find(id) != null;
    }

    /**
     * @return the option used when a modal is shown without option
     */
    public static ModalOption getDefaultOption() {
        return defaultOption;
    }

    public static void setDefaultOption(ModalOption option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not null");
        }
        defaultOption = option;
    }

    /**
     * @return a new option, copied from the default option
     */
    public static ModalOption createOption() {
        return defaultOption.copy();
    }
}
