package com.swingcraft4j.datetime.internal;

import com.formdev.flatlaf.util.UIScale;
import com.swingcraft4j.datetime.option.TimeOption;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;

/**
 * The view of a time picker: a header with the hour and the minute, and below it the clock. The clock shows
 * the hours first, and the minutes when an hour is selected. The header selects which of them is shown,
 * and AM or PM for a clock with 12 hours.
 */
public final class TimePanel extends JPanel {

    public interface Listener {

        /**
         * The user has changed the time.
         */
        void timeChanged();

        /**
         * The user has selected the minute, the time is complete.
         */
        void timeSelected();
    }

    private final Listener listener;
    private final ClockPanel clock;
    private TimeOption option;
    // null if no time is selected
    private LocalTime time;
    private boolean hourView = true;
    // AM or PM of a clock with 12 hours while no time is selected
    private boolean pm;

    // the space between the header and the clock with the medium size
    private static final int HEADER_GAP = 9;
    // what is added to the size of the font for the hour and the minute
    private static final float TIME_FONT_SIZE = 16;

    private PickerButton buttonHour;
    private PickerButton buttonMinute;
    private PickerButton buttonAm;
    private PickerButton buttonPm;
    private JLabel separator;
    private JPanel panelAmPm;

    public TimePanel(TimeOption option, Listener listener) {
        this.option = option;
        this.listener = listener;
        clock = new ClockPanel(new ClockPanel.Listener() {
            @Override
            public void valueChanged(int value) {
                clockChanged(value);
            }

            @Override
            public void valueSelected() {
                clockSelected();
            }
        });
        setOpaque(false);
        setLayout(new MigLayout("wrap,fill,insets 0", "[center]", "[grow 0][fill]"));
        add(createHeader());
        add(clock);
        applySize();
        update(false);
    }

    private Component createHeader() {
        JPanel panel = new JPanel(new MigLayout("insets 0,gapx 4,hidemode 3", "[][][]8[]", "[fill]"));
        panel.setOpaque(false);
        // the hour and the minute look like two fields, the one that is shown on the clock has the accent color
        buttonHour = new PickerButton().font(TIME_FONT_SIZE, false).padding(10, 4).filled().minimumText("00");
        buttonMinute = new PickerButton().font(TIME_FONT_SIZE, false).padding(10, 4).filled().minimumText("00");
        buttonAm = new PickerButton().font(-1, false).padding(7, 2).filled();
        buttonPm = new PickerButton().font(-1, false).padding(7, 2).filled();
        buttonAm.setText("AM");
        buttonPm.setText("PM");
        buttonHour.addActionListener(e -> showView(true));
        buttonMinute.addActionListener(e -> showView(false));
        buttonAm.addActionListener(e -> setPm(false));
        buttonPm.addActionListener(e -> setPm(true));
        separator = new JLabel(":");
        // AM over PM, the two have the same height and are together as high as the hour
        panelAmPm = new JPanel(new GridLayout(2, 1, 0, UIScale.scale(3)));
        panelAmPm.setOpaque(false);
        panelAmPm.add(buttonAm);
        panelAmPm.add(buttonPm);
        panel.add(buttonHour);
        panel.add(separator);
        panel.add(buttonMinute);
        panel.add(panelAmPm);
        return panel;
    }

    // the size of the picker: the text of the header and the clock
    private void applySize() {
        // the space between the header and the clock is smaller in a smaller picker
        ((MigLayout) getLayout()).setLayoutConstraints(
                "wrap,fill,insets 0,gapy " + Math.round(HEADER_GAP * option.getSize().getScale()));
        buttonHour.setPickerSize(option.getSize());
        buttonMinute.setPickerSize(option.getSize());
        buttonAm.setPickerSize(option.getSize());
        buttonPm.setPickerSize(option.getSize());
        separator.setFont(PickerUtils.font(option.getSize(), TIME_FONT_SIZE));
        clock.setPickerSize(option.getSize());
    }

    /**
     * The option has changed: the clock is shown again with it, at the hours.
     */
    public void setOption(TimeOption option) {
        this.option = option;
        hourView = true;
        applySize();
        update(false);
        revalidate();
    }

    public LocalTime getTime() {
        return time;
    }

    /**
     * @param time    the time, or null for none
     * @param animate true to turn the hand to the time, if the animation is on
     */
    public void setTime(LocalTime time, boolean animate) {
        this.time = time;
        if (time != null) {
            pm = time.getHour() >= 12;
        }
        update(animate);
    }

    /**
     * Shows the hours, as when the picker opens.
     */
    public void showHours() {
        hourView = true;
        update(false);
    }

    private void showView(boolean hourView) {
        if (this.hourView != hourView) {
            this.hourView = hourView;
            update(true);
        } else {
            updateHeader();
        }
    }

    private void setPm(boolean pm) {
        this.pm = pm;
        if (time != null && pm != time.getHour() >= 12) {
            time = time.withHour((time.getHour() + 12) % 24);
            update(false);
            listener.timeChanged();
        } else {
            update(false);
        }
    }

    // the user has moved the hand of the clock
    private void clockChanged(int value) {
        if (hourView) {
            time = time != null ? time.withHour(value) : LocalTime.of(value, 0);
        } else {
            time = time != null ? time.withMinute(value) : LocalTime.of(pm ? 12 : 0, value);
        }
        pm = time.getHour() >= 12;
        updateHeader();
        listener.timeChanged();
    }

    // the user has released the hand of the clock
    private void clockSelected() {
        if (time == null) {
            return;
        }
        if (hourView) {
            // the minutes are next
            hourView = false;
            update(true);
        } else {
            listener.timeSelected();
        }
    }

    private void update(boolean animate) {
        updateHeader();
        int value = time == null ? -1 : (hourView ? time.getHour() : time.getMinute());
        int duration = animate && isShowing() ? PickerUtils.duration(option.getAnimationOption()) : 0;
        clock.show(hourView, option.isHour24(), pm, value, duration);
    }

    private void updateHeader() {
        String hour = "--";
        String minute = "--";
        if (time != null) {
            int value = time.getHour();
            if (!option.isHour24()) {
                value = value % 12 == 0 ? 12 : value % 12;
            }
            hour = String.format("%02d", value);
            minute = String.format("%02d", time.getMinute());
        }
        buttonHour.setText(hour);
        buttonMinute.setText(minute);
        buttonHour.setSelected(hourView);
        buttonMinute.setSelected(!hourView);
        buttonAm.setSelected(!pm);
        buttonPm.setSelected(pm);
        panelAmPm.setVisible(!option.isHour24());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        buttonHour.setEnabled(enabled);
        buttonMinute.setEnabled(enabled);
        buttonAm.setEnabled(enabled);
        buttonPm.setEnabled(enabled);
        clock.setEnabled(enabled);
        repaint();
    }
}
