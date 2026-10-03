package com.swingcraft4j.modal;

/**
 * Controls a modal that has been shown.
 */
public interface ModalController {

    /**
     * Closes the modal, with every modal that was pushed. Does nothing if the modal is already closed.
     */
    void close();

    /**
     * Closes the modal without animation, with every modal that was pushed. A modal that is closing with
     * an animation is closed at once. Does nothing if the modal is already closed.
     */
    void closeImmediately();

    /**
     * Shows another modal in place of the current one, in the same position and with the same option.
     * The current modal is kept and comes back with {@link #pop()}.
     *
     * @throws IllegalStateException if this modal is closed or the pushed modal is already showing
     */
    void push(Modal modal);

    /**
     * Removes the modal shown by the last {@link #push(Modal)} and shows the one before it again.
     * Does nothing if no modal was pushed.
     */
    void pop();

    /**
     * @return true if a modal was pushed, so {@link #pop()} goes back
     */
    boolean canPop();

    /**
     * @return true from the moment the modal is shown until it is closed
     */
    boolean isOpen();

    /**
     * @return the id given when the modal was shown, or null
     */
    String getId();
}
