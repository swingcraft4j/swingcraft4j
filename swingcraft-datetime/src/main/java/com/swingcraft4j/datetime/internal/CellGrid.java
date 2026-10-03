package com.swingcraft4j.datetime.internal;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import com.swingcraft4j.datetime.option.PickerSize;
import com.swingcraft4j.datetime.option.StyleOption;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

/**
 * A grid of cells that are painted and can be clicked: the days of a month, the months of a year and
 * the years. It is one component, so it is light and its cells are painted the same way everywhere.
 * <p>
 * The cells are counted from 0, row after row, and follow the component orientation.
 */
abstract class CellGrid extends JComponent {

    private final int columns;
    private final int rows;
    private final Dimension cellSize;
    private final PickerSize size;
    private final StyleOption style;
    // the cell under the mouse, -1 for none
    private int hoverCell = -1;
    private int pressedCell = -1;

    /**
     * @param cellSize the preferred size of a cell with {@link PickerSize#MEDIUM}, it is scaled
     * @param size     the size of the picker
     * @param style    the style of the picker
     */
    CellGrid(int columns, int rows, Dimension cellSize, PickerSize size, StyleOption style) {
        this.columns = columns;
        this.rows = rows;
        this.cellSize = cellSize;
        this.size = size;
        this.style = style;
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setHoverCell(cellAt(e.getPoint()));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                setHoverCell(cellAt(e.getPoint()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setHoverCell(-1);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    pressedCell = cellAt(e.getPoint());
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int cell = cellAt(e.getPoint());
                    boolean clicked = cell != -1 && cell == pressedCell;
                    pressedCell = -1;
                    repaint();
                    if (clicked) {
                        cellClicked(cell);
                    }
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    /**
     * @return false for a cell that is not used with the mouse, as the name of a day
     */
    boolean isCellEnabled(int cell) {
        return true;
    }

    abstract void cellClicked(int cell);

    /**
     * The mouse is over another cell.
     */
    void cellHovered(int cell) {
    }

    /**
     * @param hover   true if the mouse is over the cell
     * @param pressed true if the mouse is pressed on the cell
     */
    abstract void paintCell(Graphics2D g, int cell, Rectangle2D.Float bounds, boolean hover, boolean pressed);

    /**
     * Paints what is behind the cells, as the band of a range that goes over many cells.
     */
    void paintBackground(Graphics2D g) {
    }

    Rectangle2D.Float getCellBounds(int cell) {
        float width = getWidth() / (float) columns;
        float height = getHeight() / (float) rows;
        int column = cell % columns;
        if (!getComponentOrientation().isLeftToRight()) {
            column = columns - 1 - column;
        }
        return new Rectangle2D.Float(column * width, cell / columns * height, width, height);
    }

    private void setHoverCell(int cell) {
        if (hoverCell != cell) {
            hoverCell = cell;
            if (cell != -1) {
                cellHovered(cell);
            }
            repaint();
        }
    }

    private int cellAt(Point point) {
        if (!isEnabled() || !contains(point)) {
            return -1;
        }
        int column = Math.min((int) (point.x / (getWidth() / (float) columns)), columns - 1);
        int row = Math.min((int) (point.y / (getHeight() / (float) rows)), rows - 1);
        if (!getComponentOrientation().isLeftToRight()) {
            column = columns - 1 - column;
        }
        int cell = row * columns + column;
        return isCellEnabled(cell) ? cell : -1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            g2.setFont(getFont());
            paintBackground(g2);
            for (int cell = 0; cell < columns * rows; cell++) {
                paintCell(g2, cell, getCellBounds(cell), cell == hoverCell, cell == pressedCell);
            }
        } finally {
            g2.dispose();
        }
    }

    /**
     * @return the color of a cell that is not selected: nothing, or a shade of the background while the
     * mouse is over it or pressed on it
     */
    Color getCellBackground(boolean hover, boolean pressed) {
        if (!hover && !pressed) {
            return null;
        }
        return PickerUtils.shade(PickerUtils.background(this), pressed ? 0.1f : 0.05f);
    }

    /**
     * @return the accent color of a selected cell, a little darker or lighter while the mouse is on it
     */
    Color getSelectedBackground(boolean hover, boolean pressed) {
        Color color = getAccentColor();
        return hover || pressed ? PickerUtils.shade(color, pressed ? 0.1f : 0.05f) : color;
    }

    StyleOption getStyle() {
        return style;
    }

    Color getAccentColor() {
        return PickerUtils.accentColor(style.getColor());
    }

    /**
     * @param height the height of the shape of a selected cell
     * @return the corner arc diameter of the shape: the one of the style, a half circle at most
     */
    float getSelectionArc(float height) {
        return Math.min(UIScale.scale((float) style.getSelectionRound()), height);
    }

    /**
     * @return the value for the size of the picker, scaled with the UI scale factor
     */
    float scale(float value) {
        return PickerUtils.scale(size, value);
    }

    @Override
    public Font getFont() {
        return PickerUtils.font(size, 0);
    }

    @Override
    public Dimension getPreferredSize() {
        return getPreferredSize(columns, rows, cellSize, size);
    }

    /**
     * @return the preferred size of a grid, without making the grid
     */
    static Dimension getPreferredSize(int columns, int rows, Dimension cellSize, PickerSize size) {
        return new Dimension(Math.round(PickerUtils.scale(size, cellSize.width)) * columns,
                Math.round(PickerUtils.scale(size, cellSize.height)) * rows);
    }
}
