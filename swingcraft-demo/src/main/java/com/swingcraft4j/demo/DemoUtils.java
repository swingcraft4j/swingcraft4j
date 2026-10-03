package com.swingcraft4j.demo;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

final class DemoUtils {

    private DemoUtils() {
    }

    /**
     * @return a panel with a title border, the components are added one after the other
     */
    static JPanel createGroup(String title, String layoutConstraints) {
        JPanel panel = new JPanel(new MigLayout(layoutConstraints));
        panel.setBorder(new TitledBorder(title));
        return panel;
    }

    /**
     * @return a panel with a title border and two columns: one for the labels and one for the values.
     * Add a value with {@link #addRow(JPanel, String, Component)}, and other components with "span 2"
     */
    static JPanel createFormGroup(String title) {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[][fill,75::]"));
        panel.setBorder(new TitledBorder(title));
        return panel;
    }

    static void addRow(JPanel panel, String label, Component component) {
        panel.add(new JLabel(label));
        // a combo box is not made smaller than its longest item by itself, it would go over the edge of
        // a panel that is smaller than it prefers
        panel.add(component, "wmin 75");
    }

    static JSpinner createSpinner(int value, int minimum, int maximum, int step) {
        return new JSpinner(new SpinnerNumberModel(value, minimum, maximum, step));
    }

    static JSpinner createSpinner(double value, double minimum, double maximum, double step) {
        return new JSpinner(new SpinnerNumberModel(value, minimum, maximum, step));
    }

    static int intValue(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    static float floatValue(JSpinner spinner) {
        // the steps of a decimal spinner do not add up to the exact value
        return Math.round(((Number) spinner.getValue()).floatValue() * 100) / 100f;
    }

    /**
     * @return a combo box to select the opacity of a shadow, the first item is the default of the look and feel
     */
    static JComboBox<String> createShadowOpacity() {
        return new JComboBox<>(new String[]{"Default", "0", "0.1", "0.2", "0.3", "0.5", "0.75", "1"});
    }

    /**
     * @return the selected opacity, or -1 for the default
     */
    static float shadowOpacityValue(JComboBox<String> comboBox) {
        return comboBox.getSelectedIndex() == 0 ? -1 : Float.parseFloat((String) comboBox.getSelectedItem());
    }

    /**
     * @return a button that looks like a link
     */
    static JButton createLink(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:1,3,1,3;" +
                "borderWidth:0;" +
                "focusWidth:0;" +
                "innerFocusWidth:0;" +
                "background:null;" +
                "foreground:$Component.accentColor;");
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        button.setFont(button.getFont().deriveFont(attributes));
        button.addActionListener(e -> action.run());
        return button;
    }
}
