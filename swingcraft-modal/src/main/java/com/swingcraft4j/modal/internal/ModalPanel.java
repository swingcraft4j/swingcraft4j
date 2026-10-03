package com.swingcraft4j.modal.internal;

import com.swingcraft4j.modal.Modal;
import com.swingcraft4j.modal.border.ModalBorder;
import com.swingcraft4j.modal.option.ModalOption;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

/**
 * Holds the modal and paints its border and shadow.
 * <p>
 * While sliding to a pushed or popped modal it holds two modals side by side, and its size
 * goes from the size of the one to the size of the other.
 * <p>
 * While animating with a snapshot it paints an image of the modal instead of the modal.
 */
final class ModalPanel extends JPanel {

    private final ModalBorder border;
    private final boolean buffered;
    private Modal modal;

    // slide: the modal that leaves, null when not sliding
    private Modal outgoing;
    private boolean slideForward;
    private float slideProgress;

    // the image the modal is painted through in a transparent window
    private Snapshot buffer;

    private Snapshot snapshot;
    private Snapshot borderSnapshot;
    private float snapshotAlpha = 1;
    private float snapshotScale = 1;

    /**
     * @param buffered true if the panel is in a transparent window, see {@link #paintChildren(Graphics)}
     */
    ModalPanel(ModalOption option, boolean buffered) {
        super(null);
        this.border = new ModalBorder(option);
        this.buffered = buffered;
        setOpaque(false);
        setBorder(border);
        // keep the focus inside the modal when tab pressed
        setFocusCycleRoot(true);
        // block the mouse event, so clicking the modal is not clicking the background
        addMouseListener(new MouseAdapter() {
        });
    }

    /**
     * @param modal the modal to show in place of the current one, or null to release the current one
     */
    void setModal(Modal modal) {
        removeAll();
        this.modal = modal;
        this.outgoing = null;
        if (modal != null) {
            add(modal);
        }
    }

    /**
     * Shows the modal next to the current one, {@link #setSlideProgress(float)} moves from the one to the other.
     *
     * @param forward true if the modal comes from the trailing side, false from the leading side
     */
    void startSlide(Modal modal, boolean forward) {
        this.outgoing = this.modal;
        this.modal = modal;
        this.slideForward = forward;
        this.slideProgress = 0;
        add(modal);
    }

    void setSlideProgress(float progress) {
        this.slideProgress = progress;
        invalidate();
    }

    boolean isSliding() {
        return outgoing != null;
    }

    float getSlideProgress() {
        return slideProgress;
    }

    void endSlide() {
        if (outgoing != null) {
            remove(outgoing);
            outgoing = null;
            invalidate();
        }
    }

    /**
     * Paints the modal to an image, the image is painted in place of the modal until {@link #endSnapshot()}.
     */
    void startSnapshot() {
        Rectangle area = getModalArea();
        if (snapshot != null || modal == null || outgoing != null || area.isEmpty()) {
            return;
        }
        snapshot = Snapshot.create(this, area.x, area.y, area.width, area.height, false, modal::paint);
        // the border has no text, and its shadow is slow to paint again in each size of a zoom
        borderSnapshot = Snapshot.create(this, 0, 0, getWidth(), getHeight(), true, this::paintBorder);
    }

    /**
     * @param snapshotAlpha how much of the image is visible, from 0 to 1
     * @param snapshotScale the size of the image, 1 is the real size and less is smaller
     */
    void setSnapshotState(float snapshotAlpha, float snapshotScale) {
        this.snapshotAlpha = Math.max(0, Math.min(snapshotAlpha, 1));
        this.snapshotScale = snapshotScale;
    }

    void endSnapshot() {
        if (snapshot != null) {
            snapshot.flush();
            snapshot = null;
            if (borderSnapshot != null) {
                borderSnapshot.flush();
                borderSnapshot = null;
            }
            repaint();
        }
    }

    Insets getShadowInsets() {
        return border.getShadowInsets();
    }

    /**
     * @return the area inside the border, where the modal is
     */
    private Rectangle getModalArea() {
        Insets insets = getInsets();
        return new Rectangle(insets.left, insets.top,
                Math.max(getWidth() - (insets.left + insets.right), 0),
                Math.max(getHeight() - (insets.top + insets.bottom), 0));
    }

