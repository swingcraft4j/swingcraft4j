package com.swingcraft4j.toast.internal;

import com.formdev.flatlaf.util.UIScale;
import com.swingcraft4j.toast.option.ToastOption;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shows the toasts one after the other, from the edge of the area: the newest at the edge, or the oldest
 * with the reverse order. A toast takes its place in the list as far as it is shown, so the toasts after it
 * move away while it is shown and come back while it closes.
 * <p>
 * A banner is a list where each toast is as wide as the area.
 */
final class ListArranger extends ToastArranger {

    // true for a banner: each toast is as wide as the area
    private final boolean stretch;

    ListArranger(boolean stretch) {
        this.stretch = stretch;
    }

    @Override
    void arrange(Rectangle area, List<ToastPanel> toasts, boolean leftToRight, float expand) {
        ToastOption option = toasts.get(0).getOption();
        boolean top = option.getLocation().isTop();
        Insets margin = getMargin(toasts.get(0), leftToRight);
        int availableWidth = area.width - (margin.left + margin.right);
        float gap = UIScale.scale((float) option.getGap());
        float alignment = option.getLocation().getAlignment(leftToRight);

        List<ToastPanel> ordered = toasts;
        if (ToastManager.isReverseOrder()) {
            ordered = new ArrayList<>(toasts);
            Collections.reverse(ordered);
        }

        // where the next toast starts, from the edge of the area
        float edge = top ? area.y + margin.top : area.y + area.height - margin.bottom;
        for (ToastPanel toast : ordered) {
            Dimension size = getSize(toast, availableWidth);
            if (stretch) {
                size.width = Math.max(availableWidth, 0);
            }
            // a banner is at the whole edge, it does not come from the side
            Point offset = getOffset(toast, size, leftToRight, stretch);
            int x = keepOnScreen(toast, area, area.x + margin.left + Math.round((availableWidth - size.width) * alignment), size.width);
            int y = Math.round(top ? edge : edge - size.height);
            toast.setToastBounds(x + offset.x, y + offset.y, size.width, size.height);
            toast.setPresentation(getAlpha(toast), 1, 0, 1);

            float taken = (size.height + gap) * toast.getProgress();
            edge += top ? taken : -taken;
        }
    }
}
