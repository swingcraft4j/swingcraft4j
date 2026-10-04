package com.swingcraft4j.demo;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.ui.FlatListUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The content of the themes modal: the accent colors at the top and the list of the themes below, as in the
 * demo of FlatLaf. A theme is set when it is selected in the list.
 */
class ThemesPanel extends JPanel {

    private static final String MATERIAL_PACKAGE = ".materialthemeuilite.";
    private static final String MATERIAL_SUFFIX = " (Material)";

    private static final String[] ACCENT_NAMES = {"Default", "Blue", "Purple", "Red", "Orange", "Yellow", "Green"};
    // the colors for a light and for a dark theme, the first one is only painted: the theme uses its own color
    private static final int[] ACCENT_LIGHT = {0x2675BF, 0x007AFF, 0xBF5AF2, 0xFF3B30, 0xFF9500, 0xFFCC00, 0x28CD41};
    private static final int[] ACCENT_DARK = {0x4B6EAF, 0x0A84FF, 0xBF5AF2, 0xFF453A, 0xFF9F0A, 0xFFCC00, 0x32D74B};

    // kept for the next time the modal is shown, 0 for the default
    private static int accentIndex;

    static {
        // the look and feel asks for the accent color when it is set up
        FlatLaf.setSystemColorGetter(name -> "accent".equals(name) && accentIndex > 0 ? getAccentColor(accentIndex) : null);
    }

    private final List<Theme> allThemes = new ArrayList<>();
    private final List<Theme> themes = new ArrayList<>();
    // the index of the first theme of a category, and the title painted above it
    private final Map<Integer, String> categories = new HashMap<>();

    private final JToggleButton[] accentButtons = new JToggleButton[ACCENT_NAMES.length];
    private final JComboBox<String> filter = new JComboBox<>(new String[]{"All", "Light", "Dark"});
    private final JList<Theme> list = new JList<>();
    // true while the list is changed by the code, so the theme is not set again
    private boolean adjusting;

