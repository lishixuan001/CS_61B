package qirkat;

import java.util.List;
import java.util.Observable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Observer;


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
        _movedNotJumped = new ArrayList<>();

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
        _movedNotJumped = new ArrayList<>();

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
        // legal diagonal
        if (start % 2 == 1 && destination % 2 == 1) {
            return false;
        }
        // reach end cannot move
        if (_pieces.get(start).equals(WHITE)) {
            if (start >= 4 * SIDE && start <= MAX_INDEX) {
                return false;
            }
        } else if (_pieces.get(start).equals(BLACK)) {
            if (start >= 0 && start < SIDE) {
                return false;
            }
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

    /** Add all legal capturing moves from the position
     *  with linearized index K to MOVES. */
    private void getJumps(ArrayList<Move> moves, int k) {
        if (validSquare(k) && _pieces.get(k).isPiece()) {
            char tempcol0 = col(k);
            char temprow0 = row(k);
            int col0 = _axisIndex.get(tempcol0);
            int row0 = _axisIndex.get(temprow0);
            HashMap<Integer, PieceColor> pieces = new HashMap<>(pieces());
            getJumps(moves, col0, row0, null, pieces);
        }
    }

    /** Helper function for getJumps. */
    private void getJumps(ArrayList<Move> moves, int tempcol0, int temprow0, Move move, HashMap<Integer, PieceColor> pieces) {

        char col0 = _colIndex.get(tempcol0);
        char row0 = _rowIndex.get(temprow0);

        boolean idx = false;

        for (int i : rowFactors) {
            for (int j : colFactors) {
                boolean con = i == 0 && j == 0;
                int tempcol1 = tempcol0 + j;
                int temprow1 = temprow0 + i;
                // Target is valid
                if (!con && validPiece(tempcol1, temprow1)) {
                    char col1 = _colIndex.get(tempcol1);
                    char row1 = _rowIndex.get(temprow1);
                    int k1 = index(col1, row1);
                    // Target is empty
                    if (!_pieces.get(k1).isPiece()) {
                        if (isLegalJump(col0, row0, col1, row1, pieces)) {
                            idx = true;
                            Move next = move(col0, row0, col1, row1);
                            getJumps(moves, tempcol1, temprow1, move(move, next)
                                    , jumpPieces(col0, row0, col1, row1, pieces));
                        }
                    }
                }
            }
        }
        // if not change
        if (!idx) {
            if (move != null) {
                moves.add(move);
            }
        }

    }

    /** Jump on pieces. */
    private HashMap<Integer, PieceColor> jumpPieces(char col0, char row0, char col1, char row1, HashMap<Integer, PieceColor> pieces) {
        int k0 = index(col0, row0);
        int k1 = index(col1, row1);
        Move mov = move(col0, row0, col1, row1);
        int km = mov.jumpedIndex();
        PieceColor temp = pieces.get(k0);
        pieces.put(k1, temp);
        pieces.put(k0, EMPTY);
        pieces.put(km,EMPTY);
        return pieces;
    }

    /** If a jump is legal. */
    private boolean isLegalJump(char col0, char row0, char col1, char row1, HashMap<Integer, PieceColor> pieces) {
        // if valid square
        if (!validSquare(col0, row0) || !validSquare(col1, row1)) {
            return false;
        }
        // start piece, middle piece(diff), end empty
        int intcol0 = _axisIndex.get(col0);
        int intcol1 = _axisIndex.get(col1);
        int introw0 = _axisIndex.get(row0);
        int introw1 = _axisIndex.get(row1);
        // is jump?
        Move mov = move(col0, row0, col1, row1);
        if (!mov.isJump()) {
            return false;
        }
        // mid valid?
        int intmidcol = (intcol0 + intcol1) / 2;
        int intmidrow = (introw0 + introw1) / 2;
        if (!validPiece(intmidcol, intmidrow)) {
            return false;
        }

        int k0 = index(col0, row0);
        int k1 = index(col1, row1);
        char midcol = _colIndex.get(intmidcol);
        char midrow = _rowIndex.get(intmidrow);
        int km = index(midcol, midrow);
        // start piece
        if (!pieces.get(k0).isPiece()) {
            return false;
        }
        // middle opposite
        if (!pieces.get(k0).opposite().equals(pieces.get(km))) {
            return false;
        }
        // end empty
        if (pieces.get(k1).isPiece()) {
            return false;
        }
        // diagonal check
        if (k0 % 2 == 1 && k1 % 2 == 1 && km % 2 == 1) {
            return false;
        }
        return true;
    }

    /** Helper determining validity. */
    private boolean validPiece(int c, int r) {
        boolean con1 = c >= 1 && c <= 5;
        boolean con2 = r >= 1 && r <= 5;
        return con1 && con2;
    }

    /** Helper parameters for getJumps. */
    private List<Integer> rowFactors = new ArrayList<>();
    {
        rowFactors.add(-2);
        rowFactors.add(0);
        rowFactors.add(2);
    }
    /** Helper parameters for getMoves. */
    private List<Integer> colFactors = rowFactors;

    /** Add all legal non-captures from the position with linearized index K
     *  to MOVES. */
    private void getMoves(ArrayList<Move> moves, int k) {
        if (validSquare(k) && _pieces.get(k).isPiece()) {
            char tempcol0 = col(k);
            char temprow0 = row(k);
            int col0 = _axisIndex.get(tempcol0);
            int row0 = _axisIndex.get(temprow0);
            HashMap<Integer, PieceColor> pieces = new HashMap<>(pieces());
            getMoves(moves, col0, row0, null, pieces);
        }
    }


    /** Helper function for getMoves. */
    private void getMoves(ArrayList<Move> moves, int tempcol0, int temprow0, Move move, HashMap<Integer, PieceColor> pieces) {

        char col0 = _colIndex.get(tempcol0);
        char row0 = _rowIndex.get(temprow0);

        boolean idx = false;

        for (int i : rFactors) {
            for (int j : cFactors) {
                boolean con = i == 0 && j == 0;
                int tempcol1 = tempcol0 + j;
                int temprow1 = temprow0 + i;
                // Target is valid
                if (!con && validPiece(tempcol1, temprow1)) {
                    char col1 = _colIndex.get(tempcol1);
                    char row1 = _rowIndex.get(temprow1);
                    int k1 = index(col1, row1);
                    // Target is empty
                    if (!_pieces.get(k1).isPiece()) {
                        if (isLegalMove(col0, row0, col1, row1, pieces)) {
                            idx = true;
                            Move mov = move(col0, row0, col1, row1);
                            moves.add(mov);
                        }
                    }
                }
            }
        }
    }

    /** Move on pieces. */
    private HashMap<Integer, PieceColor> movePieces(char col0, char row0, char col1, char row1, HashMap<Integer, PieceColor> pieces) {
        int k0 = index(col0, row0);
        int k1 = index(col1, row1);
        PieceColor temp = pieces.get(k0);
        pieces.put(k1, temp);
        pieces.put(k0, EMPTY);
        return pieces;
    }

    /** If a move is legal. */
    private boolean isLegalMove(char col0, char row0, char col1, char row1, HashMap<Integer, PieceColor> pieces) {
        // valid square?
        if (!validSquare(col0, row0) || !validSquare(col1, row1)) {
            return false;
        }
        // get index
        int k0 = index(col0, row0);
        int k1 = index(col1, row1);
        // start piece, end empty
        if (!pieces.get(k0).isPiece() || pieces.get(k1).isPiece()) {
            return false;
        }
        // not going back
        if (pieces.get(k0).equals(WHITE)) {
            int introw0 = _axisIndex.get(row0);
            int introw1 = _axisIndex.get(row1);
            if (introw1 < introw0) {
                return false;
            }
            // not move at line
            if (introw0 == 5) {
                return false;
            }
        } else if (pieces.get(k0).equals(BLACK)) {
            int introw0 = _axisIndex.get(row0);
            int introw1 = _axisIndex.get(row1);
            if (introw1 > introw0) {
                return false;
            }
            // not move at line
            if (introw0 == 1) {
                return false;
            }
        }
        // diagonal check
        if (k0 % 2 == 1 && k1 % 2 == 1) {
            return false;
        }
        return true;
    }

    /** Helper parameters for getJumps. */
    private List<Integer> rFactors = new ArrayList<>();
    {
        rFactors.add(-1);
        rFactors.add(0);
        rFactors.add(1);
    }
    /** Helper parameters for getMoves. */
    private List<Integer> cFactors = rFactors;


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
                || !_pieces.get(k).isPiece()) {
            return false;
        }
        char col0 = col(k);
        char row0 = row(k);
        int intcol = _axisIndex.get(col0);
        int introw = _axisIndex.get(row0);

        int lintcol = intcol - 1;
        int luintcol = intcol - 1;
        int ldintcol = intcol - 1;
        int lintrow = introw;
        int luintrow = introw + 1;
        int ldintrow = introw - 1;

        int uintcol = intcol;
        int dintcol = intcol;
        int uintrow = introw + 1;
        int dintrow = introw - 1;

        int rintcol = intcol + 1;
        int ruintcol = intcol + 1;
        int rdintcol = intcol + 1;
        int rintrow = introw;
        int ruintrow = introw + 1;
        int rdintrow = introw - 1;

        // left and right
        if (validPiece(lintcol, lintrow) && validPiece(rintcol, rintrow)) {
            char lcol = _colIndex.get(lintcol);
            char rcol = _colIndex.get(rintcol);
            char lrow = _rowIndex.get(lintrow);
            char rrow = _rowIndex.get(rintrow);
            int l = index(lcol, lrow);
            int r = index(rcol, rrow);
            boolean con1 = _pieces.get(l).isPiece() && _pieces.get(l).opposite().equals(_pieces.get(k)) && !_pieces.get(r).isPiece();
            boolean con2 = _pieces.get(r).isPiece() && _pieces.get(r).opposite().equals(_pieces.get(k)) && !_pieces.get(l).isPiece();
            if (con1 || con2) {
                return true;
            }
        }

        // up and down
        if (validPiece(uintcol, uintrow) && validPiece(dintcol, dintrow)) {
            char ucol = _colIndex.get(uintcol);
            char dcol = _colIndex.get(dintcol);
            char urow = _rowIndex.get(uintrow);
            char drow = _rowIndex.get(dintrow);
            int u = index(ucol, urow);
            int d = index(dcol, drow);
            boolean con1 = _pieces.get(u).isPiece() && _pieces.get(u).opposite().equals(_pieces.get(k)) && !_pieces.get(d).isPiece();
            boolean con2 = _pieces.get(d).isPiece() && _pieces.get(d).opposite().equals(_pieces.get(k)) && !_pieces.get(u).isPiece();
            if (con1 || con2) {
                return true;
            }
        }

        if (k % 2 == 0) {
            // lu and rd
            if (validPiece(luintcol, luintrow) && validPiece(rdintcol, rdintrow)) {
                char lucol = _colIndex.get(luintcol);
                char rdcol = _colIndex.get(rdintcol);
                char lurow = _rowIndex.get(luintrow);
                char rdrow = _rowIndex.get(rdintrow);
                int lu = index(lucol, lurow);
                int rd = index(rdcol, rdrow);
                boolean con1 = _pieces.get(lu).isPiece() &&  _pieces.get(lu).opposite().equals(_pieces.get(k)) && !_pieces.get(rd).isPiece();
                boolean con2 = _pieces.get(rd).isPiece() && _pieces.get(rd).opposite().equals(_pieces.get(k)) && !_pieces.get(lu).isPiece();
                if (con1 || con2) {
                    return true;
                }
            }

            // ld and ru
            if (validPiece(ruintcol, ruintrow) && validPiece(ldintcol, ldintrow)) {
                char rucol = _colIndex.get(ruintcol);
                char ldcol = _colIndex.get(ldintcol);
                char rurow = _rowIndex.get(ruintrow);
                char ldrow = _rowIndex.get(ldintrow);
                int ru = index(rucol, rurow);
                int ld = index(ldcol, ldrow);
                boolean con1 = _pieces.get(ru).isPiece() && _pieces.get(ru).opposite().equals(_pieces.get(k)) && !_pieces.get(ld).isPiece();
                boolean con2 = _pieces.get(ld).isPiece() && _pieces.get(ld).opposite().equals(_pieces.get(k)) && !_pieces.get(ru).isPiece();
                if (con1 || con2) {
                    return true;
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

        if (mov == null) {
            return;
        }
//        System.out.println(reverseBoard());
        boardList.add(reverseBoard());
        while (mov != null) {
            int position0 = mov.fromIndex();
            int position1 = mov.toIndex();

            if (mov.isJump()) {
                int jumped = mov.jumpedIndex();
                PieceColor type0 = _pieces.get(position0);
                insertPiece(position1, type0);
                removePiece(position0);
                removePiece(jumped);
                _movedNotJumped = new ArrayList<>();
            } else {
                List pair = new ArrayList();
                pair.add(position0);
                pair.add(position1);
                if (!_movedNotJumped.contains(pair) && legalMove(mov)) {
                    PieceColor type0 = _pieces.get(position0);
                    insertPiece(position1, type0);
                    removePiece(position0);
                    List newpair = new ArrayList();
                    newpair.add(position1);
                    newpair.add(position0);
                    _movedNotJumped.add(newpair);
                }
            }
            mov = mov.jumpTail();
        }

        System.out.println(toString());

        // Change player
        takeTurn();

        setChanged();
        notifyObservers();
    }

    /** Record not-retrievable move targets. */
    private List<List> _movedNotJumped = new ArrayList<>();

    /** checkGameOver. */
    public void checkGameOver() {
        List<Move> moves = getMoves();
        if (moves.isEmpty()) {
            _gameOver = true;
        }
        if (_whoseMove.equals(WHITE)) {
            for (Move mov : moves) {
                if (moveBy(mov).equals(WHITE)) {
                    return;
                }
            }
            _gameOver = true;
            _winner = BLACK;
            return;
        } else if (_whoseMove.equals(BLACK)) {
            for (Move mov : moves) {
                if (moveBy(mov).equals(BLACK)) {
                    return;
                }
            }
            _gameOver = true;
            _winner = WHITE;
            return;
        }
    }

    /** Get moves for current player. */
    public ArrayList<Move> getMyMoves(ArrayList<Move> moves, PieceColor player) {
        ArrayList<Move> result = new ArrayList<>();
        for (Move mov : moves) {
            if (moveBy(mov).equals(player)) {
                result.add(mov);
            }
        }
        return result;
    }

    /** Determine move by. */
    private PieceColor moveBy(Move mov) {
        int k0 = mov.fromIndex();
        PieceColor result = _pieces.get(k0);
        return result;
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
        setPieces(string, whoseMove().opposite());
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
        return false;
        // FIXME
        // Waiting
    }

    /** Added by Wayne, take turn for _whoseMove. */
    private void takeTurn() {
        if (_whoseMove.equals(WHITE)) {
            _whoseMove = BLACK;
        } else if (_whoseMove.equals(BLACK)) {
            _whoseMove = WHITE;
        } else {
            throw new Error("The Player is neither white or black. --Board.takeTurn()");
        }
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
        int colDistance = Math.abs(_axisIndex.get(col0) - _axisIndex.get(col1));
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

    /** Added by Wayne, winner. */
    private PieceColor _winner = EMPTY;

    /** Added by Wayne, show winner. */
    public PieceColor winner() {
        return _winner;
    }

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

    /** Reversed board. */
    private String reverseBoard() {
        StringBuilder string = new StringBuilder();
        for (int key = 0 * SIDE; key < 1 * SIDE; key++) {
            PieceColor piece = _pieces.get(key);
            String name = piece.shortName();
            string.append(name);
        }
        for (int key = 1 * SIDE; key < 2 * SIDE; key++) {
            PieceColor piece = _pieces.get(key);
            String name = piece.shortName();
            string.append(name);
        }
        for (int key = 2 * SIDE; key < 3 * SIDE; key++) {
            PieceColor piece = _pieces.get(key);
            String name = piece.shortName();
            string.append(name);
        }
        for (int key = 3 * SIDE; key < 4 * SIDE; key++) {
            PieceColor piece = _pieces.get(key);
            String name = piece.shortName();
            string.append(name);
        }
        for (int key = 4 * SIDE; key <= MAX_INDEX; key++) {
            PieceColor piece = _pieces.get(key);
            String name = piece.shortName();
            string.append(name);
        }
        return string.toString();
    }

//    /** Return a heuristic value for BOARD. */
//    public int staticScore() {
//        String string = board();
//        int black = 0;
//        int white = 0;
//        for (int i = 0; i < string.length(); i++) {
//            char piece = string.charAt(i);
//            if (piece == 'b') {
//                black += 1;
//            } else if (piece == 'w') {
//                white += 1;
//            }
//        }
//        return white - black;
//    }

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
