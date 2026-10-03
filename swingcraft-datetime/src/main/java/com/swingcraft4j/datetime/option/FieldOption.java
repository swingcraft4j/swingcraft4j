package com.swingcraft4j.datetime.option;

import java.util.Locale;

/**
 * The options of a field to type a date or a time: its pattern, its buttons, and the options of the pickers
 * in its popup. The setters return the option, so they can be chained.
 * <p>
 * The option is copied when it is given to a field, so changing it afterwards
 * does not affect the field.
 */
public class FieldOption {

    private String pattern;
    private String rangeSeparator = " - ";
    private Locale locale;
    private boolean showPickerButton = true;
    private boolean showClearButton;
    private CommitMode commitMode = CommitMode.KEEP;
    // the popup of a field closes when the date or the time is selected
    private DateOption dateOption = new DateOption().setCloseOnSelect(true);
    private TimeOption timeOption = new TimeOption().setCloseOnSelect(true);

    /**
     * @return the pattern, or null for the default pattern of the field
     */
    public String getPattern() {
        return pattern;
    }

    public String getRangeSeparator() {
        return rangeSeparator;
    }

    /**
     * @return the locale of the names of the months and the days and of AM and PM, the default locale if
     * none is set
     */
    public Locale getLocale() {
        return locale != null ? locale : Locale.getDefault();
    }

    public boolean isShowPickerButton() {
        return showPickerButton;
    }

    public boolean isShowClearButton() {
        return showClearButton;
    }

    public CommitMode getCommitMode() {
        return commitMode;
    }

    public DateOption getDateOption() {
        return dateOption;
    }

    public TimeOption getTimeOption() {
        return timeOption;
    }

    /**
     * Sets what the field shows and what is typed, with the letters of
     * {@link java.time.format.DateTimeFormatter}:
     * <ul>
     * <li>{@code d} the day, {@code M} the month, {@code y} the year. {@code MMM} is the short name of the
     * month and {@code MMMM} its name, {@code yy} a year with two digits</li>
     * <li>{@code H} the hour from 0 to 23, or {@code h} the hour from 1 to 12 with {@code a} for AM and PM</li>
     * <li>{@code m} the minute, {@code s} the second</li>
     * <li>{@code E} the short name of the day and {@code EEEE} its name. It is shown, not typed</li>
     * </ul>
     * All other characters are shown as they are, letters between two {@code '}. An hour with {@code H}
     * gives the popup a clock with 24 hours.
     *
     * @param pattern the pattern, as {@code "dd/MM/yyyy"}. Null for the default pattern of the field
     */
    public FieldOption setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * @param rangeSeparator the text between the two dates of a field for a range of dates,
     *                       {@code " - "} by default
     */
    public FieldOption setRangeSeparator(String rangeSeparator) {
        if (rangeSeparator == null || rangeSeparator.isEmpty()) {
            throw new IllegalArgumentException("range separator must not empty");
        }
        this.rangeSeparator = rangeSeparator;
        return this;
    }

    /**
     * @param locale the locale of the names of the months and the days and of AM and PM, in the field and
     *               in the popup. Null to use the default locale
     */
    public FieldOption setLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * @param showPickerButton false to hide the button that opens the popup (default true).
     *                         The popup can still be opened with the keyboard and from the code
     */
    public FieldOption setShowPickerButton(boolean showPickerButton) {
        this.showPickerButton = showPickerButton;
        return this;
    }

    /**
     * @param showClearButton true to show a button that removes what is typed, while the field is not empty
     */
    public FieldOption setShowClearButton(boolean showClearButton) {
        this.showClearButton = showClearButton;
        return this;
    }

    /**
     * @param commitMode what the field does with what is typed when it loses the focus and has no value:
     *                   it stays ({@link CommitMode#KEEP}, default), the field goes back to its last value
     *                   ({@link CommitMode#REVERT}) or is cleared ({@link CommitMode#CLEAR})
     */
    public FieldOption setCommitMode(CommitMode commitMode) {
        if (commitMode == null) {
            throw new IllegalArgumentException("commit mode must not null");
        }
        this.commitMode = commitMode;
        return this;
    }

    /**
     * @param dateOption the option of the date picker in the popup, change the current one with
     *                   {@link #getDateOption()}. The field always selects one date, and uses its own locale
     */
    public FieldOption setDateOption(DateOption dateOption) {
        if (dateOption == null) {
            throw new IllegalArgumentException("date option must not null");
        }
        this.dateOption = dateOption;
        return this;
    }

    /**
     * @param timeOption the option of the time picker in the popup, change the current one with
     *                   {@link #getTimeOption()}. The clock has 12 or 24 hours as the pattern of the field has
     */
    public FieldOption setTimeOption(TimeOption timeOption) {
        if (timeOption == null) {
            throw new IllegalArgumentException("time option must not null");
        }
        this.timeOption = timeOption;
        return this;
    }

    public FieldOption copy() {
        FieldOption option = new FieldOption();
        option.pattern = pattern;
        option.rangeSeparator = rangeSeparator;
        option.locale = locale;
        option.showPickerButton = showPickerButton;
        option.showClearButton = showClearButton;
        option.commitMode = commitMode;
        option.dateOption = dateOption.copy();
        option.timeOption = timeOption.copy();
        return option;
    }
}
