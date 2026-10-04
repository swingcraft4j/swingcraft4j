package com.swingcraft4j.datetime;

import com.formdev.flatlaf.FlatClientProperties;
import com.swingcraft4j.datetime.internal.PickerIcon;
import com.swingcraft4j.datetime.internal.SegmentEditor;
import com.swingcraft4j.datetime.internal.SegmentFieldUI;
import com.swingcraft4j.datetime.option.CommitMode;
import com.swingcraft4j.datetime.option.DateOption;
import com.swingcraft4j.datetime.option.DateSelectionMode;
import com.swingcraft4j.datetime.option.FieldOption;
import com.swingcraft4j.datetime.option.TimeOption;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultCaret;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * The base of the fields to type a date or a time: {@link JDateField}, {@link JTimeField},
 * {@link JDateTimeField} and {@link JDateRangeField}. The field has a segment for each part of its pattern,
 * as the day, the month and the year, and a button that shows a picker in a popup.
 * <p>
 * One segment is selected at a time. Digits are typed into it, the left and right keys select another
 * segment, the up and down keys change its value, and backspace and delete remove it. Alt and down, or F4,
 * shows the popup. A text that is pasted is read with the pattern of the field.
 * <p>
 * The field has a value when all its segments have one, they make a date that exists, and its validator
 * has no error for it. All methods must be called on the event dispatch thread.
 *
 * @param <T> the value of the field
 */
public abstract class JPickerField<T> extends JTextField {

    private static FieldOption defaultOption = new FieldOption();

    private FieldOption option;
    private SegmentEditor editor;
    // null if the field has no value: not all is typed, the date does not exist, or the validator has an error
    private T value;
    // the value to go back to: the one the field had when it got the focus, was set from the code or from
    // the popup. Not a value it has for a moment while it is typed, as the day 2 on the way to 25
    private T lastValue;
    private FieldValidator<T> validator;
    // what the validator said about what is typed, null if it said nothing or was not asked
    private ValidationResult validationResult;
    // what the validator was asked about last, it is asked again when the input is another one
    private T validatedInput;
    private boolean validated;
    // the icon that is shown for what the validator said
    private PickerIcon.Type statusIcon;
    // what the listeners were told last
    private T firedValue;
    // the preferred width when the field was laid out last
    private int preferredWidth;
    // the segment that was selected when the field lost the focus
    private int lastSelected = -1;
    // true while the field and a picker are set to the same value, so one does not answer the other
    private boolean syncing;
    private JDatePicker datePicker;
    private JTimePicker timePicker;

    private JToolBar toolBar;
    // the icon of what the validator said, its message is the tool tip
    private JLabel labelStatus;
    private JButton buttonClear;
    private JButton buttonDate;
    private JButton buttonTime;

