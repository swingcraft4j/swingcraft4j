package com.swingcraft4j.demo;

import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * The icon of a message modal: a circle in the color of the type with a white symbol, painted so no image is needed.
 */
class MessageIcon implements Icon {

    enum Type {
        DEFAULT("Message", new Color(0x64748B)),
        SUCCESS("Success", new Color(0x1EA97C)),
        INFO("Information", new Color(0x3B82F6)),
        WARNING("Warning", new Color(0xCC8925)),
        ERROR("Error", new Color(0xFF5757));

        private final String title;
        private final Color color;

        Type(String title, Color color) {
            this.title = title;
            this.color = color;
        }

        String getTitle() {
            return title;
        }
    }

    private static final int SIZE = 32;

    private final Type type;

    MessageIcon(Type type) {
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
            g2.setColor(type.color);
            g2.fill(new Ellipse2D.Float(0, 0, SIZE, SIZE));

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D path = new Path2D.Float();
            switch (type) {
                case SUCCESS:
                    path.moveTo(9.5f, 16.5f);
                    path.lineTo(14, 21);
                    path.lineTo(22.5f, 11.5f);
                    break;
                case INFO:
                    g2.fill(new Ellipse2D.Float(14.4f, 8.4f, 3.2f, 3.2f));
                    path.moveTo(16, 15);
                    path.lineTo(16, 23);
                    break;
                case WARNING:
                    path.moveTo(16, 9);
                    path.lineTo(16, 17);
                    g2.fill(new Ellipse2D.Float(14.4f, 20.4f, 3.2f, 3.2f));
                    break;
                case ERROR:
                    path.moveTo(11, 11);
                    path.lineTo(21, 21);
                    path.moveTo(21, 11);
                    path.lineTo(11, 21);
                    break;
                default:
                    path.moveTo(10, 13);
                    path.lineTo(22, 13);
                    path.moveTo(10, 19);
                    path.lineTo(18, 19);
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
