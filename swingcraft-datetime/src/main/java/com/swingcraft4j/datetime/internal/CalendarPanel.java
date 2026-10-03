package com.swingcraft4j.datetime.internal;

import com.swingcraft4j.datetime.option.DateOption;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;

/**
 * The calendar of a date picker: a header to go to another month, and below it the days of the month,
 * the months of the year or the years. It shows the selection and tells the picker what was clicked.
 * <p>
 * With the focus, the keyboard moves through the days: the arrow keys by a day and a week, page up and
 * page down by a month (with shift by a year), home and end to the first and the last day of the month,
 * and enter or space selects the day. Control and up goes from the days to the months and on to the years.
 * There the arrow keys move through the months and the years, and enter goes back: from a year to its
 * months, and from a month to its days.
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
    // what the keyboard is at in each view: a day, a month and a year. They start at what is shown
    private LocalDate cursor;
    private YearMonth cursorMonth;
    private int cursorYear;
    // true from the first key until the mouse is used or the focus is lost: the mark of the keyboard is
    // shown only while the keyboard is used
    private boolean keyboardUsed;

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
        installKeys();
    }

    private void installKeys() {
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isEnabled() && !e.isAltDown() && !e.isMetaDown() && handleKey(e.getKeyCode(), e.isShiftDown(), e.isControlDown())) {
                    keyboardUsed = true;
                    repaint();
                    e.consume();
                }
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                keyboardUsed = false;
                repaint();
            }
        });
    }

    // true if the key was used
    private boolean handleKey(int code, boolean shift, boolean control) {
        if (control) {
            // from the days to the months and to the years, and back
            if (code == KeyEvent.VK_UP && view != View.YEAR) {
                show(view == View.DAY ? View.MONTH : View.YEAR, SlidePanel.Direction.DOWN);
                return true;
            }
            if (code == KeyEvent.VK_DOWN && view != View.DAY) {
                selectCursor();
                return true;
            }
            return false;
        }
        boolean leftToRight = getComponentOrientation().isLeftToRight();
        int next = leftToRight ? 1 : -1;
        switch (code) {
            case KeyEvent.VK_LEFT:
                moveCursor(-next, 0, 0);
                return true;
            case KeyEvent.VK_RIGHT:
                moveCursor(next, 0, 0);
                return true;
            case KeyEvent.VK_UP:
                moveCursor(0, -1, 0);
                return true;
            case KeyEvent.VK_DOWN:
                moveCursor(0, 1, 0);
                return true;
            case KeyEvent.VK_PAGE_UP:
                moveCursor(0, 0, shift ? -12 : -1);
                return true;
            case KeyEvent.VK_PAGE_DOWN:
                moveCursor(0, 0, shift ? 12 : 1);
                return true;
            case KeyEvent.VK_HOME:
            case KeyEvent.VK_END:
                if (view == View.DAY) {
                    LocalDate date = getCursorDate();
                    setCursorDate(date.withDayOfMonth(code == KeyEvent.VK_HOME ? 1 : date.lengthOfMonth()));
                    return true;
                }
                return false;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                selectCursor();
                return true;
            default:
                return false;
        }
    }

    /**
     * Moves what the keyboard is at: a day, a month or a year, as the view is.
     *
     * @param cells how many cells to the next
     * @param rows  how many rows down
     * @param pages how many pages on: months for the days, years for the months, pages for the years
     */
    private void moveCursor(int cells, int rows, int pages) {
        if (view == View.DAY) {
            setCursorDate(getCursorDate().plusDays(cells + rows * 7L).plusMonths(pages));
        } else if (view == View.MONTH) {
            YearMonth old = getCursorMonth();
            cursorMonth = old.plusMonths(cells + rows * (long) MonthGrid.COLUMNS).plusYears(pages);
            if (cursorMonth.getYear() != old.getYear()) {
                // the months of another year
                month = month.withYear(cursorMonth.getYear());
                show(View.MONTH, cursorMonth.isAfter(old) ? SlidePanel.Direction.FORWARD : SlidePanel.Direction.BACKWARD);
            }
            repaint();
        } else {
            int old = getCursorYear();
            cursorYear = old + cells + rows * YearGrid.COLUMNS + pages * YearGrid.YEARS;
            int page = YearGrid.getFirstYear(cursorYear);
            if (page != yearPage) {
                // another page of years
                yearPage = page;
                show(View.YEAR, cursorYear > old ? SlidePanel.Direction.FORWARD : SlidePanel.Direction.BACKWARD);
            }
            repaint();
        }
    }

    // enter: selects the day, or goes from the years to the months and from the months to the days
    private void selectCursor() {
        if (view == View.DAY) {
            LocalDate date = getCursorDate();
            if (option.isSelectable(date)) {
                dateClicked(date);
            }
        } else if (view == View.MONTH) {
            monthClicked(getCursorMonth());
        } else {
            yearClicked(getCursorYear());
        }
    }

    /**
     * @return the day the keyboard is at. If it is not in the month that is shown, as after a click on the
     * header, it starts again at the selected day, today or the first day of that month
     */
    LocalDate getCursorDate() {
        if (cursor == null || !YearMonth.from(cursor).equals(month)) {
            LocalDate selected = selection.getAnchor();
            LocalDate today = LocalDate.now();
            if (selected != null && YearMonth.from(selected).equals(month)) {
                cursor = selected;
            } else if (YearMonth.from(today).equals(month)) {
                cursor = today;
            } else {
                cursor = month.atDay(1);
            }
        }
        return cursor;
    }

    private void setCursorDate(LocalDate date) {
        cursor = date;
        showMonth(YearMonth.from(date), true);
        repaint();
    }

    /**
     * @return the month the keyboard is at, in the year that is shown
     */
    YearMonth getCursorMonth() {
        if (cursorMonth == null || cursorMonth.getYear() != month.getYear()) {
            cursorMonth = month;
        }
        return cursorMonth;
    }

    /**
     * @return the year the keyboard is at, on the page that is shown
     */
    int getCursorYear() {
        if (YearGrid.getFirstYear(cursorYear) != yearPage) {
            cursorYear = YearGrid.getFirstYear(month.getYear()) == yearPage ? month.getYear() : yearPage;
        }
        return cursorYear;
    }

    /**
     * @return true if what the keyboard is at is shown with a line around it: the calendar has the focus
     * and the keyboard was used, not the mouse
     */
    boolean isCursorShown() {
        return keyboardUsed && isFocusOwner();
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
        slidePanel.setFixedSize(DayGrid.getPreferredSize(option.getSize()));
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
        // the keyboard goes on from the day that was clicked
        cursor = date;
        selection.click(date);
        repaint();
        listener.dateClicked(date);
    }

    void monthClicked(YearMonth month) {
        this.month = month;
        show(View.DAY, SlidePanel.Direction.UP);
    }

    /**
     * A click gives the calendar the focus, so the keyboard goes on from what was clicked.
     */
    void cellClicked() {
        keyboardUsed = false;
        requestFocusInWindow();
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
