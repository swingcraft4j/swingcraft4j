package com.swingcraft4j.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Shows the memory that is used by the demo, of the memory that is committed. A click runs the garbage collector,
 * to see that the memory of the closed modals and toasts is given back.
 */
class MemoryBar extends JProgressBar {

    private static final int MB = 1024 * 1024;

    private final Timer timer = new Timer(1000, e -> updateMemory());

    MemoryBar() {
        setStringPainted(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Click to run the garbage collector");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    System.gc();
                    updateMemory();
                }
            }
        });
        updateMemory();
    }

    // the timer keeps the bar from being collected, so it runs only while the bar is in a window
    @Override
    public void addNotify() {
        super.addNotify();
        updateMemory();
        timer.start();
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    private void updateMemory() {
        Runtime runtime = Runtime.getRuntime();
        long committed = runtime.totalMemory();
        long used = committed - runtime.freeMemory();
        setMaximum((int) (committed / MB));
        setValue((int) (used / MB));
        setString(used / MB + " of " + committed / MB + " MB");
    }
}