    @Override
    public void doLayout() {
        Rectangle area = getModalArea();
        if (outgoing == null) {
            if (modal != null) {
                modal.setBounds(area);
            }
        } else {
            // both modals move by the same whole pixels, so the text stays sharp
            int direction = slideForward == getComponentOrientation().isLeftToRight() ? 1 : -1;
            int moved = Math.round(area.width * slideProgress);
            outgoing.setBounds(area.x - moved * direction, area.y, area.width, area.height);
            modal.setBounds(area.x + (area.width - moved) * direction, area.y, area.width, area.height);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return getPreferredSize(slideProgress);
    }

    @Override
    public Dimension getMinimumSize() {
        return getMinimumSize(slideProgress);
    }

    /**
     * @param slideProgress 0 for the size with the modal that leaves, 1 for the size with the modal that comes.
     *                      Not used when not sliding
     */
    Dimension getPreferredSize(float slideProgress) {
        return calculateSize(modal == null ? null : modal.getPreferredSize(), outgoing == null ? null : outgoing.getPreferredSize(), slideProgress);
    }

    Dimension getMinimumSize(float slideProgress) {
        return calculateSize(modal == null ? null : modal.getMinimumSize(), outgoing == null ? null : outgoing.getMinimumSize(), slideProgress);
    }

    private Dimension calculateSize(Dimension size, Dimension outgoingSize, float slideProgress) {
        Insets insets = getInsets();
        int width = 0;
        int height = 0;
        if (size != null) {
            width = size.width;
            height = size.height;
            if (outgoingSize != null) {
                width = Math.round(outgoingSize.width + (size.width - outgoingSize.width) * slideProgress);
                height = Math.round(outgoingSize.height + (size.height - outgoingSize.height) * slideProgress);
            }
        }
        return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
    }

    @Override
    public void paint(Graphics g) {
        if (snapshot == null) {
            super.paint(g);
            return;
        }
        Rectangle area = getModalArea();
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setComposite(AlphaComposite.SrcOver.derive(snapshotAlpha));
            if (borderSnapshot == null) {
                paintBorder(g2);
                snapshot.paint(g2, area.x, area.y);
            } else if (snapshotScale == 1) {
                borderSnapshot.paint(g2, 0, 0);
                snapshot.paint(g2, area.x, area.y);
            } else {
                // zoom around the middle of the modal, the shadow is not counted
                Insets shadow = getShadowInsets();
                double centerX = shadow.left + (getWidth() - (shadow.left + shadow.right)) / 2.0;
                double centerY = shadow.top + (getHeight() - (shadow.top + shadow.bottom)) / 2.0;
                g2.translate(centerX, centerY);
                g2.scale(snapshotScale, snapshotScale);
                g2.translate(-centerX, -centerY);
                borderSnapshot.paintScaled(g2, 0, 0);
                snapshot.paintScaled(g2, area.x, area.y);
            }
        } finally {
            g2.dispose();
        }
    }

    @Override
    protected void paintChildren(Graphics g) {
        // a modal that slides in or out must not paint over the border and the shadow
        Rectangle area = getModalArea();
        Graphics g2 = g.create();
        try {
            g2.clipRect(area.x, area.y, area.width, area.height);
            Rectangle clip = g2.getClipBounds();
            Snapshot painted = null;
            if (buffered && !clip.isEmpty()) {
                // text painted on a transparent window does not look the same as the text of a window that is
                // not transparent. So the modal is painted to an image that is not transparent, as in a snapshot.
                // The image is only as large as what is painted, and it is used again for the next time
                painted = Snapshot.create(this, clip.x, clip.y, clip.width, clip.height, false, g3 -> {
                    g3.translate(-clip.x, -clip.y);
                    g3.setClip(clip);
                    super.paintChildren(g3);
                }, buffer);
            }
            if (painted != null) {
                buffer = painted;
                painted.paint(g2, clip.x, clip.y);
            } else {
                super.paintChildren(g2);
            }
        } finally {
            g2.dispose();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (buffer != null) {
            buffer.flush();
            buffer = null;
        }
    }

    @Override
    protected boolean isPaintingOrigin() {
        // when a component of the modal repaints itself, paint the image and not that component
        return snapshot != null || buffered;
    }

    @Override
    public Color getBackground() {
        return modal == null ? super.getBackground() : modal.getBackground();
    }

    @Override
    public boolean contains(int x, int y) {
        // the shadow is not part of the modal
        Insets shadow = getShadowInsets();
        return x >= shadow.left && y >= shadow.top && x < getWidth() - shadow.right && y < getHeight() - shadow.bottom;
    }
}
