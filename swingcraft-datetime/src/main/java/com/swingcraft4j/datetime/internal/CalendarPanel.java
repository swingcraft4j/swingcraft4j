package com.swingcraft4j.datetime.internal;

import com.swingcraft4j.datetime.option.DateOption;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;

/**
 * The calendar of a date picker: a header to go to another month, and below it the days of the month,
 * the months of the year or the years. It shows the selection and tells the picker what was clicked.
 */
public final class CalendarPanel extends JPanel {

    public interface Listener {

        /**
         * The user has clicked the date, the selection has changed already.
         */
        void dateClicked(LocalDate date);
    }

    private enum View {
        DAY, MONTH, YEAR
    }

    // the space between the header and the days with the medium size
    private static final int HEADER_GAP = 4;

    private final DateSelection selection;
    private final Listener listener;
    private final SlidePanel slidePanel = new SlidePanel();
    private DateOption option;
    private View view = View.DAY;
    // the month that is shown, or that the months and the years are shown for
    private YearMonth month = YearMonth.now();
    // the first year of the page of years that is shown
    private int yearPage;

    private PickerButton buttonBack;
    private PickerButton buttonForward;
    private PickerButton buttonMonth;
    private PickerButton buttonYear;

    public CalendarPanel(DateSelection selection, DateOption option, Listener listener) {
        this.selection = selection;
        this.option = option;
        this.listener = listener;
        setOpaque(false);
        setLayout(new MigLayout("wrap,fill,insets 0", "[fill]", "[grow 0][fill]"));
        add(createHeader());
        add(slidePanel);
        applySize();
        show(View.DAY, null);
    }

    private Component createHeader() {
        // the month and the year are in one cell in the center. A button that is not visible takes no
        // space, so the years are in the center without the month
        JPanel panel = new JPanel(new MigLayout("fill,insets 0,hidemode 3", "[][grow,center][]", "fill"));
        panel.setOpaque(false);
        buttonBack = new PickerButton().padding(3, 3).circle();
        buttonForward = new PickerButton().padding(3, 3).circle();
        buttonMonth = new PickerButton().font(0, true).padding(7, 3);
        buttonYear = new PickerButton().font(0, true).padding(7, 3);
        buttonBack.setIcon(new PickerIcon(PickerIcon.Type.BACK));
        buttonForward.setIcon(new PickerIcon(PickerIcon.Type.FORWARD));
        buttonBack.addActionListener(e -> move(-1));
        buttonForward.addActionListener(e -> move(1));
        // the same button comes back to the days
        buttonMonth.addActionListener(e -> show(view == View.MONTH ? View.DAY : View.MONTH,
                view == View.DAY ? SlidePanel.Direction.DOWN : SlidePanel.Direction.UP));
        buttonYear.addActionListener(e -> show(view == View.YEAR ? View.DAY : View.YEAR,
                view == View.YEAR ? SlidePanel.Direction.UP : SlidePanel.Direction.DOWN));
        panel.add(buttonBack);
        panel.add(buttonMonth, "split 2,gapx 0 2");
        panel.add(buttonYear);
        panel.add(buttonForward);
        return panel;
    }

    // the size of the picker: the text of the header and the space the days take
    private void applySize() {
        // the space between the header and the days is smaller in a smaller picker
        ((MigLayout) getLayout()).setLayoutConstraints(
                "wrap,fill,insets 0,gapy " + Math.round(HEADER_GAP * option.getSize().getScale()));
        buttonBack.setPickerSize(option.getSize());
        buttonForward.setPickerSize(option.getSize());
        buttonMonth.setPickerSize(option.getSize());
        buttonYear.setPickerSize(option.getSize());
        slidePanel.setFixedSize(new DayGrid(this, month).getPreferredSize());
    }

    DateOption getOption() {
        return option;
    }

