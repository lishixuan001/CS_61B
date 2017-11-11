package qirkat;

import java.util.List;
import java.util.Observable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Observer;
import java.util.Formatter;


import static qirkat.PieceColor.*;
import static qirkat.Move.*;

/** A Qirkat board.   The squares are labeled by column (a char value between
 *  'a' and 'e') and row (a char value between '1' and '5'.
 *
 *  For some purposes, it is useful to refer to squares using a single
 *  integer, which we call its "linearized index".  This is simply the
 *  number of the square in row-major order (with row 0 being the bottom row)
 *  counting from 0).
 *
 *  Moves on this board are denoted by Moves.
 *  @author Shixuan (Wayne) Li
 */
class Board extends Observable {

    /** A new, cleared board at the start of the game. */
    Board() {
        clear();
    }

    /** A copy of B. */
    Board(Board b) {
        internalCopy(b);
    }

    /** Return a constant view of me (allows any access method, but no
     *  method that modifies it). */
    Board constantView() {
        return this.new ConstantBoard();
    }

    /** Clear me to my starting state, with pieces in their initial
     *  positions. */
    void clear() {
        _whoseMove = WHITE;
        _gameOver = false;

        setPieces(INIT_PIECES, WHITE);
        _board = board();
        _state = "set_up";

        setChanged();
        notifyObservers();
    }

    /** Copy B into me. */
    void copy(Board b) {
        internalCopy(b);
    }

    /** Copy B into me. */
    private void internalCopy(Board b) {

        _board = b.board();
        _pieces = b.pieces();
        _state = b.state();
        _whoseMove = b.whoseMove();
        _gameOver = b.gameOver();

        setChanged();
        notifyObservers();
    }

    /** Set my contents as defined by STR.  STR consists of 25 characters,
     *  each of which is b, w, or -, optionally interspersed with whitespace.
     *  These give the contents of the Board in row-major order, starting
     *  with the bottom row (row 1) and left column (column a). All squares
     *  are initialized to allow horizontal movement in either direction.
     *  NEXTMOVE indicates whose move it is.
     */
    void setPieces(String str, PieceColor nextMove) {
        if (nextMove == EMPTY || nextMove == null) {
            throw new IllegalArgumentException("bad player color");
        }
        str = str.replaceAll("\\s", "");
        if (!str.matches("[bw-]{25}")) {
            throw new IllegalArgumentException("bad board description");
        }

        for (int i = 0; i < str.length(); i += 1) {
            int k = i;
            switch (str.charAt(i)) {
            case '-':
                set(k, EMPTY);
                break;
            case 'b': case 'B':
                set(k, BLACK);
                break;
            case 'w': case 'W':
                set(k, WHITE);
                break;
            default:
                break;
            }
        }


        _whoseMove = nextMove;

        setChanged();
        notifyObservers();
    }

    /** Return true iff the game is over: i.e., if the current player has
     *  no moves. */
    boolean gameOver() {
        return _gameOver;
    }

    /** Return the current contents of square C R, where 'a' <= C <= 'e',
     *  and '1' <= R <= '5'.  */
    PieceColor get(char c, char r) {
        assert validSquare(c, r);
        return get(index(c, r));
    }

    /** Return the current contents of the square at linearized index K. */
    PieceColor get(int k) {
        assert validSquare(k);
        return _pieces.get(k);
    }

    /** Set get(C, R) to V, where 'a' <= C <= 'e', and
     *  '1' <= R <= '5'. */
    private void set(char c, char r, PieceColor v) {
        assert validSquare(c, r);
        set(index(c, r), v);
    }

    /** Set get(K) to V, where K is the linearized index of a square. */
    private void set(int k, PieceColor v) {
        assert validSquare(k);
        _pieces.put(k, v);
    }

