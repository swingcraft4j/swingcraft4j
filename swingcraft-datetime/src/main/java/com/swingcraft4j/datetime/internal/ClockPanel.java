package com.swingcraft4j.datetime.internal;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.swingcraft4j.datetime.option.PickerSize;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * The clock of a time picker. It shows the hours or the minutes in a circle, and a hand that points to the
 * selected one. The hand is moved with the mouse, and it turns to its place when the clock shows
 * another value.
 * <p>
 * A clock with 24 hours has two circles: the hours from 1 to 12 outside, and from 13 to 0 inside.
 */
final class ClockPanel extends JComponent {

    interface Listener {

        /**
         * The user has moved the hand to the hour (0 to 23) or the minute.
         */
        void valueChanged(int value);

        /**
         * The user has released the mouse.
         */
        void valueSelected();
    }

    private static final int SIZE = 216;
    // from the edge of the clock to the middle of the numbers
    private static final float OUTER_MARGIN = 20;
    private static final float INNER_MARGIN = 49;
    private static final float KNOB_SIZE = 30;
    // the numbers of the inner circle are a little smaller, they are closer to each other
    private static final float INNER_FONT_SIZE = -1.5f;
    private static final float CENTER_SIZE = 8;
    private static final float HAND_WIDTH = 2;

    private final Listener listener;
    private final ProgressAnimation animation = new ProgressAnimation();
    private PickerSize size = PickerSize.DEFAULT;
    private boolean hourView = true;
    private boolean hour24;
    private boolean pm;
    // the hour (0 to 23) or the minute, -1 for none
    private int value = -1;
    // where the hand is: the angle in degrees from the top, and 0 for the outer circle to 1 for the inner
    private float angle;
    private float circle;

