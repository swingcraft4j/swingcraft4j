package com.swingcraft4j.datetime.internal;

import com.formdev.flatlaf.util.CubicBezierEasing;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Moves a progress value from one value to another in a given time.
 * Without time, the progress is set to the end and the animation finishes at once.
 * <p>
 * The time of the animation is the time of its frames, and a frame that takes long does not count for more
 * than {@link #MAXIMUM_FRAME_TIME}. So when the first frames are slow, as they are when a picker is shown and
 * all its components are painted for the first time, the animation takes longer but does not jump over the
 * part of it that was not seen.
 * <p>
 * All running animations have the same timer: the animations that run at the same time change in the
 * same frame, and the window is painted one time for the frame and not one time for each of them.
 */
final class ProgressAnimation {

    interface Target {

        void progressChanged(float progress);
    }

    // the time between two frames in milliseconds, the timer of the system is not more exact than about 15
    private static final int RESOLUTION = 10;
    // two frames of a 60 Hz screen, in nanoseconds
    private static final long MAXIMUM_FRAME_TIME = 34_000_000;

    // the animations that are running, and the timer of their frames. It runs while there is an animation
    private static final List<ProgressAnimation> running = new ArrayList<>();
    private static Timer timer;

    // counts the animations that were started, so a frame knows if its target has started another one
    private int started;
    private boolean active;
    private Target target;
    private Runnable onFinished;
    private float from;
    private float to;
    // in nanoseconds
    private long duration;
    private long elapsed;
    private long frameTime;
    private boolean firstFrame;

    /**
     * Starts a new animation, the running one is stopped where it is and never finishes.
     *
     * @param duration   the time in milliseconds, 0 to finish at once
     * @param onFinished runs when the progress has reached the end
     */
    void start(int duration, float from, float to, Target target, Runnable onFinished) {
        cancel();
        if (duration <= 0 || from == to) {
            target.progressChanged(to);
            onFinished.run();
            return;
        }
        this.target = target;
        this.onFinished = onFinished;
        this.from = from;
        this.to = to;
        this.duration = duration * 1_000_000L;
        elapsed = 0;
        firstFrame = true;
        started++;
        active = true;
        running.add(this);
        if (timer == null) {
            timer = new Timer(RESOLUTION, e -> nextFrames());
            timer.setInitialDelay(0);
            timer.start();
        }
    }

    private static void nextFrames() {
        // an animation can start or stop another one
        for (ProgressAnimation animation : new ArrayList<>(running)) {
            if (animation.active) {
                animation.nextFrame();
            }
        }
    }

    private void nextFrame() {
        long time = System.nanoTime();
        // the time starts with the first frame: what was shown for the animation has been painted then
        if (!firstFrame) {
            elapsed += Math.min(time - frameTime, MAXIMUM_FRAME_TIME);
        }
        firstFrame = false;
        frameTime = time;
        if (elapsed >= duration) {
            int frameStarted = started;
            target.progressChanged(to);
            // the target may have started another animation
            if (active && started == frameStarted) {
                finished();
            }
        } else {
            float fraction = CubicBezierEasing.STANDARD_EASING.interpolate((float) elapsed / duration);
            target.progressChanged(from + (to - from) * fraction);
        }
    }

    /**
     * Jumps to the end of the running animation. Does nothing if no animation is running.
     */
    void finish() {
        if (active) {
            target.progressChanged(to);
            finished();
        }
    }

    boolean isRunning() {
        return active;
    }

    private void cancel() {
        if (active) {
            active = false;
            target = null;
            onFinished = null;
            removeRunning();
        }
    }

    private void finished() {
        Runnable runnable = onFinished;
        active = false;
        target = null;
        onFinished = null;
        removeRunning();
        runnable.run();
    }

    private void removeRunning() {
        running.remove(this);
        if (running.isEmpty() && timer != null) {
            timer.stop();
            timer = null;
        }
    }
}
