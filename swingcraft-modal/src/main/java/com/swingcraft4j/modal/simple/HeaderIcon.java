package com.swingcraft4j.modal.simple;

import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * The icons of the header buttons, painted so no image is needed.
 */
final class HeaderIcon implements Icon {

    enum Type {
        CLOSE, BACK
    }

    private static final int SIZE = 16;
    private static final float PADDING = 3.5f;

    private final Type type;

    HeaderIcon(Type type) {
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
            Color color = UIManager.getColor("Label.disabledForeground");
            g2.setColor(color != null ? color : Color.GRAY);
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float end = SIZE - PADDING;
            Path2D path = new Path2D.Float();
            if (type == Type.CLOSE) {
                path.moveTo(PADDING, PADDING);
                path.lineTo(end, end);
                path.moveTo(end, PADDING);
                path.lineTo(PADDING, end);
            } else {
                // the arrow points to where the previous modal is
                if (!c.getComponentOrientation().isLeftToRight()) {
                    g2.translate(SIZE, 0);
                    g2.scale(-1, 1);
                }
                float middle = SIZE / 2f;
                path.moveTo(end, middle);
                path.lineTo(PADDING, middle);
                path.moveTo(middle, PADDING);
                path.lineTo(PADDING, middle);
                path.lineTo(middle, end);
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
