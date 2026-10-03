package com.swingcraft4j.demo;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Selects a color by its name. The first item is the default: no color, so the option uses its own.
 */
class ColorOption extends JComboBox<String> {

    private static final Map<String, Color> COLORS = new LinkedHashMap<>();

    static {
        COLORS.put("Default", null);
        COLORS.put("Black", Color.BLACK);
        COLORS.put("Gray", new Color(0x64748B));
        COLORS.put("Red", new Color(0xEF4444));
        COLORS.put("Orange", new Color(0xF97316));
        COLORS.put("Green", new Color(0x22C55E));
        COLORS.put("Blue", new Color(0x3B82F6));
        COLORS.put("Purple", new Color(0xA855F7));
    }

    ColorOption() {
        super(COLORS.keySet().toArray(new String[0]));
    }

    /**
     * @return the selected color, or null for the default
     */
    Color getColor() {
        return COLORS.get(getSelectedItem());
    }
}
