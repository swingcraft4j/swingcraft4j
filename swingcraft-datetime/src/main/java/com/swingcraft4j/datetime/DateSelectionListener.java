package com.swingcraft4j.datetime;

import java.util.EventListener;

/**
 * Listens to the selection of a date picker. Add it with
 * {@link JDatePicker#addDateSelectionListener(DateSelectionListener)}.
 */
public interface DateSelectionListener extends EventListener {

    /**
     * Another date or range is selected, or the selection was cleared. A range is told when it has its end,
     * not after the first click.
     */
    void dateSelectionChanged(DateSelectionEvent event);
}
