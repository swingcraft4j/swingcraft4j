package com.swingcraft4j.demo;

import com.formdev.flatlaf.FlatClientProperties;
import com.swingcraft4j.toast.JToast;
import com.swingcraft4j.toast.ToastController;
import com.swingcraft4j.toast.ToastListener;
import com.swingcraft4j.toast.ToastType;
import com.swingcraft4j.toast.option.BackgroundType;
import com.swingcraft4j.toast.option.BorderType;
import com.swingcraft4j.toast.option.Easing;
import com.swingcraft4j.toast.option.ListOverflow;
import com.swingcraft4j.toast.option.ProgressLinePosition;
import com.swingcraft4j.toast.option.Shadow;
import com.swingcraft4j.toast.option.Surface;
import com.swingcraft4j.toast.option.ToastDirection;
import com.swingcraft4j.toast.option.ToastLayoutType;
import com.swingcraft4j.toast.option.ToastLocation;
import com.swingcraft4j.toast.option.ToastOption;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shows toasts with the options selected in the form.
 */
public class ToastDemo extends JPanel {

    // the names of the two parts of a ToastLocation
    private OptionGroup<String> horizontal;
    private OptionGroup<String> vertical;
    private OptionGroup<ToastLayoutType> layoutType;

    private JCheckBox chAnimation;
    private JCheckBox chFade;
    private JCheckBox chPauseOnHover;
    private JCheckBox chAutoClose;
    private JCheckBox chCloseOnClick;
    private JCheckBox chRelativeToOwner;
    private JCheckBox chReverseOrder;
    private JCheckBox chGroupRepeated;
    private JCheckBox chCancellable;
    private JSpinner delay;
    private JSpinner duration;
    private JSpinner openDuration;
    private JSpinner closeDuration;
    private JComboBox<ToastDirection> direction;
    private JComboBox<String> easing;

    // an easing has no name, so they are selected by these
    private final Map<String, Easing> easings = new LinkedHashMap<>();

    private JSpinner margin;
    private JSpinner gap;
    private JSpinner listMaxVisible;
    private JComboBox<Surface> surface;
    private JComboBox<ListOverflow> listOverflow;
    private JSpinner stackOffset;
    private JSpinner stackMaxVisible;
    private JCheckBox chStackExpandOnHover;

    private OptionGroup<BackgroundType> backgroundType;
    private ColorOption color;

    private OptionGroup<BorderType> borderType;
    private JSpinner lineSize;
    private JSpinner round;

    private JComboBox<Shadow> shadow;
    private ColorOption shadowColor;
    private JComboBox<String> shadowOpacity;

    private JCheckBox chShowIcon;
    private JCheckBox chCustomIcon;
    private JCheckBox chIconSeparateLine;
    private JCheckBox chShowLabel;
    private JCheckBox chShowCloseButton;
    private JCheckBox chPaintTextColor;
    private JCheckBox chShowProgressLine;
    private JComboBox<ProgressLinePosition> progressLinePosition;
    private ColorOption progressLineColor;
    private JTextField label;
    private JSpinner padding;
    private JSpinner maxWidth;

    private JLabel labelEvent;

    // shows what the listener of a toast receives
    private final ToastListener listener = new ToastListener() {
        @Override
        public void toastClicked(ToastController toast) {
            labelEvent.setText(toast.getId() + " clicked");
        }

        @Override
        public void toastClosed(ToastController toast) {
            labelEvent.setText(toast.getId() + " closed");
        }
    };

    public ToastDemo() {
        // the groups are placed straight in this layout, so the default gap is between all of them
        setLayout(new MigLayout("fillx,wrap 5", "[fill][fill][fill][fill][grow,fill]"));
        add(createHorizontalOption());
        add(createVerticalOption());
        add(createLayoutTypeOption(), "span 3");
        add(createToastOption(), "growy");
        add(createLayoutOption(), "growy");
        add(createBackgroundAndShadowOption(), "growy");
        add(createBorderOption(), "growy");
        add(createOtherOption(), "growy");
        add(createSampleToast(), "span 5");
        add(createCustomToast(), "span 5");
    }

    // the two short groups in one column, so all the groups fit in the width of the window
    private Component createBackgroundAndShadowOption() {
        JPanel panel = new JPanel(new MigLayout("insets 0,fill,wrap", "[fill]", "[][grow,fill]"));
        panel.add(createBackgroundOption());
        panel.add(createShadowOption());
        return panel;
    }

