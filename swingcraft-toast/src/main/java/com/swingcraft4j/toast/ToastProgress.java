package com.swingcraft4j.toast;

/**
 * What a {@link ToastTask} can do with its toast while it runs. The methods can be called on any thread.
 */
public interface ToastProgress {

    /**
     * Changes the message of the toast, for example for the next step of the work.
     * Does nothing when the work is done or cancelled.
     */
    void setMessage(String message);

    /**
     * @return true if the user has cancelled the work with the cancel button of the toast. The thread of the
     * work is interrupted too, so work that waits ends by itself. Work that does not wait should look at
     * this from time to time, and stop
     */
    boolean isCancelled();
}
