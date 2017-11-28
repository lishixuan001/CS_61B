package qirkat;

import ucb.gui2.Pad;

import java.awt.*;
import java.util.HashMap;
import java.util.Observer;
import java.util.Observable;

import java.awt.event.MouseEvent;

/** Widget for displaying a Qirkat board.
 *  @author Shixuan (Wayne) Li
 */
class BoardWidget extends Pad implements Observer {

    /** Length of side of one square, in pixels. */
    static final int SQDIM = 50;
    /** Number of squares on a side. */
    static final int SIDE = Move.SIDE;
    /** Length of an edge. */
    static final int EDGE = SQDIM * (SIDE + 1);
    /** Radius of circle representing a piece. */
    static final int PIECE_RADIUS = 15;
    /** Radius of a selected area. */
    static final int LIT_RADIUS = 20;
    /** Radius of acceptable error range. */
    static final int ERROR_RADIUS = 20;
    /** Radius of the trace point. */
    static final int TRACE_RADIUS = 2;

    /** Color of white pieces. */
    private static final Color WHITE_COLOR = Color.WHITE;
    /** Color of "phantom" white pieces. */
    /** Color of black pieces. */
    private static final Color BLACK_COLOR = Color.BLACK;
    /** Color of painted lines. */
    private static final Color LINE_COLOR = Color.BLACK;
    /** Color of selected piece. */
    private static final Color LIT_COLOR = Color.ORANGE;
    /** Color of blank squares. */
    private static final Color BLANK_COLOR = new Color(100, 100, 100);
    /** Color of the moved spots in trace. */
    private static final Color MOVED_COLOR = Color.RED;
    /** Color of the eat-ed spots in trace. */
    private static final Color EATED_COLOR = new Color(240, 255, 255);

    /** Stroke for lines.. */
    private static final BasicStroke LINE_STROKE = new BasicStroke(1.0f);

    /** Stroke for outlining pieces. */
    private static final BasicStroke OUTLINE_STROKE = new BasicStroke(2.0f);

    /** Model being displayed. */
    private static Board _model;
    /** String board in _model. */
    private static String _board;

    /** A new widget displaying MODEL. */
    BoardWidget(Board model) {
        _model = model;
        _board = model.board();
        setMouseHandler("click", this::readMove);
        _model.addObserver(this);
        _dim = EDGE;
        setPreferredSize(EDGE, EDGE);
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        _board = _model.board();

        // Set background color
        g.setColor(BLANK_COLOR);
        g.fillRect(0, 0, _dim, _dim);

        // Get attribute for the lines
        g.setStroke(LINE_STROKE);
        g.setColor(LINE_COLOR);

        for (int i = 1; i <= 5; i++) {
            // Draw the vertical lines
            g.drawLine(i * SQDIM, SQDIM, i * SQDIM, SQDIM * SIDE);
            // Draw the horizontal lines
            g.drawLine(SQDIM, i * SQDIM, SQDIM * SIDE, i * SQDIM);

        }

        // Draw the cross lines
        g.drawLine(SQDIM, SQDIM, SQDIM * SIDE, SQDIM * SIDE);
        g.drawLine(SQDIM, SQDIM * SIDE, SQDIM * SIDE, SQDIM);
        g.drawLine(SQDIM, EDGE / 2, EDGE/ 2, SQDIM);
        g.drawLine(SQDIM, EDGE / 2, EDGE/ 2, SQDIM * SIDE);
        g.drawLine(SQDIM * SIDE, EDGE / 2, EDGE/ 2, SQDIM);
        g.drawLine(SQDIM * SIDE, EDGE / 2, EDGE/ 2, SQDIM * SIDE);

        // Set attribute for the boundary lines
        g.setStroke(OUTLINE_STROKE);
        g.setColor(LINE_COLOR);

        // Draw boundary lines
        g.drawLine(0,0, EDGE, 0);
        g.drawLine(0, EDGE, EDGE, EDGE);
        g.drawLine(0, 0, 0, EDGE);
        g.drawLine(EDGE, 0, EDGE, EDGE);

        // Run if some point is selected
        if (_pointSelected) {
            int[] positions = getPosition(_selectedx, _selectedy);
            if (positions != null) {
                int xpos = positions[0], ypos = positions[1];
                int xloc = xpos * SQDIM, yloc = ypos * SQDIM;

                // Check what piece is at the point, show different
                // colors for different piece.
                int index = (ypos - 1) * 5 + (xpos - 1) % 5;
                if (_board.charAt(index) == '-') {
                    g.setColor(Color.GRAY);
                } else {
                    g.setColor(LIT_COLOR);
                }

                // Draw graph for the selected effect
                g.fillOval(xloc - LIT_RADIUS, yloc - LIT_RADIUS
                        , 2 * LIT_RADIUS, 2 * LIT_RADIUS);
            }
            _pointSelected = !_pointSelected;

        }

        // Draw the pieces according to the board
        for (int i = 0; i < _board.length(); i++) {
            int col = i % 5 + 1;
            int row = i / 5 + 1;
            char c = _board.charAt(i);
            if (c == 'b') {
                g.setColor(BLACK_COLOR);
                g.fillOval(col * SQDIM - PIECE_RADIUS, row * SQDIM - PIECE_RADIUS
                        , 2 * PIECE_RADIUS, 2 * PIECE_RADIUS);
            } else if (c == 'w') {
                g.setColor(WHITE_COLOR);
                g.fillOval(col * SQDIM - PIECE_RADIUS, row * SQDIM - PIECE_RADIUS
                        , 2 * PIECE_RADIUS, 2 * PIECE_RADIUS);
            }
        }

        // Draw the trace based on the last move of the board
        String lastmove = _model.getLastMove();
        if (lastmove != null) {

            // Draw the eat-ed points
            String _recordBoard = _model.getLastBoard();
            if (_recordBoard != null) {
                for (int i = 0; i < _board.length(); i++) {
                    int col = i % 5 + 1;
                    int row = i / 5 + 1;

                    if (_recordBoard.charAt(i) != '-' && _board.charAt(i) == '-') {
                        g.setColor(EATED_COLOR);
                        g.fillOval(col * SQDIM - TRACE_RADIUS, row * SQDIM - TRACE_RADIUS
                                , 2 * TRACE_RADIUS, 2 * TRACE_RADIUS);
                    }
                }
            }

            // Draw the moved-to spots
            g.setColor(MOVED_COLOR);
            lastmove.trim();
            String[] movs = lastmove.split("-");
            for (int i = 0; i < movs.length; i++) {
                String mov = movs[i];

                if (i == movs.length - 1) {
                    int[] position = getPosition(mov);
                    int xpos = position[0], ypos = position[1];
                    int xloc = xpos * SQDIM, yloc = ypos * SQDIM;
                    g.fillOval(xloc - TRACE_RADIUS, yloc - TRACE_RADIUS
                            , 2 * TRACE_RADIUS, 2 * TRACE_RADIUS);
                } else {
                    int[] position = getPosition(mov);
                    int xpos = position[0], ypos = position[1];
                    int xloc = xpos * SQDIM, yloc = ypos * SQDIM;
                    g.fillOval(xloc - TRACE_RADIUS, yloc - TRACE_RADIUS
                            , 2 * TRACE_RADIUS, 2 * TRACE_RADIUS);
                }

            }

            _model.resetLastMove();
        }
    }

