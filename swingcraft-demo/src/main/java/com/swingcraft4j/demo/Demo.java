package com.swingcraft4j.demo;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.FontUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * The demo of the modal, the toast and the date and time pickers, each in its own tab, with a status bar at the bottom.
 */
public class Demo extends JFrame {

    public Demo() {
        super("SwingCraft4j Demo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, createThemeOption());
        tabbedPane.addTab("Modal", new ModalDemo());
        tabbedPane.addTab("Toast", new ToastDemo());
        tabbedPane.addTab("Date time", new DateTimeDemo());
        // space between the edge of the window and the tabs, the line and the status bar take the whole width
        JPanel contentPane = new JPanel(new MigLayout("fill,insets 0,gapy 0,wrap"));
        contentPane.add(tabbedPane, "grow,push,gap 10 10 10 10");
        contentPane.add(new JSeparator(), "growx");
        contentPane.add(new StatusBar(), "growx");
        setContentPane(contentPane);
        setSize(new Dimension(1250, 800));
        setLocationRelativeTo(null);
    }

    private Component createThemeOption() {
        JPanel panel = new JPanel(new MigLayout("al trailing center"));
        JCheckBox chDark = new JCheckBox("Dark mode");
        chDark.addActionListener(e -> {
            if (chDark.isSelected()) {
                FlatMacDarkLaf.setup();
            } else {
                FlatMacLightLaf.setup();
            }
            FlatLaf.updateUI();
        });
        panel.add(chDark);
        return panel;
    }

    public static void main(String[] args) {
        FlatRobotoFont.install();
        UIManager.put("defaultFont", FontUtils.getCompositeFont(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        FlatMacLightLaf.setup();
        EventQueue.invokeLater(() -> new Demo().setVisible(true));
    }
}
