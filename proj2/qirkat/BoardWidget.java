package qirkat;

import javafx.scene.input.KeyCode;
import javafx.scene.shape.Circle;
import ucb.gui2.Pad;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.image.*;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Observer;
import java.util.Observable;

import java.awt.event.MouseEvent;

import qirkat.Board;
import static qirkat.PieceColor.*;

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

    /** Indicate that the squares indicated by MOV are the currently selected
     *  squares for a pending move. */
    void indicateMove(Move mov) {
        _selectedMove = mov;
        _model.makeMove(_selectedMove);
        repaint();
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

                if (where.getClickCount() == 1) {

                    // If clicked once, lit the selected piece
                    _string.append("-");
                    selectPoint(xpos, ypos);
                } else if (where.getClickCount() == 2) {

                    // If double clicked, report the message
                    _string.delete(_string.length() - 3, _string.length());
                    String string = _string.toString();
                    clearString();
                    setChanged();
                    notifyObservers(string);
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

    /** Dimension of current drawing surface in pixels. */
    private int _dim;

    /** A partial Move indicating selected squares. */
    private Move _selectedMove;
    /** Selected x, and y. */
    private int _selectedx;
    private int _selectedy;
    /** If some point is selected. */
    private boolean _pointSelected = false;
    /** Record Mouse Movement. */
    private StringBuilder _string = new StringBuilder();
}
