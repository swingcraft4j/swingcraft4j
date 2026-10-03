# Toast

Toast notifications, also for background tasks.

- [Installation](#installation)
- [Show a toast](#show-a-toast)
- [Control a toast](#control-a-toast)
- [Toast for a background task](#toast-for-a-background-task)
- [Your own component](#your-own-component)
- [Options](#options)
- [Animation](#animation)
- [Style](#style)
- [Listener](#listener)
- [Default option](#default-option)

## Installation

```xml
<dependency>
    <groupId>com.swingcraft4j</groupId>
    <artifactId>swingcraft-toast</artifactId>
    <version>1.0.0</version>
</dependency>
```

FlatLaf and MigLayout come in with the module. Set up a FlatLaf look and feel before a toast is shown.

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
    <artifactId>swingcraft-toast</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

All the methods of `JToast` must be called on the event dispatch thread.

## Show a toast

The owner is the window, or any component inside it.

```java
JToast.show(owner, ToastType.SUCCESS, "Your changes have been saved.");
JToast.show(owner, ToastType.ERROR, "The file was not found.", ToastLocation.BOTTOM_TRAILING);
JToast.show(owner, ToastType.INFO, "A new version is ready.", option);
```

`show` returns a [`ToastController`](#control-a-toast). It throws `IllegalArgumentException` if the owner is
not in a window.

The type sets the color, the icon and the label of the toast.

| `ToastType` | Label   | What it is for                                                                  |
|-------------|---------|---------------------------------------------------------------------------------|
| `DEFAULT`   | Message | A plain message                                                                 |
| `SUCCESS`   | Success |                                                                                 |
| `INFO`      | Info    |                                                                                 |
| `WARNING`   | Warning |                                                                                 |
| `ERROR`     | Error   |                                                                                 |
| `LOADING`   | Loading | Something is running: the icon turns, and the toast does not close by itself    |

## Control a toast

| Method                  | What it does                                                                  |
|-------------------------|-------------------------------------------------------------------------------|
| `close()`               | Closes the toast                                                              |
| `closeImmediately()`    | Closes it without animation, also when it is closing with an animation        |
| `isOpen()`              | True from the moment the toast is shown until it starts to close              |
| `getId()`               | The id of the toast, to use with `JToast.close(id)` and `JToast.isOpen(id)`   |
| `getType()`             | The type                                                                      |
| `getMessage()`          | The message, or null for a toast with your own component                      |
| `setMessage(String)`    | Changes the message                                                           |
| `setType(ToastType)`    | Changes the type                                                              |
| `update(type, message)` | Changes both                                                                  |
| `getCount()`            | How often the toast was shown, see `setGroupRepeated`                         |

### Change a toast that is open

```java
ToastController toast = JToast.show(owner, ToastType.LOADING, "Saving your changes...");
// later, on the event dispatch thread
toast.update(ToastType.SUCCESS, "Your changes have been saved.");
```

The toast gets its new size at its place, and the delay to close it starts again. It does nothing if the
toast is not open any more.

### Close

```java
String id = toast.getId();

JToast.isOpen(id);
JToast.close(id);
JToast.closeImmediately(id);

JToast.closeAll();
JToast.closeAll(ToastLocation.TOP_CENTER);
JToast.closeAllImmediately();
JToast.closeAllImmediately(ToastLocation.TOP_CENTER);
```

## Toast for a background task

`showTask` runs the work on another thread. The toast is a `LOADING` toast with the message while the work
runs, then the same toast shows the result. The task must not use Swing components.

```java
JToast.showTask(owner, "Uploading the file...", progress -> {
    upload();
    progress.setMessage("Checking the file...");
    return "report.pdf";
}, name -> "The file " + name + " has been uploaded.", error -> "The upload failed: " + error.getMessage());
```

| Parameter   | What it is                                                                                       |
|-------------|--------------------------------------------------------------------------------------------------|
| `message`   | The message while the task runs                                                                  |
| `task`      | The work. It gets a `ToastProgress` and returns the result                                       |
| `onSuccess` | Makes the message of the `SUCCESS` toast from the result. Null, or a null message, closes the toast |
| `onFailure` | Makes the message of the `ERROR` toast from the exception. Null shows the message of the exception, a null message closes the toast |
| `option`    | The option, the default option if it is left out                                                 |

The task can use its `ToastProgress` from any thread:

| Method               | What it does                                                    |
|----------------------|-----------------------------------------------------------------|
| `setMessage(String)` | Changes the message of the toast, for the next step of the work |
| `isCancelled()`      | True if the user has cancelled the work                         |

### Cancel

With `setCancellable(true)` the toast has a cancel button while the task runs. It interrupts the thread of
the task and closes the toast. Nothing is shown for a task that was cancelled.

```java
ToastOption option = JToast.createOption().setCancellable(true);

JToast.showTask(owner, "Importing...", progress -> {
    for (File file : files) {
        if (progress.isCancelled()) {
            return 0;
        }
        importFile(file);
    }
    return files.size();
}, count -> count + " files imported.", null, option);
```

Work that waits ends by itself when its thread is interrupted. Work that does not wait should look at
`isCancelled()` from time to time, and stop.

If the user closes the toast in another way, the task goes on and its result is shown in a new toast.

### A future

`showFuture` does the same for work that has its own thread or executor.

```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> download());

JToast.showFuture(owner, "Downloading...", future, name -> name + " is ready.", null);
```

The cancel button cancels the future. That does not stop work that is running already, unless the future
does that itself.

## Your own component

`showCustom` shows a toast with your component in place of the icon, the message and the close button.

```java
JPanel panel = new JPanel();
JButton button = new JButton("Undo");
button.addActionListener(e -> {
    ToastController toast = JToast.getController(button);
    if (toast != null) {
        toast.close();
    }
});
panel.add(new JLabel("The file was deleted."));
panel.add(button);

JToast.showCustom(owner, panel, new ToastOption().setAutoClose(false));
```

`JToast.getController(component)` gives the controller of the toast the component is in, or null. `showCustom`
throws `IllegalStateException` if the component is showing already.

## Options

`ToastOption` sets where a toast is placed, how the toasts are arranged and when a toast closes. The setters
return the option, so they can be chained. The option is copied when the toast is shown: changing it later
does not change the toasts that are showing. All sizes are scaled with the UI scale factor.

```java
ToastOption option = JToast.createOption()
        .setLocation(ToastLocation.BOTTOM_TRAILING)
        .setLayoutType(ToastLayoutType.STACK)
        .setStackExpandOnHover(true)
        .setDelay(5000)
        .setCloseOnClick(true);

JToast.show(owner, ToastType.INFO, "A new version is ready.", option);
```

### Layout

| Setter                               | Default      | What it does                                                    |
|--------------------------------------|--------------|-----------------------------------------------------------------|
| `setSurface(Surface)`                | `LAYER`      | What the toast is shown on, see below                           |
| `setLocation(ToastLocation)`         | `TOP_CENTER` | Where the toasts are placed in the window                       |
| `setMargin(int)`                     | `10`         | The space between the window edges and the toasts               |
| `setMargin(top, left, bottom, right)`|              | The same, for each side                                         |
| `setLayoutType(ToastLayoutType)`     | `LIST`       | How the toasts at the same location are arranged, see below     |
| `setGap(int)`                        | `10`         | The space between the toasts of a list, and of an expanded stack|
| `setRelativeToOwner(boolean)`        | `false`      | Places the toast in the owner component, not the whole window   |

**Location.** `TOP_LEADING`, `TOP_CENTER`, `TOP_TRAILING`, `BOTTOM_LEADING`, `BOTTOM_CENTER` or
`BOTTOM_TRAILING`. Leading and trailing follow the component orientation.

**Layout type.**

| `ToastLayoutType` | What it does                                                                               |
|-------------------|--------------------------------------------------------------------------------------------|
| `LIST`            | The toasts are shown one after the other, with a gap between them                          |
| `STACK`           | The newest toast is in front. The older ones are behind it, and only their edge is visible |
| `REPLACE`         | One toast at a time: a new toast takes the place of the one that is showing                |
| `BANNER`          | As a list, but each toast is as wide as the window, without the margin                     |

For a banner from edge to edge, set the margin and the round of the style to 0.

The toasts with the same location and layout type are arranged together. They should use the same margin,
gap and stack options: these are taken from the newest toast.

**Surface.**

| `Surface` | What it does                                                                                           |
|-----------|--------------------------------------------------------------------------------------------------------|
| `LAYER`   | The toast is shown inside the window of the owner. The toasts that do not fit in the window are cut off, and they are behind heavyweight components such as an embedded browser |
| `WINDOW`  | The toast is shown in a window of its own. It is over heavyweight components, and it is not limited by the window of the owner |

The window of a toast never gets the focus, so a component in your own toast can be used with the mouse but
not with the keyboard. The toasts at the same location should use the same surface. Where the system can not
show a transparent window, the toast is shown in a `LAYER`.

**Relative to owner.** The toast follows the owner when it moves, hides while the owner is hidden, and closes
when the owner is removed from the window.

**Order.** In a list the newest toast is at the edge of the window. `JToast.setReverseOrder(true)` shows the
oldest toast at the edge and the newest after the others.

### List

| Setter                          | Default | What it does                                                          |
|---------------------------------|---------|-----------------------------------------------------------------------|
| `setListMaxVisible(int)`        | `0`     | How many toasts of a list or a banner are shown at once, 0 for no limit |
| `setListOverflow(ListOverflow)` | `WAIT`  | What happens with a toast over the limit                              |

| `ListOverflow` | What it does                                                                               |
|----------------|--------------------------------------------------------------------------------------------|
| `WAIT`         | The new toast waits, and is shown when a toast closes. Its delay starts when it is shown   |
| `CLOSE_OLDEST` | The new toast is shown at once, and the oldest toast is closed                             |

### Stack

| Setter                           | Default | What it does                                                        |
|----------------------------------|---------|---------------------------------------------------------------------|
| `setStackOffset(int)`            | `10`    | How far the edge of each older toast is visible behind the one in front |
| `setStackMaxVisible(int)`        | `3`     | How many toasts are visible. The older ones are hidden              |
| `setStackExpandOnHover(boolean)` | `false` | Shows all the toasts one after the other while the mouse is over the stack |

### Closing

| Setter                      | Default | What it does                                                               |
|-----------------------------|---------|----------------------------------------------------------------------------|
| `setAutoClose(boolean)`     | `true`  | False keeps the toast until the user or the application closes it          |
| `setDelay(int)`             | `3000`  | The time in milliseconds the toast is shown before it closes itself        |
| `setPauseOnHover(boolean)`  | `true`  | The toast does not close while the mouse is over it                        |
| `setCloseOnClick(boolean)`  | `false` | A click on the toast closes it                                             |
| `setGroupRepeated(boolean)` | `false` | A message that is showing already is not shown again, see below            |
| `setCancellable(boolean)`   | `false` | The toast of a task has a cancel button, see [Cancel](#cancel)             |

**Pause on hover.** The delay starts again when the mouse leaves. In a stack the mouse over one toast pauses
all of them.

**Group repeated.** A toast with the same type and message, at the same location with the same layout type,
is not shown again. The toast that is showing gets a count of how often it was shown, and its delay starts
again. `show` then returns the controller of the toast that is showing. Not for a toast with your own
component.

## Animation

`AnimationOption` sets how a toast is shown and closed. The other toasts at the same location move with it.
Get it from the option.

```java
option.getAnimationOption()
        .setDuration(400)
        .setEasing(Easing.EASE_OUT)
        .setDirection(ToastDirection.TRAILING_TO_LEADING);
```

| Setter                         | Default    | What it does                                                         |
|--------------------------------|------------|----------------------------------------------------------------------|
| `setEnabled(boolean)`          | `true`     | False for no animation at all                                        |
| `setDuration(int)`             | `350`      | The time in milliseconds to show or close a toast and to expand a stack |
| `setOpenDuration(int)`         | `-1`       | The time to show the toast, -1 to use the duration                   |
| `setCloseDuration(int)`        | `-1`       | The time to close the toast, -1 to use the duration                  |
| `setEasing(Easing)`            | `STANDARD` | How the speed changes over the time                                  |
| `setFade(boolean)`             | `true`     | Fades the toast in and out                                           |
| `setDirection(ToastDirection)` | `AUTO`     | The direction the toast moves in when it is shown                    |

**Easing.**

| `Easing`      | What it does                 |
|---------------|------------------------------|
| `LINEAR`      | The same speed all the time  |
| `STANDARD`    | Starts fast and ends slow    |
| `EASE_IN`     | Starts slow                  |
| `EASE_OUT`    | Ends slow                    |
| `EASE_IN_OUT` | Starts and ends slow         |

`Easing.cubicBezier(x1, y1, x2, y2)` makes an easing from a curve, as `cubic-bezier()` in CSS. `Easing` is an
interface with one method, so a lambda works too:

```java
option.getAnimationOption().setEasing(fraction -> fraction * fraction);
```

**Direction.**

| `ToastDirection`      | What it does                                                                         |
|-----------------------|--------------------------------------------------------------------------------------|
| `AUTO`                | From the edge of the window the toast is at: the top or the bottom for a location in the center, the side for a leading or trailing location. In a stack always the top or the bottom |
| `TOP_TO_BOTTOM`       |                                                                                      |
| `BOTTOM_TO_TOP`       |                                                                                      |
| `LEADING_TO_TRAILING` |                                                                                      |
| `TRAILING_TO_LEADING` |                                                                                      |
| `NONE`                | The toast does not move                                                              |

The toast moves back when it is closed. A fade animates an image of the toast, and the real components are
painted again when the animation ends. The animation is also off when the system property `flatlaf.animation`
is `false`.

## Style

`StyleOption` sets how a toast looks. Get it from the option.

```java
option.getStyleOption()
        .setBorderType(BorderType.LEADING_LINE)
        .setBackgroundType(BackgroundType.TINT)
        .setShowLabel(true)
        .setShowProgressLine(true);
```

### Border

| Setter                    | Default   | What it does                                                         |
|---------------------------|-----------|----------------------------------------------------------------------|
| `setBorderType(BorderType)`| `DEFAULT`| The line painted in the color of the type, see below                 |
| `setLineSize(int)`        | `3`       | The width of that line                                               |
| `setRound(int)`           | `10`      | The corner arc, 0 for square corners                                 |
| `setShadow(Shadow)`       | `MEDIUM`  | `NONE`, `SMALL`, `MEDIUM`, `LARGE` or `EXTRA_LARGE`                  |
| `setShadowSize(int)`      |           | The space the shadow takes, the same on each side                    |
| `setShadowSize(Insets)`   |           | The space on each side. A larger bottom moves the shadow down        |
| `setShadowColor(Color)`   | `null`    | Null for the popup shadow color of the look and feel                 |
| `setShadowOpacity(float)` | `-1`      | From 0 to 1. -1 for the default, stronger with a dark look and feel  |

The border type is `DEFAULT` (no line), `OUTLINE` (a line around the toast), `LEADING_LINE`, `TRAILING_LINE`,
`TOP_LINE` or `BOTTOM_LINE`.

### Background

| Setter                              | Default   | What it does                                                 |
|-------------------------------------|-----------|--------------------------------------------------------------|
| `setBackgroundType(BackgroundType)` | `DEFAULT` | How the background uses the color of the type                |
| `setColor(Color)`                   | `null`    | The color of the toast in place of the color of its type     |

The background type is `DEFAULT` (the color is not used), `TINT` (the background is tinted with the color)
or `GRADIENT` (the color fades out from the leading side).

### Content

| Setter                         | Default | What it does                                                            |
|--------------------------------|---------|-------------------------------------------------------------------------|
| `setPadding(int)`              | `5`     | The space between the edge of the toast and its content                 |
| `setPadding(Insets)`           |         | The same, for each side                                                 |
| `setMaxWidth(int)`             | `0`     | The largest width of a toast with a message, 0 for no limit             |
| `setShowIcon(boolean)`         | `true`  | Shows the icon                                                          |
| `setIcon(Icon)`                | `null`  | The icon in place of the icon of the type                               |
| `setIconSeparateLine(boolean)` | `false` | Shows a line between the icon and the text                              |
| `setShowLabel(boolean)`        | `false` | Shows a label above the message, the name of the type                   |
| `setLabel(String)`             | `null`  | The text of the label in place of the name of the type                  |
| `setShowCloseButton(boolean)`  | `true`  | Shows the close button                                                  |
| `setPaintTextColor(boolean)`   | `false` | Paints the message in the color of the type                             |

A message that is longer than the max width goes on at the next line. The max width is not used for a banner
and for a toast with your own component.

### Progress line

A line at an edge of a toast that closes by itself: it gets shorter as the delay passes.

| Setter                            | Default  | What it does                                             |
|-----------------------------------|----------|----------------------------------------------------------|
| `setShowProgressLine(boolean)`    | `false`  | Shows the line                                           |
| `setProgressLineSize(int)`        | `2`      | The height of the line                                   |
| `setProgressLinePosition(position)`| `BOTTOM`| `TOP` or `BOTTOM`                                        |
| `setProgressLineColor(Color)`     | `null`   | Null for the color of the toast                          |

The line is full again while the mouse is over the toast, as the delay starts again then.

## Listener

A `ToastListener` is told when a toast is clicked and when it has been closed. Implement the methods that are
needed.

```java
ToastOption option = JToast.createOption().addListener(new ToastListener() {
    @Override
    public void toastClicked(ToastController toast) {
        toast.close();
    }

    @Override
    public void toastClosed(ToastController toast) {
        // the toast has been removed
    }
});
```

## Default option

A toast shown without an option uses the default option.

```java
JToast.getDefaultOption()
        .setLocation(ToastLocation.BOTTOM_TRAILING)
        .setDelay(5000);

JToast.setDefaultOption(option);
```

`JToast.createOption()` gives a copy of the default option, to change for one toast.
