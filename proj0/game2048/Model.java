package game2048;

import java.util.Arrays;
import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author
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
        // FIXME START
        int[] columns = {0, 1, 2, 3};

        for(int col : columns){
            int drow = 3;
            int crow = 2;

            Tile destin_place;
            Tile curr_place;

            // Change: Deleted "drow >= 0"
            while(crow >= 0){
                destin_place = vtile(col, drow, side);
                curr_place = vtile(col, crow, side);

                boolean curr_is_null = curr_place == null;
                boolean destin_is_null = destin_place == null;
                boolean same_value = false;
                if ((!curr_is_null) && (!destin_is_null)){
                    if (destin_place.value() == curr_place.value()){
                        same_value = true;
                    }
                }

                if (drow == crow){
                    crow -= 1;
                }
                else if (curr_is_null){
                    crow -= 1;
                }
                else if (same_value){
                    setVtile(col, drow, side, curr_place);
                    changed = true;
                    _score += vtile(col, drow, side).value();
                    drow -= 1;
                    crow -= 1;
                }
                else if(destin_is_null){
                    setVtile(col, drow, side, curr_place);
                    changed = true;
                    crow -= 1;
                }
                else {
                    drow -= 1;
                }
            }
        }
        // FIXME END
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
        // FIXME START
        /* 1. Check 2048 -- true
         *  2. Full-filled?
         *      1) No
         *      2) Yes
         *          1' Can continue
         *          2' Cannot -- true
         */
        int[] columns = {0,1,2,3};
        int[] rows = {0,1,2,3};
        int the_value;
        boolean index_2048 = false;
        boolean not_filled = false;
        boolean still_possible = false;

        for(int col : columns){
            for(int row : rows){
                if(tile(col,row) != null){
                    the_value = tile(col,row).value();
                    if (the_value == 2048){
                        index_2048 = true;
                    }
                    if (check(col-1, row, the_value) || check(col+1, row, the_value) || check(col, row-1, the_value) || check(col, row+1, the_value)){
                        still_possible = true;
                    }
                }
                else{
                    not_filled = true;
                }

            }
        }
        if (index_2048){
            _gameOver = true;

        }
        else if(!not_filled && !still_possible){
            _gameOver = true;

        }

        /*Update Score*/
        if (_gameOver) {
            _maxScore = _score;
        }
        // FIXME END
    }

    private boolean check(int col, int row, int value){
        int low = 0;
        int high = 3;
        if ((col < low) || (col > high) || (row < low) || (row > high)){
            return false;
        }
        else{
            if(tile(col,row) != null){
                if (tile(col,row).value() == value){
                    return true;
                }
                else{
                    return false;
                }
            }
            else{
                return true;
            }
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
