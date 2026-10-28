package com.swingcraft4j.modal.internal;

import com.formdev.flatlaf.util.UIScale;
import com.swingcraft4j.modal.option.Location;
import com.swingcraft4j.modal.option.ModalOption;

import java.awt.*;
import java.util.function.Supplier;

/**
 * Lays out the modal panel inside its container with the layout options.
 * Size and location apply to the visible part of the panel, the shadow is placed around it.
 * <p>
 * The panel is placed in an area of the container. In a layer the area is the whole container. In a window
 * of its own the container is larger than the area when the modal does not fit in it. A modal with an anchor
 * is placed next to its owner instead of at its location in the area.
 */
final class ModalLayout implements LayoutManager {

    private final ModalPanel panel;
    private final ModalOption option;
    // true if the modal can be larger than the area
    private final boolean overflow;
    // where the owner is in the area, for a modal placed next to it. Null if it is not
    private final Supplier<Rectangle> anchor;
    // how far the user has dragged the modal away from its location
    private final Point offset = new Point();
    // 0 when the modal starts to show, 1 when it is at its location
    private float animationProgress = 1;
    // where the panel was when its size started to change, relative to the area. Null when the size is not animated
    private Rectangle resizeFrom;
    // 0 when the size starts to change, 1 when the panel has its new size
    private float resizeProgress = 1;
    // null for the whole container
    private Rectangle area;
    // the part of the screen a modal that leaves its area stays in, relative to the area. Null if not known
    private Rectangle screen;

    ModalLayout(ModalPanel panel, ModalOption option, boolean overflow, Supplier<Rectangle> anchor) {
        this.panel = panel;
        this.option = option;
        this.overflow = overflow;
        this.anchor = anchor;
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
     * @param from     where the panel was when its size started to change, relative to the area. Null when the
     *                 size is not animated
     * @param progress 0 at the bounds it comes from, 1 at its new bounds
     */
    void setResize(Rectangle from, float progress) {
        this.resizeFrom = from == null ? null : new Rectangle(from);
        this.resizeProgress = progress;
    }

    /**
     * @return the bounds of the panel relative to the area, where the panel goes when its size is animated
     */
    Rectangle getTargetBounds(Container parent) {
        Rectangle area = getArea(parent);
        return getPanelBounds(parent, area.width, area.height, screen, animationProgress, panel.getSlideProgress(), false);
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
            if (resizeFrom != null) {
                // whole pixels, so the text of the modal stays sharp
                bounds = new Rectangle(
                        Math.round(resizeFrom.x + (bounds.x - resizeFrom.x) * resizeProgress),
                        Math.round(resizeFrom.y + (bounds.y - resizeFrom.y) * resizeProgress),
                        Math.round(resizeFrom.width + (bounds.width - resizeFrom.width) * resizeProgress),
                        Math.round(resizeFrom.height + (bounds.height - resizeFrom.height) * resizeProgress));
            }
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
        if (resizeFrom != null) {
            // the panel is between the bounds it comes from and its new bounds
            reach = reach.union(resizeFrom);
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

        int x;
        int y;
        // the side of the owner the modal is at: -1 before it, 1 after it, 0 for no side
        Point direction = new Point();
        if (anchor != null) {
            // inside the area, or the screen when the modal can leave the area
            Rectangle limit = new Rectangle(margin.left, margin.top, availableWidth, availableHeight);
            if (overflow && screen != null) {
                limit = new Rectangle(screen.x + margin.left, screen.y + margin.top,
                        screen.width - (margin.left + margin.right), screen.height - (margin.top + margin.bottom));
            }
            Point location = getAnchorLocation(anchor.get(), width, height, limit, ltr, direction);
            x = location.x;
            y = location.y;
        } else {
            x = margin.left + Math.round((availableWidth - width) * option.getHorizontalLocation().getValue(ltr));
            y = margin.top + Math.round((availableHeight - height) * option.getVerticalLocation().getValue(true));
        }
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
            Point animationOffset = getAnimationOffset(direction, ltr);
            float distance = 1f - animationProgress;
            x += Math.round(UIScale.scale(animationOffset.x) * distance);
            y += Math.round(UIScale.scale(animationOffset.y) * distance);
        }
        return new Rectangle(x - shadow.left, y - shadow.top, width + shadowWidth, height + shadowHeight);
    }

    /**
     * @param direction set to the side of the owner the modal is placed at, after it went to the other side
     * @return the location of the modal next to the owner, moved along the owner to stay inside the limit
     */
    private Point getAnchorLocation(Rectangle owner, int width, int height, Rectangle limit, boolean ltr, Point direction) {
        Location side = option.getAnchorSide();
        Location align = option.getAnchorAlign();
        int gap = UIScale.scale(option.getAnchorGap());
        int x;
        int y;
        if (side == Location.CENTER) {
            x = owner.x + Math.round((owner.width - width) * (align.isHorizontal() ? align.getValue(ltr) : 0.5f));
            y = owner.y + Math.round((owner.height - height) * (align.isVertical() ? align.getValue(ltr) : 0.5f));
        } else if (side.isVertical()) {
            int dir = getSideDirection(side == Location.TOP ? -1 : 1, owner.y, owner.height, height + gap, limit.y, limit.height);
            x = owner.x + Math.round((owner.width - width) * align.getValue(ltr));
            y = dir < 0 ? owner.y - gap - height : owner.y + owner.height + gap;
            direction.setLocation(0, dir);
        } else {
            int dir = getSideDirection(side.getValue(ltr) < 0.5f ? -1 : 1, owner.x, owner.width, width + gap, limit.x, limit.width);
            x = dir < 0 ? owner.x - gap - width : owner.x + owner.width + gap;
            y = owner.y + Math.round((owner.height - height) * align.getValue(ltr));
            direction.setLocation(dir, 0);
        }
        x = Math.max(Math.min(x, limit.x + limit.width - width), limit.x);
        y = Math.max(Math.min(y, limit.y + limit.height - height), limit.y);
        return new Point(x, y);
    }

    /**
     * @param direction -1 for the side before the owner, 1 for the side after it
     * @param size      the room the modal needs at the side, with the gap
     * @return the other side when the modal does not fit at the side and there is more room at the other side
     */
    private static int getSideDirection(int direction, int ownerStart, int ownerSize, int size, int limitStart, int limitSize) {
        int before = ownerStart - limitStart;
        int after = limitStart + limitSize - (ownerStart + ownerSize);
        int room = direction < 0 ? before : after;
        int otherRoom = direction < 0 ? after : before;
        return room < size && otherRoom > room ? -direction : direction;
    }

    /**
     * @param direction the side of the owner the modal is at, 0 if it is not next to the owner
     * @return the unscaled offset the modal starts to show at. Next to the owner, the y of the option is turned to
     * point to the owner: the modal comes out of the owner
     */
    private Point getAnimationOffset(Point direction, boolean ltr) {
        Point offset = option.getAnimationOption().getOffset();
        if (direction.y != 0) {
            return new Point(ltr ? offset.x : -offset.x, -direction.y * offset.y);
        }
        if (direction.x != 0) {
            return new Point(-direction.x * offset.y, offset.x);
        }
        return new Point(ltr ? offset.x : -offset.x, offset.y);
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
