# Modal

Modal dialogs shown inside the window, with animation and a back stack.

- [Installation](#installation)
- [Show a modal](#show-a-modal)
- [SimpleModal](#simplemodal)
- [Custom modal](#custom-modal)
- [Control a modal](#control-a-modal)
- [Show a modal in place of another](#show-a-modal-in-place-of-another)
- [Options](#options)
- [Animation](#animation)
- [Default option](#default-option)

## Installation

```xml
<dependency>
    <groupId>com.swingcraft4j</groupId>
    <artifactId>swingcraft-modal</artifactId>
    <version>1.0.0</version>
</dependency>
```

FlatLaf and MigLayout come in with the module. Set up a FlatLaf look and feel before a modal is shown.

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
    <artifactId>swingcraft-modal</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

All the methods of `JModal` must be called on the event dispatch thread.

## Show a modal

A modal is a `Modal` panel shown over the window of its owner. The owner is the window, or any component
inside it.

```java
JModal.show(owner, modal);
JModal.show(owner, modal, "login");          // with an id
JModal.show(owner, modal, option);           // with an option
JModal.show(owner, modal, option, "login");  // with both
```

`show` returns a [`ModalController`](#control-a-modal). It throws:

- `IllegalArgumentException` if the owner is not in a window, or the id is used by a modal that is open
- `IllegalStateException` if the modal is showing already

## SimpleModal

`SimpleModal` is the default modal: a header with a title and a close button, your content, and the option
buttons below it.

```java
JPanel content = new JPanel();
content.add(new JLabel("Do you want to save the changes?"));

SimpleModal modal = new SimpleModal(content, "Save", SimpleModal.OptionType.YES_NO, e -> {
    if (e.getAction() == SimpleModal.YES_OPTION) {
        // save
    }
});
JModal.show(owner, modal);
```

### Buttons

| `OptionType`    | Buttons         | Action sent to the callback               |
|-----------------|-----------------|-------------------------------------------|
| `NONE`          | no button       |                                           |
| `CLOSE`         | Close           | `CLOSE_OPTION`                            |
| `OK_CANCEL`     | OK, Cancel      | `OK_OPTION`, `CANCEL_OPTION`              |
| `YES_NO`        | Yes, No         | `YES_OPTION`, `NO_OPTION`                 |
| `YES_NO_CANCEL` | Yes, No, Cancel | `YES_OPTION`, `NO_OPTION`, `CANCEL_OPTION`|

The close button of the header sends `CLOSE_OPTION`. `OK_OPTION` and `YES_OPTION` are the same value. The text
of the buttons is the text of `JOptionPane`, so it follows the language of the look and feel.

For your own buttons, give each one a text and an action:

```java
SimpleModal.Button[] buttons = {
        new SimpleModal.Button("Delete", 10, true),   // true paints it as the default button
        new SimpleModal.Button("Keep", 11)
};
SimpleModal modal = new SimpleModal(content, "Delete the file?", buttons, e -> {
    if (e.getAction() == 10) {
        // delete
    }
});
```

### Callback

The callback gets a `ModalEvent` when a button is pressed. The modal closes after the callback. A null
callback only closes the modal.

| Method         | What it does                                                    |
|----------------|-----------------------------------------------------------------|
| `getAction()`  | The action of the pressed button                                |
| `getModal()`   | The modal                                                       |
| `consume()`    | Keeps the modal open, for example when the input is not valid   |
| `isConsumed()` | True if `consume()` was called                                  |

```java
SimpleModal modal = new SimpleModal(content, "Name", SimpleModal.OptionType.OK_CANCEL, e -> {
    if (e.getAction() == SimpleModal.OK_OPTION && textName.getText().trim().isEmpty()) {
        e.consume();
    }
});
```

### Padding

`setPadding(top, left, bottom, right)` sets the space around the header, the content and the buttons. Set it
before the modal is shown.

### Change a part

Override a create method to change a part of the modal: `createTitle(String)`, `createCloseButton()`,
`createBackButton()` and `createButton(Button)`.

```java
SimpleModal modal = new SimpleModal(content, "Title") {
    @Override
    protected JComponent createTitle(String title) {
        JLabel label = new JLabel(title);
        label.setIcon(icon);
        return label;
    }
};
```

## Custom modal

Extend `Modal` and add your components to it, as to a `JPanel`.

```java
public class LoginModal extends Modal {

    public LoginModal() {
        setLayout(new MigLayout("wrap,fillx,insets 20", "[fill,250]"));
        JButton button = new JButton("Login");
        button.addActionListener(e -> closeModal());
        add(new JLabel("User name"));
        add(new JTextField());
        add(button);
    }

    @Override
    public void modalOpened() {
        // the modal is visible now
    }
}
```

| Method               | What it does                                                                          |
|----------------------|---------------------------------------------------------------------------------------|
| `installComponent()` | Called once before the modal is shown the first time, to create the components there  |
| `modalOpened()`      | Called each time the modal becomes the visible one: shown, pushed, or back after a pop |
| `modalClosed()`      | Called each time the modal has been closed or popped                                  |
| `closeModal()`       | Closes the modal                                                                      |
| `pushModal(Modal)`   | Shows another modal in place of this one                                              |
| `popModal()`         | Goes back to the modal that pushed this one                                           |
| `getController()`    | The controller, or null if the modal is not showing                                   |

The background color of the modal fills its round shape, change it with `setBackground(Color)`.

## Control a modal

`show` returns a `ModalController`, and a modal gives its own with `getController()`.

| Method               | What it does                                                               |
|----------------------|----------------------------------------------------------------------------|
| `close()`            | Closes the modal, with every modal that was pushed                         |
| `closeImmediately()` | Closes it without animation, also when it is closing with an animation     |
| `push(Modal)`        | Shows another modal in its place                                           |
| `pop()`              | Goes back to the modal before the last push                                |
| `canPop()`           | True if a modal was pushed                                                 |
| `isOpen()`           | True from the moment the modal is shown until it is closed                 |
| `getId()`            | The id given when the modal was shown, or null                             |

A modal that was shown with an id can be controlled from anywhere with `JModal`:

```java
JModal.show(owner, modal, "login");

JModal.isOpen("login");
JModal.close("login");
JModal.closeImmediately("login");
JModal.push("login", nextModal);
JModal.pop("login");
```

`close`, `closeImmediately` and `pop` do nothing if no modal with the id is open. `push` throws
`IllegalArgumentException` then.

```java
JModal.closeAll();
JModal.closeAllImmediately();
```

## Show a modal in place of another

A modal can show the next modal in its place, in the same position and with the same option. The next modal
slides in. The first modal is kept, and comes back with a pop.

```java
modal.pushModal(nextModal);
nextModal.popModal();
```

A `SimpleModal` that was pushed has a back button in its header. Closing the modal closes every modal that
was pushed too.

## Options

`ModalOption` sets the background, the layout and the border of a modal. The setters return the option, so
they can be chained. The option is copied when the modal is shown: changing it later does not change the
modals that are open. All sizes are scaled with the UI scale factor.

```java
ModalOption option = JModal.createOption()
        .setLocation(Location.TRAILING, Location.TOP)
        .setSize(0.4f, -1)
        .setBackgroundMode(BackgroundMode.BLOCK)
        .setBackgroundOpacity(0.3f)
        .setMovable(true)
        .setRound(20)
        .setShadow(Shadow.LARGE);

JModal.show(owner, modal, option);
```

### Background

| Setter                         | Default          | What it does                                                    |
|--------------------------------|------------------|-----------------------------------------------------------------|
| `setBackgroundMode(mode)`      | `CLOSE_ON_CLICK` | What the area around the modal does, see below                  |
| `setBackgroundColor(Color)`    | `null`           | The color painted over the window, null for black               |
| `setBackgroundOpacity(float)`  | `0.4f`           | From 0 (not visible, but still blocking) to 1                   |
| `setCloseOnEscape(boolean)`    | `true`           | The escape key closes the modal                                 |

| `BackgroundMode` | What it does                                                                                 |
|------------------|----------------------------------------------------------------------------------------------|
| `CLOSE_ON_CLICK` | The background is covered and blocked. A click on it closes the modal                        |
| `BLOCK`          | The background is covered and blocked. A click on it does nothing                            |
| `NONE`           | No background. The components behind the modal can be used, and the modal stays open         |
| `POPUP`          | No background. A click outside closes the modal, and still reaches the component behind it   |

### Layout

| Setter                              | Default            | What it does                                                  |
|-------------------------------------|--------------------|---------------------------------------------------------------|
| `setSurface(Surface)`               | `LAYER`            | What the modal is shown on, see below                         |
| `setLocation(horizontal, vertical)` | `CENTER`, `CENTER` | Where the modal is placed                                     |
| `setMargin(int)`                    | `15`               | The least space between the window edges and the modal        |
| `setMargin(top, left, bottom, right)`|                   | The same, for each side                                       |
| `setSize(width, height)`            | `-1`, `-1`         | The size of the modal, see below                              |
| `setRelativeToOwner(boolean)`       | `false`            | Shows the modal over the owner component, not the whole window|
| `setMovable(boolean)`               | `false`            | The user can drag the modal with the mouse                    |

**Location.** The horizontal location is `LEADING`, `LEFT`, `CENTER`, `RIGHT` or `TRAILING`. The vertical
location is `TOP`, `CENTER` or `BOTTOM`. Leading and trailing follow the component orientation.

**Size.** Each value of `setSize` is one of:

- `-1` for the preferred size of the modal
- a `float` such as `0.5f` for a part of the space that is there
- an `int` for a size in pixels

```java
option.setSize(-1, -1);      // preferred size
option.setSize(0.5f, 1f);    // half the width, the whole height
option.setSize(400, -1);     // 400 wide, preferred height
```

The modal does not get larger than the space that is there.

**Surface.**

| `Surface` | What it does                                                                                           |
|-----------|--------------------------------------------------------------------------------------------------------|
| `LAYER`   | The modal is shown inside the window of the owner. It can not be larger than the window, and it is behind heavyweight components such as an embedded browser |
| `WINDOW`  | The modal is shown in a window of its own. It is over heavyweight components, and it can be larger than the window of the owner |

While a `WINDOW` modal is open, every modal shown in the same window gets a window of its own too. Where the
system can not show a transparent window, the modal is shown in a `LAYER`.

**Relative to owner.** The background, the location, the size and the margin then apply to the visible area
of the owner. The modal follows the owner when it moves, hides while the owner is hidden, and closes when the
owner is removed from the window.

**Movable.** The modal can be dragged by any part that does not use the mouse itself, such as the title.

### Border

| Setter                     | Default  | What it does                                                         |
|----------------------------|----------|----------------------------------------------------------------------|
| `setRound(int)`            | `10`     | The corner arc, 0 for square corners                                 |
| `setShadow(Shadow)`        | `MEDIUM` | `NONE`, `SMALL`, `MEDIUM`, `LARGE` or `EXTRA_LARGE`                  |
| `setShadowSize(int)`       |          | The space the shadow takes, the same on each side                    |
| `setShadowSize(Insets)`    |          | The space on each side. A larger bottom moves the shadow down        |
| `setShadowColor(Color)`    | `null`   | Null for the popup shadow color of the look and feel                 |
| `setShadowOpacity(float)`  | `-1`     | From 0 to 1. -1 for the default, stronger with a dark look and feel  |
| `setBorderWidth(int)`      | `0`      | The width of the outline                                             |
| `setBorderColor(Color)`    | `null`   | Null for the border color of the look and feel                       |

## Animation

`AnimationOption` sets how a modal is shown, closed, pushed and popped. Get it from the option.

```java
option.getAnimationOption()
        .setDuration(300)
        .setOffset(0, -30)
        .setScale(0.1f);
```

| Setter                   | Default | What it does                                                                |
|--------------------------|---------|-----------------------------------------------------------------------------|
| `setEnabled(boolean)`    | `true`  | False for no animation at all                                               |
| `setDuration(int)`       | `200`   | The time in milliseconds to show or close the modal, 0 for no animation     |
| `setSlideDuration(int)`  | `300`   | The time in milliseconds to slide to the pushed or popped modal             |
| `setOffset(x, y)`        | `0, 20` | Where the modal starts, from its location. It moves from there to its place |
| `setFade(boolean)`       | `true`  | Fades the modal in and out                                                  |
| `setScale(float)`        | `0`     | How much smaller the modal starts, from 0 to less than 1                    |
| `setSnapshot(boolean)`   | `false` | Animates an image of the window behind the modal, see below                 |

**Offset.** A positive `x` is to the trailing side and a positive `y` is below. The default starts the modal
20 below its location.

**Scale.** With `0.1f` the modal grows from 90% to its size. The zoom is added to the offset, set the offset
to `0, 0` for a zoom only.

**Snapshot.** The window behind the modal is painted again for each frame while the background fades. That is
slow for a window with many components. With a snapshot the window is painted once and its image is animated:
faster, but the window behind does not change during the animation. A `WINDOW` modal does not need it.

**Images.** Fade, scale and snapshot animate an image of the modal, and the real components are painted
again when the animation ends. On a screen with a scale that is not a whole number (125%, 150%) the border
lines of the components can change a little then. Without the three, the real components are painted for
each frame.

The animation is also off when the system property `flatlaf.animation` is `false`.

## Default option

A modal shown without an option uses the default option.

```java
JModal.getDefaultOption()
        .setBackgroundMode(BackgroundMode.BLOCK)
        .setRound(20);

JModal.setDefaultOption(option);
```

`JModal.createOption()` gives a copy of the default option, to change for one modal.
