package com.swingcraft4j.datetime.internal;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.MigLayoutVisualPadding;
import com.formdev.flatlaf.util.ColorFunctions;
import com.formdev.flatlaf.util.UIScale;
import com.swingcraft4j.datetime.option.PickerSize;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * A button of the header of a picker: the month, the year and the arrows of the date picker, and the hour,
 * the minute, AM and PM of the time picker. It is painted here, so all of them look the same with every
 * look and feel: no border, a background while the mouse is over it, and the accent color when it is
 * selected. Its corners are as round as the buttons of the look and feel.
 */
final class PickerButton extends JButton {

    private float fontSize;
    private boolean bold;
    private int paddingX = 6;
    private int paddingY = 4;
    private boolean filled;
    private boolean circle;
    private boolean leading;
    private String widthText;
    private PickerSize size = PickerSize.DEFAULT;
    // the color of the style for a selected button, null for the accent color of the look and feel
    private Color accentColor;

    PickerButton() {
        setFocusable(false);
        setRolloverEnabled(true);
    }

    /**
     * @param fontSize what is added to the size of the font
     */
    PickerButton font(float fontSize, boolean bold) {
        this.fontSize = fontSize;
        this.bold = bold;
        return this;
    }

    /**
     * @param paddingX the space at the left and the right of the text or the icon
     * @param paddingY the space above and below it
     */
    PickerButton padding(int paddingX, int paddingY) {
        this.paddingX = paddingX;
        this.paddingY = paddingY;
        return this;
    }

    /**
     * Paints a background also when the mouse is not over the button, so it looks like a field.
     */
    PickerButton filled() {
        this.filled = true;
        return this;
    }

    /**
     * Makes the button a circle, for an icon.
     */
    PickerButton circle() {
        this.circle = true;
        return this;
    }

    /**
     * Puts the text at the leading side of the button, and not in its center.
     */
    PickerButton leading() {
        this.leading = true;
        return this;
    }

    /**
     * @param widthText a text the button is at least as wide as, so it keeps its width when its text changes
     */
    PickerButton minimumText(String widthText) {
        this.widthText = widthText;
        return this;
    }

    void setPickerSize(PickerSize size) {
        this.size = size;
        revalidate();
        repaint();
    }

    /**
     * @param accentColor the color of the button when it is selected, or null for the accent color of the
     *                    look and feel
     */
    void setAccentColor(Color accentColor) {
        this.accentColor = accentColor;
        repaint();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        // the look and feel paints nothing of this button
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        // the look and feel tells MigLayout that its buttons have a focus border around them, and MigLayout
        // makes them larger by that border: larger than their cell, so they are cut off at the edge of their
        // panel. This button has no border, it is as large as its cell
        setBorder(null);
        MigLayoutVisualPadding.uninstall(this);
    }

    @Override
    public Font getFont() {
        // the constructor of the button asks for the font before the size is set
        Font font = PickerUtils.font(size != null ? size : PickerSize.DEFAULT, fontSize);
        return bold ? font.deriveFont(Font.BOLD) : font;
    }

    @Override
    public Dimension getPreferredSize() {
        int width;
        int height;
        Icon icon = getIcon();
        if (icon != null) {
            width = icon.getIconWidth();
            height = icon.getIconHeight();
        } else {
            FontMetrics metrics = getFontMetrics(getFont());
            String text = getText() != null ? getText() : "";
            width = Math.max(metrics.stringWidth(text), widthText != null ? metrics.stringWidth(widthText) : 0);
            height = metrics.getHeight();
        }
        width += Math.round(PickerUtils.scale(size, paddingX)) * 2;
        height += Math.round(PickerUtils.scale(size, paddingY)) * 2;
        if (circle) {
            width = height = Math.max(width, height);
        }
        return new Dimension(width, height);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            Color background = PickerUtils.background(this);
            ButtonModel model = getModel();
            boolean hover = isEnabled() && model.isRollover();
            boolean pressed = isEnabled() && model.isArmed() && model.isPressed();
            Color color = null;
            if (isSelected()) {
                color = ColorFunctions.mix(PickerUtils.accentColor(accentColor), background, isEnabled() ? 0.18f : 0.08f);
            } else if (hover || pressed || filled) {
                color = PickerUtils.shade(background, pressed ? 0.12f : hover ? 0.08f : 0.05f);
            }
            if (color != null) {
                // a circle, or the round corners of a button of the look and feel
                float arc = circle ? Math.min(getWidth(), getHeight()) : UIScale.scale((float) getButtonArc());
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
            }
            Icon icon = getIcon();
            if (icon != null) {
                icon.paintIcon(this, g2, (getWidth() - icon.getIconWidth()) / 2, (getHeight() - icon.getIconHeight()) / 2);
                return;
            }
            if (!isEnabled()) {
                g2.setColor(PickerUtils.disabledForeground());
            } else {
                g2.setColor(isSelected() ? PickerUtils.accentColor(accentColor) : PickerUtils.foreground());
            }
            g2.setFont(getFont());
            String text = getText() != null ? getText() : "";
            Rectangle2D.Float bounds = new Rectangle2D.Float(0, 0, getWidth(), getHeight());
            if (leading) {
                // as wide as the text, at the padding of the leading side
                float width = g2.getFontMetrics().stringWidth(text);
                float padding = PickerUtils.scale(size, paddingX);
                bounds.x = getComponentOrientation().isLeftToRight() ? padding : getWidth() - padding - width;
                bounds.width = width;
            }
            PickerUtils.paintText(g2, text, bounds);
        } finally {
            g2.dispose();
        }
    }

    private static int getButtonArc() {
        Object arc = UIManager.get("Button.arc");
        return arc instanceof Integer ? (Integer) arc : 6;
    }
}
