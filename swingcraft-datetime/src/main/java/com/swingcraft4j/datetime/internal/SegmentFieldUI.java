package com.swingcraft4j.datetime.internal;

import com.formdev.flatlaf.ui.FlatTextFieldUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Supplier;

/**
 * The UI of a field with segments. The field is a text field of the look and feel, with its border, its
 * background and its buttons. It has no text: the segments are painted in the place of the text, and
 * a click selects the segment under the mouse.
 */
public final class SegmentFieldUI extends FlatTextFieldUI {

    // the space at the left and the right of the text of a segment that is edited
    private static final int SEGMENT_PADDING = 2;
    private static final int SELECTION_ARC = 6;

    private final Supplier<SegmentEditor> editor;
    private MouseListener mouseListener;

    /**
     * @param editor gives the segments of the field. It can give null while the field is created
     */
    public SegmentFieldUI(Supplier<SegmentEditor> editor) {
        this.editor = editor;
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JTextComponent field = getComponent();
                SegmentEditor segments = editor.get();
                if (segments != null && field.isEnabled() && SwingUtilities.isLeftMouseButton(e)) {
                    int index = getSegmentAt(e.getX());
                    if (index != -1) {
                        segments.select(index);
                    }
                }
            }
        };
        getComponent().addMouseListener(mouseListener);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        getComponent().removeMouseListener(mouseListener);
        mouseListener = null;
    }

    private int getPadding(Segment segment) {
        return segment.isEditable() ? UIScale.scale(SEGMENT_PADDING) : 0;
    }

    private int getWidth(SegmentEditor segments, Segment segment, FontMetrics metrics) {
        return metrics.stringWidth(segments.getText(segment)) + getPadding(segment) * 2;
    }

    // the x of the first segment: the padding of a segment is not a space in front of the text of the field
    private int getStart(SegmentEditor segments, Rectangle bounds) {
        List<Segment> list = segments.getSegments();
        return bounds.x - (list.isEmpty() ? 0 : getPadding(list.get(0)));
    }

    /**
     * @return the index of the segment at the x, the nearest one if the x is outside of all. -1 for none
     */
    private int getSegmentAt(int x) {
        SegmentEditor segments = editor.get();
        Rectangle bounds = getVisibleEditorRect();
        if (segments == null || bounds == null) {
            return -1;
        }
        JTextComponent field = getComponent();
        FontMetrics metrics = field.getFontMetrics(field.getFont());
        List<Segment> list = segments.getSegments();
        int left = getStart(segments, bounds);
        for (int i = 0; i < list.size(); i++) {
            left += getWidth(segments, list.get(i), metrics);
            if (x < left) {
                return i;
            }
        }
        return list.size() - 1;
    }

    @Override
    protected void paintSafely(Graphics g) {
        // the text field leaves its clip at the place of its text. The background of the first selected
        // segment starts a little in front of that place, and would be cut off there
        Shape clip = g.getClip();
        super.paintSafely(g);
        SegmentEditor segments = editor.get();
        Rectangle bounds = getVisibleEditorRect();
        if (segments == null || bounds == null) {
            return;
        }
        JTextComponent field = getComponent();
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            g2.setClip(clip);
            g2.clipRect(bounds.x - UIScale.scale(SEGMENT_PADDING), bounds.y, bounds.width + UIScale.scale(SEGMENT_PADDING), bounds.height);
            g2.setFont(field.getFont());
            FontMetrics metrics = g2.getFontMetrics();
            int baseline = bounds.y + (bounds.height - metrics.getHeight()) / 2 + metrics.getAscent();
            Color foreground = field.isEnabled() ? field.getForeground() : field.getDisabledTextColor();
            Color placeholder = getPlaceholderColor(field);
            // a text of the pattern is as light as a placeholder until something is typed
            boolean empty = segments.isEmpty();
            boolean focused = FlatUIUtils.isPermanentFocusOwner(field);
            List<Segment> list = segments.getSegments();
            int x = getStart(segments, bounds);
            for (int i = 0; i < list.size(); i++) {
                Segment segment = list.get(i);
                String text = segments.getText(segment);
                int padding = getPadding(segment);
                int width = metrics.stringWidth(text) + padding * 2;
                boolean selected = focused && i == segments.getSelectedIndex();
                if (selected) {
                    float arc = UIScale.scale((float) SELECTION_ARC);
                    g2.setColor(field.getSelectionColor());
                    g2.fill(new RoundRectangle2D.Float(x, baseline - metrics.getAscent(), width, metrics.getHeight(), arc, arc));
                    g2.setColor(field.getSelectedTextColor());
                } else if (!field.isEnabled()) {
                    g2.setColor(foreground);
                } else if (segment.isEditable() || segment.getType() == SegmentType.WEEKDAY) {
                    g2.setColor(segments.isPlaceholder(segment) ? placeholder : foreground);
                } else {
                    g2.setColor(empty ? placeholder : foreground);
                }
                FlatUIUtils.drawString(field, g2, text, x + padding, baseline);
                x += width;
            }
        } finally {
            g2.dispose();
        }
    }

    private Color getPlaceholderColor(JTextComponent field) {
        Color color = UIManager.getColor("TextField.placeholderForeground");
        return color != null ? color : field.getDisabledTextColor();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension size = super.getPreferredSize(c);
        SegmentEditor segments = editor.get();
        if (segments == null) {
            return size;
        }
        // as wide as the widest text of each segment, so the field keeps its width while it is edited
        FontMetrics metrics = c.getFontMetrics(c.getFont());
        int width = 0;
        for (Segment segment : segments.getSegments()) {
            int widest = 0;
            for (String text : segments.getTexts(segment)) {
                widest = Math.max(widest, metrics.stringWidth(text));
            }
            width += widest + getPadding(segment) * 2;
        }
        Insets insets = c.getInsets();
        width += insets.left + insets.right + getLeadingIconWidth() + getTrailingIconWidth();
        for (JComponent component : getLeadingComponents()) {
            if (component != null && component.isVisible()) {
                width += component.getPreferredSize().width;
            }
        }
        for (JComponent component : getTrailingComponents()) {
            if (component != null && component.isVisible()) {
                width += component.getPreferredSize().width;
            }
        }
        size.width = Math.max(size.width, width);
        return size;
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        Dimension size = super.getMinimumSize(c);
        size.width = Math.max(size.width, getPreferredSize(c).width);
        return size;
    }
}
