package com.swingcraft4j.demo;

import com.swingcraft4j.datetime.DateField;
import com.swingcraft4j.datetime.DatePicker;
import com.swingcraft4j.datetime.DateRange;
import com.swingcraft4j.datetime.DateRangeField;
import com.swingcraft4j.datetime.DateSelectionEvent;
import com.swingcraft4j.datetime.DateTimeField;
import com.swingcraft4j.datetime.TimeField;
import com.swingcraft4j.datetime.TimePicker;
import com.swingcraft4j.datetime.ValidationResult;
import com.swingcraft4j.datetime.option.CommitMode;
import com.swingcraft4j.datetime.option.DateOption;
import com.swingcraft4j.datetime.option.DateSelectionMode;
import com.swingcraft4j.datetime.option.FieldOption;
import com.swingcraft4j.datetime.option.PickerSize;
import com.swingcraft4j.datetime.option.TimeOption;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

/**
 * The demo of the date picker and the time picker: each one in the window and in a popup, with its options.
 * Next to them the fields to type a date, a time and a range of dates, they show the same pickers in
 * their popup.
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
    private final DateField dateField = new DateField();
    private final TimeField timeField = new TimeField();
    private final DateTimeField dateTimeField = new DateTimeField();
    private final DateRangeField dateRangeField = new DateRangeField();

    private OptionGroup<DateSelectionMode> selectionMode;
    private JComboBox<PickerSize> dateSize;
    private JComboBox<DayOfWeek> firstDayOfWeek;
    private JComboBox<String> locale;
    private final Map<String, Locale> locales = new LinkedHashMap<>();
    private JCheckBox chDateCloseOnSelect;
    private JCheckBox chDateAnimation;
    private JSpinner dateDuration;
    private JCheckBox chDateEnabled;
    private JComboBox<String> dateValidation;
    private final Map<String, Predicate<LocalDate>> dateRules = new LinkedHashMap<>();

    private JComboBox<PickerSize> timeSize;
    private JCheckBox chHour24;
    private JCheckBox chTimeCloseOnSelect;
    private JCheckBox chTimeAnimation;
    private JSpinner timeDuration;
    private JCheckBox chTimeEnabled;
    private JComboBox<String> timeValidation;
    private final Map<String, Predicate<LocalTime>> timeRules = new LinkedHashMap<>();

    private JComboBox<String> datePattern;
    private JComboBox<String> timePattern;
    private JCheckBox chPickerButton;
    private JCheckBox chClearButton;
    private JCheckBox chFieldEnabled;
    private JCheckBox chValidation;
    private JComboBox<CommitMode> commitMode;

    public DateTimeDemo() {
        // the options of both pickers one below the other at the left, the two pickers next to them, and
        // the fields at the right. A picker has its own size, so its group is as large as the selected
        // size needs
        setLayout(new MigLayout("", "[fill," + OPTION_WIDTH + "::][fill][fill][fill," + OPTION_WIDTH + "::]", "[top][top]"));
        add(createDateOption(), "cell 0 0");
        add(createTimeOption(), "cell 0 1");
        add(createDatePicker(), "cell 1 0,spany 2");
        add(createTimePicker(), "cell 2 0,spany 2");
        // the fields and their options are one below the other, whatever the height of the groups at the left is
        JPanel fields = new JPanel(new MigLayout("wrap,fillx,insets 0", "[fill]"));
        fields.add(createFieldOption());
        fields.add(createFields());
        add(fields, "cell 3 0,spany 2");
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
        // which dates can be selected in the picker, null for all
        dateRules.put("None", null);
        dateRules.put("No weekend", date -> !isWeekend(date));
        dateRules.put("No past date", date -> !date.isBefore(LocalDate.now()));
        dateRules.put("Next 30 days", date -> !date.isBefore(LocalDate.now()) && date.isBefore(LocalDate.now().plusDays(30)));
        dateRules.put("Only Monday", date -> date.getDayOfWeek() == DayOfWeek.MONDAY);
        dateValidation = new JComboBox<>(dateRules.keySet().toArray(new String[0]));
        // the values first, then the check boxes together
        panel.add(panelMode, "span 2");
        DemoUtils.addRow(panel, "Size", dateSize);
        DemoUtils.addRow(panel, "First day", firstDayOfWeek);
        DemoUtils.addRow(panel, "Locale", locale);
        DemoUtils.addRow(panel, "Validation", dateValidation);
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
        dateValidation.addActionListener(e -> applyDateOption());
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
        // which times can be selected in the picker, null for all
        timeRules.put("None", null);
        timeRules.put("08:00 to 17:30", DateTimeDemo::isOfficeHours);
        timeRules.put("Not before now", time -> !time.isBefore(LocalTime.now().withSecond(0).withNano(0)));
        timeRules.put("Every 15 minutes", time -> time.getMinute() % 15 == 0);
        timeValidation = new JComboBox<>(timeRules.keySet().toArray(new String[0]));
        // the values first, then the check boxes together
        DemoUtils.addRow(panel, "Size", timeSize);
        DemoUtils.addRow(panel, "Validation", timeValidation);
        DemoUtils.addRow(panel, "Duration", timeDuration);
        panel.add(chHour24, "span 2");
        panel.add(chTimeAnimation, "span 2");
        panel.add(chTimeCloseOnSelect, "span 2");
        panel.add(chTimeEnabled, "span 2");
        timeSize.addActionListener(e -> applyTimeOption());
        chHour24.addActionListener(e -> applyTimeOption());
        chTimeCloseOnSelect.addActionListener(e -> applyTimeOption());
        timeValidation.addActionListener(e -> applyTimeOption());
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

    private Component createFieldOption() {
        JPanel panel = createOptionGroup("Field option");
        datePattern = new JComboBox<>(new String[]{"dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd", "dd MMM yy", "EEEE dd MMMM yyyy"});
        timePattern = new JComboBox<>(new String[]{"hh:mm a", "HH:mm", "HH:mm:ss"});
        chPickerButton = new JCheckBox("Picker button", true);
        chClearButton = new JCheckBox("Clear button");
        chFieldEnabled = new JCheckBox("Enabled", true);
        chValidation = new JCheckBox("Validation");
        commitMode = new JComboBox<>(CommitMode.values());
        DemoUtils.addRow(panel, "Date", datePattern);
        DemoUtils.addRow(panel, "Time", timePattern);
        DemoUtils.addRow(panel, "Commit", commitMode);
        panel.add(chPickerButton, "span 2");
        panel.add(chClearButton, "span 2");
        panel.add(chValidation, "span 2");
        panel.add(chFieldEnabled, "span 2");
        commitMode.addActionListener(e -> applyFieldOption());
        chValidation.addActionListener(e -> applyValidation());
        datePattern.addActionListener(e -> applyFieldOption());
        timePattern.addActionListener(e -> applyFieldOption());
        chPickerButton.addActionListener(e -> applyFieldOption());
        chClearButton.addActionListener(e -> applyFieldOption());
        chFieldEnabled.addActionListener(e -> {
            dateField.setEnabled(chFieldEnabled.isSelected());
            timeField.setEnabled(chFieldEnabled.isSelected());
            dateTimeField.setEnabled(chFieldEnabled.isSelected());
            dateRangeField.setEnabled(chFieldEnabled.isSelected());
        });
        return panel;
    }

    // a weekend and a time outside of the office hours are errors, a day in the past is a warning,
    // and all other values are good
    private void applyValidation() {
        if (!chValidation.isSelected()) {
            dateField.setValidator(null);
            timeField.setValidator(null);
            dateTimeField.setValidator(null);
            dateRangeField.setValidator(null);
            return;
        }
        dateField.setValidator(date -> {
            if (date == null) {
                return null;
            }
            if (isWeekend(date)) {
                return ValidationResult.error("The weekend can not be selected.");
            }
            if (date.isBefore(LocalDate.now())) {
                return ValidationResult.warning("The date is in the past.");
            }
            return ValidationResult.success("The date can be used.");
        });
        timeField.setValidator(time -> {
            if (time == null) {
                return null;
            }
            return isOfficeHours(time)
                    ? ValidationResult.success("The time can be used.")
                    : ValidationResult.error("The time is not from 08:00 to 17:30.");
        });
        dateTimeField.setValidator(dateTime -> {
            if (dateTime == null) {
                return null;
            }
            return dateTime.isBefore(LocalDateTime.now())
                    ? ValidationResult.error("The date and the time are in the past.")
                    : ValidationResult.success("The date and the time can be used.");
        });

        dateRangeField.setValidator(range -> {
            if (range == null) {
                return null;
            }
            return ChronoUnit.DAYS.between(range.getFrom(), range.getTo()) >= 14
                    ? ValidationResult.error("The range is longer than 14 days.")
                    : ValidationResult.success("The range can be used.");
        });
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private static boolean isOfficeHours(LocalTime time) {
        return !time.isBefore(LocalTime.of(8, 0)) && !time.isAfter(LocalTime.of(17, 30));
    }

    // each field, and below it what it has as its value
    private Component createFields() {
        JPanel panel = DemoUtils.createGroup("Field", "wrap 2,fillx", "[][grow]", "");
        JLabel labelDate = new JLabel("No date");
        JLabel labelTime = new JLabel("No time");
        JLabel labelDateTime = new JLabel("No date and time");
        JLabel labelDateRange = new JLabel("No date range");
        dateField.addChangeListener(e -> labelDate.setText(
                dateField.getSelectedDate() != null ? DATE_FORMAT.format(dateField.getSelectedDate()) : "No date"));
        timeField.addChangeListener(e -> labelTime.setText(
                timeField.getSelectedTime() != null ? timeField.getSelectedTime().toString() : "No time"));
        dateTimeField.addChangeListener(e -> labelDateTime.setText(
                dateTimeField.getSelectedDateTime() != null ? dateTimeField.getSelectedDateTime().toString() : "No date and time"));
        dateRangeField.addChangeListener(e -> {
            DateRange range = dateRangeField.getSelectedDateRange();
            labelDateRange.setText(range != null
                    ? DATE_FORMAT.format(range.getFrom()) + " - " + DATE_FORMAT.format(range.getTo()) : "No date range");
        });
        addField(panel, "Date", dateField, labelDate);
        addField(panel, "Time", timeField, labelTime);
        addField(panel, "Date time", dateTimeField, labelDateTime);
        addField(panel, "Date range", dateRangeField, labelDateRange);
        return panel;
    }

    private void addField(JPanel panel, String text, Component field, JLabel value) {
        value.setEnabled(false);
        panel.add(new JLabel(text));
        panel.add(field);
        // below the field
        panel.add(value, "skip 1," + VALUE_LABEL);
    }

    private JComboBox<PickerSize> createSize() {
        JComboBox<PickerSize> comboBox = new JComboBox<>(PickerSize.values());
        comboBox.setSelectedItem(PickerSize.DEFAULT);
        return comboBox;
    }

    private Component createDatePicker() {
        JPanel panel = DemoUtils.createGroup("Date picker", "wrap,fillx");
        JLabel label = new JLabel("No date selected");
        JLabel labelPopup = new JLabel("No date selected");
        datePicker.addDateSelectionListener(e -> label.setText(toText(e)));
        datePopup.addDateSelectionListener(e -> labelPopup.setText(toText(e)));
        JButton buttonPopup = new JButton("Show popup");
        buttonPopup.addActionListener(e -> datePopup.showPopup(buttonPopup));
        panel.add(datePicker, "al center");
        panel.add(new JSeparator(), "growx");
        // what is selected, and below it the links
        panel.add(label, VALUE_LABEL);
        panel.add(DemoUtils.createLink("Today", datePicker::selectToday), "split 3");
        panel.add(DemoUtils.createLink("This week", () -> selectWeek(datePicker)));
        panel.add(DemoUtils.createLink("Clear", datePicker::clearSelection));
        panel.add(new JSeparator(), "growx");
        // the label is below the button: next to it, it has no space in a small picker
        panel.add(buttonPopup);
        panel.add(labelPopup, VALUE_LABEL);
        return panel;
    }

    private Component createTimePicker() {
        JPanel panel = DemoUtils.createGroup("Time picker", "wrap,fillx");
        JLabel label = new JLabel("No time selected");
        JLabel labelPopup = new JLabel("No time selected");
        timePicker.addTimeSelectionListener(e -> label.setText(toText(e.getTime())));
        timePopup.addTimeSelectionListener(e -> labelPopup.setText(toText(e.getTime())));
        JButton buttonPopup = new JButton("Show popup");
        buttonPopup.addActionListener(e -> timePopup.showPopup(buttonPopup));
        panel.add(timePicker, "al center");
        panel.add(new JSeparator(), "growx");
        panel.add(label, VALUE_LABEL);
        panel.add(DemoUtils.createLink("Now", timePicker::selectNow), "split 2");
        panel.add(DemoUtils.createLink("Clear", timePicker::clearSelection));
        panel.add(new JSeparator(), "growx");
        // the label is below the button: next to it, it has no space in a small picker
        panel.add(buttonPopup);
        panel.add(labelPopup, VALUE_LABEL);
        return panel;
    }

    private void applyDateOption() {
        DateOption option = readDateOption();
        datePicker.setOption(option);
        datePopup.setOption(option);
        applyFieldOption();
    }

    private DateOption readDateOption() {
        DateOption option = DatePicker.createOption()
                .setSelectionMode(selectionMode.getValue())
                .setSize((PickerSize) dateSize.getSelectedItem())
                .setFirstDayOfWeek((DayOfWeek) firstDayOfWeek.getSelectedItem())
                .setLocale(locales.get((String) locale.getSelectedItem()))
                .setCloseOnSelect(chDateCloseOnSelect.isSelected())
                .setSelectable(dateRules.get((String) dateValidation.getSelectedItem()));
        option.getAnimationOption()
                .setEnabled(chDateAnimation.isSelected())
                .setDuration(DemoUtils.intValue(dateDuration));
        return option;
    }

    private void applyTimeOption() {
        TimeOption option = readTimeOption();
        timePicker.setOption(option);
        timePopup.setOption(option);
        applyFieldOption();
    }

    private TimeOption readTimeOption() {
        TimeOption option = TimePicker.createOption()
                .setHour24(chHour24.isSelected())
                .setSize((PickerSize) timeSize.getSelectedItem())
                .setCloseOnSelect(chTimeCloseOnSelect.isSelected())
                .setSelectable(timeRules.get((String) timeValidation.getSelectedItem()));
        option.getAnimationOption()
                .setEnabled(chTimeAnimation.isSelected())
                .setDuration(DemoUtils.intValue(timeDuration));
        return option;
    }

    // the popups of the fields have the pickers with the options above, they close when a value is selected
    private void applyFieldOption() {
        String date = (String) datePattern.getSelectedItem();
        String time = (String) timePattern.getSelectedItem();
        FieldOption option = new FieldOption()
                .setLocale(locales.get((String) locale.getSelectedItem()))
                .setShowPickerButton(chPickerButton.isSelected())
                .setShowClearButton(chClearButton.isSelected())
                .setCommitMode((CommitMode) commitMode.getSelectedItem())
                .setDateOption(readDateOption().setCloseOnSelect(true))
                .setTimeOption(readTimeOption().setCloseOnSelect(true));
        dateField.setOption(option.setPattern(date));
        timeField.setOption(option.setPattern(time));
        dateTimeField.setOption(option.setPattern(date + " " + time));
        dateRangeField.setOption(option.setPattern(date));
        revalidate();
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