    private Component createHorizontalOption() {
        JPanel panel = DemoUtils.createGroup("Horizontal option", "");
        horizontal = new OptionGroup<String>(panel, "")
                .add("Leading", "LEADING")
                .add("Center", "CENTER", true)
                .add("Trailing", "TRAILING");
        return panel;
    }

    private Component createVerticalOption() {
        JPanel panel = DemoUtils.createGroup("Vertical option", "");
        vertical = new OptionGroup<String>(panel, "")
                .add("Top", "TOP", true)
                .add("Bottom", "BOTTOM");
        return panel;
    }

    private Component createLayoutTypeOption() {
        JPanel panel = DemoUtils.createGroup("Layout type", "");
        layoutType = new OptionGroup<ToastLayoutType>(panel, "")
                .add("List", ToastLayoutType.LIST, true)
                .add("Stack", ToastLayoutType.STACK)
                .add("Replace", ToastLayoutType.REPLACE)
                .add("Banner", ToastLayoutType.BANNER);
        return panel;
    }

    private Component createToastOption() {
        JPanel panel = DemoUtils.createFormGroup("Toast option");
        chAnimation = new JCheckBox("Animation enable", true);
        chFade = new JCheckBox("Animate fade", true);
        chPauseOnHover = new JCheckBox("Pause delay on hover", true);
        chAutoClose = new JCheckBox("Auto close", true);
        chCloseOnClick = new JCheckBox("Close on click");
        chRelativeToOwner = new JCheckBox("Relative to owner");
        chReverseOrder = new JCheckBox("Reverse order");
        chGroupRepeated = new JCheckBox("Group repeated");
        chCancellable = new JCheckBox("Cancellable task");
        chCancellable.setToolTipText("The toast of a task has a cancel button while the task runs");
        delay = DemoUtils.createSpinner(3000, 0, 60000, 500);
        duration = DemoUtils.createSpinner(350, 0, 5000, 50);
        openDuration = DemoUtils.createSpinner(-1, -1, 5000, 50);
        openDuration.setToolTipText("-1 to use the duration");
        closeDuration = DemoUtils.createSpinner(-1, -1, 5000, 50);
        closeDuration.setToolTipText("-1 to use the duration");
        direction = new JComboBox<>(ToastDirection.values());
        easings.put("Standard", Easing.STANDARD);
        easings.put("Linear", Easing.LINEAR);
        easings.put("Ease in", Easing.EASE_IN);
        easings.put("Ease out", Easing.EASE_OUT);
        easings.put("Ease in out", Easing.EASE_IN_OUT);
        easing = new JComboBox<>(easings.keySet().toArray(new String[0]));
        // the order is not an option of a toast, it applies to the toasts that are showing too
        chReverseOrder.addActionListener(e -> JToast.setReverseOrder(chReverseOrder.isSelected()));
        panel.add(chAnimation, "span 2");
        panel.add(chFade, "span 2");
        panel.add(chPauseOnHover, "span 2");
        panel.add(chAutoClose, "span 2");
        panel.add(chCloseOnClick, "span 2");
        panel.add(chRelativeToOwner, "span 2");
        panel.add(chReverseOrder, "span 2");
        panel.add(chGroupRepeated, "span 2");
        panel.add(chCancellable, "span 2");
        DemoUtils.addRow(panel, "Delay", delay);
        DemoUtils.addRow(panel, "Duration", duration);
        DemoUtils.addRow(panel, "Open duration", openDuration);
        DemoUtils.addRow(panel, "Close duration", closeDuration);
        DemoUtils.addRow(panel, "Direction", direction);
        DemoUtils.addRow(panel, "Easing", easing);
        return panel;
    }

    private Component createLayoutOption() {
        JPanel panel = DemoUtils.createFormGroup("Layout");
        surface = new JComboBox<>(Surface.values());
        surface.setToolTipText("WINDOW shows each toast in a window of its own: it is not limited by this window, try it with a small window");
        margin = DemoUtils.createSpinner(10, 0, 200, 1);
        gap = DemoUtils.createSpinner(10, 0, 100, 1);
        listMaxVisible = DemoUtils.createSpinner(0, 0, 20, 1);
        listMaxVisible.setToolTipText("0 for no limit");
        listOverflow = new JComboBox<>(ListOverflow.values());
        stackOffset = DemoUtils.createSpinner(10, 0, 100, 1);
        stackMaxVisible = DemoUtils.createSpinner(3, 1, 20, 1);
        chStackExpandOnHover = new JCheckBox("Stack expand on hover");
        DemoUtils.addRow(panel, "Surface", surface);
        DemoUtils.addRow(panel, "Margin", margin);
        DemoUtils.addRow(panel, "Gap", gap);
        DemoUtils.addRow(panel, "List max visible", listMaxVisible);
        DemoUtils.addRow(panel, "List overflow", listOverflow);
        DemoUtils.addRow(panel, "Stack offset", stackOffset);
        DemoUtils.addRow(panel, "Stack max visible", stackMaxVisible);
        panel.add(chStackExpandOnHover, "span 2");
        return panel;
    }

