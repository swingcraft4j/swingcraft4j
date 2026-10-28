package com.swingcraft4j.modal.border;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Renders a blurred round rectangle and keeps the image, so the shadow is only rendered again when its style
 * changed.
 * <p>
 * The image is of a small rectangle, and is painted in nine parts: the corners as they are, the sides and the
 * middle stretched. Along a straight side the blurred shadow is the same at every place, so the shadow of any
 * size is painted from one image. A modal that changes its size, as while it is animated, does not blur a
 * new image for each size. Only a rectangle too small for the corners is rendered in its own size.
 */
final class ShadowRenderer {

    private BufferedImage image;
    // the size of the rectangle of the image, the image is blur pixels larger on each side
    private int width;
    private int height;
    private float arc;
    private int blur;
    private int rgb;
    private float opacity;

    /**
     * Paints the shadow of a rectangle. The shadow is {@code blur} pixels larger than the rectangle on each side.
     *
     * @param x where the shadow starts, {@code blur} pixels before the rectangle
     */
    void paintShadow(Graphics2D g, int x, int y, int width, int height, float arc, int blur, Color color, float opacity) {
        // a part of a corner: the blur outside, the round corner and the blur inside the rectangle
        int corner = blur * 2 + (int) Math.ceil(arc / 2) + 1;
        // the rectangle of the image has one line between its corners, which is stretched to the size
        int sliced = (corner - blur) * 2 + 1;
        if (width < sliced || height < sliced) {
            g.drawImage(getShadow(width, height, arc, blur, color, opacity), x, y, null);
            return;
        }
        BufferedImage image = getShadow(sliced, sliced, arc, blur, color, opacity);
        int[] dx = {x, x + corner, x + width + blur * 2 - corner, x + width + blur * 2};
        int[] dy = {y, y + corner, y + height + blur * 2 - corner, y + height + blur * 2};
        int[] s = {0, corner, corner + 1, image.getWidth()};
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                g.drawImage(image, dx[column], dy[row], dx[column + 1], dy[row + 1],
                        s[column], s[row], s[column + 1], s[row + 1], null);
            }
        }
    }

    /**
     * @return the shadow image, {@code blur} pixels larger than the rectangle on each side
     */
    private BufferedImage getShadow(int width, int height, float arc, int blur, Color color, float opacity) {
        int rgb = color.getRGB() & 0xFFFFFF;
        if (image == null || this.width != width || this.height != height || this.arc != arc
                || this.blur != blur || this.rgb != rgb || this.opacity != opacity) {
            this.width = width;
            this.height = height;
            this.arc = arc;
            this.blur = blur;
            this.rgb = rgb;
            this.opacity = opacity;
            if (image != null) {
                image.flush();
            }
            image = render(width, height, arc, blur, rgb, opacity);
        }
        return image;
    }

    private static BufferedImage render(int width, int height, float arc, int blur, int rgb, float opacity) {
        int imageWidth = width + blur * 2;
        int imageHeight = height + blur * 2;
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fill(new RoundRectangle2D.Float(blur, blur, width, height, arc, arc));
        g.dispose();

        int[] pixels = image.getRGB(0, 0, imageWidth, imageHeight, null, 0, imageWidth);
        int[] alpha = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            alpha[i] = pixels[i] >>> 24;
        }

        // three box blur passes are close to a smooth blur. They spread the shape by 3 * radius,
        // which keeps it inside the image. A very small blur is one pass
        int passes = blur >= 3 ? 3 : 1;
        int radius = blur / passes;
        if (radius > 0) {
            int[] temp = new int[alpha.length];
            for (int pass = 0; pass < passes; pass++) {
                boxBlur(alpha, temp, imageHeight, imageWidth, imageWidth, 1, radius);
                boxBlur(temp, alpha, imageWidth, imageHeight, 1, imageWidth, radius);
            }
        }

        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = ((int) (alpha[i] * opacity) << 24) | rgb;
        }
        image.setRGB(0, 0, imageWidth, imageHeight, pixels, 0, imageWidth);
        return image;
    }

    /**
     * Blurs each line of {@code src} into {@code dst}. Horizontal lines use lineStep = width and
     * step = 1, vertical lines use lineStep = 1 and step = width.
     */
    private static void boxBlur(int[] src, int[] dst, int lines, int length, int lineStep, int step, int radius) {
        int window = radius * 2 + 1;
        for (int line = 0; line < lines; line++) {
            int start = line * lineStep;
            int sum = 0;
            for (int i = 0; i <= radius && i < length; i++) {
                sum += src[start + i * step];
            }
            for (int i = 0; i < length; i++) {
                dst[start + i * step] = sum / window;
                int add = i + radius + 1;
                int remove = i - radius;
                if (add < length) {
                    sum += src[start + add * step];
                }
                if (remove >= 0) {
                    sum -= src[start + remove * step];
                }
            }
        }
    }
}
