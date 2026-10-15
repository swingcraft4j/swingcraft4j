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
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
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
     * <p>
     * From left to right a text of more than one character is centered by what is painted of it, not by its
     * width: in a font where all digits are as wide as each other, a 1 has space at its sides, and 11 or 15
     * would look too far right. One character is centered by its width: the font places it in its width so it
     * looks centered, and a 1 by what is painted would have its line right of the center, moved by its flag.
     * From top to bottom it is centered by the height of a capital letter, the same for every text, so the
     * texts of one row stay on one line.
     * <p>
     * What is painted of a text is not known exactly. Its outline does not know that the font moves the edges of
     * a letter to the pixels, more in a font as Segoe UI. The pixels it paints count a pixel at its edge that is
     * hardly painted as much as the others. The middle of the two is closer than each of them, in every font
     * that was tried. The height of a capital letter is taken from its pixels.
     * <p>
     * The text starts at a whole pixel of the screen. A text in the middle of two pixels is rounded to one
     * of them by where the component is painted, and that is not the same place each time: a component that
     * paints only itself again, as when the mouse moves over it, would show the text one pixel off.
     */
    static void paintText(Graphics2D g, String text, Rectangle2D bounds) {
        Font font = g.getFont();
        FontRenderContext context = g.getFontRenderContext();
        AffineTransform transform = g.getTransform();
        Point2D center = transform.transform(new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()), null);
        // where the text is from its start, in pixels of the screen. The pixels are of a start at a whole pixel,
        // they are the same from every whole pixel. The space of the font is the one of the screen, without the
        // place of the graphics
        GlyphVector glyphs = font.createGlyphVector(context, text);
        double scale = transform.getScaleX();
        Rectangle capital = font.createGlyphVector(context, "H").getPixelBounds(context, 0, 0);
        double textCenter = text.length() == 1
                ? glyphs.getLogicalBounds().getWidth() * scale / 2
                : (glyphs.getVisualBounds().getCenterX() * scale + glyphs.getPixelBounds(context, 0, 0).getCenterX()) / 2;
        Point2D start = new Point2D.Double(Math.round(center.getX() - textCenter), Math.round(center.getY() - capital.getCenterY()));
        Point2D origin = toUser(transform, start);
        g.drawString(text, (float) origin.getX(), (float) origin.getY());
    }

    // the point of the screen in the space of the graphics
    private static Point2D toUser(AffineTransform transform, Point2D point) {
        try {
            return transform.inverseTransform(point, null);
        } catch (NoninvertibleTransformException e) {
            return new Point2D.Double(point.getX(), point.getY());
        }
    }
}