    private Component createBackgroundOption() {
        JPanel panel = DemoUtils.createFormGroup("Background style");
        backgroundType = new OptionGroup<BackgroundType>(panel, "span 2")
                .add("Default", BackgroundType.DEFAULT, true)
                .add("Tint", BackgroundType.TINT)
                .add("Gradient", BackgroundType.GRADIENT);
        color = new ColorOption();
        color.setToolTipText("The color of the toast in place of the color of its type");
        DemoUtils.addRow(panel, "Color", color);
        return panel;
    }

    private Component createBorderOption() {
        JPanel panel = DemoUtils.createFormGroup("Border style");
        borderType = new OptionGroup<BorderType>(panel, "span 2")
                .add("Default", BorderType.DEFAULT, true)
                .add("Outline", BorderType.OUTLINE)
                .add("Trailing line", BorderType.TRAILING_LINE)
                .add("Leading line", BorderType.LEADING_LINE)
                .add("Top line", BorderType.TOP_LINE)
                .add("Bottom line", BorderType.BOTTOM_LINE);
        lineSize = DemoUtils.createSpinner(3, 0, 20, 1);
        round = DemoUtils.createSpinner(10, 0, 100, 2);
        DemoUtils.addRow(panel, "Line size", lineSize);
        DemoUtils.addRow(panel, "Round", round);
        return panel;
    }

    private Component createShadowOption() {
        JPanel panel = DemoUtils.createFormGroup("Shadow style");
        shadow = new JComboBox<>(Shadow.values());
        shadow.setSelectedItem(Shadow.MEDIUM);
        shadowColor = new ColorOption();
        shadowOpacity = DemoUtils.createShadowOpacity();
        DemoUtils.addRow(panel, "Shadow", shadow);
        DemoUtils.addRow(panel, "Color", shadowColor);
        DemoUtils.addRow(panel, "Opacity", shadowOpacity);
        return panel;
    }

    private Component createOtherOption() {
        JPanel panel = DemoUtils.createFormGroup("Other style");
        chShowIcon = new JCheckBox("Show icon", true);
        chCustomIcon = new JCheckBox("Custom icon");
        chIconSeparateLine = new JCheckBox("Icon separate line");
        chShowLabel = new JCheckBox("Show label");
        chShowCloseButton = new JCheckBox("Show close button", true);
        chPaintTextColor = new JCheckBox("Paint text color");
        chShowProgressLine = new JCheckBox("Show progress line");
        label = new JTextField();
        label.setToolTipText("The text of the label in place of the name of the type");
        padding = DemoUtils.createSpinner(5, 0, 50, 1);
        maxWidth = DemoUtils.createSpinner(0, 0, 2000, 10);
        maxWidth.setToolTipText("0 for no limit");
        progressLinePosition = new JComboBox<>(ProgressLinePosition.values());
        progressLinePosition.setSelectedItem(ProgressLinePosition.BOTTOM);
        progressLineColor = new ColorOption();
        progressLineColor.setToolTipText("The color of the progress line in place of the color of the toast");
        panel.add(chShowIcon, "span 2");
        panel.add(chCustomIcon, "span 2");
        panel.add(chIconSeparateLine, "span 2");
        panel.add(chShowLabel, "span 2");
        panel.add(chShowCloseButton, "span 2");
        panel.add(chPaintTextColor, "span 2");
        panel.add(chShowProgressLine, "span 2");
        DemoUtils.addRow(panel, "Label", label);
        DemoUtils.addRow(panel, "Padding", padding);
        DemoUtils.addRow(panel, "Max width", maxWidth);
        DemoUtils.addRow(panel, "Progress line", progressLinePosition);
        DemoUtils.addRow(panel, "Progress color", progressLineColor);
        return panel;
    }

