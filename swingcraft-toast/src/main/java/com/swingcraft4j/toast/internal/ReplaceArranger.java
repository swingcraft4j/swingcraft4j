package com.swingcraft4j.toast.internal;

import com.swingcraft4j.toast.option.ToastOption;

import java.awt.*;
import java.util.List;

/**
 * Shows one toast at a time. A new toast takes the place of the one that is showing: the layer closes that
 * one when the new toast is shown. While both are there, the new one moves in over the old one,
 * and the old one stays where it is and fades out.
 */
final class ReplaceArranger extends ToastArranger {

    @Override
    void arrange(Rectangle area, List<ToastPanel> toasts, boolean leftToRight, float expand) {
        ToastOption option = toasts.get(0).getOption();
        boolean top = option.getLocation().isTop();
        Insets margin = getMargin(toasts.get(0), leftToRight);
        int availableWidth = area.width - (margin.left + margin.right);
        float alignment = option.getLocation().getAlignment(leftToRight);
        int edge = top ? area.y + margin.top : area.y + area.height - margin.bottom;

        for (int i = 0; i < toasts.size(); i++) {
            ToastPanel toast = toasts.get(i);
            Dimension size = getSize(toast, availableWidth);
            int x = keepOnScreen(toast, area, area.x + margin.left + Math.round((availableWidth - size.width) * alignment), size.width);
            int y = top ? edge : edge - size.height;
            if (i == 0) {
                Point offset = getOffset(toast, size, leftToRight, false);
                toast.setToastBounds(x + offset.x, y + offset.y, size.width, size.height);
                toast.setPresentation(getAlpha(toast), 1, 0, 1);
            } else {
                // a toast that is replaced always fades, it would not go away under the new one otherwise
                toast.setToastBounds(x, y, size.width, size.height);
                toast.setPresentation(toast.getProgress(), 1, 0, 1);
            }
        }
    }
}
