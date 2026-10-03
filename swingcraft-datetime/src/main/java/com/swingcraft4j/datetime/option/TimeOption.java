package com.swingcraft4j.datetime.option;

import java.time.LocalTime;
import java.util.function.Predicate;

/**
 * The options of a time picker. The animation is in its own {@link AnimationOption}.
 * The setters return the option, so they can be chained.
 * <p>
 * The option is copied when it is given to a picker, so changing it afterwards
 * does not affect the picker.
 */
public class TimeOption {

    private boolean hour24;
    private PickerSize size = PickerSize.DEFAULT;
    private boolean closeOnSelect;
    private Predicate<LocalTime> selectable;
    private AnimationOption animationOption = new AnimationOption();

    public boolean isHour24() {
        return hour24;
    }

    public PickerSize getSize() {
        return size;
    }

    public boolean isCloseOnSelect() {
        return closeOnSelect;
    }

    public Predicate<LocalTime> getSelectable() {
        return selectable;
    }

    /**
     * @return true if the user can select the time
     */
    public boolean isSelectable(LocalTime time) {
        return selectable == null || selectable.test(time);
    }

    public AnimationOption getAnimationOption() {
        return animationOption;
    }

    /**
     * @param hour24 true for a clock with the hours from 0 to 23, false (default) for a clock with
     *               the hours from 1 to 12 and AM and PM
     */
    public TimeOption setHour24(boolean hour24) {
        this.hour24 = hour24;
        return this;
    }

    /**
     * @param size how large the picker is, {@link PickerSize#DEFAULT} by default
     */
    public TimeOption setSize(PickerSize size) {
        if (size == null) {
            throw new IllegalArgumentException("size must not null");
        }
        this.size = size;
        return this;
    }

    /**
     * @param closeOnSelect true to close the popup of the picker when the minute is selected
     */
    public TimeOption setCloseOnSelect(boolean closeOnSelect) {
        this.closeOnSelect = closeOnSelect;
        return this;
    }

    /**
     * @param selectable says which times the user can select: true for a time that can be selected.
     *                   An hour without any minute that can be selected, and the other minutes, are shown as
     *                   disabled on the clock and can not be selected. Null (default) to select every time.
     *                   A time that is set from the code is not checked
     */
    public TimeOption setSelectable(Predicate<LocalTime> selectable) {
        this.selectable = selectable;
        return this;
    }

    /**
     * @param animationOption how the picker is animated, change the current one with {@link #getAnimationOption()}
     */
    public TimeOption setAnimationOption(AnimationOption animationOption) {
        if (animationOption == null) {
            throw new IllegalArgumentException("animation option must not null");
        }
        this.animationOption = animationOption;
        return this;
    }

    public TimeOption copy() {
        TimeOption option = new TimeOption();
        option.hour24 = hour24;
        option.size = size;
        option.closeOnSelect = closeOnSelect;
        option.selectable = selectable;
        option.animationOption = animationOption.copy();
        return option;
    }
}