    private Component createSampleToast() {
        JPanel panel = DemoUtils.createGroup("Sample toast", "fillx");
        labelEvent = new JLabel();
        labelEvent.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground");
        panel.add(DemoUtils.createLink("Show default", () -> show(ToastType.DEFAULT, "Hello, this is a toast message.")));
        panel.add(DemoUtils.createLink("Show success", () -> show(ToastType.SUCCESS, "Your changes have been saved.")));
        panel.add(DemoUtils.createLink("Show info", () -> show(ToastType.INFO, "A new version is available. It is installed the next time the application starts.")));
        panel.add(DemoUtils.createLink("Show warning", () -> show(ToastType.WARNING, "The disk is almost full.")));
        panel.add(DemoUtils.createLink("Show error", () -> show(ToastType.ERROR, "The connection to the server has been lost.")));
        panel.add(DemoUtils.createLink("Show update", this::showUpdate));
        panel.add(DemoUtils.createLink("Show task", () -> showTask(false)));
        panel.add(DemoUtils.createLink("Show failing task", () -> showTask(true)));
        panel.add(DemoUtils.createLink("Close all", JToast::closeAll));
        panel.add(DemoUtils.createLink("Close location", () -> JToast.closeAll(getToastLocation())));
        panel.add(labelEvent, "push,al trailing");
        return panel;
    }

    private Component createCustomToast() {
        JPanel panel = DemoUtils.createGroup("Custom toast", "");
        panel.add(DemoUtils.createLink("Show notification (fixed opt)", this::showNotification));
        panel.add(DemoUtils.createLink("Show custom HTML (fixed opt)", this::showCustomHtml));
        panel.add(DemoUtils.createLink("Show custom", this::showCustom));
        return panel;
    }

    private ToastLocation getToastLocation() {
        return ToastLocation.valueOf(vertical.getValue() + "_" + horizontal.getValue());
    }

    /**
     * @return the option with all that is selected in the form
     */
    private ToastOption createOption() {
        ToastOption option = JToast.createOption()
                .setLocation(getToastLocation())
                .setLayoutType(layoutType.getValue())
                .setMargin(DemoUtils.intValue(margin))
                .setGap(DemoUtils.intValue(gap))
                .setListMaxVisible(DemoUtils.intValue(listMaxVisible))
                .setListOverflow((ListOverflow) listOverflow.getSelectedItem())
                .setStackOffset(DemoUtils.intValue(stackOffset))
                .setStackMaxVisible(DemoUtils.intValue(stackMaxVisible))
                .setStackExpandOnHover(chStackExpandOnHover.isSelected())
                .setRelativeToOwner(chRelativeToOwner.isSelected())
                .setSurface((Surface) surface.getSelectedItem())
                .setAutoClose(chAutoClose.isSelected())
                .setDelay(DemoUtils.intValue(delay))
                .setPauseOnHover(chPauseOnHover.isSelected())
                .setCloseOnClick(chCloseOnClick.isSelected())
                .setGroupRepeated(chGroupRepeated.isSelected())
                .setCancellable(chCancellable.isSelected())
                .addListener(listener);
        option.getAnimationOption()
                .setEnabled(chAnimation.isSelected())
                .setFade(chFade.isSelected())
                .setDuration(DemoUtils.intValue(duration))
                .setOpenDuration(DemoUtils.intValue(openDuration))
                .setCloseDuration(DemoUtils.intValue(closeDuration))
                .setEasing(easings.get((String) easing.getSelectedItem()))
                .setDirection((ToastDirection) direction.getSelectedItem());
        String labelText = label.getText().trim();
        option.getStyleOption()
                .setBackgroundType(backgroundType.getValue())
                .setColor(color.getColor())
                .setBorderType(borderType.getValue())
                .setLineSize(DemoUtils.intValue(lineSize))
                .setRound(DemoUtils.intValue(round))
                .setShadow((Shadow) shadow.getSelectedItem())
                .setShadowColor(shadowColor.getColor())
                .setShadowOpacity(DemoUtils.shadowOpacityValue(shadowOpacity))
                .setPadding(DemoUtils.intValue(padding))
                .setMaxWidth(DemoUtils.intValue(maxWidth))
                .setShowIcon(chShowIcon.isSelected())
                .setIcon(chCustomIcon.isSelected() ? UIManager.getIcon("FileView.floppyDriveIcon") : null)
                .setIconSeparateLine(chIconSeparateLine.isSelected())
                .setShowLabel(chShowLabel.isSelected())
                .setLabel(labelText.isEmpty() ? null : labelText)
                .setShowCloseButton(chShowCloseButton.isSelected())
                .setPaintTextColor(chPaintTextColor.isSelected())
                .setShowProgressLine(chShowProgressLine.isSelected())
                .setProgressLinePosition((ProgressLinePosition) progressLinePosition.getSelectedItem())
                .setProgressLineColor(progressLineColor.getColor());
        return option;
    }

