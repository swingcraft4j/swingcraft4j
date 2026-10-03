package com.swingcraft4j.datetime.internal;

import javax.swing.*;
import java.awt.*;

/**
 * Shows one component, and slides to the next one: the next component comes in from one side while the
 * current one goes out at the other side. The real components are moved, so they are sharp all the time.
 */
final class SlidePanel extends JPanel {

    /**
     * Where the next component comes from. Forward and backward follow the component orientation.
     */
    enum Direction {
        /**
         * From the trailing side, as the next month.
         */
        FORWARD,
        /**
         * From the leading side, as the month before.
         */
        BACKWARD,
        /**
         * From the top.
         */
        DOWN,
        /**
         * From the bottom.
         */
        UP
    }

    private final ProgressAnimation animation = new ProgressAnimation();
    private Component current;
    // the component that slides out, null if no slide is running
    private Component previous;
    private Direction direction;
    private float progress = 1;
    private Dimension size;

    SlidePanel() {
        super(null);
        setOpaque(false);
    }

    /**
     * @param size the preferred size, the same for all the components that are shown
     */
    void setFixedSize(Dimension size) {
        this.size = size;
    }

    /**
     * Shows the component in place of the current one.
     *
     * @param duration the time of the slide in milliseconds, 0 to show the component at once
     */
    void show(Component component, Direction direction, int duration) {
        // a slide that is running ends here
        animation.finish();
        if (current == null || duration <= 0 || !isShowing()) {
            if (current != null) {
                remove(current);
            }
            current = component;
            add(component);
            progress = 1;
            revalidate();
            repaint();
            return;
        }
        previous = current;
        current = component;
        this.direction = direction;
        progress = 0;
        add(component);
        layoutComponents();
        animation.start(duration, 0, 1, value -> {
            progress = value;
            layoutComponents();
        }, () -> {
            remove(previous);
            previous = null;
            layoutComponents();
            repaint();
        });
    }

    Component getCurrent() {
        return current;
    }

    @Override
    public void doLayout() {
        layoutComponents();
    }

    private void layoutComponents() {
        int width = getWidth();
        int height = getHeight();
        if (current == null) {
            return;
        }
        if (previous == null) {
            current.setBounds(0, 0, width, height);
            return;
        }
        int x = 0;
        int y = 0;
        switch (direction) {
            case FORWARD:
            case BACKWARD:
                boolean fromRight = (direction == Direction.FORWARD) == getComponentOrientation().isLeftToRight();
                x = Math.round(width * (1 - progress)) * (fromRight ? 1 : -1);
                previous.setBounds(x - (fromRight ? width : -width), 0, width, height);
                break;
            case DOWN:
                y = -Math.round(height * (1 - progress));
                previous.setBounds(0, y + height, width, height);
                break;
            default:
                y = Math.round(height * (1 - progress));
                previous.setBounds(0, y - height, width, height);
                break;
        }
        current.setBounds(x, y, width, height);
    }

    @Override
    public Dimension getPreferredSize() {
        if (size != null) {
            return new Dimension(size);
        }
        return current != null ? current.getPreferredSize() : new Dimension();
    }
}
