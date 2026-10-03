package com.swingcraft4j.demo;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.ColorFunctions;
import com.swingcraft4j.modal.JModal;
import com.swingcraft4j.modal.option.BackgroundMode;
import com.swingcraft4j.modal.option.Location;
import com.swingcraft4j.modal.option.ModalOption;
import com.swingcraft4j.modal.option.Shadow;
import com.swingcraft4j.modal.option.Surface;
import com.swingcraft4j.modal.simple.SimpleModal;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Shows modals with the options selected in the form.
 */
public class ModalDemo extends JPanel {

    private OptionGroup<Location> horizontal;
    private OptionGroup<Location> vertical;

    private JCheckBox chAnimation;
    private JCheckBox chFade;
    private JCheckBox chSnapshot;
    private JCheckBox chCloseOnEscape;
    private JCheckBox chRelativeToOwner;
    private JCheckBox chMovable;

    private OptionGroup<BackgroundMode> backgroundMode;
    private JSpinner backgroundOpacity;
    private ColorOption backgroundColor;

    private JComboBox<Surface> surface;
    private JSpinner margin;
    private JTextField width;
    private JTextField height;

    private JSpinner round;
    private JSpinner borderWidth;
    private ColorOption borderColor;
    private JComboBox<Shadow> shadow;
    private ColorOption shadowColor;
    private JComboBox<String> shadowOpacity;

    private JSpinner duration;
    private JSpinner slideDuration;
    private JSpinner offsetX;
    private JSpinner offsetY;
    private JSpinner scale;

    public ModalDemo() {
        // the groups are placed straight in this layout, so the default gap is between all of them
        setLayout(new MigLayout("fillx,wrap 5", "[fill][fill][fill][fill][grow,fill]"));
        add(createHorizontalOption(), "span 2");
        add(createVerticalOption(), "span 3");
        add(createOptions(), "growy");
        add(createBackgroundOption(), "growy");
        add(createLayoutOption(), "growy");
        add(createBorderOption(), "growy");
        add(createAnimationOption(), "growy");
        add(createSampleModal(), "span 5");
        add(createMessageModal(), "span 5");
    }

    private Component createHorizontalOption() {
        JPanel panel = DemoUtils.createGroup("Horizontal option", "");
        horizontal = new OptionGroup<Location>(panel, "")
                .add("Left", Location.LEFT)
                .add("Leading", Location.LEADING)
                .add("Center", Location.CENTER, true)
                .add("Trailing", Location.TRAILING)
                .add("Right", Location.RIGHT);
        return panel;
    }

    private Component createVerticalOption() {
        JPanel panel = DemoUtils.createGroup("Vertical option", "");
        vertical = new OptionGroup<Location>(panel, "")
                .add("Top", Location.TOP)
                .add("Center", Location.CENTER, true)
                .add("Bottom", Location.BOTTOM);
        return panel;
    }

    private Component createOptions() {
        JPanel panel = DemoUtils.createGroup("Options", "wrap");
        chAnimation = new JCheckBox("Animation enable", true);
        chFade = new JCheckBox("Animate fade", true);
        chSnapshot = new JCheckBox("Animate snapshot");
        chCloseOnEscape = new JCheckBox("Close on pressed escape", true);
        chRelativeToOwner = new JCheckBox("Relative to owner");
        chMovable = new JCheckBox("Movable");
        panel.add(chAnimation);
        panel.add(chFade);
        panel.add(chSnapshot);
        panel.add(chCloseOnEscape);
        panel.add(chRelativeToOwner);
        panel.add(chMovable);
        return panel;
    }

    private Component createBackgroundOption() {
        JPanel panel = DemoUtils.createFormGroup("Background click type");
        backgroundMode = new OptionGroup<BackgroundMode>(panel, "span 2")
                .add("Close modal", BackgroundMode.CLOSE_ON_CLICK, true)
                .add("Block", BackgroundMode.BLOCK)
                .add("None", BackgroundMode.NONE)
                .add("Popup", BackgroundMode.POPUP);
        backgroundOpacity = DemoUtils.createSpinner(0.2, 0, 1, 0.1);
        backgroundColor = new ColorOption();
        DemoUtils.addRow(panel, "Opacity", backgroundOpacity);
        DemoUtils.addRow(panel, "Color", backgroundColor);
        return panel;
    }

