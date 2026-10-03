package com.swingcraft4j.modal;

import com.swingcraft4j.modal.internal.ModalContainer;

import javax.swing.*;
import java.awt.*;

/**
 * The base class of all modals. Extend this class and add components to it to create a custom modal,
 * then show it with {@link JModal#show(Component, Modal)}.
 */
public class Modal extends JPanel {

    private boolean installed;

    public Modal() {
    }

    public Modal(LayoutManager layout) {
        super(layout);
    }

    /**
     * Called once before the modal is shown for the first time.
     * Subclasses that are configured after the constructor can create their components here.
     */
    protected void installComponent() {
    }

    /**
     * Called each time the modal becomes the visible one: when it is shown, when it is pushed
     * and when it comes back because the modal pushed over it was popped.
     */
    public void modalOpened() {
    }

    /**
     * Called each time the modal has been closed or popped.
     */
    public void modalClosed() {
    }

    /**
     * @return the controller of this modal, or null if the modal is not showing
     */
    public ModalController getController() {
        return (ModalController) SwingUtilities.getAncestorOfClass(ModalContainer.class, this);
    }

    /**
     * Closes this modal. Does nothing if the modal is not showing.
     */
    public void closeModal() {
        ModalController controller = getController();
        if (controller != null) {
            controller.close();
        }
    }

    /**
     * Shows another modal in place of this one, {@link #popModal()} comes back to this one.
     *
     * @throws IllegalStateException if this modal is not showing
     * @see ModalController#push(Modal)
     */
    public void pushModal(Modal modal) {
        ModalController controller = getController();
        if (controller == null) {
            throw new IllegalStateException("modal is not showing");
        }
        controller.push(modal);
    }

    /**
     * Goes back to the modal that pushed this one. Does nothing if this modal is not showing or was not pushed.
     */
    public void popModal() {
        ModalController controller = getController();
        if (controller != null) {
            controller.pop();
        }
    }

    @Override
    public void addNotify() {
        if (!installed) {
            installed = true;
            installComponent();
        }
        super.addNotify();
    }
}
