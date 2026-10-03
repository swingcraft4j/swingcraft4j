package com.swingcraft4j.datetime;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A range of dates with a name, as "Last 7 days". A date picker shows its presets next to the calendar, and
 * a click on one selects its range. Set them with
 * {@link com.swingcraft4j.datetime.option.DateOption#setPresets(List)}.
 * <p>
 * The range is asked for each time the preset is used, so a preset as "Today" is right on the next day too.
 */
public final class DatePreset {

    private final String name;
    private final Supplier<DateRange> range;

    /**
     * @param name  the text of the preset
     * @param range gives the range of the preset. A picker that selects one date uses its first date
     */
    public DatePreset(String name, Supplier<DateRange> range) {
        if (name == null || range == null) {
            throw new IllegalArgumentException("name and range must not null");
        }
        this.name = name;
        this.range = range;
    }

    public String getName() {
        return name;
    }

    /**
     * @return the range of the preset, as it is now
     */
    public DateRange getRange() {
        return range.get();
    }

    public static DatePreset today() {
        return new DatePreset("Today", () -> range(LocalDate.now(), LocalDate.now()));
    }

    public static DatePreset yesterday() {
        return new DatePreset("Yesterday", () -> range(LocalDate.now().minusDays(1), LocalDate.now().minusDays(1)));
    }

    /**
     * @param days how many days the range has, today is its last day
     */
    public static DatePreset lastDays(int days) {
        if (days < 1) {
            throw new IllegalArgumentException("days must be >= 1");
        }
        return new DatePreset("Last " + days + " days", () -> range(LocalDate.now().minusDays(days - 1), LocalDate.now()));
    }

    public static DatePreset thisMonth() {
        return new DatePreset("This month", () -> {
            LocalDate today = LocalDate.now();
            return range(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()));
        });
    }

    public static DatePreset lastMonth() {
        return new DatePreset("Last month", () -> {
            LocalDate date = LocalDate.now().minusMonths(1);
            return range(date.withDayOfMonth(1), date.withDayOfMonth(date.lengthOfMonth()));
        });
    }

    public static DatePreset thisYear() {
        return new DatePreset("This year", () -> {
            LocalDate today = LocalDate.now();
            return range(today.withDayOfYear(1), today.withDayOfYear(today.lengthOfYear()));
        });
    }

    public static DatePreset lastYear() {
        return new DatePreset("Last year", () -> {
            LocalDate date = LocalDate.now().minusYears(1);
            return range(date.withDayOfYear(1), date.withDayOfYear(date.lengthOfYear()));
        });
    }

    /**
     * @return today, yesterday, the last 7 and the last 30 days, this month, the last month and the last year
     */
    public static List<DatePreset> defaults() {
        List<DatePreset> presets = new ArrayList<>();
        presets.add(today());
        presets.add(yesterday());
        presets.add(lastDays(7));
        presets.add(lastDays(30));
        presets.add(thisMonth());
        presets.add(lastMonth());
        presets.add(lastYear());
        return presets;
    }

    private static DateRange range(LocalDate from, LocalDate to) {
        return new DateRange(from, to);
    }
}
