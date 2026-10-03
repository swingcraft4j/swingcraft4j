package com.swingcraft4j.demo;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The bar at the bottom of the demo: the version of the project, of java and the name of the system at the
 * leading side, the used memory at the trailing side.
 */
class StatusBar extends JPanel {

    StatusBar() {
        super(new MigLayout("insets 5 10 5 10,gapx 20", "[][][]push[]"));
        add(createLabel(StatusIcon.Type.VERSION, "SwingCraft4j " + getProjectVersion(), null));
        add(createLabel(StatusIcon.Type.JAVA, "Java " + System.getProperty("java.version"),
                System.getProperty("java.vm.name") + ", " + System.getProperty("java.vendor")));
        add(createLabel(StatusIcon.Type.OS, System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")",
                "Version " + System.getProperty("os.version")));
        add(new MemoryBar(), "width 200!");
    }

    private JLabel createLabel(StatusIcon.Type type, String text, String toolTip) {
        JLabel label = new JLabel(text, new StatusIcon(type), SwingConstants.LEADING);
        label.setToolTipText(toolTip);
        // less strong than the options above, the icon follows the color of the text
        label.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground");
        return label;
    }

    // maven writes the version of the project in the file when it builds the demo
    private static String getProjectVersion() {
        Properties properties = new Properties();
        try (InputStream in = StatusBar.class.getResourceAsStream("demo.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            // the bar shows that the version is not known
        }
        return properties.getProperty("version", "unknown");
    }
}
