package com.swingcraft4j.demo;

import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.FontUtils;
import com.swingcraft4j.modal.JModal;
import com.swingcraft4j.modal.option.Location;
import com.swingcraft4j.modal.option.ModalOption;
import com.swingcraft4j.modal.simple.SimpleModal;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

/**
 * A frame with split panes to test a modal that is relative to its owner, and a modal placed next to a button.
 * Each part of the split panes shows its modal over itself, with the options selected in the modal demo.
 * The buttons are at the top of the top part and at the bottom of the bottom part, so the modal next to them
 * has to go to the other side.
 */
public class ModalTestFrame extends JFrame {

    private final Supplier<ModalOption> option;
    private final OptionGroup<Integer> align;

    public ModalTestFrame(Component parent, Supplier<ModalOption> option) {
        super("Modal relative to owner");
        this.option = option;
        JPanel alignPanel = DemoUtils.createGroup("Anchor align", "");
        align = new OptionGroup<Integer>(alignPanel, "")
                .add("Start", 0)
                .add("Center", 1, true)
                .add("End", 2);
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createPart("Top", "top"), createPart("Bottom", "bottom"));
        rightSplit.setResizeWeight(0.5);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createPart("Left", "center"), rightSplit);
        split.setResizeWeight(0.4);
        JPanel content = new JPanel(new MigLayout("wrap,fill", "[fill]", "[][grow,fill]"));
        content.add(alignPanel);
        content.add(split);
        setContentPane(content);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(1000, 650));
        setLocationRelativeTo(parent);
    }

    /**
     * @param position where the buttons are in the part: top, center or bottom
     */
    private Component createPart(String name, String position) {
        JPanel panel = new JPanel(new MigLayout("wrap,fill", "[center]", "[" + position + "]"));
        JPanel buttons = new JPanel(new MigLayout("wrap", "[center]"));
        buttons.add(new JLabel(name + " part"));
        buttons.add(DemoUtils.createLink("Show modal over the part", () -> showModal(panel, name.toLowerCase() + " part", option.get().setRelativeToOwner(true))));
        JPanel sides = new JPanel(new MigLayout());
        addAnchorButton(sides, "Top", Location.TOP);
        addAnchorButton(sides, "Bottom", Location.BOTTOM);
        addAnchorButton(sides, "Leading", Location.LEADING);
        addAnchorButton(sides, "Trailing", Location.TRAILING);
        addAnchorButton(sides, "Over", Location.CENTER);
        buttons.add(sides);
        panel.add(buttons);
        return panel;
    }

    private void addAnchorButton(JPanel panel, String text, Location side) {
        JButton button = new JButton(text);
        button.addActionListener(e -> showModal(button, "button", option.get().setAnchor(side, getAlign(side))));
        panel.add(button);
    }

    // start and end are along the side: the edges from leading to trailing, or from top to bottom
    private Location getAlign(Location side) {
        boolean horizontal = side == Location.TOP || side == Location.BOTTOM || side == Location.CENTER;
        switch (align.getValue()) {
            case 0:
                return horizontal ? Location.LEADING : Location.TOP;
            case 2:
                return horizontal ? Location.TRAILING : Location.BOTTOM;
            default:
                return Location.CENTER;
        }
    }

    private void showModal(Component owner, String name, ModalOption option) {
        JPanel content = new JPanel(new MigLayout("wrap"));
        content.setOpaque(false);
        content.add(new JLabel("<html><body style='width:250px'>This modal is shown at the " + name
                + ". Move the divider of the split pane, the modal follows it.</body></html>"));
        SimpleModal modal = new SimpleModal(content, "Modal", SimpleModal.OptionType.OK_CANCEL, null);
        JModal.show(owner, modal, option);
    }

    // runs the frame on its own, with the default option of the modal
    public static void main(String[] args) {
        FlatRobotoFont.install();
        UIManager.put("defaultFont", FontUtils.getCompositeFont(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        FlatMacLightLaf.setup();
        EventQueue.invokeLater(() -> new ModalTestFrame(null, JModal::createOption).setVisible(true));
    }
}
