package com.swingcraft4j.modal.border;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Renders a blurred round rectangle and keeps the last image, so the shadow
 * is only rendered again when the size or style changed.
 */
final class ShadowRenderer {

    private BufferedImage image;
    private int width;
    private int height;
    private float arc;
    private int blur;
    private int rgb;
    private float opacity;

    /**
     * @return the shadow image, {@code blur} pixels larger than the rectangle on each side
     */
    BufferedImage getShadow(int width, int height, float arc, int blur, Color color, float opacity) {
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
