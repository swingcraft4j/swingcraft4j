package com.swingcraft4j.modal.internal;

import com.formdev.flatlaf.util.UIScale;
import com.swingcraft4j.modal.option.ModalOption;

import java.awt.*;

/**
 * Lays out the modal panel inside its container with the layout options.
 * Size and location apply to the visible part of the panel, the shadow is placed around it.
 * <p>
 * The panel is placed in an area of the container. In a layer the area is the whole container. In a window
 * of its own the container is larger than the area when the modal does not fit in it.
 */
final class ModalLayout implements LayoutManager {

    private final ModalPanel panel;
    private final ModalOption option;
    // true if the modal can be larger than the area
    private final boolean overflow;
    // how far the user has dragged the modal away from its location
    private final Point offset = new Point();
    // 0 when the modal starts to show, 1 when it is at its location
    private float animationProgress = 1;
    // null for the whole container
    private Rectangle area;
    // the part of the screen a modal that leaves its area stays in, relative to the area. Null if not known
    private Rectangle screen;

    ModalLayout(ModalPanel panel, ModalOption option, boolean overflow) {
        this.panel = panel;
        this.option = option;
        this.overflow = overflow;
    }

    Point getOffset() {
        return new Point(offset);
    }

    void setOffset(int x, int y) {
        offset.setLocation(x, y);
    }

    void setAnimationProgress(float animationProgress) {
        this.animationProgress = animationProgress;
    }

    /**
     * @return the area of the container the modal is placed in
     */
    Rectangle getArea(Container parent) {
        return area != null ? new Rectangle(area) : new Rectangle(0, 0, parent.getWidth(), parent.getHeight());
    }

    /**
     * @param area   where the area is in the container
     * @param screen the part of the screen the modal stays in when it leaves the area, relative to the area
     * @return true if one of them changed
     */
    boolean setArea(Rectangle area, Rectangle screen) {
        if (area.equals(this.area) && screen.equals(this.screen)) {
            return false;
        }
        this.area = new Rectangle(area);
        this.screen = new Rectangle(screen);
        return true;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return new Dimension(0, 0);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(0, 0);
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Rectangle area = getArea(parent);
            Rectangle bounds = getPanelBounds(parent, area.width, area.height, screen, animationProgress, panel.getSlideProgress(), true);
            panel.setBounds(area.x + bounds.x, area.y + bounds.y, bounds.width, bounds.height);
        }
    }

    /**
     * @return the bounds of every place the panel is at while it is animated: from where it starts to show
     * to its location, and in the size of both modals of a slide. Relative to an area of the given size
     *
     * @param screen the part of the screen the modal stays in when it leaves the area, relative to the area
     */
    Rectangle getReach(Container parent, int areaWidth, int areaHeight, Rectangle screen) {
        float[] slides = panel.isSliding() ? new float[]{0, 1} : new float[]{panel.getSlideProgress()};
        Rectangle reach = null;
        for (float slide : slides) {
            for (int progress = 0; progress <= 1; progress++) {
                Rectangle bounds = getPanelBounds(parent, areaWidth, areaHeight, screen, progress, slide, false);
                reach = reach == null ? bounds : reach.union(bounds);
            }
        }
        return reach;
    }

    /**
     * @param forgetOutside true to forget the part of the drag that went outside the area
     * @return the bounds of the panel, relative to an area of the given size
     */
    private Rectangle getPanelBounds(Container parent, int areaWidth, int areaHeight, Rectangle screen, float animationProgress, float slideProgress, boolean forgetOutside) {
        boolean ltr = parent.getComponentOrientation().isLeftToRight();
        Insets margin = UIScale.scale(option.getMargin());
        if (!ltr) {
            int left = margin.left;
            margin.left = margin.right;
            margin.right = left;
        }
        Insets shadow = panel.getShadowInsets();
        int shadowWidth = shadow.left + shadow.right;
        int shadowHeight = shadow.top + shadow.bottom;
        int availableWidth = areaWidth - (margin.left + margin.right);
        int availableHeight = areaHeight - (margin.top + margin.bottom);
        int maximumWidth = availableWidth;
        int maximumHeight = availableHeight;
        if (overflow && screen != null) {
            // outside the area, but not larger than the screen
            maximumWidth = Math.max(maximumWidth, screen.width - (margin.left + margin.right));
            maximumHeight = Math.max(maximumHeight, screen.height - (margin.top + margin.bottom));
        }

        Dimension preferred = panel.getPreferredSize(slideProgress);
        Dimension minimum = panel.getMinimumSize(slideProgress);
        int width = getSize(option.getWidth(), preferred.width - shadowWidth, minimum.width - shadowWidth, availableWidth, maximumWidth);
        int height = getSize(option.getHeight(), preferred.height - shadowHeight, minimum.height - shadowHeight, availableHeight, maximumHeight);

        int x = margin.left + Math.round((availableWidth - width) * option.getHorizontalLocation().getValue(ltr));
        int y = margin.top + Math.round((availableHeight - height) * option.getVerticalLocation().getValue(true));
        if (offset.x != 0 || offset.y != 0) {
            // keep the dragged modal inside, and forget the part of the drag that went outside
            int movedX = keepInside(x + offset.x, areaWidth - width);
            int movedY = keepInside(y + offset.y, areaHeight - height);
            if (forgetOutside) {
                offset.setLocation(movedX - x, movedY - y);
            }
            x = movedX;
            y = movedY;
        }
        if (overflow && screen != null) {
            // a modal that leaves its area does not leave the screen. Inside the area it is not moved,
            // also when the area itself is not on the screen
            Rectangle inside = screen.union(new Rectangle(0, 0, areaWidth, areaHeight));
            x = Math.max(Math.min(x, inside.x + inside.width - width), inside.x);
            y = Math.max(Math.min(y, inside.y + inside.height - height), inside.y);
        }
        if (animationProgress < 1) {
            // whole pixels, so the text of the moving modal stays sharp
            Point animationOffset = option.getAnimationOption().getOffset();
            float distance = 1f - animationProgress;
            x += Math.round(UIScale.scale(animationOffset.x) * distance) * (ltr ? 1 : -1);
            y += Math.round(UIScale.scale(animationOffset.y) * distance);
        }
        return new Rectangle(x - shadow.left, y - shadow.top, width + shadowWidth, height + shadowHeight);
    }

    /**
     * @param space the space that is left in the area, negative if the modal is larger than the area:
     *              then it covers the area in every place
     */
    private int keepInside(int location, int space) {
        return Math.max(Math.min(location, Math.max(space, 0)), Math.min(space, 0));
    }

    private int getSize(Number size, int preferred, int minimum, int available, int maximum) {
        int value;
        if (size.floatValue() < 0) {
            value = preferred;
        } else if (size instanceof Float || size instanceof Double) {
            value = (int) (available * size.floatValue());
        } else {
            value = UIScale.scale(size.intValue());
        }
        // the maximum wins over the minimum size, otherwise the modal is cut off at the edges
        return Math.max(Math.min(Math.max(value, minimum), maximum), 0);
    }
}
