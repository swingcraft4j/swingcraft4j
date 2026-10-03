package com.swingcraft4j.toast;

/**
 * Controls a toast that has been shown.
 */
public interface ToastController {

    /**
     * Closes the toast. Does nothing if the toast is already closed.
     */
    void close();

    /**
     * Closes the toast without animation. A toast that is closing with an animation is closed at once.
     * Does nothing if the toast is already closed.
     */
    void closeImmediately();

    /**
     * @return true from the moment the toast is shown until it starts to close
     */
    boolean isOpen();

    /**
     * @return the id of the toast, to use with {@link JToast#close(String)} and {@link JToast#isOpen(String)}
     */
    String getId();

    ToastType getType();

    /**
     * @return the message of the toast, or null for a toast with a custom component
     */
    String getMessage();

    /**
     * Changes the message of the toast, see {@link #update(ToastType, String)}.
     */
    void setMessage(String message);

    /**
     * Changes the type of the toast, see {@link #update(ToastType, String)}.
     */
    void setType(ToastType type);

    /**
     * Changes the type and the message of the toast while it is showing, for example from a message that
     * a task is running to its result. The toast gets its new size at its place, and the delay to close it
     * starts again. Does nothing if the toast is not open any more.
     * <p>
     * The count of a repeated message starts again. A toast with a custom component only changes
     * its color, it has no message.
     */
    void update(ToastType type, String message);

    /**
     * @return how often the toast was shown: more than 1 if it was shown again while it was showing,
     * with {@link com.swingcraft4j.toast.option.ToastOption#setGroupRepeated(boolean)}
     */
    int getCount();
}
