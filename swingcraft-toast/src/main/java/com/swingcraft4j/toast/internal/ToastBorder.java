package com.swingcraft4j.toast.internal;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.ColorFunctions;
import com.formdev.flatlaf.util.HiDPIUtils;
import com.formdev.flatlaf.util.UIScale;
import com.swingcraft4j.toast.option.BackgroundType;
import com.swingcraft4j.toast.option.BorderType;
import com.swingcraft4j.toast.option.ProgressLinePosition;
import com.swingcraft4j.toast.option.StyleOption;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Paints the drop shadow, the round background and the line of a toast.
 * The toast must be non-opaque, its background color is used to fill the round shape.
 */
final class ToastBorder extends AbstractBorder {

    // extra space as a fraction of the arc, keeps the square content out of the round corners
    private static final float ROUND_PADDING = 0.2f;
    private static final int OUTLINE_WIDTH = 1;
    private static final float LIGHT_SHADOW_OPACITY = 0.22f;
    private static final float DARK_SHADOW_OPACITY = 0.5f;

    private final ToastPanel toast;
    private final StyleOption style;
    private final ShadowRenderer shadowRenderer = new ShadowRenderer();

    ToastBorder(ToastPanel toast) {
        this.toast = toast;
        this.style = toast.getOption().getStyleOption();
    }

