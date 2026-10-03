package com.swingcraft4j.toast;

/**
 * The work of a toast that is shown with {@link JToast#showTask}. It runs on a thread of the toast, not on
 * the event dispatch thread, so it must not use Swing components.
 *
 * @param <T> the result of the work
 */
@FunctionalInterface
public interface ToastTask<T> {

    /**
     * Does the work.
     *
     * @param progress to change the message of the toast while the work runs, and to see if it was cancelled
     * @return the result, it is given to the function that makes the message of the toast
     * @throws Exception if the work failed, the toast then shows an error
     */
    T run(ToastProgress progress) throws Exception;
}
