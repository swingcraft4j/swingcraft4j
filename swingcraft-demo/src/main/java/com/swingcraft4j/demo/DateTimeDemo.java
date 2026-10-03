package com.swingcraft4j.demo;

import com.swingcraft4j.datetime.DatePicker;
import com.swingcraft4j.datetime.DateRange;
import com.swingcraft4j.datetime.DateSelectionEvent;
import com.swingcraft4j.datetime.TimePicker;
import com.swingcraft4j.datetime.option.DateOption;
import com.swingcraft4j.datetime.option.DateSelectionMode;
import com.swingcraft4j.datetime.option.PickerSize;
import com.swingcraft4j.datetime.option.TimeOption;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The demo of the date picker and the time picker: each one in the window and in a popup, with its options.
 */
public class DateTimeDemo extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    // the least width of the groups with the options
    private static final int OPTION_WIDTH = 240;
    // a label that shows what is selected takes the width of its group and never makes the group wider:
    // a text that is longer, as a range of dates, would change the width of the group each time
    private static final String VALUE_LABEL = "growx,width 0:0";

    // a picker is used in the window or in a popup, so there are two of each
    private final DatePicker datePicker = new DatePicker();
    private final DatePicker datePopup = new DatePicker();
    private final TimePicker timePicker = new TimePicker();
    private final TimePicker timePopup = new TimePicker();

    private OptionGroup<DateSelectionMode> selectionMode;
    private JComboBox<PickerSize> dateSize;
    private JComboBox<DayOfWeek> firstDayOfWeek;
    private JComboBox<String> locale;
    private final Map<String, Locale> locales = new LinkedHashMap<>();
    private JCheckBox chDateCloseOnSelect;
    private JCheckBox chDateAnimation;
    private JSpinner dateDuration;
    private JCheckBox chDateEnabled;

    private JComboBox<PickerSize> timeSize;
    private JCheckBox chHour24;
    private JCheckBox chTimeCloseOnSelect;
    private JCheckBox chTimeAnimation;
    private JSpinner timeDuration;
    private JCheckBox chTimeEnabled;

    public DateTimeDemo() {
        // the options of both pickers one below the other at the left, and the two pickers next to them.
        // A picker has its own size, so its group is as large as the selected size needs
        setLayout(new MigLayout("", "[fill," + OPTION_WIDTH + "::][fill][fill]", "[top][top]"));
        add(createDateOption(), "cell 0 0");
        add(createTimeOption(), "cell 0 1");
        add(createDatePicker(), "cell 1 0,spany 2");
        add(createTimePicker(), "cell 2 0,spany 2");
    }

    private Component createDateOption() {
        JPanel panel = createOptionGroup("Date option");
        JPanel panelMode = new JPanel(new MigLayout("insets 0"));
        selectionMode = new OptionGroup<DateSelectionMode>(panelMode, "")
                .add("Single", DateSelectionMode.SINGLE, true)
                .add("Range", DateSelectionMode.RANGE);
        dateSize = createSize();
        firstDayOfWeek = new JComboBox<>(DayOfWeek.values());
        firstDayOfWeek.setSelectedItem(DayOfWeek.SUNDAY);
        // null is the default locale
        locales.put("Default", null);
        locales.put("English", Locale.ENGLISH);
        locales.put("French", Locale.FRENCH);
        locales.put("German", Locale.GERMAN);
        locales.put("Spanish", new Locale("es"));
        locales.put("Japanese", Locale.JAPANESE);
        locales.put("Khmer", new Locale("km"));
        locale = new JComboBox<>(locales.keySet().toArray(new String[0]));
        chDateCloseOnSelect = new JCheckBox("Close popup on select");
        chDateAnimation = new JCheckBox("Animation enable", true);
        dateDuration = DemoUtils.createSpinner(300, 0, 5000, 50);
        chDateEnabled = new JCheckBox("Enabled", true);
        panel.add(panelMode, "span 2");
        DemoUtils.addRow(panel, "Size", dateSize);
        DemoUtils.addRow(panel, "First day", firstDayOfWeek);
        DemoUtils.addRow(panel, "Locale", locale);
        DemoUtils.addRow(panel, "Duration", dateDuration);
        panel.add(chDateAnimation, "span 2");
        panel.add(chDateCloseOnSelect, "span 2");
        panel.add(chDateEnabled, "span 2");
        for (Component component : panelMode.getComponents()) {
            ((JRadioButton) component).addActionListener(e -> applyDateOption());
        }
        dateSize.addActionListener(e -> applyDateOption());
        firstDayOfWeek.addActionListener(e -> applyDateOption());
        locale.addActionListener(e -> applyDateOption());
        chDateCloseOnSelect.addActionListener(e -> applyDateOption());
        chDateAnimation.addActionListener(e -> applyDateOption());
        dateDuration.addChangeListener(e -> applyDateOption());
        chDateEnabled.addActionListener(e -> datePicker.setEnabled(chDateEnabled.isSelected()));
        return panel;
    }

    private Component createTimeOption() {
        JPanel panel = createOptionGroup("Time option");
        timeSize = createSize();
        chHour24 = new JCheckBox("24 hours");
        chTimeCloseOnSelect = new JCheckBox("Close popup on select");
        chTimeAnimation = new JCheckBox("Animation enable", true);
        timeDuration = DemoUtils.createSpinner(300, 0, 5000, 50);
        chTimeEnabled = new JCheckBox("Enabled", true);
        panel.add(chHour24, "span 2");
        DemoUtils.addRow(panel, "Size", timeSize);
        DemoUtils.addRow(panel, "Duration", timeDuration);
        panel.add(chTimeAnimation, "span 2");
        panel.add(chTimeCloseOnSelect, "span 2");
        panel.add(chTimeEnabled, "span 2");
        timeSize.addActionListener(e -> applyTimeOption());
        chHour24.addActionListener(e -> applyTimeOption());
        chTimeCloseOnSelect.addActionListener(e -> applyTimeOption());
        chTimeAnimation.addActionListener(e -> applyTimeOption());
        timeDuration.addChangeListener(e -> applyTimeOption());
        chTimeEnabled.addActionListener(e -> timePicker.setEnabled(chTimeEnabled.isSelected()));
        return panel;
    }

    // a form group that is wider than its content needs: the values take the width that is left
    private JPanel createOptionGroup(String title) {
        JPanel panel = DemoUtils.createFormGroup(title);
        MigLayout layout = (MigLayout) panel.getLayout();
        layout.setLayoutConstraints("wrap 2,fillx");
        layout.setColumnConstraints("[][grow,fill,75::]");
        return panel;
    }

    private JComboBox<PickerSize> createSize() {
        JComboBox<PickerSize> comboBox = new JComboBox<>(PickerSize.values());
        comboBox.setSelectedItem(PickerSize.DEFAULT);
        return comboBox;
    }

    private Component createDatePicker() {
        JPanel panel = DemoUtils.createGroup("Date picker", "wrap,fillx,insets 5 10 10 10");
        JLabel label = new JLabel("No date selected");
        JLabel labelPopup = new JLabel("No date selected");
        datePicker.addDateSelectionListener(e -> label.setText(toText(e)));
        datePopup.addDateSelectionListener(e -> labelPopup.setText(toText(e)));
        JButton buttonPopup = new JButton("Show popup");
        buttonPopup.addActionListener(e -> datePopup.showPopup(buttonPopup));
        panel.add(datePicker, "al center");
        panel.add(new JSeparator(), "growx,gapy 5 5");
        // what is selected, and below it the links
        panel.add(label, VALUE_LABEL);
        panel.add(DemoUtils.createLink("Today", datePicker::selectToday), "split 3");
        panel.add(DemoUtils.createLink("This week", () -> selectWeek(datePicker)));
        panel.add(DemoUtils.createLink("Clear", datePicker::clearSelection));
        panel.add(new JSeparator(), "growx,gapy 5 5");
        // the label is below the button: next to it, it has no space in a small picker
        panel.add(buttonPopup);
        panel.add(labelPopup, VALUE_LABEL);
        return panel;
    }

    private Component createTimePicker() {
        JPanel panel = DemoUtils.createGroup("Time picker", "wrap,fillx,insets 5 10 10 10");
        JLabel label = new JLabel("No time selected");
        JLabel labelPopup = new JLabel("No time selected");
        timePicker.addTimeSelectionListener(e -> label.setText(toText(e.getTime())));
        timePopup.addTimeSelectionListener(e -> labelPopup.setText(toText(e.getTime())));
        JButton buttonPopup = new JButton("Show popup");
        buttonPopup.addActionListener(e -> timePopup.showPopup(buttonPopup));
        panel.add(timePicker, "al center");
        panel.add(new JSeparator(), "growx,gapy 5 5");
        panel.add(label, VALUE_LABEL);
        panel.add(DemoUtils.createLink("Now", timePicker::selectNow), "split 2");
        panel.add(DemoUtils.createLink("Clear", timePicker::clearSelection));
        panel.add(new JSeparator(), "growx,gapy 5 5");
        // the label is below the button: next to it, it has no space in a small picker
        panel.add(buttonPopup);
        panel.add(labelPopup, VALUE_LABEL);
        return panel;
    }

    private void applyDateOption() {
        DateOption option = DatePicker.createOption()
                .setSelectionMode(selectionMode.getValue())
                .setSize((PickerSize) dateSize.getSelectedItem())
                .setFirstDayOfWeek((DayOfWeek) firstDayOfWeek.getSelectedItem())
                .setLocale(locales.get((String) locale.getSelectedItem()))
                .setCloseOnSelect(chDateCloseOnSelect.isSelected());
        option.getAnimationOption()
                .setEnabled(chDateAnimation.isSelected())
                .setDuration(DemoUtils.intValue(dateDuration));
        datePicker.setOption(option);
        datePopup.setOption(option);
    }

    private void applyTimeOption() {
        TimeOption option = TimePicker.createOption()
                .setHour24(chHour24.isSelected())
                .setSize((PickerSize) timeSize.getSelectedItem())
                .setCloseOnSelect(chTimeCloseOnSelect.isSelected());
        option.getAnimationOption()
                .setEnabled(chTimeAnimation.isSelected())
                .setDuration(DemoUtils.intValue(timeDuration));
        timePicker.setOption(option);
        timePopup.setOption(option);
    }

    // the week of today, or its first day if the picker selects one date
    private void selectWeek(DatePicker picker) {
        DayOfWeek firstDay = picker.getOption().getFirstDayOfWeek();
        LocalDate today = LocalDate.now();
        LocalDate first = today.minusDays((today.getDayOfWeek().getValue() - firstDay.getValue() + 7) % 7);
        if (picker.getOption().getSelectionMode() == DateSelectionMode.RANGE) {
            picker.setSelectedDateRange(first, first.plusDays(6));
        } else {
            picker.setSelectedDate(first);
        }
    }

    private String toText(DateSelectionEvent event) {
        DateRange range = event.getDateRange();
        if (range != null) {
            return DATE_FORMAT.format(range.getFrom()) + " - " + DATE_FORMAT.format(range.getTo());
        }
        return event.getDate() != null ? DATE_FORMAT.format(event.getDate()) : "No date selected";
    }

    private String toText(LocalTime time) {
        if (time == null) {
            return "No time selected";
        }
        return DateTimeFormatter.ofPattern(chHour24.isSelected() ? "HH:mm" : "hh:mm a").format(time);
    }
}