    ThemesPanel() {
        // the modal has the insets. Without the visual padding, so the focus border is not cut off at the edge
        super(new MigLayout("fill,wrap,insets 0,novisualpadding", "[fill]", "[][][][grow,fill]"));
        loadThemes();
        add(new JLabel("Accent color"));
        add(createAccentColors());
        add(new JLabel("Themes"), "split 2");
        add(filter, "gapbefore push");
        JScrollPane scrollPane = new JScrollPane(list);
        // a line of 1 pixel, the same with and without the focus
        scrollPane.putClientProperty(FlatClientProperties.STYLE, "" +
                "focusWidth:0;" +
                "focusedBorderColor:$Component.borderColor;");
        add(scrollPane, "width 250");

        // not wider than its text
        filter.putClientProperty(FlatClientProperties.MINIMUM_WIDTH, 0);
        // the list has the focus, so the arrow keys go through the themes
        filter.setFocusable(false);
        filter.addActionListener(e -> updateList());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new ThemeRenderer());
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !adjusting) {
                Theme theme = list.getSelectedValue();
                // after the list has painted the selection
                EventQueue.invokeLater(() -> setTheme(theme));
            }
        });
        updateList();
        updateAccentColors();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // the list has its size after the modal is laid out
        EventQueue.invokeLater(() -> list.ensureIndexIsVisible(list.getSelectedIndex()));
    }

    private Component createAccentColors() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < accentButtons.length; i++) {
            int index = i;
            JToggleButton button = new JToggleButton(new AccentIcon(i), i == accentIndex);
            button.setToolTipText(ACCENT_NAMES[i]);
            button.addActionListener(e -> setAccent(index));
            accentButtons[i] = button;
            group.add(button);
            toolBar.add(button);
        }
        return toolBar;
    }

    private void loadThemes() {
        allThemes.add(new Theme("FlatLaf Light", FlatLightLaf.class.getName(), false, "Core themes"));
        allThemes.add(new Theme("FlatLaf Dark", FlatDarkLaf.class.getName(), true, "Core themes"));
        allThemes.add(new Theme("FlatLaf IntelliJ", FlatIntelliJLaf.class.getName(), false, "Core themes"));
        allThemes.add(new Theme("FlatLaf Darcula", FlatDarculaLaf.class.getName(), true, "Core themes"));
        allThemes.add(new Theme("FlatLaf macOS Light", FlatMacLightLaf.class.getName(), false, "Core themes"));
        allThemes.add(new Theme("FlatLaf macOS Dark", FlatMacDarkLaf.class.getName(), true, "Core themes"));

        List<Theme> intellij = new ArrayList<>();
        List<Theme> material = new ArrayList<>();
        for (FlatAllIJThemes.FlatIJLookAndFeelInfo info : FlatAllIJThemes.INFOS) {
            String name = info.getName();
            if (info.getClassName().contains(MATERIAL_PACKAGE)) {
                // the title of the category says it
                if (name.endsWith(MATERIAL_SUFFIX)) {
                    name = name.substring(0, name.length() - MATERIAL_SUFFIX.length());
                }
                material.add(new Theme(name, info.getClassName(), info.isDark(), "Material Theme UI Lite"));
            } else {
                intellij.add(new Theme(name, info.getClassName(), info.isDark(), "IntelliJ themes"));
            }
        }
        intellij.sort((t1, t2) -> t1.name.compareToIgnoreCase(t2.name));
        material.sort((t1, t2) -> t1.name.compareToIgnoreCase(t2.name));
        allThemes.addAll(intellij);
        allThemes.addAll(material);
    }

    /**
     * Fills the list with the themes of the filter and selects the theme that is used.
     */
    private void updateList() {
        int filterIndex = filter.getSelectedIndex();
        themes.clear();
        categories.clear();
        String lastCategory = null;
        for (Theme theme : allThemes) {
            if ((filterIndex == 1 && theme.dark) || (filterIndex == 2 && !theme.dark)) {
                continue;
            }
            if (!theme.category.equals(lastCategory)) {
                lastCategory = theme.category;
                categories.put(themes.size(), lastCategory);
            }
            themes.add(theme);
        }

        String className = UIManager.getLookAndFeel().getClass().getName();
        adjusting = true;
        list.setListData(themes.toArray(new Theme[0]));
        for (int i = 0; i < themes.size(); i++) {
            if (themes.get(i).className.equals(className)) {
                list.setSelectedIndex(i);
                // before the modal is shown the list has no size, addNotify does it then
                if (list.isShowing()) {
                    list.ensureIndexIsVisible(i);
                }
                break;
            }
        }
        adjusting = false;
    }

    private void setTheme(Theme theme) {
        if (theme == null || theme.className.equals(UIManager.getLookAndFeel().getClass().getName())) {
            return;
        }
        try {
            UIManager.setLookAndFeel(theme.className);
        } catch (Exception e) {
            LoggingFacade.INSTANCE.logSevere(null, e);
            return;
        }
        FlatLaf.updateUI();
        updateAccentColors();
    }

    private void setAccent(int index) {
        if (index == accentIndex) {
            return;
        }
        accentIndex = index;
        // the accent color is read when the look and feel is set up, so the same one is set up again
        try {
            FlatLaf.setup(UIManager.getLookAndFeel().getClass().getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            LoggingFacade.INSTANCE.logSevere(null, e);
            return;
        }
        FlatLaf.updateUI();
    }

    // only the core themes take an accent color, an IntelliJ theme has its own colors
    private void updateAccentColors() {
        boolean supported = !UIManager.getLookAndFeel().getClass().getName().contains(".intellijthemes.");
        for (JToggleButton button : accentButtons) {
            button.setEnabled(supported);
        }
    }

    private static Color getAccentColor(int index) {
        return new Color(FlatLaf.isLafDark() ? ACCENT_DARK[index] : ACCENT_LIGHT[index]);
    }

    private static final class Theme {

        private final String name;
        private final String className;
        private final boolean dark;
        private final String category;

        private Theme(String name, String className, boolean dark, String category) {
            this.name = name;
            this.className = className;
            this.dark = dark;
            this.category = category;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * A square in an accent color. The color is read when painting, it is not the same in a light and a dark theme.
     */
    private static final class AccentIcon implements Icon {

        private static final int SIZE = 16;

        private final int index;

        private AccentIcon(int index) {
            this.index = index;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                UIScale.scaleGraphics(g2);
                if (!c.isEnabled()) {
                    g2.setComposite(AlphaComposite.SrcOver.derive(0.3f));
                }
                g2.setColor(getAccentColor(index));
                g2.fill(new RoundRectangle2D.Float(1, 1, SIZE - 2, SIZE - 2, 5, 5));
            } finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return UIScale.scale(SIZE);
        }

        @Override
        public int getIconHeight() {
            return UIScale.scale(SIZE);
        }
    }

    /**
     * Paints the title of a category above the first theme of it.
     */
    private final class ThemeRenderer extends DefaultListCellRenderer {

        private int index;
        private boolean selected;
        private int titleHeight;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            this.index = index;
            this.selected = isSelected;
            this.titleHeight = 0;
            JComponent c = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String title = categories.get(index);
            if (title != null) {
                Border border = new TitleBorder(title);
                c.setBorder(new CompoundBorder(border, c.getBorder()));
                titleHeight = border.getBorderInsets(c).top;
            }
            return c;
        }

        // the selection is painted below the title only
        @Override
        public boolean isOpaque() {
            return !isSelectedTitle();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isSelectedTitle()) {
                g.setColor(getBackground());
                FlatListUI.paintCellSelection(list, g, index, 0, titleHeight, getWidth(), getHeight() - titleHeight);
            }
            super.paintComponent(g);
        }

        private boolean isSelectedTitle() {
            return titleHeight > 0 && selected;
        }
    }

    /**
     * The title of a category, with a line at both sides of it.
     */
    private final class TitleBorder implements Border {

        private final String title;

        private TitleBorder(String title) {
            this.title = title;
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(c.getFontMetrics(list.getFont()).getHeight(), 0, 0, 0);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            FontMetrics fm = c.getFontMetrics(list.getFont());
            int titleWidth = fm.stringWidth(title);
            int titleHeight = fm.getHeight();
            g.setColor(list.getBackground());
            g.fillRect(x, y, width, titleHeight);

            Graphics2D g2 = (Graphics2D) g.create();
            try {
                FlatUIUtils.setRenderingHints(g2);
                g2.setColor(UIManager.getColor("Label.disabledForeground"));
                int gap = UIScale.scale(4);
                int lineWidth = (width - titleWidth) / 2 - gap * 2;
                if (lineWidth > 0) {
                    int lineY = y + Math.round(titleHeight / 2f);
                    float lineHeight = UIScale.scale(1f);
                    g2.fill(new Rectangle2D.Float(x + gap, lineY, lineWidth, lineHeight));
                    g2.fill(new Rectangle2D.Float(x + width - gap - lineWidth, lineY, lineWidth, lineHeight));
                }
                FlatUIUtils.drawString(list, g2, title, x + (width - titleWidth) / 2, y + fm.getAscent());
            } finally {
                g2.dispose();
            }
        }
    }
}
