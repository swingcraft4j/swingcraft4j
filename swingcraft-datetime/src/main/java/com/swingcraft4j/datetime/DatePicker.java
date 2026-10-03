package com.swingcraft4j.datetime;

import com.swingcraft4j.datetime.internal.CalendarPanel;
import com.swingcraft4j.datetime.internal.DateSelection;
import com.swingcraft4j.datetime.internal.PickerPopup;
import com.swingcraft4j.datetime.option.DateOption;
import com.swingcraft4j.datetime.option.DateSelectionMode;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

/**
 * A calendar to select a date, or a range of dates with {@link DateSelectionMode#RANGE}.
 * Add it to a container as any component, or show it in a popup with {@link #showPopup(Component)}.
 * All methods must be called on the event dispatch thread.
 */
public class DatePicker extends JPanel {

    private static DateOption defaultOption = new DateOption();

    private final DateSelection selection = new DateSelection();
    private final PickerPopup popup = new PickerPopup(this);
    private final CalendarPanel calendar;
    private DateOption option;
    // what the listeners were told last: a date, a range or null
    private Object firedValue;

    public DatePicker() {
        this(defaultOption);
    }

    /**
     * @param option the option, it is copied
     */
    public DatePicker(DateOption option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not null");
        }
        this.option = option.copy();
        selection.setMode(this.option.getSelectionMode());
        calendar = new CalendarPanel(selection, this.option, date -> userSelected());
        setLayout(new MigLayout("fill,insets 10", "[fill]", "[fill]"));
        add(calendar);
    }

    /**
     * @return a copy of the option of this picker
     */
    public DateOption getOption() {
        return option.copy();
    }

    /**
     * Changes the option. The selection is cleared if the selection mode is another one.
     *
     * @param option the option, it is copied
     */
    public void setOption(DateOption option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not null");
        }
        this.option = option.copy();
        selection.setMode(this.option.getSelectionMode());
        calendar.setOption(this.option);
        fireSelectionChanged();
    }

    /**
     * @return the selected date, the first date of a selected range, or null if nothing is selected
     */
    public LocalDate getSelectedDate() {
        return selection.getDate();
    }

    /**
     * Selects the date and shows its month. With {@link DateSelectionMode#RANGE} the range is that one date.
     *
     * @param date the date, or null to clear the selection
     */
    public void setSelectedDate(LocalDate date) {
        if (date == null) {
            clearSelection();
            return;
        }
        selection.setDate(date);
        selected(date);
    }

    /**
     * @return the selected range, or null if no range is selected or the picker selects one date
     */
    public DateRange getSelectedDateRange() {
        return selection.getRange();
    }

    /**
     * Selects the range and shows the month of its first date.
     *
     * @param range the range, or null to clear the selection
     * @throws IllegalStateException if the picker selects one date
     */
    public void setSelectedDateRange(DateRange range) {
        if (option.getSelectionMode() != DateSelectionMode.RANGE) {
            throw new IllegalStateException("the picker does not select a range");
        }
        if (range == null) {
            clearSelection();
            return;
        }
        selection.setRange(range);
        selected(range.getFrom());
    }

    /**
     * @see #setSelectedDateRange(DateRange)
     */
    public void setSelectedDateRange(LocalDate from, LocalDate to) {
        setSelectedDateRange(new DateRange(from, to));
    }

    /**
     * Selects the date of today.
     */
    public void selectToday() {
        setSelectedDate(LocalDate.now());
    }

    public void clearSelection() {
        selection.clear();
        calendar.selectionChanged();
        fireSelectionChanged();
    }

    /**
     * @return true if a date, or a range with its end, is selected
     */
    public boolean isDateSelected() {
        return selection.isSelected();
    }

    /**
     * Shows the days of the month of the date, the selection does not change.
     */
    public void showDate(LocalDate date) {
        calendar.showMonth(YearMonth.from(date), isShowing());
    }

    public void addDateSelectionListener(DateSelectionListener listener) {
        listenerList.add(DateSelectionListener.class, listener);
    }

    public void removeDateSelectionListener(DateSelectionListener listener) {
        listenerList.remove(DateSelectionListener.class, listener);
    }

    /**
     * Shows the picker in a popup below the component, or above it if there is no space below.
     * The popup closes when the user clicks outside of it. A picker is used in a popup or in a container,
     * not in both.
     *
     * @throws IllegalArgumentException if the component is not showing
     */
    public void showPopup(Component invoker) {
        // opens at the selected date, or at this month
        LocalDate date = selection.getAnchor();
        calendar.showMonth(YearMonth.from(date != null ? date : LocalDate.now()), false);
        popup.show(invoker);
    }

    public void closePopup() {
        popup.close();
    }

    public boolean isPopupVisible() {
        return popup.isVisible();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        calendar.setEnabled(enabled);
    }

    private void selected(LocalDate date) {
        calendar.selectionChanged();
        showDate(date);
        fireSelectionChanged();
    }

    // the user has clicked a date
    private void userSelected() {
        fireSelectionChanged();
        if (option.isCloseOnSelect() && selection.isSelected()) {
            closePopup();
        }
    }

    // tells the listeners, if the selected date or range is another one. A range without its end is
    // not a selection yet
    private void fireSelectionChanged() {
        if (selection.isSelecting()) {
            return;
        }
        DateRange range = selection.getRange();
        LocalDate date = selection.getDate();
        Object value = range != null ? range : date;
        if (Objects.equals(value, firedValue)) {
            return;
        }
        firedValue = value;
        DateSelectionEvent event = new DateSelectionEvent(this, date, range);
        for (DateSelectionListener listener : listenerList.getListeners(DateSelectionListener.class)) {
            listener.dateSelectionChanged(event);
        }
    }

    /**
     * @return the option used when a picker is created without option
     */
    public static DateOption getDefaultOption() {
        return defaultOption;
    }

    public static void setDefaultOption(DateOption option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not null");
        }
        defaultOption = option;
    }

    /**
     * @return a new option, copied from the default option
     */
    public static DateOption createOption() {
        return defaultOption.copy();
    }
}
