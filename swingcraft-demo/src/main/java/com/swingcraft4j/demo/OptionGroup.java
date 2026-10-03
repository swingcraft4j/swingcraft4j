package com.swingcraft4j.demo;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Radio buttons that select one value. The radio buttons are added to a panel that can have other components too.
 */
class OptionGroup<T> {

    private final ButtonGroup group = new ButtonGroup();
    private final Map<ButtonModel, T> values = new HashMap<>();
    private final JPanel panel;
    private final String constraints;

    /**
     * @param panel       the panel the radio buttons are added to
     * @param constraints the layout constraints of each radio button
     */
    OptionGroup(JPanel panel, String constraints) {
        this.panel = panel;
        this.constraints = constraints;
    }

    OptionGroup<T> add(String text, T value) {
        return add(text, value, false);
    }

    OptionGroup<T> add(String text, T value, boolean selected) {
        JRadioButton radio = new JRadioButton(text, selected);
        group.add(radio);
        values.put(radio.getModel(), value);
        panel.add(radio, constraints);
        return this;
    }

    T getValue() {
        return values.get(group.getSelection());
    }
}
