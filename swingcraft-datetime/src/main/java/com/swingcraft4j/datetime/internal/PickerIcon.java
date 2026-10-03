package com.swingcraft4j.datetime.internal;

import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * The icons of the header buttons, painted so no image is needed.
 */
final class PickerIcon implements Icon {

    enum Type {
        BACK, FORWARD
    }

    private static final int SIZE = 16;

    private final Type type;

    PickerIcon(Type type) {
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
            g2.setColor(c.isEnabled() ? PickerUtils.foreground() : PickerUtils.disabledForeground());
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // back points to the leading side
            boolean left = (type == Type.BACK) == c.getComponentOrientation().isLeftToRight();
            float middle = SIZE / 2f;
            float tip = left ? 6 : 10;
            float end = left ? 10 : 6;
            Path2D path = new Path2D.Float();
            path.moveTo(end, 4);
            path.lineTo(tip, middle);
            path.lineTo(end, 12);
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
