# Datetime

A date picker and a time picker with animation, and fields to type a date and a time.

- [Installation](#installation)
- [Date picker](#date-picker)
- [Date range](#date-range)
- [Date options](#date-options)
- [Time picker](#time-picker)
- [Time options](#time-options)
- [Selectable dates and times](#selectable-dates-and-times)
- [Presets](#presets)
- [Keyboard](#keyboard)
- [Fields](#fields)
- [Field options](#field-options)
- [Size](#size)
- [Style](#style)
- [Popup](#popup)
- [Animation](#animation)
- [Default option](#default-option)

## Installation

```xml
<dependency>
    <groupId>com.swingcraft4j</groupId>
    <artifactId>swingcraft-datetime</artifactId>
    <version>1.0.0</version>
</dependency>
```

FlatLaf and MigLayout come in with the module. Set up a FlatLaf look and feel before a picker is shown.

### Snapshot

The snapshot is the version in development, before the release. Add the snapshot repository to use it:

```xml
<repositories>
    <repository>
        <id>central-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>

<dependency>
    <groupId>com.swingcraft4j</groupId>
    <artifactId>swingcraft-datetime</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

All the methods of the pickers must be called on the event dispatch thread.

## Date picker

`DatePicker` is a calendar to select a date. It is a component: add it to a container, or show it in a
[popup](#popup).

```java
DatePicker datePicker = new DatePicker();
datePicker.setSelectedDate(LocalDate.now());
datePicker.addDateSelectionListener(e -> {
    LocalDate date = e.getDate();
});
panel.add(datePicker);
```

The header of the calendar goes to the month before and after. A click on the month shows the months of the
year, and a click on the year shows the years. The same click comes back to the days.

| Method                                | What it does                                                          |
|---------------------------------------|-----------------------------------------------------------------------|
| `getSelectedDate()`                   | The selected date, or null if nothing is selected                     |
| `setSelectedDate(LocalDate)`          | Selects the date and shows its month, null clears the selection       |
| `selectToday()`                       | Selects the date of today                                             |
| `clearSelection()`                    | Clears the selection                                                  |
| `isDateSelected()`                    | True if a date, or a range with its end, is selected                  |
| `showDate(LocalDate)`                 | Shows the month of the date, the selection does not change            |
| `addDateSelectionListener(listener)`  | Listens to the selection                                              |
| `removeDateSelectionListener(listener)` | Removes the listener                                                |
| `getOption()`                         | A copy of the option of the picker                                    |
| `setOption(DateOption)`               | Changes the option                                                    |
| `setEnabled(boolean)`                 | False shows the picker, but it can not be used                        |

The listener gets a `DateSelectionEvent` when another date is selected, and when the selection is cleared.

| Method            | What it does                                                        |
|-------------------|---------------------------------------------------------------------|
| `getDate()`       | The selected date, or null if the selection was cleared             |
| `getDateRange()`  | The selected range, or null if the picker selects one date          |
| `getDatePicker()` | The picker                                                          |

## Date range

With `DateSelectionMode.RANGE` the picker selects a range of dates: the first click selects the start, and the
second click the end. The two can be clicked in any order.

```java
DatePicker datePicker = new DatePicker(new DateOption()
        .setSelectionMode(DateSelectionMode.RANGE));

datePicker.setSelectedDateRange(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 16));
datePicker.addDateSelectionListener(e -> {
    DateRange range = e.getDateRange();
    if (range != null) {
        LocalDate from = range.getFrom();
        LocalDate to = range.getTo();
    }
});
```

| Method                                 | What it does                                                         |
|----------------------------------------|----------------------------------------------------------------------|
| `getSelectedDateRange()`               | The selected range, or null if no range is selected                  |
| `setSelectedDateRange(DateRange)`      | Selects the range and shows the month of its first date              |
| `setSelectedDateRange(from, to)`       | The same, with the two dates                                         |
| `getSelectedDate()`                    | The first date of the selected range                                 |
| `setSelectedDate(LocalDate)`           | Selects a range of that one date                                     |

The listener is told when the range has its end, not after the first click. `setSelectedDateRange` throws
`IllegalStateException` if the picker selects one date.

A `DateRange` has the first and the last date, both are in the range.

| Method                | What it does                          |
|-----------------------|---------------------------------------|
| `getFrom()`           | The first date                        |
| `getTo()`             | The last date                         |
| `contains(LocalDate)` | True if the date is in the range      |

## Date options

`DateOption` sets what a date picker selects and how the calendar is shown. The setters return the option, so
they can be chained. The option is copied when it is given to a picker: changing it later does not change
the picker, give it to the picker again with `setOption`.

```java
DateOption option = new DateOption()
        .setSelectionMode(DateSelectionMode.RANGE)
        .setFirstDayOfWeek(DayOfWeek.MONDAY)
        .setCloseOnSelect(true);

DatePicker datePicker = new DatePicker(option);
```

| Setter                         | Default          | What it does                                                    |
|--------------------------------|------------------|-----------------------------------------------------------------|
| `setSelectionMode(mode)`       | `SINGLE`         | `SINGLE` for one date, `RANGE` for a range of dates             |
| `setSize(PickerSize)`          | `DEFAULT`        | How large the picker is, see [Size](#size)                      |
| `setFirstDayOfWeek(DayOfWeek)` | `SUNDAY`         | The day in the first column of the calendar                     |
| `setLocale(Locale)`            | `null`           | The language of the names of the months and the days, null for the default locale |
| `setCloseOnSelect(boolean)`    | `false`          | Closes the popup when a date, or the end of a range, is selected |
| `setSelectable(Predicate)`     | `null`           | Which dates can be selected, see [Selectable dates and times](#selectable-dates-and-times) |
| `setPresets(List)`             | none             | Ranges with a name next to the calendar, see [Presets](#presets) |
| `setAnimationOption(option)`   |                  | How the picker is animated, see [Animation](#animation)         |
| `setStyleOption(option)`       |                  | How the picker looks, see [Style](#style)                       |

`setOption` clears the selection if the selection mode is another one.

## Time picker

`TimePicker` is a clock to select a time, the hour and the minute. The clock shows the hours first, and the
minutes when an hour is selected. Drag the hand, or click a number. The header shows the time: a click on
the hour or the minute shows it on the clock, and AM and PM select the half of the day.

```java
TimePicker timePicker = new TimePicker();
timePicker.setSelectedTime(LocalTime.of(9, 30));
timePicker.addTimeSelectionListener(e -> {
    LocalTime time = e.getTime();
});
panel.add(timePicker);
```

| Method                                  | What it does                                                        |
|-----------------------------------------|---------------------------------------------------------------------|
| `getSelectedTime()`                     | The selected time, or null if no time is selected                   |
| `setSelectedTime(LocalTime)`            | Selects the time, its seconds are not used. Null clears the selection |
| `selectNow()`                           | Selects the time of now                                             |
| `clearSelection()`                      | Clears the selection                                                |
| `isTimeSelected()`                      | True if a time is selected                                          |
| `addTimeSelectionListener(listener)`    | Listens to the selection                                            |
| `removeTimeSelectionListener(listener)` | Removes the listener                                                |
| `getOption()`                           | A copy of the option of the picker                                  |
| `setOption(TimeOption)`                 | Changes the option, the selected time stays                         |
| `setEnabled(boolean)`                   | False shows the picker, but it can not be used                      |

The listener gets a `TimeSelectionEvent` with `getTime()` and `getTimePicker()` for each hour and minute the
hand is moved to, and when the selection is cleared. An hour that is selected first has the minute 0.

## Time options

```java
TimeOption option = new TimeOption()
        .setHour24(true)
        .setCloseOnSelect(true);

TimePicker timePicker = new TimePicker(option);
```

| Setter                       | Default | What it does                                                       |
|------------------------------|---------|--------------------------------------------------------------------|
| `setHour24(boolean)`         | `false` | True for a clock with 24 hours, false for 12 hours with AM and PM  |
| `setSize(PickerSize)`        | `DEFAULT` | How large the picker is, see [Size](#size)                       |
| `setCloseOnSelect(boolean)`  | `false` | Closes the popup when the minute is selected                       |
| `setSelectable(Predicate)`   | `null`  | Which times can be selected, see [Selectable dates and times](#selectable-dates-and-times) |
| `setAnimationOption(option)` |         | How the picker is animated, see [Animation](#animation)            |
| `setStyleOption(option)`     |         | How the picker looks, see [Style](#style)                          |

A clock with 24 hours has two circles: the hours from 1 to 12 outside, and from 13 to 0 inside.

## Selectable dates and times

A picker can be told which dates or times the user can select. The others are shown as disabled and can not
be clicked.

```java
DateOption dateOption = new DateOption()
        .setSelectable(date -> date.getDayOfWeek() != DayOfWeek.SUNDAY);

TimeOption timeOption = new TimeOption()
        .setSelectable(time -> !time.isBefore(LocalTime.of(8, 0)) && !time.isAfter(LocalTime.of(17, 30)));
```

On the clock, an hour is disabled if none of its minutes can be selected. An hour that is selected gets its
first minute that can be selected, and so does a time that is moved to the other half of the day with AM and
PM. If that hour has none, AM and PM stay as they are. A date or a time that is set from the code is not
checked.

## Presets

A date picker can show ranges of dates with a name next to its calendar, as "Last 7 days". A click on one
selects its range. The preset that has the selection of the picker is shown as selected.

```java
DatePicker datePicker = new DatePicker(new DateOption()
        .setSelectionMode(DateSelectionMode.RANGE)
        .setPresets(DatePreset.defaults()));
```

`DatePreset.defaults()` has today, yesterday, the last 7 and the last 30 days, this month, the last month and
the last year. Make your own list from the ones of `DatePreset`, or with your own name and range:

```java
List<DatePreset> presets = new ArrayList<>();
presets.add(DatePreset.today());
presets.add(DatePreset.lastDays(14));
presets.add(DatePreset.thisYear());
presets.add(new DatePreset("Next week", () -> {
    LocalDate monday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    return new DateRange(monday, monday.plusDays(6));
}));
```

| `DatePreset`          | Range                                         |
|-----------------------|-----------------------------------------------|
| `today()`             | Today                                         |
| `yesterday()`         | Yesterday                                     |
| `lastDays(int)`       | That many days, today is the last             |
| `thisMonth()`         | From the first to the last day of this month  |
| `lastMonth()`         | The month before this one                     |
| `thisYear()`          | From the first to the last day of this year   |
| `lastYear()`          | The year before this one                      |

A preset has `getName()` and `getRange()`, the range as it is now. `lastDays` throws
`IllegalArgumentException` if the days are less than 1.

The range is asked for each time, so "Today" is right on the next day too. A picker that selects one date
selects the first date of the range. The names are in English: for another language, make the presets with
your own names.

## Keyboard

A picker in a container can be used with the keyboard when it has the focus: with the tab key, a click, or
`requestFocusInWindow()`.

| Date picker            | What it does                                             |
|------------------------|----------------------------------------------------------|
| Left, Right            | The day before or after                                  |
| Up, Down               | The same day of the week before or after                 |
| Page Up, Page Down     | The month before or after, with Shift the year           |
| Home, End              | The first or the last day of the month                   |
| Enter, Space           | Selects the day                                          |
| Ctrl + Up              | Shows the months, and from the months the years          |

In the months and the years, the arrow keys move through them and Page Up and Page Down go to the year or
the page before and after. Enter, Space or Ctrl + Down goes back: from a year to its months, and from a month
to its days.

What the keyboard is at has a line of dashes around it, so it does not look as the line of today. The line
is shown from the first key on, and not after a click with the mouse. It moves without selecting, and the
calendar goes to another month, year or page with it.

| Time picker            | What it does                                             |
|------------------------|----------------------------------------------------------|
| Up, Down               | The next hour or minute that can be selected             |
| Left, Right            | Shows the hours or the minutes                           |
| Enter, Space           | Goes on from the hours to the minutes                    |
| `A`, `P`               | Select AM or PM                                          |

A picker in a popup does not get the focus. The popup of a field is used from the field: what is typed
there is selected in the picker.

## Fields

A field is a text field to type a date or a time, with a button that shows the picker in a popup. There are
four of them, one for each kind of value.

| Field            | Value           | Default pattern           | Popup                           |
|------------------|-----------------|---------------------------|---------------------------------|
| `DateField`      | `LocalDate`     | `dd/MM/yyyy`              | The date picker                 |
| `TimeField`      | `LocalTime`     | `hh:mm a`                 | The time picker                 |
| `DateTimeField`  | `LocalDateTime` | `dd/MM/yyyy hh:mm a`      | One button for each picker      |
| `DateRangeField` | `DateRange`     | `dd/MM/yyyy - dd/MM/yyyy` | The date picker, with a range   |

```java
DateField dateField = new DateField();
dateField.setSelectedDate(LocalDate.now());
dateField.addChangeListener(e -> {
    LocalDate date = dateField.getSelectedDate();
});

TimeField timeField = new TimeField();
LocalTime time = timeField.getSelectedTime();

DateTimeField dateTimeField = new DateTimeField();
LocalDateTime dateTime = dateTimeField.getSelectedDateTime();
```

### Typing

The field has a segment for each part of its pattern: the day, the month, the year, the hour and so on. One
segment is selected at a time, and it shows its placeholder (`dd`, `mm`, `yyyy`) until it has a value.

| Key                    | What it does                                                                |
|------------------------|-----------------------------------------------------------------------------|
| Digits                 | Type the value of the selected segment. When it is full, the next one is selected |
| Left, Right            | Select the segment before or after                                          |
| Home, End              | Select the first or the last segment                                        |
| Up, Down               | Make the value larger or smaller. A segment without value gets the value of now |
| Backspace, Delete      | Remove the value of the segment. Backspace on an empty segment goes back    |
| `/`, `:`, `-`, space   | A character of the pattern goes to the next segment                         |
| `A`, `P`               | Select AM or PM                                                             |
| Alt + Down, F4         | Show the popup                                                              |
| Ctrl + C, X, V         | Copy, cut and paste. A text that is pasted is read with the pattern         |

A click selects the segment under the mouse. A year that is typed with one or two digits is a year of this
century: `26` is 2026.

### Value

The field has a value when all its segments have one, they make a date that exists, and its
[validator](#validation) has no error. While it is filled in part, the value is `null`. If every segment has
a value but there is no such date, as the 31 of April, the value is `null` and the field has the error
outline of the look and feel.

| Method                         | What it does                                                             |
|--------------------------------|--------------------------------------------------------------------------|
| `getSelectedDate()`            | `DateField`: the date, or null                                           |
| `setSelectedDate(LocalDate)`   | `DateField`: sets the date, null clears the field                        |
| `getSelectedTime()`            | `TimeField`: the time, or null                                           |
| `setSelectedTime(LocalTime)`   | `TimeField`: sets the time, null clears the field                        |
| `getSelectedDateTime()`        | `DateTimeField`: the date and the time, or null                          |
| `setSelectedDateTime(LocalDateTime)` | `DateTimeField`: sets both, null clears the field                  |
| `getSelectedDateRange()`       | `DateRangeField`: the range, or null                                     |
| `setSelectedDateRange(DateRange)` | `DateRangeField`: sets the range, null clears the field. Also with the two dates |
| `clear()`                      | Removes what is typed                                                    |
| `isInputEmpty()`               | True if nothing is typed                                                 |
| `isInputValid()`               | True if the field is empty or has a value, false while it is filled in part |
| `getInputText()`               | The text as it is shown, with the placeholders                           |
| `addChangeListener(listener)`  | Listens to the value, with a `ChangeListener`                            |
| `removeChangeListener(listener)` | Removes the listener                                                   |
| `getOption()`                  | A copy of the option of the field                                        |
| `setOption(FieldOption)`       | Changes the option, the value stays                                      |

### Popup of a field

The button of the field shows its picker below the field. A date or a time that is selected there goes into
the field, and the popup closes. What is typed in the field is selected in the picker.

| Method              | What it does                                          |
|---------------------|-------------------------------------------------------|
| `showPopup()`       | `DateField`, `TimeField` and `DateRangeField`: shows the picker |
| `showDatePopup()`   | `DateTimeField`: shows the date picker                |
| `showTimePopup()`   | `DateTimeField`: shows the time picker                |
| `closePopup()`      | Closes the popup                                      |
| `isPopupVisible()`  | True if the popup is showing                          |

### Date range field

`DateRangeField` has the first and the last date of a range in one field, with a separator between them.
Its popup has the date picker that selects a range.

```java
DateRangeField rangeField = new DateRangeField();
rangeField.setSelectedDateRange(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 16));
rangeField.addChangeListener(e -> {
    DateRange range = rangeField.getSelectedDateRange();
});
```

The pattern of the option is the pattern of one date, the two dates have the same. The text between them is
`setRangeSeparator` of the option:

```java
new DateRangeField(new FieldOption()
        .setPattern("dd MMM yyyy")
        .setRangeSeparator(" to "));
```

The field has a value when both dates are typed and the last date is not before the first. A range that
ends before it starts has no value, and the field has the error outline. In the popup, the field gets the
range when its second date is clicked.

### Validation

A validator checks the value of a field and says what the field shows: an error, a warning or a success.
Each field has `setValidator` with the type of its value.

```java
dateField.setValidator(date -> {
    if (date == null) {
        return ValidationResult.error("A date is required.");
    }
    if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
        return ValidationResult.error("Sunday can not be selected.");
    }
    if (date.isBefore(LocalDate.now())) {
        return ValidationResult.warning("The date is in the past.");
    }
    return null;
});
```

| Result                              | Border | Value of the field                    |
|-------------------------------------|--------|---------------------------------------|
| `ValidationResult.error(message)`   | Red    | `null`, the value is not allowed      |
| `ValidationResult.warning(message)` | Yellow | The value                             |
| `ValidationResult.success(message)` | Green  | The value                             |
| `null`                              | Normal | The value, nothing is shown           |

The field shows an icon with the color of the result, the message is its tool tip. The colors are the ones
of the look and feel. A `ValidationResult` has `getSeverity()` (`ERROR`, `WARNING` or `SUCCESS`),
`getMessage()` and `isError()`.

The validator is called each time the field has another value. A field that is empty or filled in part is not
checked by itself, so a form does not show errors before it is used. Call `validateInput()` to check the field
also then, for example when the form is saved: the validator gets `null` and can say that a value is required.

```java
if (dateField.validateInput()) {
    LocalDate date = dateField.getSelectedDate();
}
```

| Method                  | What it does                                                                   |
|-------------------------|--------------------------------------------------------------------------------|
| `setValidator(validator)` | Sets the validator, null for none                                            |
| `getValidator()`        | The validator, or null                                                         |
| `validateInput()`       | Checks the field now, also if it is empty. True if the field can be used       |
| `isInputValid()`        | True if the field is empty or has a value, and the validator has no error      |
| `getValidationResult()` | What the validator said, or null                                               |

In the popup of a `DateField` and a `TimeField`, the dates and the times the validator has an error for can
not be selected. The validator of a `DateTimeField` gets the date and the time together, and the one of
a `DateRangeField` the whole range, so the pickers of their popup do not know it: set what can be selected
there with `setSelectable` of the date option and the time option of the field.

### Commit mode

The commit mode says what the field does with what is typed when it loses the focus and has no value: it is
filled in part, has a date that does not exist, or its validator has an error.

```java
new DateField(new FieldOption().setCommitMode(CommitMode.REVERT));
```

| `CommitMode` | What it does                                                               |
|--------------|----------------------------------------------------------------------------|
| `KEEP`       | What is typed stays in the field, and the field has no value. The default  |
| `REVERT`     | The field goes back to the value it had before it was edited, or is cleared if it had none |
| `CLEAR`      | The field is cleared                                                       |

## Field options

`FieldOption` sets the pattern and the buttons of a field, and the options of the pickers in its popup.

```java
FieldOption option = new FieldOption()
        .setPattern("EEEE dd MMMM yyyy")
        .setShowClearButton(true);
option.getDateOption()
        .setFirstDayOfWeek(DayOfWeek.MONDAY)
        .setSize(PickerSize.SMALL);

DateField dateField = new DateField(option);
```

| Setter                         | Default | What it does                                                         |
|--------------------------------|---------|----------------------------------------------------------------------|
| `setPattern(String)`           | `null`  | What the field shows and what is typed, null for the default pattern |
| `setRangeSeparator(String)`    | `" - "` | The text between the two dates of a `DateRangeField`                 |
| `setLocale(Locale)`            | `null`  | The language of the names and of AM and PM, in the field and in the popup |
| `setShowPickerButton(boolean)` | `true`  | False hides the button of the popup, the keyboard still shows it     |
| `setShowClearButton(boolean)`  | `false` | Shows a button that removes what is typed, while the field is not empty |
| `setStableWidth(boolean)`      | `false` | True makes a field with the name of a month or a day as wide as its longest name, so it keeps its width. False makes it as wide as the name that is shown |
| `setCommitMode(CommitMode)`    | `KEEP`  | What happens to what is typed when the field loses the focus without a value, see [Commit mode](#commit-mode) |
| `setDateOption(DateOption)`    |         | The option of the date picker in the popup                           |
| `setTimeOption(TimeOption)`    |         | The option of the time picker in the popup                           |

The two picker options close the popup on select by default. The field sets three things of them itself: the
date picker selects one date, or a range in a `DateRangeField`, the clock has 12 or 24 hours as the pattern
has, and the locale is the one of the field option.

### Pattern

The pattern has the letters of `DateTimeFormatter`.

| Letter         | What it is                                   | Example        |
|----------------|----------------------------------------------|----------------|
| `d`, `dd`      | The day                                      | `3`, `03`      |
| `M`, `MM`      | The month as a number                        | `7`, `07`      |
| `MMM`, `MMMM`  | The short name and the name of the month     | `Jul`, `July`  |
| `yy`, `yyyy`   | The year with two or four digits             | `26`, `2026`   |
| `H`, `HH`      | The hour from 0 to 23                        | `21`           |
| `h`, `hh`      | The hour from 1 to 12, use it with `a`       | `09`           |
| `m`, `mm`      | The minute                                   | `05`           |
| `s`, `ss`      | The second                                   | `30`           |
| `a`            | AM or PM                                     | `PM`           |
| `E`, `EEEE`    | The short name and the name of the day. It is shown, not typed | `Sat`, `Saturday` |

All other characters are shown as they are. Letters that are a text go between two `'`:

```java
new FieldOption().setPattern("dd 'of' MMMM, yyyy");
new FieldOption().setPattern("HH:mm:ss");
new FieldOption().setPattern("yyyy-MM-dd");
```

A month with a name is typed as its number, or changed with the up and down keys. A pattern can have each
part one time. A `DateField` and a `DateRangeField` can not have a time in their pattern, a `TimeField` no
date, and a `DateTimeField` needs both: the constructor and `setOption` throw `IllegalArgumentException`
if not.

## Size

Both pickers have four sizes. The size sets how large the cells, the clock and the text are.

```java
DatePicker datePicker = new DatePicker(new DateOption().setSize(PickerSize.SMALL));
TimePicker timePicker = new TimePicker(new TimeOption().setSize(PickerSize.LARGE));
```

| `PickerSize` | What it is                          |
|--------------|-------------------------------------|
| `SMALL`      | The smallest, for a place with little space |
| `DEFAULT`    | The size if no other size is set    |
| `MEDIUM`     | Larger than the default             |
| `LARGE`      | The largest                         |

## Style

`StyleOption` sets how a picker looks. Get it from the option of the picker. Both pickers have it, and the
same style can be given to both.

```java
DateOption option = new DateOption();
option.getStyleOption()
        .setColor(new Color(0x16A34A))
        .setSelectionRound(8)
        .setWeekendColor(new Color(0xEF4444));

TimeOption timeOption = new TimeOption()
        .setStyleOption(option.getStyleOption());
```

| Setter                        | Default | Picker | What it does                                                   |
|-------------------------------|---------|--------|----------------------------------------------------------------|
| `setColor(Color)`             | `null`  | Both   | The color of what is selected, null for the accent color of the look and feel |
| `setBackground(Color)`        | `null`  | Both   | The background of the picker, null for the one of the look and feel |
| `setPadding(int)`             | `10`    | Both   | The space between the edge of the picker and its content       |
| `setPadding(Insets)`          |         | Both   | The same, for each side                                        |
| `setSelectionRound(int)`      | `999`   | Date   | The corner arc of the selected day, month and year: 0 for square, a large value for a circle |
| `setWeekendColor(Color)`      | `null`  | Date   | The color of the Saturdays and the Sundays and of their names  |
| `setShowOutsideDays(boolean)` | `true`  | Date   | False leaves the days of the months before and after out       |
| `setShowToday(boolean)`       | `true`  | Date   | False shows today, this month and this year without the outline |
| `setClockBackground(Color)`   | `null`  | Time   | The color of the face of the clock, null for a shade of the background |

The color is used for the selected day and the band of a range, the outline of today, the hand of the clock
and the selected hour and minute. The text on it is white or dark, as the color needs.

A field has no style of its own: it is a text field of the look and feel. Its popup has the style of the
date option and the time option of the field:

```java
FieldOption fieldOption = new FieldOption();
fieldOption.getDateOption().getStyleOption().setColor(new Color(0x16A34A));
```

## Popup

Both pickers can be shown in a popup at a component, for example a button.

```java
DatePicker datePicker = new DatePicker(new DateOption().setCloseOnSelect(true));
datePicker.addDateSelectionListener(e -> button.setText(String.valueOf(e.getDate())));

button.addActionListener(e -> datePicker.showPopup(button));
```

| Method                 | What it does                                                               |
|------------------------|----------------------------------------------------------------------------|
| `showPopup(Component)` | Shows the picker below the component, or above it if there is no space below |
| `closePopup()`         | Closes the popup                                                           |
| `isPopupVisible()`     | True if the popup is showing                                               |

The popup closes when the user clicks outside of it. A date picker opens at the selected date, or at this
month. A time picker opens at the hours.

A picker is used in a popup or in a container, not in both: make one picker for each.
`showPopup` throws `IllegalArgumentException` if the component is not showing.

## Animation

The date picker slides to another month and to the months and the years. The hand of the clock of the time
picker turns to its place. The animation is on by default. Get the `AnimationOption` from the option.

```java
DateOption option = new DateOption();
option.getAnimationOption()
        .setEnabled(true)
        .setDuration(400);
```

| Setter                | Default | What it does                                               |
|-----------------------|---------|------------------------------------------------------------|
| `setEnabled(boolean)` | `true`  | False to change the picker without animation               |
| `setDuration(int)`    | `300`   | The time in milliseconds of an animation, 0 for no animation |

The animation is also off when the system property `flatlaf.animation` is `false`.

## Default option

A picker created without an option uses the default option.

```java
DatePicker.getDefaultOption()
        .setFirstDayOfWeek(DayOfWeek.MONDAY);

TimePicker.getDefaultOption()
        .setHour24(true);
```

`DatePicker.setDefaultOption(option)` and `TimePicker.setDefaultOption(option)` change it for all the pickers
that are created later. `createOption()` gives a copy of the default option, to change for one picker.

The fields have one default option together: `PickerField.getDefaultOption()`,
`PickerField.setDefaultOption(option)` and `PickerField.createOption()`.
