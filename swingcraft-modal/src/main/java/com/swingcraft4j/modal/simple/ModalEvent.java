package com.swingcraft4j.modal.simple;

import java.util.EventObject;

public class ModalEvent extends EventObject {

    private final int action;
    private boolean consumed;

    public ModalEvent(SimpleModal source, int action) {
        super(source);
        this.action = action;
    }

    /**
     * @return the action of the pressed button, such as {@link SimpleModal#YES_OPTION}
     */
    public int getAction() {
        return action;
    }

    public SimpleModal getModal() {
        return (SimpleModal) getSource();
    }

    /**
     * Keeps the modal open, for example when the input is not valid.
     */
    public void consume() {
        consumed = true;
    }

    public boolean isConsumed() {
        return consumed;
    }
}