    /** Lit a selected piece. */
    void selectPoint(int x, int y) {
        _selectedx = x;
        _selectedy = y;
        _pointSelected = !_pointSelected;
        repaint();
    }

    /** Notify observers of mouse's current position from click event WHERE. */
    private void readMove(String unused, MouseEvent where) {
        // Get location of the click
        int xpos = where.getX(), ypos = where.getY();
        char mouseCol, mouseRow;

        // If clicked left mouse
        if (where.getButton() == MouseEvent.BUTTON1) {

            // See which square the mouse click
            int[] position = getPosition(xpos, ypos);
            if (position != null) {
                int xloc = position[0], yloc = position[1];
                mouseCol = _colIndex.get(xloc);
                mouseRow = _rowIndex.get(6 - yloc);

                // Add information to report message
                _string.append("" + mouseCol + mouseRow);
                _string.append("-");

                if (where.getClickCount() == 1) {

                    // If clicked once, lit the selected piece
                    selectPoint(xpos, ypos);
                } else if (where.getClickCount() == 2) {

                    // If double clicked, report the message
                    if (_string.length() > 4) {
                        _string.delete(_string.length() - 4, _string.length());
                    }
                    String string = _string.toString();
                    clearString();

                    // if get things like "c2" or "c2-", ignore
                    if (string.length() > 3) {
                        setChanged();
                        notifyObservers(string);
                    } else {
                        return;
                    }
                }
            }
        }
    }

    /** Get position by pixel location. */
    private int[] getPosition(int x, int y) {
        int[] result = null;

        for (int xnum = 1; xnum <= 5; xnum++) {
            int xloc = xnum * SQDIM;
            if (x >= xloc - ERROR_RADIUS && x <= xloc + ERROR_RADIUS) {

                for (int ynum = 1; ynum <= 5; ynum++) {
                    int yloc = ynum * SQDIM;
                    if (y >= yloc - ERROR_RADIUS && y <= yloc + ERROR_RADIUS) {

                        result = new int[] {xnum, ynum};
                        break;
                    }
                }
            }
        }
        return result;
    }

    /** Get position by [a-e][1-5] format. */
    private int[] getPosition(String location) {
        int[] result = null;
        char xchar = location.charAt(0);
        char ychar = location.charAt(1);
        int x = _axisIndex.get(xchar);
        int y = _axisIndex.get(ychar);
        result = new int[] {x, 6 - y};
        return result;
    }

    /** Clear the _string for Mouse movement. */
    void clearString() {
        _string = new StringBuilder();
    }

    @Override
    public synchronized void update(Observable model, Object arg) {
        repaint();
    }

    /** Added by Wayne, convert column expression into char. */
    @SuppressWarnings("unchecked")
    private HashMap<Integer, Character> _colIndex = new HashMap() {
        {
            put(1, 'a');
            put(2, 'b');
            put(3, 'c');
            put(4, 'd');
            put(5, 'e');
        }
    };

    /** Added by Wayne, convert row expression into char. */
    @SuppressWarnings("unchecked")
    private HashMap<Integer, Character> _rowIndex = new HashMap() {
        {
            put(1, '1');
            put(2, '2');
            put(3, '3');
            put(4, '4');
            put(5, '5');
        }
    };

    /** Added by Wayne, convert axis expression into int. */
    @SuppressWarnings("unchecked")
    private HashMap<Character, Integer> _axisIndex = new HashMap() {
        {
            put('a', 1);
            put('b', 2);
            put('c', 3);
            put('d', 4);
            put('e', 5);
            put('1', 1);
            put('2', 2);
            put('3', 3);
            put('4', 4);
            put('5', 5);
        }
    };

    /** Dimension of current drawing surface in pixels. */
    private int _dim;

    /** Selected x, and y. */
    private int _selectedx;
    private int _selectedy;
    /** If some point is selected. */
    private boolean _pointSelected = false;
    /** Record Mouse Movement. */
    private StringBuilder _string = new StringBuilder();
    /** Record the board for comparing. */
}
