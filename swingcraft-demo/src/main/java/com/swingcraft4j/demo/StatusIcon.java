package com.swingcraft4j.demo;

import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * The icon of a label in the status bar, painted so no image is needed. The cup of java has its own colors,
 * the other icons take the color of the text of the label.
 */
class StatusIcon implements Icon {

    enum Type {
        VERSION, JAVA, OS
    }

    private static final int SIZE = 16;
    private static final Color JAVA_CUP = new Color(0x5382A1);
    private static final Color JAVA_STEAM = new Color(0xE76F00);

    private final Type type;

    StatusIcon(Type type) {
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
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D path = new Path2D.Float();
            switch (type) {
                case JAVA:
                    // the steam
                    path.moveTo(5.5f, 5.5f);
                    path.curveTo(4.3f, 4.2f, 6.7f, 2.8f, 5.5f, 1.5f);
                    path.moveTo(8.5f, 5.5f);
                    path.curveTo(7.3f, 4.2f, 9.7f, 2.8f, 8.5f, 1.5f);
                    g2.setColor(JAVA_STEAM);
                    g2.draw(path);

                    // the cup with its handle, on a saucer
                    path = new Path2D.Float();
                    path.moveTo(3, 7.5f);
                    path.lineTo(11, 7.5f);
                    path.lineTo(11, 10);
                    path.quadTo(11, 12.5f, 8, 12.5f);
                    path.lineTo(6, 12.5f);
                    path.quadTo(3, 12.5f, 3, 10);
                    path.closePath();
                    path.moveTo(11, 8.5f);
                    path.curveTo(14, 8.5f, 14, 11.5f, 11, 11.5f);
                    path.moveTo(2.5f, 14.75f);
                    path.lineTo(11.5f, 14.75f);
                    g2.setColor(JAVA_CUP);
                    break;
                case OS:
                    // a screen on its stand
                    g2.draw(new RoundRectangle2D.Float(1.5f, 2.5f, 13, 9, 2, 2));
                    path.moveTo(8, 11.5f);
                    path.lineTo(8, 14);
                    path.moveTo(5, 14);
                    path.lineTo(11, 14);
                    break;
                default:
                    // a tag
                    g2.fill(new Ellipse2D.Float(4.5f, 4.5f, 2, 2));
                    path.moveTo(2.5f, 2.5f);
                    path.lineTo(8, 2.5f);
                    path.lineTo(13.5f, 8);
                    path.lineTo(8, 13.5f);
                    path.lineTo(2.5f, 8);
                    path.closePath();
                    break;
            }
            g2.draw(path);
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