    private void show(ToastType type, String message) {
        JToast.show(this, type, message, createOption());
    }

    // a message that a task is running, changed to the result of the task
    // a toast that is loading does not close by itself, it waits for the update
    private void showUpdate() {
        ToastController controller = JToast.show(this, ToastType.LOADING, "Saving your changes...", createOption());
        Timer timer = new Timer(1500, e -> controller.update(ToastType.SUCCESS, "Your changes have been saved."));
        timer.setRepeats(false);
        timer.start();
    }

    // the task runs on another thread, the toast shows that it is running and then its result
    private void showTask(boolean fail) {
        JToast.showTask(this, "Uploading the file...", progress -> {
            Thread.sleep(1500);
            progress.setMessage("Checking the file...");
            Thread.sleep(1500);
            if (fail) {
                throw new IOException("the server does not answer");
            }
            return "report.pdf";
        }, name -> "The file " + name + " has been uploaded.", error -> "The upload failed: " + error.getMessage(), createOption());
    }

    private void showNotification() {
        JPanel panel = createContent("wrap,width 260", "[fill]");
        JLabel title = new JLabel("New message");
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +1");
        JButton buttonReply = new JButton("Reply");
        JButton buttonDismiss = new JButton("Dismiss");
        buttonReply.addActionListener(e -> {
            close(panel);
            show(ToastType.SUCCESS, "Your reply has been sent.");
        });
        buttonDismiss.addActionListener(e -> close(panel));
        panel.add(title);
        panel.add(new JLabel("<html>Raven sent you a message:<br>\"Do you have time for a call today?\"</html>"));
        panel.add(buttonReply, "split 2,sg button");
        panel.add(buttonDismiss, "sg button");

        ToastOption option = new ToastOption()
                .setLocation(ToastLocation.TOP_TRAILING)
                .setAutoClose(false)
                .addListener(listener);
        JToast.showCustom(this, panel, option);
    }

    private void showCustomHtml() {
        JPanel panel = createContent("", "");
        panel.add(new JLabel("<html><b>Custom HTML</b><br>"
                + "A toast can show <i>any</i> component, such as a label with "
                + "<font color='#3B82F6'><u>HTML</u></font> text.</html>"));

        ToastOption option = new ToastOption()
                .setLocation(ToastLocation.BOTTOM_TRAILING)
                .setCloseOnClick(true)
                .addListener(listener);
        option.getStyleOption()
                .setBorderType(BorderType.LEADING_LINE)
                .setShowProgressLine(true);
        JToast.showCustom(this, panel, option);
    }

    // a progress bar that closes its toast when it is done
    private void showCustom() {
        JPanel panel = createContent("wrap,width 220", "[fill]");
        JProgressBar progressBar = new JProgressBar();
        panel.add(new JLabel("Uploading file..."), "split 2,growx");
        panel.add(DemoUtils.createLink("Cancel", () -> close(panel)), "grow 0");
        panel.add(progressBar);

        ToastOption option = createOption()
                .setAutoClose(false);
        ToastController controller = JToast.showCustom(this, panel, option);
        Timer timer = new Timer(40, null);
        timer.addActionListener(e -> {
            if (!controller.isOpen()) {
                timer.stop();
            } else if (progressBar.getValue() < progressBar.getMaximum()) {
                progressBar.setValue(progressBar.getValue() + 1);
            } else {
                timer.stop();
                controller.close();
                JToast.show(this, ToastType.SUCCESS, "The file has been uploaded.", option.setAutoClose(chAutoClose.isSelected()));
            }
        });
        timer.start();
    }

    private void close(Component component) {
        ToastController controller = JToast.getController(component);
        if (controller != null) {
            controller.close();
        }
    }

    // the background of the toast is painted behind the content
    private JPanel createContent(String layoutConstraints, String columnConstraints) {
        JPanel panel = new JPanel(new MigLayout(layoutConstraints, columnConstraints));
        panel.setOpaque(false);
        return panel;
    }
}
