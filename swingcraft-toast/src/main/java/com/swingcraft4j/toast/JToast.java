package com.swingcraft4j.toast;

import com.swingcraft4j.toast.internal.ToastManager;
import com.swingcraft4j.toast.internal.ToastTasks;
import com.swingcraft4j.toast.option.ToastLocation;
import com.swingcraft4j.toast.option.ToastOption;

import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Shows toasts inside a window. All methods must be called on the event dispatch thread.
 */
public final class JToast {

    private static ToastOption defaultOption = new ToastOption();

    private JToast() {
    }

    public static ToastController show(Component owner, ToastType type, String message) {
        return show(owner, type, message, defaultOption);
    }

    /**
     * Shows the toast with the default option at another location.
     */
    public static ToastController show(Component owner, ToastType type, String message, ToastLocation location) {
        return show(owner, type, message, createOption().setLocation(location));
    }

    /**
     * Shows a toast with a message in the window of the owner.
     *
     * @param owner  the window to show the toast in, or any component inside it. With
     *               {@link ToastOption#setRelativeToOwner(boolean)} the toast is shown in this component
     * @param type   the kind of message, it sets the color and the icon
     * @param option the option, it is copied
     * @return the controller to close the toast. With {@link ToastOption#setGroupRepeated(boolean)} it is the
     * controller of the toast that is showing already, if the message is shown again
     * @throws IllegalArgumentException if the owner is not in a window
     */
    public static ToastController show(Component owner, ToastType type, String message, ToastOption option) {
        return ToastManager.show(owner, type, message, null, option);
    }

    public static ToastController showCustom(Component owner, Component component) {
        return showCustom(owner, component, defaultOption);
    }

    /**
     * Shows a toast with the component in place of the icon, the message and the close button.
     * The component can close the toast with {@link #getController(Component)}.
     *
     * @throws IllegalArgumentException if the owner is not in a window
     * @throws IllegalStateException    if the component is already showing
     */
    public static ToastController showCustom(Component owner, Component component, ToastOption option) {
        if (component == null) {
            throw new IllegalArgumentException("component must not null");
        }
        return ToastManager.show(owner, ToastType.DEFAULT, null, component, option);
    }

    public static <T> ToastController showTask(Component owner, String message, ToastTask<T> task,
                                               Function<? super T, String> onSuccess, Function<Throwable, String> onFailure) {
        return showTask(owner, message, task, onSuccess, onFailure, defaultOption);
    }

    /**
     * Runs the task on another thread and shows a toast for it: a {@link ToastType#LOADING} toast with the
     * message while the task runs, then the same toast shows the result. It stays open until the task is done,
     * and closes after that as the option says.
     * <p>
     * With {@link ToastOption#setCancellable(boolean)} the toast has a cancel button while the task runs.
     * It interrupts the thread of the task and closes the toast, nothing is shown for a task that was
     * cancelled. If the user closes the toast in another way, the task goes on and its result is shown in
     * a new toast.
     *
     * @param message   the message while the task runs
     * @param task      the work, it must not use Swing components
     * @param onSuccess makes the message of the {@link ToastType#SUCCESS} toast from the result of the task.
     *                  Null, or a null message, to close the toast without a message
     * @param onFailure makes the message of the {@link ToastType#ERROR} toast from the exception of the task.
     *                  Null to show the message of the exception, a null message to close the toast
     * @param option    the option, it is copied
     * @return the controller of the toast
     * @throws IllegalArgumentException if the owner is not in a window
     */
    public static <T> ToastController showTask(Component owner, String message, ToastTask<T> task,
                                               Function<? super T, String> onSuccess, Function<Throwable, String> onFailure, ToastOption option) {
        return ToastTasks.showTask(owner, message, task, onSuccess, onFailure, option);
    }

    public static <T> ToastController showFuture(Component owner, String message, CompletableFuture<T> future,
                                                 Function<? super T, String> onSuccess, Function<Throwable, String> onFailure) {
        return showFuture(owner, message, future, onSuccess, onFailure, defaultOption);
    }

    /**
     * Shows a toast for work that runs somewhere else, as {@link #showTask} does for a task: a
     * {@link ToastType#LOADING} toast until the future is done, then its result. Use it for work that has
     * its own thread or executor.
     * <p>
     * The cancel button of {@link ToastOption#setCancellable(boolean)} cancels the future. That does not
     * stop work that is running already, unless the future does that itself.
     *
     * @throws IllegalArgumentException if the owner is not in a window
     */
    public static <T> ToastController showFuture(Component owner, String message, CompletableFuture<T> future,
                                                 Function<? super T, String> onSuccess, Function<Throwable, String> onFailure, ToastOption option) {
        return ToastTasks.showFuture(owner, message, future, onSuccess, onFailure, option);
    }

    /**
     * @return the controller of the toast the component is in, or null if it is not in a toast
     */
    public static ToastController getController(Component component) {
        return ToastManager.find(component);
    }

    /**
     * Closes the toast with the id. Does nothing if no toast with the id is open.
     */
    public static void close(String id) {
        ToastController controller = ToastManager.find(id);
        if (controller != null) {
            controller.close();
        }
    }

    /**
     * Closes the toast with the id without animation. Does nothing if no toast with the id is open.
     */
    public static void closeImmediately(String id) {
        ToastController controller = ToastManager.find(id);
        if (controller != null) {
            controller.closeImmediately();
        }
    }

    public static void closeAll() {
        ToastManager.closeAll(null, true);
    }

    /**
     * Closes the toasts at the location.
     */
    public static void closeAll(ToastLocation location) {
        if (location == null) {
            throw new IllegalArgumentException("location must not null");
        }
        ToastManager.closeAll(location, true);
    }

    /**
     * Closes all toasts without animation, also the ones that are closing with an animation.
     */
    public static void closeAllImmediately() {
        ToastManager.closeAll(null, false);
    }

    /**
     * Closes the toasts at the location without animation.
     */
    public static void closeAllImmediately(ToastLocation location) {
        if (location == null) {
            throw new IllegalArgumentException("location must not null");
        }
        ToastManager.closeAll(location, false);
    }

    public static boolean isOpen(String id) {
        return ToastManager.find(id) != null;
    }

    public static boolean isReverseOrder() {
        return ToastManager.isReverseOrder();
    }

    /**
     * @param reverseOrder true to show the oldest toast of a list at the edge of the window and the newest
     *                     after the others, false (default) to show the newest at the edge
     */
    public static void setReverseOrder(boolean reverseOrder) {
        ToastManager.setReverseOrder(reverseOrder);
    }

    /**
     * @return the option used when a toast is shown without option
     */
    public static ToastOption getDefaultOption() {
        return defaultOption;
    }

    public static void setDefaultOption(ToastOption option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not null");
        }
        defaultOption = option;
    }

    /**
     * @return a new option, copied from the default option
     */
    public static ToastOption createOption() {
        return defaultOption.copy();
    }
}
