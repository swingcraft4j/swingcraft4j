package com.swingcraft4j.toast.internal;

import com.formdev.flatlaf.util.UIScale;
import com.swingcraft4j.toast.ToastType;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * The icons of a toast, painted so no image is needed: the icon of each type and the close icon.
 * <p>
 * The icon of the loading type turns: where it is depends on the time it is painted, so it only needs to be
 * painted again and again.
 */
final class ToastIcon implements Icon {

    private static final int SIZE = 18;
    private static final int CLOSE_SIZE = 14;
    // the time of one turn of the loading icon in milliseconds
    private static final int TURN_TIME = 900;

    // null for the close icon
    private final ToastType type;
    private final Color color;

    /**
     * @return the icon of the type in the color, or null for a type without icon
     */
    static ToastIcon of(ToastType type, Color color) {
        return type == ToastType.DEFAULT ? null : new ToastIcon(type, color);
    }

    static ToastIcon close() {
        return new ToastIcon(null, null);
    }

    private ToastIcon(ToastType type, Color color) {
        this.type = type;
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            UIScale.scaleGraphics(g2);
            if (type == null) {
                paintClose(g2);
            } else if (type == ToastType.LOADING) {
                paintLoading(g2);
            } else {
                paintType(g2);
            }
        } finally {
            g2.dispose();
        }
    }

    private void paintClose(Graphics2D g) {
        Color closeColor = UIManager.getColor("Label.disabledForeground");
        g.setColor(closeColor != null ? closeColor : Color.GRAY);
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float start = 3.5f;
        float end = CLOSE_SIZE - start;
        Path2D path = new Path2D.Float();
        path.moveTo(start, start);
        path.lineTo(end, end);
        path.moveTo(end, start);
        path.lineTo(start, end);
        g.draw(path);
    }

    /**
     * A ring, and a part of it in the full color that goes around.
     */
    private void paintLoading(Graphics2D g) {
        float stroke = 2.2f;
        float size = SIZE - stroke - 2;
        float start = (SIZE - size) / 2;
        g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
        g.draw(new Ellipse2D.Float(start, start, size, size));
        double turn = (System.nanoTime() / 1000000 % TURN_TIME) / (double) TURN_TIME;
        g.setColor(color);
        g.draw(new Arc2D.Double(start, start, size, size, 90 - turn * 360, -100, Arc2D.OPEN));
    }

    private void paintType(Graphics2D g) {
        // the shape in the color of the type, the sign on it in white
        g.setColor(color);
        if (type == ToastType.WARNING) {
            Path2D triangle = new Path2D.Float();
            triangle.moveTo(9, 2);
            triangle.lineTo(17, 16);
            triangle.lineTo(1, 16);
            triangle.closePath();
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.fill(triangle);
            g.draw(triangle);
        } else {
            g.fill(new Ellipse2D.Float(0, 0, SIZE, SIZE));
        }

        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D sign = new Path2D.Float();
        switch (type) {
            case SUCCESS:
                sign.moveTo(5, 9.3f);
                sign.lineTo(7.8f, 12);
                sign.lineTo(13, 6.5f);
                break;
            case INFO:
                sign.moveTo(9, 8.5f);
                sign.lineTo(9, 13);
                sign.moveTo(9, 5.2f);
                sign.lineTo(9, 5.3f);
                break;
            case WARNING:
                sign.moveTo(9, 7);
                sign.lineTo(9, 11);
                sign.moveTo(9, 13.8f);
                sign.lineTo(9, 13.9f);
                break;
            default:
                sign.moveTo(6, 6);
                sign.lineTo(12, 12);
                sign.moveTo(12, 6);
                sign.lineTo(6, 12);
                break;
        }
        g.draw(sign);
    }

    @Override
    public int getIconWidth() {
        return UIScale.scale(type == null ? CLOSE_SIZE : SIZE);
    }

    @Override
    public int getIconHeight() {
        return UIScale.scale(type == null ? CLOSE_SIZE : SIZE);
    }
}
