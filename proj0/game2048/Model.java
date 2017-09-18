package game2048;

import java.util.Arrays;
import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author Wayne Li & P. N. Hilfinger
 */
class Model extends Observable {

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to _board[c][r].  Be careful! This is not the usual 2D matrix
     * numbering, where rows are numbered from the top, and the row
     * number is the *first* index. Rather it works like (x, y) coordinates.
     */

    /** Largest piece value. */
    static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    Model(int size) {
        _board = new Tile[size][size];
        _score = _maxScore = 0;
        _gameOver = false;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there. */
    Tile tile(int col, int row) {
        return _board[col][row];
    }

    /** Return the number of squares on one side of the board. */
    int size() {
        return _board.length;
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    boolean gameOver() {
        return _gameOver;
    }

    /** Return the current score. */
    int score() {
        return _score;
    }

    /** Return the current maximum game score (updated at end of game). */
    int maxScore() {
        return _maxScore;
    }

    /** Clear the board to empty and reset the score. */
    void clear() {
        _score = 0;
        _gameOver = false;
        for (Tile[] column : _board) {
            Arrays.fill(column, null);
        }
        setChanged();
    }

    /** Add TILE to the board.  There must be no Tile currently at the
     *  same position. */
    void addTile(Tile tile) {
        assert _board[tile.col()][tile.row()] == null;
        _board[tile.col()][tile.row()] = tile;
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board. */
    boolean tilt(Side side) {
        boolean changed;
        changed = false;
        int[] columns = {0, 1, 2, 3};

        for (int col : columns) {
            int drow = 3;
            int crow = 2;
            Tile destinPlace;
            Tile currPlace;
            while (crow >= 0) {
                destinPlace = vtile(col, drow, side);
                currPlace = vtile(col, crow, side);
                boolean currNull = currPlace == null;
                boolean destinNull = destinPlace == null;
                boolean sameValue = false;
                if ((!currNull) && (!destinNull)) {
                    if (destinPlace.value() == currPlace.value()) {
                        sameValue = true;
                    }
                }
                if (drow == crow) {
                    crow -= 1;
                } else if (currNull) {
                    crow -= 1;
                } else if (sameValue) {
                    setVtile(col, drow, side, currPlace);
                    changed = true;
                    _score += vtile(col, drow, side).value();
                    drow -= 1;
                    crow -= 1;
                } else if (destinNull) {
                    setVtile(col, drow, side, currPlace);
                    changed = true;
                    crow -= 1;
                } else {
                    drow -= 1;
                }
            }
        }
        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }

    /** Return the current Tile at (COL, ROW), when sitting with the board
     *  oriented so that SIDE is at the top (farthest) from you. */
    private Tile vtile(int col, int row, Side side) {
        return _board[side.col(col, row, size())][side.row(col, row, size())];
    }

    /** Move TILE to (COL, ROW), merging with any tile already there,
     *  where (COL, ROW) is as seen when sitting with the board oriented
     *  so that SIDE is at the top (farthest) from you. */
    private void setVtile(int col, int row, Side side, Tile tile) {
        int pcol = side.col(col, row, size()),
            prow = side.row(col, row, size());
        if (tile.col() == pcol && tile.row() == prow) {
            return;
        }
        Tile tile1 = vtile(col, row, side);
        _board[tile.col()][tile.row()] = null;

        if (tile1 == null) {
            _board[pcol][prow] = tile.move(pcol, prow);
        } else {
            _board[pcol][prow] = tile.merge(pcol, prow, tile1);
        }
    }

    /** Deternmine whether game is over and update _gameOver and _maxScore
     *  accordingly. */
    private void checkGameOver() {

        int[] columns = {0, 1, 2, 3};
        int[] rows = {0, 1, 2, 3};
        int theValue;
        boolean indexNum = false;
        boolean notFilled = false;
        boolean stillPossible = false;

        for (int col : columns) {
            for (int row : rows) {
                if (tile(col, row) != null) {
                    theValue = tile(col, row).value();
                    if (theValue == MAX_PIECE) {
                        indexNum = true;
                    }

                    boolean check1 = check(col - 1, row, theValue);
                    boolean check2 = check(col + 1, row, theValue);
                    boolean check3 = check(col, row - 1, theValue);
                    boolean check4 = check(col, row + 1, theValue);

                    if (check1 || check2 || check3 || check4) {
                        stillPossible = true;
                    }
                } else {
                    notFilled = true;
                }
            }
        }
        if (indexNum) {
            _gameOver = true;

        } else if (!notFilled && !stillPossible) {
            _gameOver = true;
        }

        if (_gameOver) {
            _maxScore = _score;
        }
    }

    /** Check the tiles around the current tile,
     *  determining if there are same values.
     *  @return
     *  @param col ** The column number
     *  @param row ** The row number
     *  @param value ** The value in tile
     *  */
    private boolean check(int col, int row, int value) {
        int low = 0;
        int high = 3;
        if ((col < low) || (col > high) || (row < low) || (row > high)) {
            return false;
        } else if (tile(col, row) != null) {
            return tile(col, row).value() == value;
        } else {
            return true;
        }
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        out.format("] %d (max: %d)", score(), maxScore());
        return out.toString();
    }

    /** Current contents of the board. */
    private Tile[][] _board;
    /** Current score. */
    private int _score;
    /** Maximum score so far.  Updated when game ends. */
    private int _maxScore;
    /** True iff game is ended. */
    private boolean _gameOver;

}