    JPickerField(FieldOption option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not null");
        }
        // the field has no text: nothing is typed into its document, and its caret is not painted
        setDocument(new PlainDocument() {
            @Override
            public void insertString(int offset, String text, AttributeSet attributes) {
            }
        });
        setCaret(new DefaultCaret() {
            @Override
            public void paint(Graphics g) {
            }
        });
        installKeys();
        installButtons();
        applyOption(option.copy());
    }

    /**
     * @return the pattern of the field if its option has none
     */
    abstract String getDefaultPattern();

    /**
     * @throws IllegalArgumentException if the field can not be used with the pattern
     */
    abstract void checkPattern(SegmentEditor editor);

    /**
     * @return the value the segments make, or null if they make none: not all is typed, or it does not exist
     */
    abstract T read(SegmentEditor editor);

    /**
     * Sets the segments to the value.
     *
     * @param value the value, or null to remove what is typed
     */
    abstract void write(SegmentEditor editor, T value);

    /**
     * @return the segments of the field, for the pattern. A field for a range has them two times
     */
    SegmentEditor createEditor(String pattern, FieldOption option, Runnable listener) {
        return new SegmentEditor(pattern, option.getLocale(), listener);
    }

    /**
     * @return what the user can select in the date picker of the popup, from the validator of the field.
     * Null for every date
     */
    Predicate<LocalDate> getPopupDates() {
        return null;
    }

    /**
     * @return what the user can select in the time picker of the popup, from the validator of the field.
     * Null for every time
     */
    Predicate<LocalTime> getPopupTimes() {
        return null;
    }

    /**
     * @return what the date picker of the popup selects
     */
    DateSelectionMode getPopupSelectionMode() {
        return DateSelectionMode.SINGLE;
    }

    /**
     * The date picker of the popup shows what the field has.
     */
    void syncDatePicker(JDatePicker datePicker) {
        datePicker.setSelectedDate(editor.getDate());
    }

    /**
     * The user has selected something in the date picker of the popup, the field gets it.
     */
    void datePicked(DateSelectionEvent event) {
        editor.setDate(event.getDate());
    }

    // ---- option

    /**
     * @return a copy of the option of this field
     */
    public FieldOption getOption() {
        return option.copy();
    }

    /**
     * Changes the option. The value stays, as far as the new pattern has its parts.
     *
     * @param option the option, it is copied
     * @throws IllegalArgumentException if the pattern can not be used with this field
     */
    public void setOption(FieldOption option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not null");
        }
        applyOption(option.copy());
    }

    private void applyOption(FieldOption option) {
        String pattern = option.getPattern() != null ? option.getPattern() : getDefaultPattern();
        SegmentEditor newEditor = createEditor(pattern, option, this::editorChanged);
        newEditor.setStableWidth(option.isStableWidth());
        checkPattern(newEditor);
        resetPickers();
        T old = editor != null ? read(editor) : null;
        this.option = option;
        this.editor = newEditor;
        lastSelected = -1;
        if (old != null) {
            write(newEditor, old);
        }
        if (hasFocus()) {
            newEditor.selectFirst();
        }
        buttonDate.setVisible(option.isShowPickerButton() && newEditor.hasDate());
        buttonTime.setVisible(option.isShowPickerButton() && newEditor.hasTime());
        editorChanged();
        revalidate();
    }

    // ---- value

    SegmentEditor getEditor() {
        return editor;
    }

    /**
     * @return the value of the field, or null if it has none
     */
    T getValue() {
        return value;
    }

    /**
     * @param value the value, or null to clear the field
     */
    void setValue(T value) {
        write(editor, value);
        lastValue = this.value;
    }

    /**
     * Removes what is typed, the field shows its placeholders.
     */
    public void clear() {
        editor.clear();
        lastValue = null;
    }

    /**
     * @return true if nothing is typed
     */
    public boolean isInputEmpty() {
        return editor.isEmpty();
    }

    /**
     * @return true if the field is empty or has a value: false while it is filled in part, has a date that
     * does not exist, or its validator has an error
     */
    public boolean isInputValid() {
        if (validationResult != null && validationResult.isError()) {
            return false;
        }
        return editor.isEmpty() || read(editor) != null;
    }

    // ---- validation

    /**
     * Sets the validator of the field. It says what the field shows for its value: an error, a warning or
     * a success. The field has no value while its validator has an error for what is typed.
     *
     * @param validator the validator, or null for none
     */
    public void setValidator(FieldValidator<T> validator) {
        this.validator = validator;
        validated = false;
        resetPickers();
        editorChanged();
    }

    public FieldValidator<T> getValidator() {
        return validator;
    }

    /**
     * @return what the validator said about what is typed, or null if it said nothing, there is no validator
     * or the field has nothing to check
     */
    public ValidationResult getValidationResult() {
        return validationResult;
    }

    /**
     * Checks the field now, also if it is empty: the validator is called with null then, so it can tell
     * that the field must be filled in. Call it before the values of a form are used.
     *
     * @return true if the field can be used: it is empty or has a value, and the validator has no error
     */
    public boolean validateInput() {
        update(true);
        return isInputValid();
    }

    // true if the validator has no error for the value
    static boolean isAllowed(ValidationResult result) {
        return result == null || !result.isError();
    }

    /**
     * @return the text of the field as it is shown, with the placeholders of the segments without value
     */
    public String getInputText() {
        return editor.getText();
    }

    /**
     * Adds a listener that is told when the value of the field has changed.
     */
    public void addChangeListener(ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }

    // a segment or the selection has changed
    private void editorChanged() {
        update(false);
    }

    /**
     * Finds the value of the field from what is typed, and shows what the validator says about it.
     * The validator is asked when the input is another one, not for each key: a move to another segment
     * does not change the input.
     *
     * @param validateEmpty true to ask the validator now, also when the field has nothing to check
     */
    private void update(boolean validateEmpty) {
        T input = read(editor);
        if (validateEmpty || !validated || !Objects.equals(input, validatedInput)) {
            validationResult = validator != null && (input != null || validateEmpty) ? validator.validate(input) : null;
            validatedInput = input;
            validated = true;
        }
        value = input != null && isAllowed(validationResult) ? input : null;
        updateStatus(input);
        boolean clear = option.isShowClearButton() && !editor.isEmpty();
        if (buttonClear.isVisible() != clear) {
            buttonClear.setVisible(clear);
            revalidate();
        }
        // a name is as wide as its text: another month or day changes the width of the field
        int width = getPreferredSize().width;
        if (width != preferredWidth) {
            preferredWidth = width;
            revalidate();
        }
        repaint();
        if (isPopupVisible()) {
            syncPickers();
        }
        if (!Objects.equals(value, firedValue)) {
            firedValue = value;
            ChangeEvent event = new ChangeEvent(this);
            for (ChangeListener listener : listenerList.getListeners(ChangeListener.class)) {
                listener.stateChanged(event);
            }
        }
    }

    // the color of the border and the icon in the field
    private void updateStatus(T input) {
        String outline = null;
        PickerIcon.Type icon = null;
        if (validationResult != null) {
            switch (validationResult.getSeverity()) {
                case ERROR:
                    outline = FlatClientProperties.OUTLINE_ERROR;
                    icon = PickerIcon.Type.ERROR;
                    break;
                case WARNING:
                    outline = FlatClientProperties.OUTLINE_WARNING;
                    icon = PickerIcon.Type.WARNING;
                    break;
                default:
                    outline = FlatClientProperties.OUTLINE_SUCCESS;
                    icon = PickerIcon.Type.SUCCESS;
                    break;
            }
        } else if (input == null && editor.isComplete()) {
            // every segment has a value, but together they are no value: the 31 of a month with 30 days
            outline = FlatClientProperties.OUTLINE_ERROR;
        }
        putClientProperty(FlatClientProperties.OUTLINE, outline);
        boolean visible = icon != null;
        if (visible) {
            if (icon != statusIcon) {
                labelStatus.setIcon(new PickerIcon(icon));
            }
            labelStatus.setToolTipText(validationResult.getMessage());
        }
        statusIcon = icon;
        if (labelStatus.isVisible() != visible) {
            labelStatus.setVisible(visible);
            revalidate();
        }
    }

    // the field has lost the focus without a value
    private void commit() {
        if (isInputValid()) {
            return;
        }
        CommitMode mode = option.getCommitMode();
        if (mode == CommitMode.CLEAR) {
            editor.clear();
        } else if (mode == CommitMode.REVERT) {
            write(editor, lastValue);
        }
    }

    // ---- popup

    // the pickers are made again when they are shown: with another option, or another validator
    private void resetPickers() {
        closePopup();
        datePicker = null;
        timePicker = null;
    }

    void showDatePopup() {
        if (!editor.hasDate() || !isShowing() || !isEnabled()) {
            return;
        }
        if (datePicker == null) {
            DateOption dateOption = option.getDateOption().copy()
                    .setSelectionMode(getPopupSelectionMode())
                    .setLocale(option.getLocale());
            Predicate<LocalDate> popupDates = getPopupDates();
            if (popupDates != null) {
                // a date can be selected if the option and the validator of the field allow it
                Predicate<LocalDate> selectable = dateOption.getSelectable();
                dateOption.setSelectable(selectable != null ? selectable.and(popupDates) : popupDates);
            }
            datePicker = new JDatePicker(dateOption);
            datePicker.addDateSelectionListener(e -> {
                if (!syncing) {
                    syncing = true;
                    try {
                        datePicked(e);
                    } finally {
                        syncing = false;
                    }
                    lastValue = value;
                }
            });
        }
        closePopup();
        syncPickers();
        requestFocusInWindow();
        datePicker.showPopup(this);
    }

    void showTimePopup() {
        if (!editor.hasTime() || !isShowing() || !isEnabled()) {
            return;
        }
        if (timePicker == null) {
            TimeOption timeOption = option.getTimeOption().copy().setHour24(editor.isHour24());
            Predicate<LocalTime> popupTimes = getPopupTimes();
            if (popupTimes != null) {
                Predicate<LocalTime> selectable = timeOption.getSelectable();
                timeOption.setSelectable(selectable != null ? selectable.and(popupTimes) : popupTimes);
            }
            timePicker = new JTimePicker(timeOption);
            timePicker.addTimeSelectionListener(e -> {
                if (!syncing) {
                    syncing = true;
                    try {
                        // the clock has no seconds, the seconds of the field stay
                        LocalTime time = e.getTime();
                        LocalTime old = editor.getTime();
                        editor.setTime(time != null && old != null ? time.withSecond(old.getSecond()) : time);
                    } finally {
                        syncing = false;
                    }
                    lastValue = value;
                }
            });
        }
        closePopup();
        syncPickers();
        requestFocusInWindow();
        timePicker.showPopup(this);
    }

    /**
     * Closes the popup of the field, if it is showing.
     */
    public void closePopup() {
        if (datePicker != null) {
            datePicker.closePopup();
        }
        if (timePicker != null) {
            timePicker.closePopup();
        }
    }

    public boolean isPopupVisible() {
        return (datePicker != null && datePicker.isPopupVisible()) || (timePicker != null && timePicker.isPopupVisible());
    }

    // the pickers show what the field has. It is called before a popup is shown, and while one is showing
    private void syncPickers() {
        if (syncing) {
            return;
        }
        syncing = true;
        try {
            if (datePicker != null) {
                syncDatePicker(datePicker);
            }
            if (timePicker != null) {
                timePicker.setSelectedTime(editor.getTime());
            }
        } finally {
            syncing = false;
        }
    }

    // ---- buttons

    // the buttons are in a tool bar, the look and feel paints it and its buttons as a part of the text field
    private void installButtons() {
        toolBar = new JToolBar();
        labelStatus = new JLabel();
        labelStatus.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        labelStatus.setCursor(Cursor.getDefaultCursor());
        labelStatus.setVisible(false);
        buttonClear = createButton(PickerIcon.Type.CLEAR);
        buttonDate = createButton(PickerIcon.Type.CALENDAR);
        buttonTime = createButton(PickerIcon.Type.CLOCK);
        buttonClear.setVisible(false);
        buttonClear.addActionListener(e -> {
            clear();
            requestFocusInWindow();
        });
        buttonDate.addActionListener(e -> showDatePopup());
        buttonTime.addActionListener(e -> showTimePopup());
        toolBar.add(labelStatus);
        toolBar.add(buttonClear);
        toolBar.add(buttonDate);
        toolBar.add(buttonTime);
        putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, toolBar);
    }

    private JButton createButton(PickerIcon.Type type) {
        JButton button = new JButton(new PickerIcon(type));
        button.setFocusable(false);
        return button;
    }

    // ---- keys

    private void installKeys() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char ch = e.getKeyChar();
                if (isEditable() && isEnabled() && !e.isControlDown() && !e.isAltDown() && !e.isMetaDown()
                        && ch != KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(ch)) {
                    editor.type(ch);
                }
                e.consume();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (!isEnabled()) {
                    return;
                }
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_F4 || (code == KeyEvent.VK_DOWN && e.isAltDown())) {
                    showPopup();
                    e.consume();
                    return;
                }
                if (e.isAltDown() || e.isControlDown() || e.isMetaDown()) {
                    return;
                }
                boolean leftToRight = getComponentOrientation().isLeftToRight();
                switch (code) {
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_RIGHT:
                        if ((code == KeyEvent.VK_RIGHT) == leftToRight) {
                            editor.selectNext();
                        } else {
                            editor.selectPrevious();
                        }
                        break;
                    case KeyEvent.VK_HOME:
                        editor.selectFirst();
                        break;
                    case KeyEvent.VK_END:
                        editor.selectLast();
                        break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_DOWN:
                        if (isEditable()) {
                            editor.adjust(code == KeyEvent.VK_UP ? 1 : -1);
                        }
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                    case KeyEvent.VK_DELETE:
                        if (isEditable()) {
                            editor.delete(code == KeyEvent.VK_BACK_SPACE);
                        }
                        break;
                    default:
                        return;
                }
                e.consume();
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!e.isTemporary()) {
                    lastValue = value;
                }
                // a click has selected its segment already
                if (!editor.hasSelection()) {
                    if (lastSelected != -1) {
                        editor.select(lastSelected);
                    } else {
                        editor.selectFirst();
                    }
                }
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (!e.isTemporary()) {
                    lastSelected = editor.getSelectedIndex();
                    editor.select(-1);
                    commit();
                }
                repaint();
            }
        });
        // the keys of a text field that would beep in a field without text
        InputMap inputMap = getInputMap();
        ActionMap actionMap = getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "none");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "none");
        actionMap.put("copy-to-clipboard", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copy();
            }
        });
        actionMap.put("cut-to-clipboard", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cut();
            }
        });
        actionMap.put("paste-from-clipboard", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                paste();
            }
        });
    }

    // shows the popup of the date if the field has a date, and of the time if not
    private void showPopup() {
        if (editor.hasDate()) {
            showDatePopup();
        } else {
            showTimePopup();
        }
    }

    /**
     * Copies the text of the field, if it has a value.
     */
    @Override
    public void copy() {
        if (read(editor) != null) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(editor.getText()), null);
        }
    }

    @Override
    public void cut() {
        copy();
        if (isEditable() && isEnabled()) {
            clear();
        }
    }

    /**
     * Reads the text of the clipboard with the pattern of the field. Does nothing if the text is not
     * a date or a time of that pattern.
     */
    @Override
    public void paste() {
        if (!isEditable() || !isEnabled()) {
            return;
        }
        try {
            editor.parse((String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
        } catch (Exception e) {
            // no text in the clipboard
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (toolBar != null) {
            buttonClear.setEnabled(enabled);
            buttonDate.setEnabled(enabled);
            buttonTime.setEnabled(enabled);
        }
        if (!enabled) {
            closePopup();
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        // the constructor of the text field calls this before the editor is made
        setUI(new SegmentFieldUI(() -> editor));
    }

    // ---- default option

    /**
     * @return the option used when a field is created without option
     */
    public static FieldOption getDefaultOption() {
        return defaultOption;
    }

    public static void setDefaultOption(FieldOption option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not null");
        }
        defaultOption = option;
    }

    /**
     * @return a new option, copied from the default option
     */
    public static FieldOption createOption() {
        return defaultOption.copy();
    }
}
