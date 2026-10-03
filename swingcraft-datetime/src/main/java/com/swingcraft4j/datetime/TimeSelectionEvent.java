package com.swingcraft4j.datetime;

import java.time.LocalTime;
import java.util.EventObject;

/**
 * The selection of a time picker has changed.
 */
public class TimeSelectionEvent extends EventObject {

    private final LocalTime time;

    public TimeSelectionEvent(TimePicker source, LocalTime time) {
        super(source);
        this.time = time;
    }

    public TimePicker getTimePicker() {
        return (TimePicker) getSource();
    }

    /**
     * @return the selected time, or null if the selection was cleared
     */
    public LocalTime getTime() {
        return time;
    }
}
