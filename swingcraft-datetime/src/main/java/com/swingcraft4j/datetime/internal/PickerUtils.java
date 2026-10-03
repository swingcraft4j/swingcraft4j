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
        RectangularShape inner = (RectangularShape) shape.clone();
        inner.setFrame(shape.getX() + lineWidth, shape.getY() + lineWidth,
                shape.getWidth() - lineWidth * 2, shape.getHeight() - lineWidth * 2);
        Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        path.append(shape, false);
        path.append(inner, false);
        g.fill(path);
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
    static void paintCurrentMark(Graphics2D g, RectangularShape shape, boolean selected, float lineWidth, float gap) {
        if (!selected) {
            g.setColor(accentColor());
            paintOutline(g, shape, lineWidth);
            return;
        }
        RectangularShape inner = (RectangularShape) shape.clone();
        inner.setFrame(shape.getX() + gap, shape.getY() + gap, shape.getWidth() - gap * 2, shape.getHeight() - gap * 2);
        g.setColor(accentForeground());
        paintOutline(g, inner, lineWidth);
    }

    static Color accentColor() {
        Color color = UIManager.getColor("Component.accentColor");
        return color != null ? color : new Color(0x007AFF);
    }

    /**
     * @return the color of a text on the accent color
     */
    static Color accentForeground() {
        return ColorFunctions.luma(accentColor()) < 0.7f ? Color.WHITE : new Color(0x1E1E1E);
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