    /**
     * @return the scaled space the shadow takes on each side. The toast is only visible inside it
     */
    Insets getShadowInsets() {
        return UIScale.scale(style.getShadowSize());
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        Insets shadow = getShadowInsets();
        Insets padding = UIScale.scale(style.getPadding());
        int roundPadding = UIScale.scale((int) Math.ceil(style.getRound() * ROUND_PADDING));
        insets.top = shadow.top + roundPadding + padding.top;
        insets.left = shadow.left + roundPadding + padding.left;
        insets.bottom = shadow.bottom + roundPadding + padding.bottom;
        insets.right = shadow.right + roundPadding + padding.right;

        // the content is not over the line
        boolean ltr = c.getComponentOrientation().isLeftToRight();
        int line = UIScale.scale(style.getLineSize());
        switch (style.getBorderType()) {
            case LEADING_LINE:
                if (ltr) {
                    insets.left += line;
                } else {
                    insets.right += line;
                }
                break;
            case TRAILING_LINE:
                if (ltr) {
                    insets.right += line;
                } else {
                    insets.left += line;
                }
                break;
            case TOP_LINE:
                insets.top += line;
                break;
            case BOTTOM_LINE:
                insets.bottom += line;
                break;
            default:
                break;
        }
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

    /**
     * @return the space the shadow takes on each side, in the pixels of the screen
     */
    private Insets getShadowInsets(double scaleFactor) {
        Insets shadow = getShadowInsets();
        return new Insets(
                (int) Math.round(shadow.top * scaleFactor),
                (int) Math.round(shadow.left * scaleFactor),
                (int) Math.round(shadow.bottom * scaleFactor),
                (int) Math.round(shadow.right * scaleFactor));
    }

    /**
     * @return the corner arc diameter of a toast with the visible size, in the pixels of the screen
     */
    private float getArc(int width, int height, double scaleFactor) {
        return Math.min((float) (UIScale.scale((float) style.getRound()) * scaleFactor), Math.min(width, height));
    }

    /**
     * Paints the line that shows how much of the delay to close the toast is left, at the top or the bottom
     * of the toast and from its leading side. It is not part of the border: it changes all the time,
     * and the border is painted to an image.
     *
     * @param remaining how much of the delay is left, from 0 to 1
     */
    void paintProgressLine(Component c, Graphics g, float remaining) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            HiDPIUtils.paintAtScale1x(g2, 0, 0, c.getWidth(), c.getHeight(), (g2d, x1, y1, w1, h1, scaleFactor) ->
                    paintProgressLineImpl(c, g2d, x1, y1, w1, h1, scaleFactor, remaining));
        } finally {
            g2.dispose();
        }
    }

    private void paintProgressLineImpl(Component c, Graphics2D g, int x, int y, int width, int height, double scaleFactor, float remaining) {
        Insets shadow = getShadowInsets(scaleFactor);
        int bx = x + shadow.left;
        int by = y + shadow.top;
        int bw = width - (shadow.left + shadow.right);
        int bh = height - (shadow.top + shadow.bottom);
        float lineHeight = (float) (UIScale.scale((float) style.getProgressLineSize()) * scaleFactor);
        float lineWidth = bw * remaining;
        if (bw <= 0 || bh <= 0 || lineHeight <= 0 || lineWidth <= 0) {
            return;
        }
        float arc = getArc(bw, bh, scaleFactor);
        boolean ltr = c.getComponentOrientation().isLeftToRight();
        boolean top = style.getProgressLinePosition() == ProgressLinePosition.TOP;
        Rectangle2D line = new Rectangle2D.Float(ltr ? bx : bx + bw - lineWidth, top ? by : by + bh - lineHeight, lineWidth, lineHeight);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        // the line follows the round corners
        Area area = new Area(new RoundRectangle2D.Float(bx, by, bw, bh, arc, arc));
        area.intersect(new Area(line));
        g.setColor(style.getProgressLineColor() != null ? style.getProgressLineColor() : toast.getColor());
        g.fill(area);
    }

    private void paintImpl(Component c, Graphics2D g, int x, int y, int width, int height, double scaleFactor) {
        Insets shadow = getShadowInsets(scaleFactor);
        int top = shadow.top;
        int left = shadow.left;
        int bottom = shadow.bottom;
        int right = shadow.right;

        int bx = x + left;
        int by = y + top;
        int bw = width - (left + right);
        int bh = height - (top + bottom);
        if (bw <= 0 || bh <= 0) {
            return;
        }
        float arc = getArc(bw, bh, scaleFactor);

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
        Shape shape = new RoundRectangle2D.Float(bx, by, bw, bh, arc, arc);
        Color background = c.getBackground();
        Color color = toast.getColor();
        boolean ltr = c.getComponentOrientation().isLeftToRight();
        if (style.getBackgroundType() == BackgroundType.GRADIENT) {
            // the color of the type fades out from the leading side
            float start = ltr ? bx : bx + bw;
            float end = ltr ? bx + bw * 0.8f : bx + bw * 0.2f;
            g.setPaint(new GradientPaint(start, 0, ColorFunctions.mix(color, background, 0.3f), end, 0, background));
        } else {
            g.setColor(background);
        }
        g.fill(shape);

        // paint line
        BorderType borderType = style.getBorderType();
        if (borderType == BorderType.OUTLINE) {
            float lineWidth = (float) (UIScale.scale((float) OUTLINE_WIDTH) * scaleFactor);
            float innerArc = Math.max(arc - lineWidth * 2, 0);
            Path2D outline = new Path2D.Float(Path2D.WIND_EVEN_ODD);
            outline.append(shape, false);
            outline.append(new RoundRectangle2D.Float(bx + lineWidth, by + lineWidth, bw - lineWidth * 2, bh - lineWidth * 2, innerArc, innerArc), false);
            g.setColor(color);
            g.fill(outline);
        } else if (borderType != BorderType.DEFAULT) {
            float lineWidth = (float) (UIScale.scale((float) style.getLineSize()) * scaleFactor);
            Rectangle2D line;
            if (borderType == BorderType.TOP_LINE) {
                line = new Rectangle2D.Float(bx, by, bw, lineWidth);
            } else if (borderType == BorderType.BOTTOM_LINE) {
                line = new Rectangle2D.Float(bx, by + bh - lineWidth, bw, lineWidth);
            } else if ((borderType == BorderType.LEADING_LINE) == ltr) {
                line = new Rectangle2D.Float(bx, by, lineWidth, bh);
            } else {
                line = new Rectangle2D.Float(bx + bw - lineWidth, by, lineWidth, bh);
            }
            // the line follows the round corners
            Area area = new Area(shape);
            area.intersect(new Area(line));
            g.setColor(ColorFunctions.mix(color, background, 0.6f));
            g.fill(area);
        }
    }

    private Color getShadowColor() {
        if (style.getShadowColor() != null) {
            return style.getShadowColor();
        }
        Color color = UIManager.getColor("Popup.dropShadowColor");
        return color != null ? color : Color.BLACK;
    }

    private float getShadowOpacity() {
        if (style.getShadowOpacity() >= 0) {
            return Math.min(style.getShadowOpacity(), 1f);
        }
        // a soft shadow is spread over a larger area than the shadow of a popup, so it is stronger.
        // On a dark background a shadow is hard to see
        return FlatLaf.isLafDark() ? DARK_SHADOW_OPACITY : LIGHT_SHADOW_OPACITY;
    }
}
