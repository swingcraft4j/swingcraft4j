package com.swingcraft4j.toast.internal;

import javax.swing.*;
import javax.swing.text.View;
import java.awt.*;

/**
 * The message of a toast. It is as wide as its longest line. With a width limit that is smaller, the lines
 * that do not fit go on at the next line, and the text is higher.
 * <p>
 * A text area that wraps its lines does not tell how wide its text is, only how wide it is itself.
 * So the width is measured here, and the height is asked for that width.
 */
final class ToastText extends JTextArea {

    // room for the difference between the measured width and the width the lines are broken at
    private static final int SLACK = 2;

    private int widthLimit;

    ToastText(String text) {
        super(text);
        setEditable(false);
        setFocusable(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        setCursor(Cursor.getDefaultCursor());
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    /**
     * @param widthLimit the largest preferred width, 0 for no limit
     */
    void setWidthLimit(int widthLimit) {
        this.widthLimit = widthLimit;
    }

    /**
     * @return the width of the longest line, when no line is broken
     */
    private int getTextWidth() {
        FontMetrics metrics = getFontMetrics(getFont());
        int width = 0;
        String text = getText();
        if (text != null) {
            for (String line : text.split("\n", -1)) {
                width = Math.max(width, metrics.stringWidth(line));
            }
        }
        return width + SLACK;
    }

    @Override
    public Dimension getPreferredSize() {
        Insets insets = getInsets();
        int width = getTextWidth();
        if (widthLimit > 0) {
            width = Math.min(width, Math.max(widthLimit - (insets.left + insets.right), 1));
        }
        // the height of the text with its lines broken at the width
        View view = getUI().getRootView(this);
        view.setSize(width, Integer.MAX_VALUE);
        int height = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));
        return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
    }

    @Override
    public Dimension getMinimumSize() {
        // not wider than preferred: the layout must be able to make the toast as narrow as the limit
        return getPreferredSize();
    }
}
