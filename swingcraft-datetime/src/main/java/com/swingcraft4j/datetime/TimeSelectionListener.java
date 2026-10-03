package com.swingcraft4j.datetime;

import java.util.EventListener;

/**
 * Listens to the selection of a time picker. Add it with
 * {@link TimePicker#addTimeSelectionListener(TimeSelectionListener)}.
 */
public interface TimeSelectionListener extends EventListener {

    /**
     * Another time is selected, or the selection was cleared. It is told for each hour and minute the
     * hand of the clock is moved to.
     */
    void timeSelectionChanged(TimeSelectionEvent event);
}
