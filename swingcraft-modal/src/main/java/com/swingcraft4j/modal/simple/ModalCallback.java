package com.swingcraft4j.modal.simple;

/**
 * Receives the action when a button of a {@link SimpleModal} is pressed.
 */
public interface ModalCallback {

    /**
     * The modal closes after this method, unless {@link ModalEvent#consume()} is called.
     */
    void action(ModalEvent event);
}
