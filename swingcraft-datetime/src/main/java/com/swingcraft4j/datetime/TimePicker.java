package com.swingcraft4j.datetime;

import com.swingcraft4j.datetime.internal.PickerPopup;
import com.swingcraft4j.datetime.internal.TimePanel;
import com.swingcraft4j.datetime.option.StyleOption;
import com.swingcraft4j.datetime.option.TimeOption;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.util.Objects;

/**
 * A clock to select a time, the hour and the minute. The clock has 12 hours with AM and PM, or 24 hours with
 * {@link TimeOption#setHour24(boolean)}.
 * Add it to a container as any component, or show it in a popup with {@link #showPopup(Component)}.
 * All methods must be called on the event dispatch thread.
 */
public class TimePicker extends JPanel {

    private static TimeOption defaultOption = new TimeOption();

    private final PickerPopup popup = new PickerPopup(this);
    private final TimePanel timePanel;
    private TimeOption option;
    // what the listeners were told last
    private LocalTime firedTime;

    public TimePicker() {
        this(defaultOption);
    }

    /**
     * @param option the option, it is copied
     */
    public TimePicker(TimeOption option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not null");
        }
        this.option = option.copy();
        timePanel = new TimePanel(this.option, new TimePanel.Listener() {
            @Override
            public void timeChanged() {
                fireSelectionChanged();
            }

            @Override
            public void timeSelected() {
                if (TimePicker.this.option.isCloseOnSelect()) {
                    closePopup();
                }
            }
        });
        setLayout(new MigLayout("fill", "[fill]", "[fill]"));
        add(timePanel);
        applyStyle();
    }

    /**
     * @return a copy of the option of this picker
     */
    public TimeOption getOption() {
        return option.copy();
    }

    /**
     * Changes the option, the selected time stays.
     *
     * @param option the option, it is copied
     */
    public void setOption(TimeOption option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not null");
        }
        this.option = option.copy();
        timePanel.setOption(this.option);
        applyStyle();
    }

    /**
     * @return the selected time, or null if no time is selected
     */
    public LocalTime getSelectedTime() {
        return timePanel.getTime();
    }

    /**
     * @param time the time, its seconds are not used. Null to clear the selection
     */
    public void setSelectedTime(LocalTime time) {
        timePanel.setTime(time == null ? null : LocalTime.of(time.getHour(), time.getMinute()), true);
        fireSelectionChanged();
    }

    /**
     * Selects the time of now.
     */
    public void selectNow() {
        setSelectedTime(LocalTime.now());
    }

    public void clearSelection() {
        setSelectedTime(null);
    }

    public boolean isTimeSelected() {
        return timePanel.getTime() != null;
    }

    public void addTimeSelectionListener(TimeSelectionListener listener) {
        listenerList.add(TimeSelectionListener.class, listener);
    }

    public void removeTimeSelectionListener(TimeSelectionListener listener) {
        listenerList.remove(TimeSelectionListener.class, listener);
    }

    /**
     * Shows the picker in a popup below the component, or above it if there is no space below.
     * The popup closes when the user clicks outside of it. A picker is used in a popup or in a container,
     * not in both.
     *
     * @throws IllegalArgumentException if the component is not showing
     */
    public void showPopup(Component invoker) {
        timePanel.showHours();
        popup.show(invoker);
    }

    public void closePopup() {
        popup.close();
    }

    public boolean isPopupVisible() {
        return popup.isVisible();
    }

    // the background and the space around the content
    private void applyStyle() {
        StyleOption style = option.getStyleOption();
        Insets padding = style.getPadding();
        ((MigLayout) getLayout()).setLayoutConstraints(
                "fill,insets " + padding.top + " " + padding.left + " " + padding.bottom + " " + padding.right);
        // the color of the look and feel changes with the look and feel, as it did before
        setBackground(style.getBackground() != null ? style.getBackground() : UIManager.getColor("Panel.background"));
        revalidate();
        repaint();
    }

    /**
     * Gives the focus to the clock, so the keyboard changes its time.
     */
    @Override
    public boolean requestFocusInWindow() {
        return timePanel.requestFocusInWindow();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        timePanel.setEnabled(enabled);
    }

    // tells the listeners, if the selected time is another one
    private void fireSelectionChanged() {
        LocalTime time = timePanel.getTime();
        if (Objects.equals(time, firedTime)) {
            return;
        }
        firedTime = time;
        TimeSelectionEvent event = new TimeSelectionEvent(this, time);
        for (TimeSelectionListener listener : listenerList.getListeners(TimeSelectionListener.class)) {
            listener.timeSelectionChanged(event);
        }
    }

    /**
     * @return the option used when a picker is created without option
     */
    public static TimeOption getDefaultOption() {
        return defaultOption;
    }

    public static void setDefaultOption(TimeOption option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not null");
        }
        defaultOption = option;
    }

    /**
     * @return a new option, copied from the default option
     */
    public static TimeOption createOption() {
        return defaultOption.copy();
    }
}
