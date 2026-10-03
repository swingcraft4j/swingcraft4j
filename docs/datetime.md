# Datetime

A date picker and a time picker, with animation.

- [Installation](#installation)
- [Date picker](#date-picker)
- [Date range](#date-range)
- [Date options](#date-options)
- [Time picker](#time-picker)
- [Time options](#time-options)
- [Size](#size)
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
| `setAnimationOption(option)`   |                  | How the picker is animated, see [Animation](#animation)         |

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

The listener gets a `TimeSelectionEvent` with `getTime()` for each hour and minute the hand is moved to, and
when the selection is cleared. An hour that is selected first has the minute 0.

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
| `setAnimationOption(option)` |         | How the picker is animated, see [Animation](#animation)            |

A clock with 24 hours has two circles: the hours from 1 to 12 outside, and from 13 to 0 inside.

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
