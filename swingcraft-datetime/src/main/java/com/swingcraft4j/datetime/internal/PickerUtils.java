package com.swingcraft4j.datetime.internal;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.Animator;
import com.formdev.flatlaf.util.ColorFunctions;
import com.formdev.flatlaf.util.UIScale;
import com.swingcraft4j.datetime.option.AnimationOption;
import com.swingcraft4j.datetime.option.PickerSize;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

/**
 * The colors and the painting that the date picker and the time picker have in common.
 */
final class PickerUtils {

    private PickerUtils() {
    }

    /**
     * @return the time of an animation in milliseconds, 0 if the animation is off
     */
    static int duration(AnimationOption option) {
        return option.isEnabled() && Animator.useAnimation() ? option.getDuration() : 0;
    }

    /**
     * @return the value for the size of the picker, scaled with the UI scale factor
     */
    static float scale(PickerSize size, float value) {
        return UIScale.scale(value * size.getScale());
    }

    /**
     * @param extra what is added to the size of the font, for a text that is larger than the others
     * @return the font of the look and feel in the size of the picker
     */
    static Font font(PickerSize size, float extra) {
        Font font = UIManager.getFont("Label.font");
        if (font == null) {
            font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
        return font.deriveFont(font.getSize2D() + UIScale.scale((float) size.getFontOffset()) + scale(size, extra));
    }

    /**
     * Paints a line inside the edge of the shape. It is filled and not drawn: a line that is drawn is moved
     * to whole pixels, and a circle is not round then.
     *
     * @param shape an ellipse or a rectangle with round corners
     */
    static void paintOutline(Graphics2D g, RectangularShape shape, float lineWidth) {
        Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        path.append(shape, false);
        path.append(inset(shape, lineWidth), false);
        g.fill(path);
    }

    /**
     * @return the shape made smaller by the amount at each side. The corners of a rectangle with round
     * corners get smaller with it, so the two shapes have the same distance all around
     */
    static RectangularShape inset(RectangularShape shape, float amount) {
        double x = shape.getX() + amount;
        double y = shape.getY() + amount;
        double width = shape.getWidth() - amount * 2;
        double height = shape.getHeight() - amount * 2;
        if (shape instanceof RoundRectangle2D) {
            RoundRectangle2D round = (RoundRectangle2D) shape;
            return new RoundRectangle2D.Double(x, y, width, height,
                    Math.max(round.getArcWidth() - amount * 2, 0), Math.max(round.getArcHeight() - amount * 2, 0));
        }
        RectangularShape inner = (RectangularShape) shape.clone();
        inner.setFrame(x, y, width, height);
        return inner;
    }

    /**
     * Paints the mark of today on a cell: a line around a cell that is not selected, in the accent color.
     * A selected cell is filled with the accent color, so the line is inside of it, with a gap to its edge
     * and in the color of its text: today can be seen when it is selected too.
     *
     * @param shape     the shape of the cell, an ellipse or a rectangle with round corners
     * @param lineWidth the width of the line
     * @param gap       the space between the edge of a selected cell and the line
     */
    static void paintCurrentMark(Graphics2D g, RectangularShape shape, boolean selected, float lineWidth, float gap,
                                 Color accent) {
        if (!selected) {
            g.setColor(accent);
            paintOutline(g, shape, lineWidth);
            return;
        }
        g.setColor(accentForeground(accent));
        paintOutline(g, inset(shape, gap), lineWidth);
    }

    /**
     * Paints the mark of the keyboard on a cell: a line of dashes at the edge of the cell, as large as
     * the shape of a selected cell. It does not look as the line of today, which has no dashes.
     *
     * @param shape the shape of the cell, an ellipse or a rectangle with round corners
     * @param scale the scale of the sizes, as 1 for no scale
     */
    static void paintCursor(Graphics2D g, RectangularShape shape, float scale) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            // a line that is drawn is moved to whole pixels, and a circle is not round then
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setColor(foreground());
            float width = 1.2f * scale;
            g2.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1,
                    new float[]{3 * scale, 2.5f * scale}, 0));
            // the middle of the line is half its width inside, so its outer edge is the edge of the shape
            g2.draw(inset(shape, width / 2));
        } finally {
            g2.dispose();
        }
    }

    /**
     * @param color the color of the style, or null
     * @return the color of what is selected: the one of the style, or the accent color of the look and feel
     */
    static Color accentColor(Color color) {
        if (color != null) {
            return color;
        }
        color = UIManager.getColor("Component.accentColor");
        return color != null ? color : new Color(0x007AFF);
    }

    /**
     * @return the color of a text on the accent color
     */
    static Color accentForeground(Color accent) {
        return ColorFunctions.luma(accent) < 0.7f ? Color.WHITE : new Color(0x1E1E1E);
    }

    static Color foreground() {
        Color color = UIManager.getColor("Label.foreground");
        return color != null ? color : Color.BLACK;
    }

    static Color disabledForeground() {
        Color color = UIManager.getColor("Label.disabledForeground");
        return color != null ? color : Color.GRAY;
    }

    /**
     * @return the background a little darker with a light look and feel, and a little lighter with a dark one
     */
    static Color shade(Color background, float amount) {
        return FlatLaf.isLafDark() ? ColorFunctions.lighten(background, amount) : ColorFunctions.darken(background, amount);
    }

    static Color background(Component component) {
        Color color = FlatUIUtils.getParentBackground((JComponent) component);
        return color != null ? color : UIManager.getColor("Panel.background");
    }

    /**
     * Paints the text in the center of the bounds.
     */
    static void paintText(Graphics2D g, String text, Rectangle2D bounds) {
        FontMetrics metrics = g.getFontMetrics();
        float x = (float) (bounds.getCenterX() - metrics.stringWidth(text) / 2f);
        float y = (float) (bounds.getCenterY() - metrics.getHeight() / 2f + metrics.getAscent());
        g.drawString(text, x, y);
    }
}
