package com.swingcraft4j.datetime.internal;

import com.formdev.flatlaf.util.UIScale;
import com.swingcraft4j.datetime.DatePreset;
import com.swingcraft4j.datetime.DateRange;
import com.swingcraft4j.datetime.option.DateOption;
import com.swingcraft4j.datetime.option.DateSelectionMode;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The presets of a date picker, one below the other next to the calendar, with a line between them and
 * the calendar. The preset that has the selection of the picker is shown as selected.
 */
public final class PresetPanel extends JPanel {

    public interface Listener {

        /**
         * The user has clicked the preset.
         */
        void presetSelected(DatePreset preset);
    }

    // the space between the line and the presets
    private static final int LINE_GAP = 8;

    private final Listener listener;
    private final List<DatePreset> presets = new ArrayList<>();
    private final List<PickerButton> buttons = new ArrayList<>();

    public PresetPanel(Listener listener) {
        this.listener = listener;
        setOpaque(false);
    }

    /**
     * Shows the presets of the option. The panel is not visible if the option has none.
     */
    public void setOption(DateOption option) {
        removeAll();
        presets.clear();
        buttons.clear();
        presets.addAll(option.getPresets());
        setLayout(new MigLayout("wrap,fillx,insets 0 " + (LINE_GAP * 2) + " 0 0,gapy 2", "[fill]"));
        for (DatePreset preset : presets) {
            PickerButton button = new PickerButton().padding(8, 4).leading();
            button.setText(preset.getName());
            button.setPickerSize(option.getSize());
            button.setAccentColor(option.getStyleOption().getColor());
            button.setEnabled(isEnabled());
            button.addActionListener(e -> listener.presetSelected(preset));
            buttons.add(button);
            add(button);
        }
        setVisible(!presets.isEmpty());
        revalidate();
        repaint();
    }

    /**
     * Shows the preset that has the selection as selected.
     *
     * @param selection the selection of the picker
     */
    public void selectionChanged(DateSelection selection) {
        // a picker of one date has the first date of a preset
        Object value = selection.getMode() == DateSelectionMode.RANGE ? selection.getRange() : selection.getDate();
        boolean found = false;
        for (int i = 0; i < presets.size(); i++) {
            boolean selected = false;
            if (!found && value != null) {
                DateRange range = presets.get(i).getRange();
                selected = Objects.equals(value, selection.getMode() == DateSelectionMode.RANGE ? range : range.getFrom());
            }
            // only the first of two presets with the same dates
            found |= selected;
            buttons.get(i).setSelected(selected);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // the line between the calendar and the presets
        Color color = UIManager.getColor("Separator.foreground");
        g.setColor(color != null ? color : Color.GRAY);
        int x = getComponentOrientation().isLeftToRight() ? UIScale.scale(LINE_GAP) : getWidth() - UIScale.scale(LINE_GAP) - 1;
        g.fillRect(x, 0, 1, getHeight());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (PickerButton button : buttons) {
            button.setEnabled(enabled);
        }
    }
}
