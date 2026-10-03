package com.swingcraft4j.datetime.internal;

import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * The icons of the pickers and the fields, painted so no image is needed.
 */
public final class PickerIcon implements Icon {

    public enum Type {
        BACK, FORWARD, CALENDAR, CLOCK, CLEAR, ERROR, WARNING, SUCCESS
    }

    private static final int SIZE = 16;

    private final Type type;

    public PickerIcon(Type type) {
        this.type = type;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            UIScale.scaleGraphics(g2);
            g2.setColor(getColor(c));
            g2.setStroke(new BasicStroke(type == Type.BACK || type == Type.FORWARD ? 1.4f : 1.1f,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (type) {
                case CALENDAR:
                    paintCalendar(g2);
                    break;
                case CLOCK:
                    paintClock(g2);
                    break;
                case CLEAR:
                    paintClear(g2);
                    break;
                case ERROR:
                case WARNING:
                case SUCCESS:
                    paintStatus(g2);
                    break;
                default:
                    paintArrow(g2, c);
                    break;
            }
        } finally {
            g2.dispose();
        }
    }

    private void paintArrow(Graphics2D g, Component c) {
        // back points to the leading side
        boolean left = (type == Type.BACK) == c.getComponentOrientation().isLeftToRight();
        float tip = left ? 6 : 10;
        float end = left ? 10 : 6;
        Path2D path = new Path2D.Float();
        path.moveTo(end, 4);
        path.lineTo(tip, SIZE / 2f);
        path.lineTo(end, 12);
        g.draw(path);
    }

    // a sheet with a line below its top and the two rings that hold it
    private void paintCalendar(Graphics2D g) {
        g.draw(new RoundRectangle2D.Float(2.5f, 3.5f, 11, 10, 3, 3));
        Path2D path = new Path2D.Float();
        path.moveTo(2.5f, 6.5f);
        path.lineTo(13.5f, 6.5f);
        path.moveTo(5.5f, 2);
        path.lineTo(5.5f, 4.5f);
        path.moveTo(10.5f, 2);
        path.lineTo(10.5f, 4.5f);
        g.draw(path);
    }

    private void paintClock(Graphics2D g) {
        g.draw(new Ellipse2D.Float(2.5f, 2.5f, 11, 11));
        Path2D path = new Path2D.Float();
        path.moveTo(8, 5);
        path.lineTo(8, 8);
        path.lineTo(10.2f, 9.4f);
        g.draw(path);
    }

    private void paintClear(Graphics2D g) {
        Path2D path = new Path2D.Float();
        path.moveTo(5, 5);
        path.lineTo(11, 11);
        path.moveTo(11, 5);
        path.lineTo(5, 11);
        g.draw(path);
    }

    // the icons of a validation have the color the look and feel gives the border of a field with it
    private Color getColor(Component c) {
        String key = null;
        if (type == Type.ERROR) {
            key = "Component.error.focusedBorderColor";
        } else if (type == Type.WARNING) {
            key = "Component.warning.focusedBorderColor";
        } else if (type == Type.SUCCESS) {
            key = "Component.success.focusedBorderColor";
        }
        Color color = key != null ? UIManager.getColor(key) : null;
        if (color != null) {
            return color;
        }
        return c.isEnabled() ? PickerUtils.foreground() : PickerUtils.disabledForeground();
    }

    // a triangle for a warning and a circle for the others, with a mark or a check inside
    private void paintStatus(Graphics2D g) {
        Path2D path = new Path2D.Float();
        if (type == Type.WARNING) {
            path.moveTo(8, 2.5f);
            path.lineTo(14, 13.5f);
            path.lineTo(2, 13.5f);
            path.closePath();
        } else {
            path.append(new Ellipse2D.Float(2.5f, 2.5f, 11, 11), false);
        }
        if (type == Type.SUCCESS) {
            path.moveTo(5.4f, 8.2f);
            path.lineTo(7.2f, 10);
            path.lineTo(10.6f, 6.2f);
        } else {
            float top = type == Type.WARNING ? 6.5f : 5.2f;
            path.moveTo(8, top);
            path.lineTo(8, top + 3);
            path.moveTo(8, top + 5.3f);
            path.lineTo(8, top + 5.4f);
        }
        g.draw(path);
    }

    @Override
    public int getIconWidth() {
        return UIScale.scale(SIZE);
    }

    @Override
    public int getIconHeight() {
        return UIScale.scale(SIZE);
    }
}
