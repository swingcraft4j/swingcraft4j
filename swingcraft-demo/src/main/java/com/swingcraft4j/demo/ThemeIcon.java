package com.swingcraft4j.demo;

import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * The icon of the themes button: a palette, painted so no image is needed. It takes the color of the text
 * of the button.
 */
class ThemeIcon implements Icon {

    private static final int SIZE = 16;

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            UIScale.scaleGraphics(g2);
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Ellipse2D.Float(1.5f, 1.5f, 13, 13));
            // the colors, and the hole for the thumb
            g2.fill(new Ellipse2D.Float(3.8f, 5.6f, 2.4f, 2.4f));
            g2.fill(new Ellipse2D.Float(6.8f, 3.4f, 2.4f, 2.4f));
            g2.fill(new Ellipse2D.Float(9.8f, 5.6f, 2.4f, 2.4f));
            g2.draw(new Ellipse2D.Float(8.2f, 9.2f, 3, 3));
        } finally {
            g2.dispose();
        }
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
