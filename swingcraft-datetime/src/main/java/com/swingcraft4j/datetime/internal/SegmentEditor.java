package com.swingcraft4j.datetime.internal;

import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The segments of a field and how they are edited: which segment is selected, what the keys do with it,
 * and the date and the time the segments make together. It knows nothing of Swing, the field shows it and
 * gives it the keys.
 * <p>
 * The segments come from a pattern with the letters of {@link java.time.format.DateTimeFormatter}:
 * {@code d} day, {@code M} month ({@code MMM} and {@code MMMM} for its name), {@code y} year, {@code H} hour
 * of 24, {@code h} hour of 12, {@code m} minute, {@code s} second, {@code a} AM or PM and {@code E} the name
 * of the day. All other characters are shown as they are, letters between two {@code '}.
 * <p>
 * An editor of a range has the segments of the pattern two times, with a separator between them: the first
 * group is the first date of the range and the second group its last date.
 */
public final class SegmentEditor {

    private static final int NONE = -1;

    private final String pattern;
    private final String separator;
    private final Locale locale;
    private final Runnable listener;
    private final List<Segment> segments;
    private final Set<SegmentType> types = EnumSet.noneOf(SegmentType.class);
    private final String[] amPm;
    private int selected = NONE;
    private boolean stableWidth;

    /**
     * @param listener runs after each change of a value or of the selection
     * @throws IllegalArgumentException if the pattern has a letter that is not known, has the same part two
     *                                  times, or has nothing to edit
     */
    public SegmentEditor(String pattern, Locale locale, Runnable listener) {
        this(pattern, null, locale, listener);
    }

    /**
     * @param separator the text between the two dates of a range, or null for an editor of one value
     * @param listener  runs after each change of a value or of the selection
     * @throws IllegalArgumentException if the pattern has a letter that is not known, has the same part two
     *                                  times, or has nothing to edit
     */
    public SegmentEditor(String pattern, String separator, Locale locale, Runnable listener) {
        this.pattern = pattern;
        this.separator = separator;
        this.locale = locale;
        this.listener = listener;
        List<Segment> list = new ArrayList<>(parse(pattern, 0));
        for (Segment segment : list) {
            if (segment.isEditable() && !types.add(segment.getType())) {
                throw new IllegalArgumentException("pattern has the same part more than one time: " + pattern);
            }
        }
        if (separator != null) {
            list.add(new Segment(SegmentType.LITERAL, separator, 0));
            list.addAll(parse(pattern, 1));
        }
        this.segments = Collections.unmodifiableList(list);
        this.amPm = DateFormatSymbols.getInstance(locale).getAmPmStrings();
        if (types.isEmpty()) {
            throw new IllegalArgumentException("pattern has no part of a date or a time: " + pattern);
        }
        if (types.contains(SegmentType.HOUR_12) && types.contains(SegmentType.HOUR_24)) {
            throw new IllegalArgumentException("pattern has two hours: " + pattern);
        }
    }

    // ---- pattern

    private static List<Segment> parse(String pattern, int group) {
        List<Segment> segments = new ArrayList<>();
        StringBuilder literal = new StringBuilder();
        int length = pattern.length();
        int index = 0;
        while (index < length) {
            char ch = pattern.charAt(index);
            if (ch == '\'') {
                // a text between two ', two of them are one that is shown
                int end = index + 1;
                while (end < length) {
                    char next = pattern.charAt(end);
                    if (next != '\'') {
                        literal.append(next);
                        end++;
                    } else if (end + 1 < length && pattern.charAt(end + 1) == '\'') {
                        literal.append('\'');
                        end += 2;
                    } else {
                        break;
                    }
                }
                if (end == index + 1) {
                    // nothing between the two
                    literal.append('\'');
                }
                index = end + 1;
            } else if (Character.isLetter(ch) && ch < 128) {
                int end = index;
                while (end < length && pattern.charAt(end) == ch) {
                    end++;
                }
                if (literal.length() > 0) {
                    segments.add(new Segment(SegmentType.LITERAL, literal.toString(), group));
                    literal.setLength(0);
                }
                segments.add(new Segment(getType(ch), pattern.substring(index, end), group));
                index = end;
            } else {
                literal.append(ch);
                index++;
            }
        }
        if (literal.length() > 0) {
            segments.add(new Segment(SegmentType.LITERAL, literal.toString(), group));
        }
        return segments;
    }

    private static SegmentType getType(char letter) {
        switch (letter) {
            case 'd':
                return SegmentType.DAY;
            case 'M':
                return SegmentType.MONTH;
            case 'y':
            case 'u':
                return SegmentType.YEAR;
            case 'H':
                return SegmentType.HOUR_24;
            case 'h':
                return SegmentType.HOUR_12;
            case 'm':
                return SegmentType.MINUTE;
            case 's':
                return SegmentType.SECOND;
            case 'a':
                return SegmentType.AM_PM;
            case 'E':
                return SegmentType.WEEKDAY;
            default:
                throw new IllegalArgumentException("pattern letter '" + letter + "' is not supported");
        }
    }

    public String getPattern() {
        return pattern;
    }

    /**
     * @return true if the editor has the two dates of a range
     */
    public boolean isRange() {
        return separator != null;
    }

    List<Segment> getSegments() {
        return segments;
    }

    /**
     * @return true if the pattern has a day, a month or a year
     */
    public boolean hasDate() {
        return types.contains(SegmentType.DAY) || types.contains(SegmentType.MONTH) || types.contains(SegmentType.YEAR);
    }

    /**
     * @return true if the pattern has an hour, a minute or a second
     */
    public boolean hasTime() {
        return types.contains(SegmentType.HOUR_12) || types.contains(SegmentType.HOUR_24)
                || types.contains(SegmentType.MINUTE) || types.contains(SegmentType.SECOND);
    }

    /**
     * @return true if the hour of the pattern is from 0 to 23
     */
    public boolean isHour24() {
        return types.contains(SegmentType.HOUR_24);
    }

    // ---- text

    /**
     * @return what is shown for the segment: its value, or its placeholder if it has none
     */
    String getText(Segment segment) {
        switch (segment.getType()) {
            case LITERAL:
                return segment.getPattern();
            case WEEKDAY:
                LocalDate date = getDate(segment.getGroup());
                return date != null ? getDayName(segment, date.getDayOfWeek()) : placeholder(segment);
            case AM_PM:
                return segment.isEmpty() ? placeholder(segment) : amPm[segment.getValue()];
            case MONTH:
                if (segment.isName()) {
                    // a month that is typed can be 0 for a moment
                    boolean month = segment.getValue() >= 1 && segment.getValue() <= 12;
                    return month ? getMonthName(segment, Month.of(segment.getValue())) : placeholder(segment);
                }
                // falls through
            default:
                return segment.isEmpty() ? placeholder(segment) : segment.getNumberText();
        }
    }

    /**
     * @return true if the segment shows its placeholder and not a value
     */
    boolean isPlaceholder(Segment segment) {
        switch (segment.getType()) {
            case LITERAL:
                return false;
            case WEEKDAY:
                return getDate(segment.getGroup()) == null;
            default:
                return segment.isEmpty() || (segment.isName() && segment.getValue() == 0);
        }
    }

    private String placeholder(Segment segment) {
        if (segment.getType() == SegmentType.AM_PM || segment.getType() == SegmentType.WEEKDAY) {
            return "--";
        }
        return segment.getPattern().toLowerCase(Locale.ROOT);
    }

    private String getMonthName(Segment segment, Month month) {
        return month.getDisplayName(segment.getPattern().length() >= 4 ? TextStyle.FULL : TextStyle.SHORT, locale);
    }

    private String getDayName(Segment segment, DayOfWeek day) {
        return day.getDisplayName(segment.getPattern().length() >= 4 ? TextStyle.FULL : TextStyle.SHORT, locale);
    }

    /**
     * @param stableWidth true to give the name of a month or a day the space of the longest name, so the
     *                    field keeps its width. False for the space of the name that is shown
     */
    public void setStableWidth(boolean stableWidth) {
        this.stableWidth = stableWidth;
    }

    /**
     * @return the texts that give the width of the segment: its space is as wide as the widest of them.
     * A number has the space of its widest digits, so the field keeps its width while it is typed.
     * The name of a month or a day has the space of the name that is shown, or of the longest name if the
     * width is stable
     */
    List<String> getTexts(Segment segment) {
        List<String> texts = new ArrayList<>();
        texts.add(getText(segment));
        switch (segment.getType()) {
            case LITERAL:
                break;
            case WEEKDAY:
                if (stableWidth) {
                    for (DayOfWeek day : DayOfWeek.values()) {
                        texts.add(getDayName(segment, day));
                    }
                }
                break;
            case AM_PM:
                texts.add(amPm[0]);
                texts.add(amPm[1]);
                break;
            default:
                if (segment.isName()) {
                    if (stableWidth) {
                        for (Month month : Month.values()) {
                            texts.add(getMonthName(segment, month));
                        }
                    }
                } else {
                    texts.add(placeholder(segment));
                    // the widest digit is not the same in every font
                    int digits = Math.max(segment.getDigits(), segment.getPattern().length());
                    for (char digit = '0'; digit <= '9'; digit++) {
                        StringBuilder text = new StringBuilder();
                        for (int i = 0; i < digits; i++) {
                            text.append(digit);
                        }
                        texts.add(text.toString());
                    }
                }
                break;
        }
        return texts;
    }

    /**
     * @return the text of the whole field, as it is shown
     */
    public String getText() {
        StringBuilder text = new StringBuilder();
        for (Segment segment : segments) {
            text.append(getText(segment));
        }
        return text.toString();
    }

    // ---- selection

    /**
     * @return the index of the selected segment, -1 if none is selected
     */
    public int getSelectedIndex() {
        return selected;
    }

    private Segment getSelected() {
        return selected == NONE ? null : segments.get(selected);
    }

    /**
     * Selects the segment, or the one that can be edited next to it.
     *
     * @param index the index of a segment, or -1 to select none
     */
    public void select(int index) {
        if (index != NONE && !segments.get(index).isEditable()) {
            int next = findEditable(index, 1);
            index = next != NONE ? next : findEditable(index, -1);
        }
        if (index != selected) {
            Segment old = getSelected();
            if (old != null) {
                old.commit();
            }
            selected = index;
            changed();
        }
    }

    /**
     * Selects the first segment that can be edited.
     */
    public void selectFirst() {
        select(findEditable(0, 1));
    }

    public void selectLast() {
        select(findEditable(segments.size() - 1, -1));
    }

    public boolean hasSelection() {
        return selected != NONE;
    }

    /**
     * @return false if there is no segment to edit in that direction
     */
    public boolean selectNext() {
        return move(1);
    }

    public boolean selectPrevious() {
        return move(-1);
    }

    private boolean move(int step) {
        if (selected == NONE) {
            return false;
        }
        int index = findEditable(selected + step, step);
        if (index == NONE) {
            return false;
        }
        select(index);
        return true;
    }

    // the first segment that can be edited, from the index in the direction of the step
    private int findEditable(int index, int step) {
        for (int i = index; i >= 0 && i < segments.size(); i += step) {
            if (segments.get(i).isEditable()) {
                return i;
            }
        }
        return NONE;
    }

    // ---- keys

    /**
     * The user has typed a character: a digit goes to the selected segment, a letter selects AM or PM, and
     * a character of the pattern after the segment, as the line after the day, goes to the next segment.
     */
    public void type(char ch) {
        Segment segment = getSelected();
        if (segment == null) {
            return;
        }
        if (Character.isDigit(ch)) {
            if (segment.getDigits() > 0) {
                boolean full = segment.type(Character.digit(ch, 10));
                if (!full || !selectNext()) {
                    changed();
                }
            }
        } else if (segment.getType() == SegmentType.AM_PM && Character.isLetter(ch)) {
            for (int i = 0; i < amPm.length && i < 2; i++) {
                if (!amPm[i].isEmpty() && Character.toLowerCase(amPm[i].charAt(0)) == Character.toLowerCase(ch)) {
                    segment.setValue(i);
                    changed();
                }
            }
        } else if (isLiteral(selected + 1, ch) && (segment.isTyping() || !isLiteral(selected - 1, ch))) {
            // the character after the segment goes to the next one. Not if the segment was just selected
            // and the character is the one in front of it: after 5 the day is full and the month is
            // selected, the line that is typed then is the one after the day
            selectNext();
        }
    }

    // true if the segment at the index is a text of the pattern with the character
    private boolean isLiteral(int index, char ch) {
        if (index < 0 || index >= segments.size()) {
            return false;
        }
        Segment segment = segments.get(index);
        return segment.getType() == SegmentType.LITERAL && segment.getPattern().indexOf(ch) != -1;
    }

    /**
     * Makes the value of the selected segment larger or smaller. After the largest value comes the smallest,
     * and a segment without value gets the value of now.
     */
    public void adjust(int amount) {
        Segment segment = getSelected();
        if (segment == null) {
            return;
        }
        int minimum = segment.getType().getMinimum();
        int maximum = getMaximum(segment);
        if (segment.isEmpty() || segment.getValue() < minimum || segment.getValue() > maximum) {
            segment.setValue(getValue(segment.getType(), LocalDateTime.now()));
        } else {
            int count = maximum - minimum + 1;
            segment.setValue(minimum + Math.floorMod(segment.getValue() - minimum + amount, count));
        }
        changed();
    }

    // the last day of the month, if the month and the year are known
    private int getMaximum(Segment segment) {
        if (segment.getType() == SegmentType.DAY) {
            int month = getValue(SegmentType.MONTH, segment.getGroup());
            int year = getValue(SegmentType.YEAR, segment.getGroup());
            if (month >= 1 && month <= 12) {
                // a month without year can be the February of a leap year
                return year >= 1 ? YearMonth.of(year, month).lengthOfMonth() : Month.of(month).maxLength();
            }
        }
        return segment.getType().getMaximum();
    }

    /**
     * Removes the value of the selected segment. If it has none, the segment before it is selected.
     *
     * @param back true for the backspace key, false to stay at the segment
     */
    public void delete(boolean back) {
        Segment segment = getSelected();
        if (segment == null) {
            return;
        }
        if (!segment.isEmpty()) {
            segment.clear();
            changed();
        } else if (back) {
            selectPrevious();
        }
    }

    // ---- value

    private Segment find(SegmentType type, int group) {
        for (Segment segment : segments) {
            if (segment.getType() == type && segment.getGroup() == group) {
                return segment;
            }
        }
        return null;
    }

    // the value of the part, or -1 if the pattern does not have it or it has no value
    private int getValue(SegmentType type, int group) {
        Segment segment = find(type, group);
        return segment != null ? segment.getValue() : Segment.EMPTY;
    }

    private int getValue(SegmentType type) {
        return getValue(type, 0);
    }

    private static int getValue(SegmentType type, LocalDateTime dateTime) {
        switch (type) {
            case DAY:
                return dateTime.getDayOfMonth();
            case MONTH:
                return dateTime.getMonthValue();
            case YEAR:
                return dateTime.getYear();
            case HOUR_24:
                return dateTime.getHour();
            case HOUR_12:
                return dateTime.getHour() % 12 == 0 ? 12 : dateTime.getHour() % 12;
            case MINUTE:
                return dateTime.getMinute();
            case SECOND:
                return dateTime.getSecond();
            case AM_PM:
                return dateTime.getHour() < 12 ? 0 : 1;
            default:
                return Segment.EMPTY;
        }
    }

    /**
     * @return true if no segment has a value
     */
    public boolean isEmpty() {
        for (Segment segment : segments) {
            if (segment.isEditable() && !segment.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if every segment has a value. The values can still be a date that does not exist,
     * as the 31 of a month with 30 days
     */
    public boolean isComplete() {
        for (Segment segment : segments) {
            if (segment.isEditable() && segment.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // true if each of the parts that the pattern has, has a value in its range
    private boolean isValid(int group, SegmentType... parts) {
        for (SegmentType type : parts) {
            Segment segment = find(type, group);
            if (segment != null && (segment.getValue() < type.getMinimum() || segment.getValue() > type.getMaximum())) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the date of the segments, or null if the pattern has no date, a part of it has no value or
     * the date does not exist. A part that the pattern does not have is the first: the first day, January.
     * Without year it is this year
     */
    public LocalDate getDate() {
        return getDate(0);
    }

    /**
     * @param group 0 for the date, or the first date of a range. 1 for the last date of a range
     * @see #getDate()
     */
    public LocalDate getDate(int group) {
        if (!hasDate() || !isValid(group, SegmentType.DAY, SegmentType.MONTH, SegmentType.YEAR)) {
            return null;
        }
        int year = types.contains(SegmentType.YEAR) ? getValue(SegmentType.YEAR, group) : LocalDate.now().getYear();
        int month = types.contains(SegmentType.MONTH) ? getValue(SegmentType.MONTH, group) : 1;
        int day = types.contains(SegmentType.DAY) ? getValue(SegmentType.DAY, group) : 1;
        if (day > YearMonth.of(year, month).lengthOfMonth()) {
            return null;
        }
        return LocalDate.of(year, month, day);
    }

    /**
     * @return the time of the segments, or null if the pattern has no time or a part of it has no value.
     * A part that the pattern does not have is 0
     */
    public LocalTime getTime() {
        if (!hasTime() || !isValid(0, SegmentType.HOUR_24, SegmentType.HOUR_12, SegmentType.MINUTE,
                SegmentType.SECOND, SegmentType.AM_PM)) {
            return null;
        }
        int hour = 0;
        if (types.contains(SegmentType.HOUR_24)) {
            hour = getValue(SegmentType.HOUR_24);
        } else if (types.contains(SegmentType.HOUR_12)) {
            // 12 is the first hour of the half of the day
            hour = getValue(SegmentType.HOUR_12) % 12;
            if (getValue(SegmentType.AM_PM) == 1) {
                hour += 12;
            }
        }
        int minute = types.contains(SegmentType.MINUTE) ? getValue(SegmentType.MINUTE) : 0;
        int second = types.contains(SegmentType.SECOND) ? getValue(SegmentType.SECOND) : 0;
        return LocalTime.of(hour, minute, second);
    }

    /**
     * @return the date and the time of the segments, or null if one that the pattern has is missing.
     * Without date in the pattern the date is today, without time it is the start of the day
     */
    public LocalDateTime getDateTime() {
        LocalDate date = hasDate() ? getDate() : LocalDate.now();
        LocalTime time = hasTime() ? getTime() : LocalTime.MIDNIGHT;
        return date != null && time != null ? LocalDateTime.of(date, time) : null;
    }

    /**
     * Sets the segments of the date, the segments of the time stay.
     *
     * @param date the date, or null to remove their values
     */
    public void setDate(LocalDate date) {
        if (set(date != null ? date.atStartOfDay() : null, true, 0)) {
            changed();
        }
    }

    /**
     * Sets the two dates of a range.
     *
     * @param from the first date, or null to remove its values
     * @param to   the last date, or null to remove its values
     */
    public void setRange(LocalDate from, LocalDate to) {
        // one change for the two dates, not a range of the new first date and the old last date between them
        boolean first = set(from != null ? from.atStartOfDay() : null, true, 0);
        boolean last = set(to != null ? to.atStartOfDay() : null, true, 1);
        if (first || last) {
            changed();
        }
    }

    /**
     * Sets the segments of the time, the segments of the date stay.
     *
     * @param time the time, or null to remove their values
     */
    public void setTime(LocalTime time) {
        if (set(time != null ? time.atDate(LocalDate.now()) : null, false, 0)) {
            changed();
        }
    }

    public void setDateTime(LocalDateTime dateTime) {
        // one change for the date and the time
        boolean date = set(dateTime, true, 0);
        boolean time = set(dateTime, false, 0);
        if (date || time) {
            changed();
        }
    }

    public void clear() {
        boolean first = set(null, true, 0);
        boolean last = set(null, true, 1);
        boolean time = set(null, false, 0);
        if (first || last || time) {
            changed();
        }
    }

    /**
     * Reads a text that has the pattern of the editor, as a text that is pasted. A range is two texts with
     * the separator between them.
     *
     * @return false if the text is not of the pattern, nothing has changed then
     */
    public boolean parse(String text) {
        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern(pattern)
                    .toFormatter(locale);
            if (isRange()) {
                // the whole separator: a part of it, as the line, can be in the pattern of the dates too
                int index = text.indexOf(separator);
                if (index <= 0) {
                    return false;
                }
                LocalDate from = formatter.parse(text.substring(0, index).trim()).query(TemporalQueries.localDate());
                LocalDate to = formatter.parse(text.substring(index + separator.length()).trim()).query(TemporalQueries.localDate());
                if (from == null || to == null) {
                    return false;
                }
                setRange(from, to);
                return true;
            }
            TemporalAccessor parsed = formatter.parse(text.trim());
            LocalDate date = hasDate() ? parsed.query(TemporalQueries.localDate()) : null;
            LocalTime time = hasTime() ? parsed.query(TemporalQueries.localTime()) : null;
            if ((hasDate() && date == null) || (hasTime() && time == null)) {
                return false;
            }
            boolean dateChanged = hasDate() && set(date.atStartOfDay(), true, 0);
            boolean timeChanged = hasTime() && set(time.atDate(LocalDate.now()), false, 0);
            if (dateChanged || timeChanged) {
                changed();
            }
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    // sets the segments of the date or of the time of the group, and says if one of them has changed
    private boolean set(LocalDateTime dateTime, boolean date, int group) {
        boolean change = false;
        for (Segment segment : segments) {
            SegmentType type = segment.getType();
            if (segment.getGroup() == group && (date ? type.isDate() : type.isTime())) {
                int value = dateTime != null ? getValue(type, dateTime) : Segment.EMPTY;
                if (segment.getValue() != value) {
                    segment.setValue(value);
                    change = true;
                }
            }
        }
        return change;
    }

    private void changed() {
        listener.run();
    }
}