    ClockPanel(Listener listener) {
        this.listener = listener;
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled() && SwingUtilities.isLeftMouseButton(e)) {
                    select(e.getPoint());
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isEnabled() && SwingUtilities.isLeftMouseButton(e)) {
                    select(e.getPoint());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled() && SwingUtilities.isLeftMouseButton(e)) {
                    ClockPanel.this.listener.valueSelected();
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    void setPickerSize(PickerSize size) {
        this.size = size;
        revalidate();
        repaint();
    }

    private float scale(float value) {
        return PickerUtils.scale(size, value);
    }

    /**
     * Shows the hours or the minutes with the hand at the value.
     *
     * @param hourView true for the hours, false for the minutes
     * @param hour24   true for a clock with 24 hours
     * @param pm       true if the hours of a clock with 12 hours are after noon
     * @param value    the hour (0 to 23) or the minute, -1 for none
     * @param duration the time in milliseconds the hand takes to turn to the value, 0 to place it at once
     */
    void show(boolean hourView, boolean hour24, boolean pm, int value, int duration) {
        this.hourView = hourView;
        this.hour24 = hour24;
        this.pm = pm;
        this.value = value;
        float fromAngle = angle;
        float fromCircle = circle;
        float toAngle = getAngle(value);
        float toCircle = isInnerCircle(value) ? 1 : 0;
        // the short way around
        float turn = ((toAngle - fromAngle) % 360 + 540) % 360 - 180;
        animation.start(value == -1 ? 0 : duration, 0, 1, progress -> {
            angle = fromAngle + turn * progress;
            circle = fromCircle + (toCircle - fromCircle) * progress;
            repaint();
        }, () -> {
            angle = toAngle;
            circle = toCircle;
            repaint();
        });
        repaint();
    }

    private float getAngle(int value) {
        if (value == -1) {
            return 0;
        }
        return hourView ? value % 12 * 30 : value * 6;
    }

    private boolean isInnerCircle(int value) {
        return hourView && hour24 && (value == 0 || value > 12);
    }

    private void select(Point point) {
        float size = getClockSize();
        double x = point.x - getWidth() / 2.0;
        double y = point.y - getHeight() / 2.0;
        double degrees = (Math.toDegrees(Math.atan2(x, -y)) + 360) % 360;
        int selected;
        if (hourView) {
            int hour = (int) Math.round(degrees / 30) % 12;
            if (hour24) {
                float middle = size / 2 - scale((OUTER_MARGIN + INNER_MARGIN) / 2);
                boolean inner = Math.sqrt(x * x + y * y) < middle;
                selected = inner ? (hour == 0 ? 0 : hour + 12) : (hour == 0 ? 12 : hour);
            } else {
                selected = hour + (pm ? 12 : 0);
            }
        } else {
            selected = (int) Math.round(degrees / 6) % 60;
        }
        if (selected != value) {
            // the hand follows the mouse without animation
            show(hourView, hour24, pm, selected, 0);
            listener.valueChanged(selected);
        }
    }

    private float getClockSize() {
        return Math.min(getWidth(), getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            // the hand is painted where it is, not moved to whole pixels
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setFont(getFont());
            float size = getClockSize();
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;
            g2.setColor(PickerUtils.shade(PickerUtils.background(this), 0.04f));
            g2.fill(new Ellipse2D.Float(centerX - size / 2, centerY - size / 2, size, size));

            g2.setColor(isEnabled() ? PickerUtils.foreground() : PickerUtils.disabledForeground());
            paintNumbers(g2, centerX, centerY, size);
            Color accent = isEnabled() ? PickerUtils.accentColor() : PickerUtils.disabledForeground();
            float centerSize = scale(CENTER_SIZE);
            g2.setColor(accent);
            g2.fill(new Ellipse2D.Float(centerX - centerSize / 2, centerY - centerSize / 2, centerSize, centerSize));
            if (value == -1) {
                return;
            }
            // the hand, and the numbers under its end in the color of the selection
            float margin = scale(OUTER_MARGIN + (INNER_MARGIN - OUTER_MARGIN) * circle);
            float radius = size / 2 - margin;
            double radians = Math.toRadians(angle);
            float knobX = centerX + (float) (Math.sin(radians) * radius);
            float knobY = centerY - (float) (Math.cos(radians) * radius);
            float knobSize = scale(KNOB_SIZE);
            Ellipse2D.Float knob = new Ellipse2D.Float(knobX - knobSize / 2, knobY - knobSize / 2, knobSize, knobSize);
            g2.setStroke(new BasicStroke(scale(HAND_WIDTH)));
            g2.draw(new Line2D.Float(centerX, centerY, knobX, knobY));
            g2.fill(knob);
            g2.setColor(PickerUtils.accentForeground());
            if (!hourView && value % 5 != 0 && !animation.isRunning()) {
                // a minute between two numbers
                float dot = scale(4f);
                g2.fill(new Ellipse2D.Float(knobX - dot / 2, knobY - dot / 2, dot, dot));
            }
            g2.clip(knob);
            paintNumbers(g2, centerX, centerY, size);
        } finally {
            g2.dispose();
        }
    }

    private void paintNumbers(Graphics2D g, float centerX, float centerY, float size) {
        for (int i = 0; i < 12; i++) {
            if (hourView) {
                paintNumber(g, centerX, centerY, size / 2 - scale(OUTER_MARGIN), i, i == 0 ? "12" : String.valueOf(i));
                if (hour24) {
                    Font font = g.getFont();
                    g.setFont(PickerUtils.font(this.size, INNER_FONT_SIZE));
                    paintNumber(g, centerX, centerY, size / 2 - scale(INNER_MARGIN), i, i == 0 ? "00" : String.valueOf(i + 12));
                    g.setFont(font);
                }
            } else {
                paintNumber(g, centerX, centerY, size / 2 - scale(OUTER_MARGIN), i, i == 0 ? "00" : String.valueOf(i * 5));
            }
        }
    }

    // index is the place on the clock, from 0 at the top to 11
    private void paintNumber(Graphics2D g, float centerX, float centerY, float radius, int index, String text) {
        double radians = Math.toRadians(index * 30);
        double x = centerX + Math.sin(radians) * radius;
        double y = centerY - Math.cos(radians) * radius;
        PickerUtils.paintText(g, text, new Rectangle2D.Double(x, y, 0, 0));
    }

    @Override
    public Font getFont() {
        return PickerUtils.font(size, 0);
    }

    @Override
    public Dimension getPreferredSize() {
        int length = Math.round(scale(SIZE));
        return new Dimension(length, length);
    }
}
