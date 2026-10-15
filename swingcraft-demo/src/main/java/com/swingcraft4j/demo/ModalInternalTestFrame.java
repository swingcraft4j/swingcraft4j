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
import java.beans.PropertyVetoException;
import java.util.function.Supplier;

/**
 * A frame with internal frames to test modals in them, with the options selected in the modal demo. An internal
 * frame has a root pane of its own, so a modal shown from a component inside it covers only that internal frame.
 * A modal shown from the desktop covers the whole frame.
 */
public class ModalInternalTestFrame extends JFrame {

    private final Supplier<ModalOption> option;
    private final JDesktopPane desktop = new JDesktopPane();
    private int count;

    public ModalInternalTestFrame(Component parent, Supplier<ModalOption> option) {
        super("Modal in internal frames");
        this.option = option;
        JPanel actions = DemoUtils.createGroup("Frame", "");
        actions.add(DemoUtils.createLink("Show modal over the window", () -> showModal(desktop, "whole window",
                option.get().setRelativeToOwner(false).setAnchor(null, null))));
        actions.add(DemoUtils.createLink("New internal frame", this::addInternalFrame));
        actions.add(DemoUtils.createLink("Close all", JModal::closeAll));
        JPanel content = new JPanel(new MigLayout("wrap,fill", "[fill]", "[][grow,fill]"));
        content.add(actions);
        content.add(desktop);
        setContentPane(content);
        for (int i = 0; i < 3; i++) {
            addInternalFrame();
        }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(1100, 750));
        setLocationRelativeTo(parent);
    }

    private void addInternalFrame() {
        count++;
        String name = "Internal frame " + count;
        JInternalFrame frame = new JInternalFrame(name, true, true, true, true);
        frame.setContentPane(createContent(name));
        frame.setSize(new Dimension(420, 320));
        int place = (count - 1) % 6;
        frame.setLocation(20 + place * 60, 20 + place * 40);
        desktop.add(frame);
        frame.setVisible(true);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException e) {
            // the frame stays not selected
        }
    }

    private JPanel createContent(String name) {
        JPanel panel = new JPanel(new MigLayout("wrap,fillx", "[fill]"));
        // a text field, so the focus can be seen going to the modal and back
        panel.add(new JTextField("Text of " + name.toLowerCase()));
        panel.add(DemoUtils.createLink("Show modal in this frame", () -> showModal(panel, name.toLowerCase(),
                option.get().setRelativeToOwner(false).setAnchor(null, null))));
        JPanel part = DemoUtils.createGroup("A part of the frame", "wrap");
        part.add(DemoUtils.createLink("Show modal over this part", () -> showModal(part, "part of " + name.toLowerCase(),
                option.get().setRelativeToOwner(true).setAnchor(null, null))));
        panel.add(part);
        JPanel sides = DemoUtils.createGroup("Next to the button", "");
        addAnchorButton(sides, "Top", Location.TOP);
        addAnchorButton(sides, "Bottom", Location.BOTTOM);
        addAnchorButton(sides, "Leading", Location.LEADING);
        addAnchorButton(sides, "Trailing", Location.TRAILING);
        panel.add(sides);
        return panel;
    }

    private void addAnchorButton(JPanel panel, String text, Location side) {
        JButton button = new JButton(text);
        button.addActionListener(e -> showModal(button, "button", option.get().setAnchor(side, Location.CENTER)));
        panel.add(button);
    }

    private void showModal(Component owner, String name, ModalOption option) {
        JPanel content = new JPanel(new MigLayout("wrap", "[fill]"));
        content.setOpaque(false);
        content.add(new JLabel("<html><body style='width:250px'>This modal is shown at the " + name
                + ". Move or resize the internal frame, the modal follows it.</body></html>"));
        content.add(new JTextField());
        SimpleModal modal = new SimpleModal(content, "Modal", SimpleModal.OptionType.OK_CANCEL, null);
        JModal.show(owner, modal, option);
    }

    // runs the frame on its own, with the default option of the modal
    public static void main(String[] args) {
        FlatRobotoFont.install();
        UIManager.put("defaultFont", FontUtils.getCompositeFont(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        FlatMacLightLaf.setup();
        EventQueue.invokeLater(() -> new ModalInternalTestFrame(null, JModal::createOption).setVisible(true));
    }
}