    /** Return true iff MOV is legal on the current board. */
    boolean legalMove(Move mov) {
        char col0 = mov.col0();
        char col1 = mov.col1();
        char row0 = mov.row0();
        char row1 = mov.row1();
        int start = index(col0, row0);
        int destination = index(col1, row1);

        // Check if start/end is in scale
        if (!validSquare(start) || !validSquare(destination)) {
            return false;
        }
        // Check if moved for 1 scale
        if (!isDistanceUnit(col0, col1, row0, row1)) {
            return false;
        }
        // Check if the end is empty
        if (!isSquareEmpty(destination)) {
            return false;
        }
        return true;
    }

    /** Return a list of all legal moves from the current position. */
    ArrayList<Move> getMoves() {
        ArrayList<Move> result = new ArrayList<>();
        getMoves(result);
        return result;
    }

    /** Add all legal moves from the current position to MOVES. */
    void getMoves(ArrayList<Move> moves) {
        if (gameOver()) {
            return;
        }
        if (jumpPossible()) {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                getJumps(moves, k);
            }
        } else {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                getMoves(moves, k);
            }
        }
    }

    /** Add all legal non-capturing moves from the position
     *  with linearized index K to MOVES. */
    private void getMoves(ArrayList<Move> moves, int k) {
        // FIXME
        // Waiting
    }

    /** Add all legal captures from the position with linearized index K
     *  to MOVES. */
    private void getJumps(ArrayList<Move> moves, int k) {
        // FIXME
        // Waiting
    }

    /** Return true iff MOV is a valid jump sequence on the current board.
     *  MOV must be a jump or null.  If ALLOWPARTIAL, allow jumps that
     *  could be continued and are valid as far as they go.  */
    boolean checkJump(Move mov, boolean allowPartial) {
        if (mov == null) {
            return true;
        }
        // FIXME
        // Waiting
        return false;
    }

    /** Return true iff a jump is possible for a piece at position C R. */
    boolean jumpPossible(char c, char r) {
        return jumpPossible(index(c, r));
    }

    /** Return true iff a jump is possible for a piece at position with
     *  linearized index K. */
    boolean jumpPossible(int k) {
        if (!validSquare(k)
                || !_pieces.get(k).isPiece()
                || !(k % 2 == 0)) {
            return false;
        }
        int l, r, u, d, lu, ld, ru, rd;
        List<Integer> lst1 = new ArrayList<>();
        List<Integer> lst2 = new ArrayList<>();
        l = k - 1;
        r = k + 1;
        u = k - 5;
        d = k + 5;
        lu = u - 1;
        ld = d - 1;
        ru = u + 1;
        rd = d + 1;
        lst1.add(0, l);
        lst1.add(1, u);
        lst1.add(2, lu);
        lst1.add(3, ld);
        lst2.add(0, r);
        lst2.add(1, d);
        lst2.add(2, rd);
        lst2.add(3, ru);
        for (int i = 0; i < lst1.size(); i++) {
            int first = lst1.get(i);
            int second = lst2.get(i);
            if (validSquare(first) && validSquare(second)) {
                boolean condition1 =
                        (_pieces.get(first).isPiece()
                                && _pieces.get(first) != EMPTY)
                                && _pieces.get(second) == EMPTY;
                boolean condition2 =
                        (_pieces.get(second).isPiece()
                                || _pieces.get(second) != EMPTY)
                                && _pieces.get(first) == EMPTY;
                if (condition1) {
                    if (_pieces.get(first).opposite() == _pieces.get(k)) {
                        return true;
                    }
                } else if (condition2) {
                    if (_pieces.get(second).opposite() == _pieces.get(k)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Return true iff a jump is possible from the current board. */
    boolean jumpPossible() {
        for (int k = 0; k <= MAX_INDEX; k += 1) {
            if (jumpPossible(k)) {
                return true;
            }
        }
        return false;
    }

    /** Return the color of the player who has the next move.  The
     *  value is arbitrary if gameOver(). */
    PieceColor whoseMove() {
        return _whoseMove;
    }

    /** Perform the move C0R0-C1R1, or pass if C0 is '-'.  For moves
     *  other than pass, assumes that legalMove(C0, R0, C1, R1). */
    void makeMove(char c0, char r0, char c1, char r1) {
        makeMove(Move.move(c0, r0, c1, r1, null));
    }

    /** Make the multi-jump C0 R0-C1 R1..., where NEXT is C1R1....
     *  Assumes the result is legal. */
    void makeMove(char c0, char r0, char c1, char r1, Move next) {
        makeMove(Move.move(c0, r0, c1, r1, next));
    }

    /** Added by Wayne, generate Move from String.
     * @param string -- input 'string'
     * @return */
    private Move stringToMove(String string) {
        assert string.length() >= 2;
        int indicator = 0;
        char r1 = string.charAt(string.length() - 1);
        char c1 = string.charAt(string.length() - 2);
        char c0 = c1;
        char r0 = r1;
        Move mov = Move.move(c1, r1);
        for (int i = string.length() - 1; i >= 0; i -= 2) {
            if (indicator == 0) {
                r1 = string.charAt(i);
                c1 = string.charAt(i - 1);
                mov = Move.move(c1, r1, c0, r0, mov);
            } else {
                r0 = string.charAt(i);
                c0 = string.charAt(i - 1);
                mov = Move.move(c0, r0, c1, r1, mov);
            }
            indicator = Math.abs(indicator - 1);
        }
        return mov;
    }

    /** Make the Move MOV on this Board, assuming it is legal. */
    void makeMove(Move mov) {

        // FIXME
        // -- Fixed -- Legal Jump/Move -- isMove()? -- gameOver()?
        boardList.add(board());
        while (mov != null) {
            int position0 = mov.fromIndex();
            int position1 = mov.toIndex();

            if (mov.isJump()) {
                int jumped = mov.jumpedIndex();
                PieceColor type0 = _pieces.get(position0);
                insertPiece(position1, type0);
                removePiece(position0);
                removePiece(jumped);
            } else {
                assert legalMove(mov);
                PieceColor type0 = _pieces.get(position0);
                insertPiece(position1, type0);
                removePiece(position0);
            }
            mov = mov.jumpTail();

//            System.out.println(position0 + "  " + position1);
//            System.out.println(toString());
        }


        setChanged();
        notifyObservers();
    }

    /** Remove a piece.
     * @param position -- input 'position'*/
    private void removePiece(int position) {
        _pieces.put(position, EMPTY);
    }

    /** Insert a piece.
     * @param position -- input 'position'
     * @param type -- input 'type'*/
    private void insertPiece(int position, PieceColor type) {
        _pieces.put(position, type);
    }

    /** Undo the last move, if any. */
    void undo() {
        String string = boardList.get(boardList.size() - 1);
        setPieces(string, WHITE);
        boardList.remove(boardList.size() - 1);

        setChanged();
        notifyObservers();
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /** Return a text depiction of the board.  If LEGEND, supply row and
     *  column numbers around the edges. */
    private String toString(boolean legend) {

        StringBuilder string = new StringBuilder();
        int rowIndex = 5;

        if (legend) { string.append(rowIndex); }
        string.append(" ");

        for (int i = 0; i <= MAX_INDEX; i++) {
            Character name = board().charAt(i);
            if (i % 5 == 0 && i != 0) {
                string.append("\n");
                rowIndex -= 1;
                if (legend) { string.append(rowIndex); }
                string.append(" ");
            }
            string.append(" " + name);
        }

        if (legend) {
            string.append("\n");
            string.append("   a b c d e");
        }
        String result = string.toString();
        return result;
    }

    /** Return true iff there is a move for the current player. */
    private boolean isMove() {
        return false;  // FIXME
    }


    /** Player that is on move. */
    private PieceColor _whoseMove;

    /** Set true when game ends. */
    private boolean _gameOver;

    /** Convenience value giving values of pieces at each ordinal position. */
    static final PieceColor[] PIECE_VALUES = PieceColor.values();

    /** One cannot create arrays of ArrayList<Move>, so we introduce
     *  a specialized private list type for this purpose. */
    private static class MoveList extends ArrayList<Move> {
    }

    /** Added by Wayne, track moves' changes by remembering the board. */
    private List<String> boardList = new ArrayList<>();

    /** Added by Wayne, check if move is valid in distance.
     * @param col0 -- input 'col0'
     * @param col1 -- input 'col1'
     * @param row0 -- input 'row0'
     * @param row1 -- input 'row1'
     * @return */
    private boolean isDistanceUnit(char col0, char col1, char row0, char row1) {
        int colDistance = Math.abs(_colIndex.get(col0) - _colIndex.get(col1));
        int rowDistance = Math.abs(row0 - row1);
        boolean colZero = colDistance == 0;
        boolean rowZero = rowDistance == 0;
        boolean colOne = colDistance == 1;
        boolean rowOne = rowDistance == 1;

        boolean con1 = colZero && rowZero;
        boolean con2 = colZero && rowOne;
        boolean con3 = colOne && rowZero;
        boolean con4 = colOne && rowOne;

        return con1 || con2 || con3 || con4;
    }

    /** Added by Wayne, convert column expression into int. */
    @SuppressWarnings("unchecked")
    private HashMap<Character, Integer> _colIndex = new HashMap() {
        {
            put('a', 1);
            put('b', 2);
            put('c', 3);
            put('d', 4);
            put('e', 5);
        }
    };

    /** Added by Wayne, check if piece in scale.
     * @param k -- input 'k'
     * @return */
    private boolean isSquareEmpty(int k) {
        String status = _pieces.get(k).shortName();
        return status.equals("-");
    }

    /** Added by Wayne, create HashMap for convenience tracking
     *  piece content. */
    private HashMap<Integer, PieceColor> _pieces = new HashMap<>();

    /** Added by Wayne, get _pieces.
     * @return */
    private HashMap<Integer, PieceColor> pieces() {
        return _pieces;
    }

    /** Added by Wayne, True if board is "set_up" state.
     * @return */
    private boolean isSetUp() {
        return _state.equals("set_up");
    }

    /** Added by Wayne, True if board is "playing" state.
     * @return */
    private boolean isPlaying() {
        return _state.equals("playing");
    }

    /** Added by Wayne, get current state.
     * @return */
    private String state() {
        return _state;
    }

    /** Added by Wayne, indicating board state.*/
    private String _state;

    /** Added by Wayne, get board map.
     * @return */
    public String board() {
        StringBuilder string = new StringBuilder();
        for (int key = 4 * SIDE; key <= MAX_INDEX; key++) {
            PieceColor piece = _pieces.get(key);
            String name = piece.shortName();
            string.append(name);
        }
        for (int key = 3 * SIDE; key < 4 * SIDE; key++) {
            PieceColor piece = _pieces.get(key);
            String name = piece.shortName();
            string.append(name);
        }
        for (int key = 2 * SIDE; key < 3 * SIDE; key++) {
            PieceColor piece = _pieces.get(key);
            String name = piece.shortName();
            string.append(name);
        }
        for (int key = 1 * SIDE; key < 2 * SIDE; key++) {
            PieceColor piece = _pieces.get(key);
            String name = piece.shortName();
            string.append(name);
        }
        for (int key = 0 * SIDE; key < 1 * SIDE; key++) {
            PieceColor piece = _pieces.get(key);
            String name = piece.shortName();
            string.append(name);
        }
        _board = string.toString();
        return _board;
    }

    /** Added by Wayne, variable showing board map. */
    private String _board;

    /** Added by Wayne, standard initial game board. */
    private static final String INIT_PIECES =
            "  w w w w w\n  w w w w w\n  b b - w w\n  b b b b b\n  b b b b b";

    /** A read-only view of a Board. */
    private class ConstantBoard extends Board implements Observer {
        /** A constant view of this Board. */
        ConstantBoard() {
            super(Board.this);
            Board.this.addObserver(this);
        }

        @Override
        void copy(Board b) {
            assert false;
        }

        @Override
        void clear() {
            assert false;
        }

        @Override
        void makeMove(Move move) {
            assert false;
        }

        /** Undo the last move. */
        @Override
        void undo() {
            assert false;
        }

        @Override
        public void update(Observable obs, Object arg) {
            super.copy((Board) obs);
            setChanged();
            notifyObservers(arg);
        }
    }
}