    /**
     * The option has changed: the calendar is shown again with it, at the days of its month.
     */
    public void setOption(DateOption option) {
        this.option = option;
        applySize();
        show(View.DAY, null);
        revalidate();
    }

    DateSelection getSelection() {
        return selection;
    }

    public YearMonth getMonth() {
        return month;
    }

    /**
     * Shows the days of the month.
     *
     * @param animate true to slide to the month, if the animation is on
     */
    public void showMonth(YearMonth month, boolean animate) {
        if (view == View.DAY && month.equals(this.month)) {
            return;
        }
        SlidePanel.Direction direction = null;
        if (animate) {
            if (view != View.DAY) {
                direction = SlidePanel.Direction.UP;
            } else {
                direction = month.isAfter(this.month) ? SlidePanel.Direction.FORWARD : SlidePanel.Direction.BACKWARD;
            }
        }
        this.month = month;
        show(View.DAY, direction);
    }

    /**
     * The selection has changed, the calendar is painted again.
     */
    public void selectionChanged() {
        repaint();
    }

    // to the month, the year or the page of years before or after
    private void move(int amount) {
        if (view == View.DAY) {
            month = month.plusMonths(amount);
        } else if (view == View.MONTH) {
            month = month.plusYears(amount);
        } else {
            yearPage += amount * YearGrid.YEARS;
        }
        show(view, amount > 0 ? SlidePanel.Direction.FORWARD : SlidePanel.Direction.BACKWARD);
    }

    /**
     * @param direction where the view comes from, null to show it without animation
     */
    private void show(View view, SlidePanel.Direction direction) {
        Component component;
        if (view == View.DAY) {
            component = new DayGrid(this, month);
        } else if (view == View.MONTH) {
            component = new MonthGrid(this, month.getYear());
        } else {
            // the page of the year of the month, unless the user moves through the pages
            YearGrid grid = new YearGrid(this, this.view == View.YEAR ? yearPage : month.getYear());
            yearPage = grid.getFirstYear();
            component = grid;
        }
        this.view = view;
        component.setEnabled(isEnabled());
        component.applyComponentOrientation(getComponentOrientation());
        updateHeader();
        slidePanel.show(component, direction, direction == null ? 0 : PickerUtils.duration(option.getAnimationOption()));
    }

    private void updateHeader() {
        buttonMonth.setText(month.getMonth().getDisplayName(TextStyle.FULL, option.getLocale()));
        buttonYear.setText(view == View.YEAR
                ? yearPage + " - " + (yearPage + YearGrid.YEARS - 1)
                : String.valueOf(month.getYear()));
        // the month does not change with the pages of years
        buttonMonth.setVisible(view != View.YEAR);
    }

    void dateClicked(LocalDate date) {
        selection.click(date);
        repaint();
        listener.dateClicked(date);
    }

    void monthClicked(YearMonth month) {
        this.month = month;
        show(View.DAY, SlidePanel.Direction.UP);
    }

    void yearClicked(int year) {
        month = month.withYear(year);
        show(View.MONTH, SlidePanel.Direction.UP);
    }

    /**
     * @return true if a selected date, or the start or the end of a range, is in the month
     */
    boolean isSelected(YearMonth month) {
        LocalDate start = selection.getAnchor();
        LocalDate end = selection.getEnd();
        return (start != null && YearMonth.from(start).equals(month)) || (end != null && YearMonth.from(end).equals(month));
    }

    boolean isSelected(int year) {
        LocalDate start = selection.getAnchor();
        LocalDate end = selection.getEnd();
        return (start != null && start.getYear() == year) || (end != null && end.getYear() == year);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        buttonBack.setEnabled(enabled);
        buttonForward.setEnabled(enabled);
        buttonMonth.setEnabled(enabled);
        buttonYear.setEnabled(enabled);
        Component current = slidePanel.getCurrent();
        if (current != null) {
            current.setEnabled(enabled);
        }
        repaint();
    }
}
