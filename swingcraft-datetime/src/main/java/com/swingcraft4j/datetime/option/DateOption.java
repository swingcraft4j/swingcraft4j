package com.swingcraft4j.datetime.option;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * The options of a date picker: what it selects and how the calendar is shown. The animation is in its own
 * {@link AnimationOption}. The setters return the option, so they can be chained.
 * <p>
 * The option is copied when it is given to a picker, so changing it afterwards
 * does not affect the picker.
 */
public class DateOption {

    private DateSelectionMode selectionMode = DateSelectionMode.SINGLE;
    private PickerSize size = PickerSize.DEFAULT;
    private DayOfWeek firstDayOfWeek = DayOfWeek.SUNDAY;
    private Locale locale;
    private boolean closeOnSelect;
    private Predicate<LocalDate> selectable;
    private AnimationOption animationOption = new AnimationOption();

    public DateSelectionMode getSelectionMode() {
        return selectionMode;
    }

    public DayOfWeek getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    /**
     * @return the locale of the names of the months and the days, the default locale if none is set
     */
    public Locale getLocale() {
        return locale != null ? locale : Locale.getDefault();
    }

    public PickerSize getSize() {
        return size;
    }

    public boolean isCloseOnSelect() {
        return closeOnSelect;
    }

    public Predicate<LocalDate> getSelectable() {
        return selectable;
    }

    /**
     * @return true if the user can select the date
     */
    public boolean isSelectable(LocalDate date) {
        return selectable == null || selectable.test(date);
    }

    public AnimationOption getAnimationOption() {
        return animationOption;
    }

    /**
     * @param selectionMode {@link DateSelectionMode#SINGLE} to select one date (default), or
     *                      {@link DateSelectionMode#RANGE} to select a range of dates
     */
    public DateOption setSelectionMode(DateSelectionMode selectionMode) {
        if (selectionMode == null) {
            throw new IllegalArgumentException("selection mode must not null");
        }
        this.selectionMode = selectionMode;
        return this;
    }

    /**
     * @param firstDayOfWeek the day in the first column of the calendar, Sunday by default
     */
    public DateOption setFirstDayOfWeek(DayOfWeek firstDayOfWeek) {
        if (firstDayOfWeek == null) {
            throw new IllegalArgumentException("first day of week must not null");
        }
        this.firstDayOfWeek = firstDayOfWeek;
        return this;
    }

    /**
     * @param locale the locale of the names of the months and the days, or null to use the default locale
     */
    public DateOption setLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * @param size how large the picker is, {@link PickerSize#DEFAULT} by default
     */
    public DateOption setSize(PickerSize size) {
        if (size == null) {
            throw new IllegalArgumentException("size must not null");
        }
        this.size = size;
        return this;
    }

    /**
     * @param closeOnSelect true to close the popup of the picker when a date, or the end of a range,
     *                      is selected
     */
    public DateOption setCloseOnSelect(boolean closeOnSelect) {
        this.closeOnSelect = closeOnSelect;
        return this;
    }

    /**
     * @param selectable says which dates the user can select: true for a date that can be selected.
     *                   The other dates are shown as disabled and can not be clicked. Null (default) to
     *                   select every date. A date that is set from the code is not checked
     */
    public DateOption setSelectable(Predicate<LocalDate> selectable) {
        this.selectable = selectable;
        return this;
    }

    /**
     * @param animationOption how the picker is animated, change the current one with {@link #getAnimationOption()}
     */
    public DateOption setAnimationOption(AnimationOption animationOption) {
        if (animationOption == null) {
            throw new IllegalArgumentException("animation option must not null");
        }
        this.animationOption = animationOption;
        return this;
    }

    public DateOption copy() {
        DateOption option = new DateOption();
        option.selectionMode = selectionMode;
        option.firstDayOfWeek = firstDayOfWeek;
        option.locale = locale;
        option.size = size;
        option.closeOnSelect = closeOnSelect;
        option.selectable = selectable;
        option.animationOption = animationOption.copy();
        return option;
    }
}
