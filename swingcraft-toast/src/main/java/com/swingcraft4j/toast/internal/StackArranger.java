package com.swingcraft4j.toast.internal;

import com.formdev.flatlaf.util.UIScale;
import com.swingcraft4j.toast.option.ToastOption;

import java.awt.*;
import java.util.List;

/**
 * Shows the toasts over each other. The newest toast is in front, at the edge of the area. Each older toast
 * is one step behind: a little smaller, without its content, and moved so its far edge is visible behind the
 * toast in front of it.
 * The toasts further behind than the maximum are not visible.
 * <p>
 * How far behind a toast is, its depth, is the sum of how far the newer toasts are shown. So while a new toast
 * is shown the others move one step back, and while a toast closes the ones behind it come one step forward.
 * <p>
 * The toast in front has its own width. The toasts behind it have the width of the toast in front, so none is
 * visible at the sides of it, and a small toast in front of a large one is not made larger.
 * <p>
 * A stack can be expanded: then the toasts are shown one after the other, as a list, all of them as they are.
 * Each toast is placed between its place in the stack and its place in the list, by how far the stack
 * is expanded.
 */
final class StackArranger extends ToastArranger {

    @Override
    void arrange(Rectangle area, List<ToastPanel> toasts, boolean leftToRight, float expand) {
        ToastOption option = toasts.get(0).getOption();
        boolean top = option.getLocation().isTop();
        Insets margin = getMargin(toasts.get(0), leftToRight);
        int availableWidth = area.width - (margin.left + margin.right);
        // whole numbers: with an offset of 12.5 the toasts that move together are rounded to different sides,
        // and the space between them changes by a pixel while they move
        int stackOffset = UIScale.scale(option.getStackOffset());
        int gap = UIScale.scale(option.getGap());
        int maxVisible = option.getStackMaxVisible();
        float alignment = option.getLocation().getAlignment(leftToRight);

        // the width and the height of the toast in front. Each toast counts as far as it is shown,
        // from the oldest to the newest, so both change smoothly while a toast is shown or closed
        Dimension[] sizes = new Dimension[toasts.size()];
        float frontWidth = 0;
        float frontHeight = 0;
        for (int i = toasts.size() - 1; i >= 0; i--) {
            sizes[i] = getSize(toasts.get(i), availableWidth);
            float progress = toasts.get(i).getProgress();
            frontWidth += (sizes[i].width - frontWidth) * progress;
            frontHeight += (sizes[i].height - frontHeight) * progress;
        }

        int edge = top ? area.y + margin.top : area.y + area.height - margin.bottom;
        float depth = 0;
        // where the next toast of the expanded stack starts
        float listEdge = edge;
        for (int i = 0; i < toasts.size(); i++) {
            ToastPanel toast = toasts.get(i);
            int height = sizes[i].height;
            float visibleDepth = Math.min(depth, maxVisible);
            // in front its own width, one step behind the width of the toast in front. Expanded its own width
            float stackWidth = sizes[i].width + (frontWidth - sizes[i].width) * Math.min(depth, 1);
            int width = Math.round(stackWidth + (sizes[i].width - stackWidth) * expand);

            // in the stack: in front the toast is at the edge. Behind, its far edge is one offset further for each step
            float frontY = top ? edge : edge - height;
            float behindY = top
                    ? edge + frontHeight + stackOffset * visibleDepth - height
                    : edge - frontHeight - stackOffset * visibleDepth;
            float stackY = frontY + (behindY - frontY) * Math.min(depth, 1);
            float stackScale = width > 0 ? Math.max((width - stackOffset * 2 * visibleDepth) / width, 0) : 1;
            float stackAlpha = getAlpha(toast) * Math.max(Math.min(maxVisible - depth, 1), 0);
            // a toast behind that is higher than the toast in front is not painted outside of it
            float stackHidden = depth == 0 ? 0 : Math.max(top ? edge - stackY : stackY + height - edge, 0);
            // of the toasts behind only the edge of the background is visible, not a part of the icon and the text
            float stackContentAlpha = Math.max(1 - depth, 0);

            // expanded: after the newer toasts, as it is
            float listY = top ? listEdge : listEdge - height;

            int x = keepOnScreen(toast, area, area.x + margin.left + Math.round((availableWidth - width) * alignment), width);
            int y = Math.round(stackY + (listY - stackY) * expand);
            // only the toast in front moves in and out, the ones behind it fade. A toast that is still moving in
            // when a new one is shown goes on from where it is, and is at its place when it is one step behind
            Point offset = getOffset(toast, new Dimension(width, height), leftToRight, true);
            float front = 1 - Math.min(depth, 1);
            toast.setToastBounds(x + Math.round(offset.x * front), y + Math.round(offset.y * front), width, height);
            toast.setPresentation(
                    stackAlpha + (getAlpha(toast) - stackAlpha) * expand,
                    stackScale + (1 - stackScale) * expand,
                    Math.round(stackHidden * (1 - expand)),
                    stackContentAlpha + (1 - stackContentAlpha) * expand);

            depth += toast.getProgress();
            float taken = (height + gap) * toast.getProgress();
            listEdge += top ? taken : -taken;
        }
    }
}