    private Component createLayoutOption() {
        JPanel panel = DemoUtils.createFormGroup("Layout");
        surface = new JComboBox<>(Surface.values());
        surface.setToolTipText("WINDOW shows the modal in a window of its own: it can be larger than this window, try it with a small window");
        margin = DemoUtils.createSpinner(15, 0, 200, 5);
        width = new JTextField("-1");
        height = new JTextField("-1");
        String sizeTip = "-1 for the preferred size, a fraction of the window such as 0.5, or a size such as 400";
        width.setToolTipText(sizeTip);
        height.setToolTipText(sizeTip);
        DemoUtils.addRow(panel, "Surface", surface);
        DemoUtils.addRow(panel, "Margin", margin);
        DemoUtils.addRow(panel, "Width", width);
        DemoUtils.addRow(panel, "Height", height);
        return panel;
    }

    private Component createBorderOption() {
        JPanel panel = DemoUtils.createFormGroup("Border");
        round = DemoUtils.createSpinner(10, 0, 100, 2);
        borderWidth = DemoUtils.createSpinner(0, 0, 10, 1);
        borderColor = new ColorOption();
        shadow = new JComboBox<>(Shadow.values());
        shadow.setSelectedItem(Shadow.MEDIUM);
        shadowColor = new ColorOption();
        shadowOpacity = DemoUtils.createShadowOpacity();
        DemoUtils.addRow(panel, "Round", round);
        DemoUtils.addRow(panel, "Outline width", borderWidth);
        DemoUtils.addRow(panel, "Outline color", borderColor);
        DemoUtils.addRow(panel, "Shadow", shadow);
        DemoUtils.addRow(panel, "Shadow color", shadowColor);
        DemoUtils.addRow(panel, "Shadow opacity", shadowOpacity);
        return panel;
    }

    private Component createAnimationOption() {
        JPanel panel = DemoUtils.createFormGroup("Animation");
        duration = DemoUtils.createSpinner(200, 0, 5000, 50);
        slideDuration = DemoUtils.createSpinner(300, 0, 5000, 50);
        offsetX = DemoUtils.createSpinner(0, -500, 500, 10);
        offsetY = DemoUtils.createSpinner(20, -500, 500, 10);
        scale = DemoUtils.createSpinner(0, 0, 0.9, 0.1);
        DemoUtils.addRow(panel, "Duration", duration);
        DemoUtils.addRow(panel, "Slide duration", slideDuration);
        DemoUtils.addRow(panel, "Offset x", offsetX);
        DemoUtils.addRow(panel, "Offset y", offsetY);
        DemoUtils.addRow(panel, "Scale", scale);
        return panel;
    }

    private Component createSampleModal() {
        JPanel panel = DemoUtils.createGroup("Sample modal", "");
        panel.add(DemoUtils.createLink("Show modal", this::showModal));
        panel.add(DemoUtils.createLink("Show slide modal", this::showSlideModal));
        panel.add(DemoUtils.createLink("Show color modal", this::showColorModal));
        panel.add(DemoUtils.createLink("Close all", JModal::closeAll));
        return panel;
    }

    private Component createMessageModal() {
        JPanel panel = DemoUtils.createGroup("Custom message modal", "");
        panel.add(DemoUtils.createLink("Show default", () -> showMessage(MessageIcon.Type.DEFAULT,
                "This is a message modal. It is a simple modal with an icon and a text as the content.")));
        panel.add(DemoUtils.createLink("Show success", () -> showMessage(MessageIcon.Type.SUCCESS,
                "Your changes have been saved. They are visible to everyone from now on.")));
        panel.add(DemoUtils.createLink("Show info", () -> showMessage(MessageIcon.Type.INFO,
                "A new version is available. It is installed the next time the application starts.")));
        panel.add(DemoUtils.createLink("Show warning", () -> showMessage(MessageIcon.Type.WARNING,
                "The file has been changed by another application. Do you want to load it again?")));
        panel.add(DemoUtils.createLink("Show error", () -> showMessage(MessageIcon.Type.ERROR,
                "The connection to the server has been lost. Check the network and try again.")));
        return panel;
    }

    /**
     * @return the option with all that is selected in the form
     */
    private ModalOption createOption() {
        ModalOption option = JModal.createOption()
                .setLocation(horizontal.getValue(), vertical.getValue())
                .setBackgroundMode(backgroundMode.getValue())
                .setBackgroundOpacity(DemoUtils.floatValue(backgroundOpacity))
                .setBackgroundColor(backgroundColor.getColor())
                .setCloseOnEscape(chCloseOnEscape.isSelected())
                .setRelativeToOwner(chRelativeToOwner.isSelected())
                .setMovable(chMovable.isSelected())
                .setSurface((Surface) surface.getSelectedItem())
                .setMargin(DemoUtils.intValue(margin))
                .setSize(getSize(width), getSize(height))
                .setRound(DemoUtils.intValue(round))
                .setBorderWidth(DemoUtils.intValue(borderWidth))
                .setBorderColor(borderColor.getColor())
                .setShadow((Shadow) shadow.getSelectedItem())
                .setShadowColor(shadowColor.getColor())
                .setShadowOpacity(DemoUtils.shadowOpacityValue(shadowOpacity));
        option.getAnimationOption()
                .setEnabled(chAnimation.isSelected())
                .setFade(chFade.isSelected())
                .setSnapshot(chSnapshot.isSelected())
                .setDuration(DemoUtils.intValue(duration))
                .setSlideDuration(DemoUtils.intValue(slideDuration))
                .setOffset(DemoUtils.intValue(offsetX), DemoUtils.intValue(offsetY))
                .setScale(DemoUtils.floatValue(scale));
        return option;
    }

