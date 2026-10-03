package com.swingcraft4j.modal.internal;

/**
 * What a modal is shown on: the {@link ModalLayer} inside the window, or a {@link ModalWindow} of its own.
 * <p>
 * A host only places the {@link ModalContainer} on the screen. The modal itself (background, animation,
 * push and pop) is the same on every host, and what applies to all the modals of a window (the order,
 * the escape key, the click outside) is done by the {@link ModalRoot}.
 */
interface ModalHost {

    /**
     * Adds the container to the host and to the {@link ModalRoot}. A window of its own is not shown before
     * {@link #updateBounds} is called, the container is then ready to be painted.
     */
    void addContainer(ModalContainer container);

    /**
     * Removes the container from the screen and from the {@link ModalRoot}.
     */
    void removeContainer(ModalContainer container);

    /**
     * Places the container again. Called when its area, the place or the size of its modal or its
     * visibility changed.
     */
    void updateBounds(ModalContainer container);

    /**
     * @return true if the container has a window of its own: the modal can be larger than its area,
     * and it is painted on a transparent window
     */
    boolean isWindow();

    /**
     * @return an image of what is behind the container, to paint in place of the real components while
     * animating. Null if the host does not paint what is behind
     */
    Snapshot createBackgroundSnapshot(ModalContainer container);
}
