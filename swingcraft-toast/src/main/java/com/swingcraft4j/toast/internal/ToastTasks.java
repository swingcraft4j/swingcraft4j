package com.swingcraft4j.toast.internal;

import com.swingcraft4j.toast.ToastController;
import com.swingcraft4j.toast.ToastProgress;
import com.swingcraft4j.toast.ToastTask;
import com.swingcraft4j.toast.ToastType;
import com.swingcraft4j.toast.option.ToastOption;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Shows a loading toast for work that runs somewhere else, and its result when the work is done.
 */
public final class ToastTasks {

    private static ExecutorService executor;

    private ToastTasks() {
    }

    /**
     * The threads of the tasks. They are made when they are needed and end when they are not used for a
     * while, and they do not keep the application running.
     */
    private static synchronized ExecutorService getExecutor() {
        if (executor == null) {
            AtomicInteger count = new AtomicInteger();
            executor = Executors.newCachedThreadPool(runnable -> {
                Thread thread = new Thread(runnable, "JToast-task-" + count.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            });
        }
        return executor;
    }

    public static <T> ToastController showTask(Component owner, String message, ToastTask<T> task,
                                               Function<? super T, String> onSuccess, Function<Throwable, String> onFailure, ToastOption option) {
        if (task == null) {
            throw new IllegalArgumentException("task must not null");
        }
        CompletableFuture<T> future = new CompletableFuture<>();
        Progress progress = new Progress();
        Future<?>[] running = new Future<?>[1];
        // the toast first: the work may be done before it is shown
        ToastController toast = show(owner, message, future, onSuccess, onFailure, option, () -> {
            progress.cancelled = true;
            // first: the work that is interrupted fails, and that is not its result
            future.cancel(false);
            // ends work that waits
            running[0].cancel(true);
        });
        progress.toast = toast;
        running[0] = getExecutor().submit(() -> {
            try {
                future.complete(task.run(progress));
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }
        });
        return toast;
    }

    public static <T> ToastController showFuture(Component owner, String message, CompletableFuture<T> future,
                                                 Function<? super T, String> onSuccess, Function<Throwable, String> onFailure, ToastOption option) {
        if (future == null) {
            throw new IllegalArgumentException("future must not null");
        }
        return show(owner, message, future, onSuccess, onFailure, option, () -> future.cancel(true));
    }

    private static <T> ToastController show(Component owner, String message, CompletableFuture<T> future,
                                            Function<? super T, String> onSuccess, Function<Throwable, String> onFailure,
                                            ToastOption option, Runnable cancel) {
        if (option == null) {
            throw new IllegalArgumentException("option must not null");
        }
        ToastOption copy = option.copy();
        ToastController[] toast = new ToastController[1];
        Runnable cancelAction = !copy.isCancellable() ? null : () -> {
            cancel.run();
            toast[0].close();
        };
        toast[0] = ToastManager.show(owner, ToastType.LOADING, message, null, copy, cancelAction);
        future.whenComplete((result, error) -> SwingUtilities.invokeLater(() -> done(owner, toast[0], copy, result, error, onSuccess, onFailure)));
        return toast[0];
    }

    /**
     * The work is done: its toast shows the result. If the user has closed the toast while the work was
     * running, the result is shown in a new toast. Nothing is shown for work that was cancelled.
     */
    private static <T> void done(Component owner, ToastController toast, ToastOption option, T result, Throwable error,
                                 Function<? super T, String> onSuccess, Function<Throwable, String> onFailure) {
        ToastType type;
        String message;
        if (error == null) {
            type = ToastType.SUCCESS;
            message = onSuccess == null ? null : onSuccess.apply(result);
        } else {
            Throwable cause = getCause(error);
            if (cause instanceof CancellationException) {
                toast.close();
                return;
            }
            type = ToastType.ERROR;
            message = onFailure != null ? onFailure.apply(cause)
                    : cause.getLocalizedMessage() != null ? cause.getLocalizedMessage() : cause.toString();
        }
        if (message == null) {
            // nothing to say
            toast.close();
        } else if (toast.isOpen() && toast.getType() == ToastType.LOADING) {
            toast.update(type, message);
        } else {
            try {
                ToastManager.show(owner, type, message, null, option, null);
            } catch (IllegalArgumentException e) {
                // the owner is not in a window any more, there is no place for the result
            }
        }
    }

    /**
     * @return the error of the work, without what the future has put around it
     */
    private static Throwable getCause(Throwable error) {
        while ((error instanceof CompletionException || error instanceof ExecutionException) && error.getCause() != null) {
            error = error.getCause();
        }
        return error;
    }

    private static final class Progress implements ToastProgress {

        private volatile boolean cancelled;
        private volatile ToastController toast;

        @Override
        public void setMessage(String message) {
            SwingUtilities.invokeLater(() -> {
                ToastController current = toast;
                // not when the result is showing already
                if (current != null && !cancelled && current.isOpen() && current.getType() == ToastType.LOADING) {
                    current.setMessage(message);
                }
            });
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }
    }
}
