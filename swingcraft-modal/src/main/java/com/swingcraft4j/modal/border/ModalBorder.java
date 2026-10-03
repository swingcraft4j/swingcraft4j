package com.swingcraft4j.modal.border;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.HiDPIUtils;
import com.formdev.flatlaf.util.UIScale;
import com.swingcraft4j.modal.option.ModalOption;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Paints the drop shadow, the round background and the outline of a modal.
 * The component must be non-opaque, its background color is used to fill the round shape.
 */
public class ModalBorder extends AbstractBorder {

    // extra space as a fraction of the arc, keeps the square content out of the round corners
    private static final float ROUND_PADDING = 0.2f;
    private static final float LIGHT_SHADOW_OPACITY = 0.22f;
    private static final float DARK_SHADOW_OPACITY = 0.5f;

    private final Insets shadowSize;
    private final Color shadowColor;
    private final float shadowOpacity;
    private final int round;
    private final int borderWidth;
    private final Color borderColor;
    private final ShadowRenderer shadowRenderer = new ShadowRenderer();

    public ModalBorder(ModalOption option) {
        this.shadowSize = option.getShadowSize();
        this.shadowColor = option.getShadowColor();
        this.shadowOpacity = option.getShadowOpacity();
        this.round = option.getRound();
        this.borderWidth = option.getBorderWidth();
        this.borderColor = option.getBorderColor();
    }

    /**
     * @return the scaled space the shadow takes on each side. The modal is only visible inside it
     */
    public Insets getShadowInsets() {
        return UIScale.scale(shadowSize);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        Insets shadow = getShadowInsets();
        float innerArc = Math.max(round - borderWidth * 2f, 0);
        int padding = UIScale.scale((int) Math.ceil(borderWidth + innerArc * ROUND_PADDING));
        insets.top = shadow.top + padding;
        insets.left = shadow.left + padding;
        insets.bottom = shadow.bottom + padding;
        insets.right = shadow.right + padding;
        return insets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            HiDPIUtils.paintAtScale1x(g2, x, y, width, height, (g2d, x1, y1, w1, h1, scaleFactor) ->
                    paintImpl(c, g2d, x1, y1, w1, h1, scaleFactor));
        } finally {
            g2.dispose();
        }
    }

    private void paintImpl(Component c, Graphics2D g, int x, int y, int width, int height, double scaleFactor) {
        Insets shadow = getShadowInsets();
        int top = (int) Math.round(shadow.top * scaleFactor);
        int left = (int) Math.round(shadow.left * scaleFactor);
        int bottom = (int) Math.round(shadow.bottom * scaleFactor);
        int right = (int) Math.round(shadow.right * scaleFactor);

        int bx = x + left;
        int by = y + top;
        int bw = width - (left + right);
        int bh = height - (top + bottom);
        if (bw <= 0 || bh <= 0) {
            return;
        }
        float arc = Math.min((float) (UIScale.scale((float) round) * scaleFactor), Math.min(bw, bh));

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // paint shadow, the difference between the sides is the shadow offset
        int blur = Math.min((left + right) / 2, (top + bottom) / 2);
        float opacity = getShadowOpacity();
        if (blur > 0 && opacity > 0) {
            Image image = shadowRenderer.getShadow(bw, bh, arc, blur, getShadowColor(), opacity);
            int offsetX = (right - left) / 2;
            int offsetY = (bottom - top) / 2;
            g.drawImage(image, bx - blur + offsetX, by - blur + offsetY, null);
        }

        // paint background
        g.setColor(c.getBackground());
        g.fill(new RoundRectangle2D.Float(bx, by, bw, bh, arc, arc));

        // paint outline
        float lineWidth = (float) (UIScale.scale((float) borderWidth) * scaleFactor);
        if (lineWidth > 0) {
            float innerArc = Math.max(arc - lineWidth * 2, 0);
            Path2D outline = new Path2D.Float(Path2D.WIND_EVEN_ODD);
            outline.append(new RoundRectangle2D.Float(bx, by, bw, bh, arc, arc), false);
            outline.append(new RoundRectangle2D.Float(bx + lineWidth, by + lineWidth, bw - lineWidth * 2, bh - lineWidth * 2, innerArc, innerArc), false);
            g.setColor(getBorderColor());
            g.fill(outline);
        }
    }

    private Color getShadowColor() {
        if (shadowColor != null) {
            return shadowColor;
        }
        Color color = UIManager.getColor("Popup.dropShadowColor");
        return color != null ? color : Color.BLACK;
    }

    private float getShadowOpacity() {
        if (shadowOpacity >= 0) {
            return Math.min(shadowOpacity, 1f);
        }
        // a soft shadow is spread over a larger area than the shadow of a popup, so it is stronger.
        // On a dark background a shadow is hard to see
        return FlatLaf.isLafDark() ? DARK_SHADOW_OPACITY : LIGHT_SHADOW_OPACITY;
    }

    private Color getBorderColor() {
        if (borderColor != null) {
            return borderColor;
        }
        Color color = UIManager.getColor("Component.borderColor");
        return color != null ? color : Color.GRAY;
    }
}
