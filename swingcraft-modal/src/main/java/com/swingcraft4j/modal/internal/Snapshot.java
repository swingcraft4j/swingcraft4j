package com.swingcraft4j.modal.internal;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * An image of components, painted in place of them while animating.
 * <p>
 * The image is in memory and has one pixel for each pixel of the screen. An image of the screen kind
 * (VolatileImage) is slow to paint back.
 * <p>
 * On a screen with a scale such as 150% a component does not start at a whole pixel: the location 277 is
 * the pixel 415.5. Everything inside it is rounded from there. The image is painted with the same start,
 * otherwise its lines and text are one pixel off and jump when the real components are painted again.
 */
final class Snapshot {

    // against 414.99999 in place of 415
    private static final double EPSILON = 0.0001;

    private final BufferedImage image;
    private final GraphicsConfiguration configuration;
    private final int width;
    private final int height;
    private final double scaleX;
    private final double scaleY;
    private final double startX;
    private final double startY;

    private Snapshot(BufferedImage image, GraphicsConfiguration configuration, int width, int height, double scaleX, double scaleY, double startX, double startY) {
        this.image = image;
        this.configuration = configuration;
        this.width = width;
        this.height = height;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.startX = startX;
        this.startY = startY;
    }

    /**
     * @param component   the component the image is painted on later
     * @param x           where the image is painted on the component later
     * @param width       the width in the size of the component, the image is larger on a scaled screen
     * @param translucent true if the content does not fill the image. Text is not as sharp in such an image,
     *                    so it is only for content without text
     * @param painter     paints the content of the image
     * @return the snapshot, or null if the component is not on a screen or the size is empty
     */
    static Snapshot create(Component component, int x, int y, int width, int height, boolean translucent, Consumer<Graphics2D> painter) {
        return create(component, x, y, width, height, translucent, painter, null);
    }

    /**
     * @param reuse a snapshot that is not used any more, or null. Its image is used again if it is large enough,
     *              so painting through an image many times does not make a new image each time. The snapshot
     *              that is returned must then be the only one in use
     */
    static Snapshot create(Component component, int x, int y, int width, int height, boolean translucent, Consumer<Graphics2D> painter, Snapshot reuse) {
        GraphicsConfiguration configuration = component.getGraphicsConfiguration();
        if (configuration == null || width <= 0 || height <= 0) {
            return null;
        }
        AffineTransform transform = configuration.getDefaultTransform();
        double scaleX = transform.getScaleX();
        double scaleY = transform.getScaleY();

        // the part of a pixel the real components start at, the window itself starts at a whole pixel
        Window window = SwingUtilities.getWindowAncestor(component);
        Point location = window == null ? new Point(x, y) : SwingUtilities.convertPoint(component, x, y, window);
        double startX = getPartOfPixel(location.x * scaleX);
        double startY = getPartOfPixel(location.y * scaleY);

        int imageWidth = (int) Math.ceil(width * scaleX + startX);
        int imageHeight = (int) Math.ceil(height * scaleY + startY);
        int transparency = translucent ? Transparency.TRANSLUCENT : Transparency.OPAQUE;
        BufferedImage image;
        if (reuse != null && reuse.configuration == configuration && reuse.image.getTransparency() == transparency
                && reuse.image.getWidth() >= imageWidth && reuse.image.getHeight() >= imageHeight) {
            image = reuse.image;
        } else {
            if (reuse != null) {
                reuse.flush();
            }
            image = configuration.createCompatibleImage(imageWidth, imageHeight, transparency);
        }
        Graphics2D g = image.createGraphics();
        try {
            if (translucent) {
                // an image that is used again has its last content
                g.setComposite(AlphaComposite.Clear);
                g.fillRect(0, 0, imageWidth, imageHeight);
                g.setComposite(AlphaComposite.SrcOver);
            } else {
                // on a scaled screen the image is up to one pixel larger than the content, to hold the last part
                // of a pixel. What the content does not paint there is black in an image that is not translucent
                g.setColor(component.getBackground());
                g.fillRect(0, 0, imageWidth, imageHeight);
            }
            g.translate(startX, startY);
            g.scale(scaleX, scaleY);
            painter.accept(g);
        } finally {
            g.dispose();
        }
        return new Snapshot(image, configuration, width, height, scaleX, scaleY, startX, startY);
    }

    private static double getPartOfPixel(double value) {
        return value - Math.floor(value + EPSILON);
    }

    boolean hasSize(int width, int height) {
        return this.width == width && this.height == height;
    }

    /**
     * Paints the image pixel for pixel, the scale of the graphics is not used.
     */
    void paint(Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            // not the part of a pixel the image is larger than the content
            g2.clipRect(x, y, width, height);
            AffineTransform transform = g2.getTransform();
            if (transform.getScaleX() == 1 && transform.getScaleY() == 1) {
                g2.drawImage(image, x, y, null);
            } else {
                // a scaled image is not sharp
                Point2D point = transform.transform(new Point2D.Double(x, y), null);
                g2.setTransform(new AffineTransform());
                g2.drawImage(image, (int) Math.floor(point.getX() + EPSILON), (int) Math.floor(point.getY() + EPSILON), null);
            }
        } finally {
            g2.dispose();
        }
    }

    /**
     * Paints the image with the transform of the graphics, to zoom it. It is not as sharp as {@link #paint}.
     */
    void paintScaled(Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.clipRect(x, y, width, height);
            g2.translate(x, y);
            g2.scale(1 / scaleX, 1 / scaleY);
            g2.translate(-startX, -startY);
            g2.drawImage(image, 0, 0, null);
        } finally {
            g2.dispose();
        }
    }

    void flush() {
        image.flush();
    }
}
