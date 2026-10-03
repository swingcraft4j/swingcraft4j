package com.swingcraft4j.modal.simple;

import com.formdev.flatlaf.FlatClientProperties;
import com.swingcraft4j.modal.Modal;
import com.swingcraft4j.modal.ModalController;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * The default modal: a header with title and close button, the content and the option buttons.
 * When it is pushed over another modal, the header also has a back button.
 * Override the create methods to customize a part of it.
 */
public class SimpleModal extends Modal {

    // actions
    public static final int CLOSE_OPTION = -1;
    public static final int YES_OPTION = 0;
    public static final int OK_OPTION = 0;
    public static final int NO_OPTION = 1;
    public static final int CANCEL_OPTION = 2;

    /**
     * The buttons shown below the content.
     */
    public enum OptionType {
        NONE, CLOSE, OK_CANCEL, YES_NO, YES_NO_CANCEL
    }

    protected final Component content;
    protected final String title;
    private final Button[] buttons;
    private final ModalCallback callback;
    // null to use the default insets of the layout
    private Insets padding;
    private JComponent backButton;

    public SimpleModal(Component content, String title) {
        this(content, title, OptionType.NONE, null);
    }

    public SimpleModal(Component content, String title, OptionType optionType, ModalCallback callback) {
        this(content, title, getButtons(optionType), callback);
    }

    /**
     * @param content  the component shown between the header and the buttons
     * @param title    the title
     * @param buttons  the buttons, or null for no button
     * @param callback receives the action of the pressed button, or null to only close the modal
     */
    public SimpleModal(Component content, String title, Button[] buttons, ModalCallback callback) {
        this.content = content;
        this.title = title;
        this.buttons = buttons;
        this.callback = callback;
    }

    /**
     * Sets the space around the header, the content and the buttons, in place of the default dialog insets
     * of the layout. Must be set before the modal is shown.
     */
    public void setPadding(int top, int left, int bottom, int right) {
        this.padding = new Insets(top, left, bottom, right);
    }

    @Override
    protected void installComponent() {
        String insets = padding == null
                ? "insets dialog"
                : "insets " + padding.top + " " + padding.left + " " + padding.bottom + " " + padding.right;
        // the header and the buttons are placed straight in this layout. A panel around them would need
        // insets 0 to line up, and that cuts off the focus border that is painted outside the component
        // one cell for each row: the header, the content and the buttons
        setLayout(new MigLayout("fill,hidemode 3," + insets, "[grow]", "[][grow][]"));
        backButton = createBackButton();
        backButton.setVisible(false);
        add(backButton, "cell 0 0");
        add(createTitle(title), "cell 0 0,growx");
        add(createCloseButton(), "cell 0 0");
        add(content, "cell 0 1,grow");
        if (buttons != null) {
            for (int i = 0; i < buttons.length; i++) {
                // the gap before the first button moves all of them to the trailing side
                add(createButton(buttons[i]), i == 0 ? "cell 0 2,gapbefore push,sg button" : "cell 0 2,sg button");
            }
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // the same modal can be shown first and pushed later
        if (backButton != null) {
            ModalController controller = getController();
            backButton.setVisible(controller != null && controller.canPop());
        }
    }

    /**
     * The back button is only visible when this modal was pushed over another modal.
     */
    protected JComponent createBackButton() {
        JButton button = createHeaderButton(new HeaderIcon(HeaderIcon.Type.BACK));
        button.addActionListener(e -> popModal());
        return button;
    }

    protected JComponent createTitle(String title) {
        JLabel label = new JLabel(title);
        label.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:+4");
        return label;
    }

    protected JComponent createCloseButton() {
        JButton button = createHeaderButton(new HeaderIcon(HeaderIcon.Type.CLOSE));
        button.addActionListener(e -> doAction(CLOSE_OPTION));
        return button;
    }

    private JButton createHeaderButton(Icon icon) {
        JButton button = new JButton(icon);
        button.setFocusable(false);
        button.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:999;" +
                "margin:5,5,5,5;" +
                "borderWidth:0;" +
                "focusWidth:0;" +
                "innerFocusWidth:0;" +
                "background:null;");
        return button;
    }

    protected JButton createButton(Button button) {
        JButton component = new JButton(button.getText()) {
            @Override
            public boolean isDefaultButton() {
                return button.isDefaultButton() || super.isDefaultButton();
            }
        };
        component.addActionListener(e -> doAction(button.getAction()));
        return component;
    }

    /**
     * Sends the action to the callback and closes the modal, unless the callback consumed the event.
     */
    public void doAction(int action) {
        if (callback != null) {
            ModalEvent event = new ModalEvent(this, action);
            callback.action(event);
            if (event.isConsumed()) {
                return;
            }
        }
        closeModal();
    }

    private static Button[] getButtons(OptionType optionType) {
        switch (optionType) {
            case CLOSE:
                return new Button[]{new Button("Close", CLOSE_OPTION)};
            case OK_CANCEL:
                return new Button[]{
                        new Button(getText("OptionPane.okButtonText", "OK"), OK_OPTION, true),
                        new Button(getText("OptionPane.cancelButtonText", "Cancel"), CANCEL_OPTION)};
            case YES_NO:
                return new Button[]{
                        new Button(getText("OptionPane.yesButtonText", "Yes"), YES_OPTION, true),
                        new Button(getText("OptionPane.noButtonText", "No"), NO_OPTION)};
            case YES_NO_CANCEL:
                return new Button[]{
                        new Button(getText("OptionPane.yesButtonText", "Yes"), YES_OPTION, true),
                        new Button(getText("OptionPane.noButtonText", "No"), NO_OPTION),
                        new Button(getText("OptionPane.cancelButtonText", "Cancel"), CANCEL_OPTION)};
            default:
                return null;
        }
    }

    // the same localized text as JOptionPane
    private static String getText(String key, String defaultText) {
        String text = UIManager.getString(key);
        return text != null ? text : defaultText;
    }

    /**
     * A button of the modal.
     */
    public static class Button {

        private final String text;
        private final int action;
        private final boolean defaultButton;

        public Button(String text, int action) {
            this(text, action, false);
        }

        /**
         * @param text          the button text
         * @param action        the action sent to the callback when the button is pressed
         * @param defaultButton true to paint the button as the default button
         */
        public Button(String text, int action, boolean defaultButton) {
            this.text = text;
            this.action = action;
            this.defaultButton = defaultButton;
        }

        public String getText() {
            return text;
        }

        public int getAction() {
            return action;
        }

        public boolean isDefaultButton() {
            return defaultButton;
        }
    }
}
