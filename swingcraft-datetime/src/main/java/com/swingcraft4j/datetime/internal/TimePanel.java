package com.swingcraft4j.datetime.internal;

import com.formdev.flatlaf.util.UIScale;
import com.swingcraft4j.datetime.option.TimeOption;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;

/**
 * The view of a time picker: a header with the hour and the minute, and below it the clock. The clock shows
 * the hours first, and the minutes when an hour is selected. The header selects which of them is shown,
 * and AM or PM for a clock with 12 hours.
 * <p>
 * With the focus, the keyboard changes the time: up and down make the hour or the minute larger and
 * smaller, left and right show the hours or the minutes, enter or space goes on from the hours to the
 * minutes, and A and P select AM and PM.
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
        installKeys();
    }

    private void installKeys() {
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isEnabled() && !e.isAltDown() && !e.isControlDown() && !e.isMetaDown() && handleKey(e.getKeyCode())) {
                    e.consume();
                }
            }
        });
        // a click on the clock gives the panel the focus, so the keyboard goes on from the time
        clock.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    // true if the key was used
    private boolean handleKey(int code) {
        switch (code) {
            case KeyEvent.VK_UP:
                adjust(1);
                return true;
            case KeyEvent.VK_DOWN:
                adjust(-1);
                return true;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
                showView(!hourView);
                return true;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                clockSelected();
                return true;
            case KeyEvent.VK_A:
            case KeyEvent.VK_P:
                if (!option.isHour24()) {
                    setPm(code == KeyEvent.VK_P);
                }
                return true;
            default:
                return false;
        }
    }

    // the next hour or minute that can be selected, in the direction of the amount. After the last comes
    // the first: the hours of a clock with 12 hours stay in their half of the day
    private void adjust(int amount) {
        LocalTime base = time != null ? time : LocalTime.of(pm ? 12 : 0, 0);
        int count = hourView ? (option.isHour24() ? 24 : 12) : 60;
        for (int step = 1; step <= count; step++) {
            LocalTime next;
            if (!hourView) {
                next = base.withMinute(Math.floorMod(base.getMinute() + amount * step, 60));
                next = option.isSelectable(next) ? next : null;
            } else if (option.isHour24()) {
                next = findSelectable(base.withHour(Math.floorMod(base.getHour() + amount * step, 24)));
            } else {
                int half = base.getHour() >= 12 ? 12 : 0;
                next = findSelectable(base.withHour(half + Math.floorMod(base.getHour() - half + amount * step, 12)));
            }
            if (next != null) {
                time = next;
                pm = time.getHour() >= 12;
                update(false);
                listener.timeChanged();
                return;
            }
        }
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

    // the size and the style of the picker: the text of the header, the clock and the colors
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
        Color accentColor = option.getStyleOption().getColor();
        buttonHour.setAccentColor(accentColor);
        buttonMinute.setAccentColor(accentColor);
        buttonAm.setAccentColor(accentColor);
        buttonPm.setAccentColor(accentColor);
        clock.setStyle(option.getStyleOption());
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
        if (time != null && pm != time.getHour() >= 12) {
            // the same time in the other half of the day, or the first minute of its hour that can be
            // selected. If the hour has none, AM and PM stay as they are
            LocalTime selected = findSelectable(time.withHour((time.getHour() + 12) % 24));
            if (selected == null) {
                update(false);
                return;
            }
            this.pm = pm;
            time = selected;
            update(false);
            listener.timeChanged();
        } else {
            this.pm = pm;
            update(false);
        }
    }

    // the time if it can be selected, or the first minute of its hour that can. Null if the hour has none
    private LocalTime findSelectable(LocalTime time) {
        if (option.isSelectable(time)) {
            return time;
        }
        for (int minute = 0; minute < 60; minute++) {
            LocalTime other = time.withMinute(minute);
            if (option.isSelectable(other)) {
                return other;
            }
        }
        return null;
    }

    // the user has moved the hand of the clock
    private void clockChanged(int value) {
        if (hourView) {
            // the minute stays if it can, or the hour gets its first minute that can be selected
            LocalTime selected = findSelectable(LocalTime.of(value, time != null ? time.getMinute() : 0));
            if (selected == null) {
                return;
            }
            time = selected;
        } else {
            time = time != null ? time.withMinute(value) : LocalTime.of(pm ? 12 : 0, value);
        }
        pm = time.getHour() >= 12;
        updateHeader();
        updateSelectable();
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
        updateSelectable();
        clock.show(hourView, option.isHour24(), pm, value, duration);
    }

    // an hour can be selected if one of its minutes can, a minute if the time with the hour can
    private void updateSelectable() {
        if (option.getSelectable() == null) {
            clock.setSelectable(null);
        } else if (hourView) {
            clock.setSelectable(hour -> {
                for (int minute = 0; minute < 60; minute++) {
                    if (option.isSelectable(LocalTime.of(hour, minute))) {
                        return true;
                    }
                }
                return false;
            });
        } else {
            int hour = time != null ? time.getHour() : (pm ? 12 : 0);
            clock.setSelectable(minute -> option.isSelectable(LocalTime.of(hour, minute)));
        }
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
