# SwingCraft4j

Java Swing components built on the [FlatLaf](https://github.com/JFormDesigner/FlatLaf) look and feel, for desktop applications.

| Module                | What it gives                                                          | Docs                         |
|-----------------------|------------------------------------------------------------------------|------------------------------|
| `swingcraft-modal`    | Modal dialogs shown inside the window, with animation and a back stack | [Modal](docs/modal.md)       |
| `swingcraft-toast`    | Toast notifications, also for background tasks                         | [Toast](docs/toast.md)       |
| `swingcraft-datetime` | A date picker with a date range, a time picker, and fields to type them | [Datetime](docs/datetime.md) |

Website: https://www.swingcraft4j.com

## Requirements

- Java 8 or later
- A FlatLaf look and feel set up before the components are shown

Each module is published on its own. See [Modal](docs/modal.md), [Toast](docs/toast.md) and
[Datetime](docs/datetime.md) for the installation and the usage.

## Demo

The `swingcraft-demo` module is a demo of all the options. It is not published.

```
mvn install
```

Then run `com.swingcraft4j.demo.Demo` from your IDE.

## License

[MIT](LICENSE)
