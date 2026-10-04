package com.swingcraft4j.demo;

import com.formdev.flatlaf.FlatClientProperties;
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
        JButton button = new JButton("Themes", new ThemeIcon());
        button.addActionListener(e -> showThemes());
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(button);
        panel.add(toolBar);
        return panel;
    }

    // at the trailing side, over the whole height of the window. It slides in from the edge
    private void showThemes() {
        ModalOption option = JModal.createOption()
                .setLocation(Location.TRAILING, Location.CENTER)
                .setSize(-1, 1f)
                .setMargin(0)
                .setRound(0)
                // a light background, so the theme is seen behind it
                .setBackgroundOpacity(0.1f);
        option.getAnimationOption()
                .setFade(false)
                .setOffset(300, 0);
        JModal.show(this, new SimpleModal(new ThemesPanel(), "Themes"), option);
    }

    public static void main(String[] args) {
        FlatRobotoFont.install();
        UIManager.put("defaultFont", FontUtils.getCompositeFont(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        FlatMacLightLaf.setup();
        EventQueue.invokeLater(() -> new Demo().setVisible(true));
    }
}