    // a decimal number is a fraction of the available space, a whole number is a fixed size
    private Number getSize(JTextField field) {
        String text = field.getText().trim();
        try {
            if (text.contains(".")) {
                return Float.parseFloat(text);
            }
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            field.setText("-1");
            return -1;
        }
    }

    private void showModal() {
        JPanel content = createContent("wrap,fillx,width 320", "[fill]");
        JTextField textName = new JTextField();
        JComboBox<String> comboCategory = new JComboBox<>(new String[]{"General", "Design", "Development", "Marketing", "Support"});
        JTextArea textDescription = new JTextArea(4, 0);
        // a popup and a tooltip are shown over the modal
        textName.setToolTipText("The name is required");
        comboCategory.setToolTipText("The category of the item");
        content.add(new JLabel("Name"));
        content.add(textName);
        content.add(new JLabel("Category"));
        content.add(comboCategory);
        content.add(new JLabel("Description"));
        content.add(new JScrollPane(textDescription), "grow,push");
        SimpleModal modal = new SimpleModal(content, "Sample modal", SimpleModal.OptionType.OK_CANCEL, e -> {
            // keep the modal open until the input is valid
            if (e.getAction() == SimpleModal.OK_OPTION && textName.getText().trim().isEmpty()) {
                e.consume();
                textName.requestFocusInWindow();
            }
        });
        JModal.show(this, modal, createOption());
    }

    private void showSlideModal() {
        JPanel content = createContent("wrap", "");
        SimpleModal modal = new SimpleModal(content, "Slide modal", SimpleModal.OptionType.CLOSE, null);
        content.add(createText("This modal can show another modal in place of itself. The next modal slides in and has a back button to come back to this one."));
        content.add(DemoUtils.createLink("Show next modal", () -> modal.pushModal(createNextModal(2))));
        JModal.show(this, modal, createOption());
    }

    private SimpleModal createNextModal(int index) {
        JPanel content = createContent("wrap", "");
        SimpleModal modal = new SimpleModal(content, "Slide modal " + index, SimpleModal.OptionType.CLOSE, null);
        content.add(createText("This is modal " + index + ". Use the back button in the header to go back, or show one more modal."));
        content.add(DemoUtils.createLink("Show next modal", () -> modal.pushModal(createNextModal(index + 1))));
        return modal;
    }

    private void showColorModal() {
        JPanel content = createContent("wrap", "");
        content.add(createText("The background color is set on the modal. The round border is filled with it, and it is seen behind the content that is not opaque."));
        SimpleModal modal = new SimpleModal(content, "Color modal", SimpleModal.OptionType.OK_CANCEL, null);
        // a light or a dark color of the accent color, so the text of the look and feel can be read on it
        Color accent = UIManager.getColor("Component.accentColor");
        modal.setBackground(FlatLaf.isLafDark() ? ColorFunctions.shade(accent, 0.7f) : ColorFunctions.tint(accent, 0.85f));
        JModal.show(this, modal, createOption());
    }

    private void showMessage(MessageIcon.Type type, String message) {
        JPanel content = createContent("", "");
        content.add(new JLabel(new MessageIcon(type)), "top");
        content.add(createText(message));
        SimpleModal.OptionType optionType = type == MessageIcon.Type.WARNING
                ? SimpleModal.OptionType.YES_NO_CANCEL
                : SimpleModal.OptionType.OK_CANCEL;
        JModal.show(this, new SimpleModal(content, type.getTitle(), optionType, null), createOption());
    }

    // the background of the modal is painted behind the content
    private JPanel createContent(String layoutConstraints, String columnConstraints) {
        JPanel panel = new JPanel(new MigLayout(layoutConstraints, columnConstraints));
        panel.setOpaque(false);
        return panel;
    }

    private Component createText(String text) {
        return new JLabel("<html><body style='width:250px'>" + text + "</body></html>");
    }
}
