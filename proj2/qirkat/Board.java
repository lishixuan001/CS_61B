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
        boardList = new ArrayList<>();
        setPieces(INIT_PIECES, WHITE);
        _board = board();
        _state = "set_up";
        _winner = EMPTY;
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
        _movedNotJumped = b._movedNotJumped;
        _winner = b.winner();

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

    /** Just get moves, not jumps.
     * @return --ArrayList with just all possible moves(no jumps) */
    ArrayList<Move> getJustMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        for (int k = 0; k <= MAX_INDEX; k += 1) {
            getMoves(moves, k);
        }
        return moves;
    }

    /** Add all legal capturing moves from the position
     *  with linearized index K to MOVES. */
    private void getJumps(ArrayList<Move> moves, int k) {
        // run only if piece is valid, and piece is White/Black
        if (validSquare(k) && _pieces.get(k).isPiece()) {
            char tempcol0 = col(k);
            char temprow0 = row(k);
            int col0 = _axisIndex.get(tempcol0);
            int row0 = _axisIndex.get(temprow0);
            HashMap<Integer, PieceColor> pieces = new HashMap<>(pieces());
            getJumps(moves, col0, row0, null, pieces);
        }
    }

    /** Helper function for getJumps.
     * @param moves --input
     * @param move --input
     * @param pieces --input
     * @param tempcol0 --input
     * @param temprow0 --input
     * */
    private void getJumps(ArrayList<Move> moves, int tempcol0, int temprow0
            , Move move, HashMap<Integer, PieceColor> pieces) {

        char col0 = _colIndex.get(tempcol0);
        char row0 = _rowIndex.get(temprow0);

        // index to check if any around piece support possible jumps
        boolean idx = false;

        // check every piece that is 2-steps away
        for (int i : rowFactors) {
            for (int j : colFactors) {
                boolean con = i == 0 && j == 0;
                int tempcol1 = tempcol0 + j;
                int temprow1 = temprow0 + i;

                // check if the piece is valid
                if (!con && validPiece(tempcol1, temprow1)) {
                    char col1 = _colIndex.get(tempcol1);
                    char row1 = _rowIndex.get(temprow1);
                    int k1 = index(col1, row1);

                    // check if the target piece is White/Black
                    if (!_pieces.get(k1).isPiece()) {

                        // check if the two points support legal jump
                        if (isLegalJump(col0, row0, col1, row1, pieces)) {
                            idx = true;
                            Move next = move(col0, row0, col1, row1);
                            getJumps(moves, tempcol1, temprow1, move(move, next)
                                    , jumpPieces(col0, row0, col1, row1
                                            , pieces));
                        }
                    }
                }
            }
        }
        // if no around piece support legal jump, add input move(if not null)
        if (!idx) {
            if (move != null) {
                moves.add(move);
            }
        }

    }

    /** Jump on pieces.
     * @param pieces --input
     * @param col0 --input
     * @param col1 --input
     * @param row0 --input
     * @param row1 --input
     * @return */
    private HashMap<Integer, PieceColor> jumpPieces(char col0, char row0
            , char col1, char row1, HashMap<Integer, PieceColor> pieces) {
        int k0 = index(col0, row0);
        int k1 = index(col1, row1);
        Move mov = move(col0, row0, col1, row1);
        int km = mov.jumpedIndex();
        PieceColor temp = pieces.get(k0);
        pieces.put(k1, temp);
        pieces.put(k0, EMPTY);
        pieces.put(km, EMPTY);
        return pieces;
    }

    /** Recursively test legal Jump. */
    boolean isLegalJump(Move mov) {
        boolean result = true;
        HashMap<Integer, PieceColor> pieces = new HashMap<>(_pieces);

        // We need to recursively check legal jump
        while(mov != null) {
            char col0 = mov.col0();
            char row0 = mov.row0();
            char col1 = mov.col1();
            char row1 = mov.row1();

            int intcol0 = _axisIndex.get(col0);
            int intcol1 = _axisIndex.get(col1);
            int introw0 = _axisIndex.get(row0);
            int introw1 = _axisIndex.get(row1);

            result = result && isLegalJump(col0, row0, col1, row1, pieces);

            // if this step is legal jump, then do makeMove operation
            if (result) {
                int intmidcol = (intcol0 + intcol1) / 2;
                int intmidrow = (introw0 + introw1) / 2;

                char midcol = _colIndex.get(intmidcol);
                char midrow = _rowIndex.get(intmidrow);

                int k0 = index(col0, row0);
                int k1 = index(col1, row1);
                int km = index(midcol, midrow);

                pieces.put(k1, pieces.get(k0));
                pieces.put(k0, EMPTY);
                pieces.put(km, EMPTY);

                mov = mov.jumpTail();
            } else {
                return result;
            }

        }

        return result;
    }

    /** If one jump is legal.
     * @param row1 --input
     * @param row0 --input
     * @param col1 --input
     * @param col0 --input
     * @param pieces --input
     * @return */
    private boolean isLegalJump(char col0, char row0, char col1, char row1
            , HashMap<Integer, PieceColor> pieces) {

        // if start/end pieces are valid
        if (!validSquare(col0, row0) || !validSquare(col1, row1)) {
            return false;
        }

        int intcol0 = _axisIndex.get(col0);
        int intcol1 = _axisIndex.get(col1);
        int introw0 = _axisIndex.get(row0);
        int introw1 = _axisIndex.get(row1);

        // false if it's not a jump
        Move mov = move(col0, row0, col1, row1);
        if (!mov.isJump()) {
            return false;
        }

        // check if the jump jumps over exactly one piece
        int diffcol = Math.abs(intcol0 - intcol1);
        int diffrow = Math.abs(introw0 - introw1);
        boolean con1 = diffcol == 2 && diffrow == 0;
        boolean con2 = diffcol == 0 && diffrow == 2;
        boolean con3 = diffcol == 2 && diffrow == 2;
        boolean condition = con1 || con2 || con3;
        if (!condition) {
            return false;
        }

        // false if the jumped middle piece is not valid
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

        // false if start piece is Empty
        if (!pieces.get(k0).isPiece()) {
            return false;
        }

        // false if jumped piece not have opposite as start
        if (!pieces.get(k0).opposite().equals(pieces.get(km))) {
            return false;
        }

        // false if end piece is not Empty
        if (pieces.get(k1).isPiece()) {
            return false;
        }

        // false if illegal diagonal jump
        if (k0 % 2 == 1 && k1 % 2 == 1 && km % 2 == 1) {
            return false;
        }

        return true;
    }

    /** Helper determining validity.
     * @param c --input
     * @param r --input
     * @return */
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


    /** Helper function for getMoves.
     * @param pieces --input
     * @param temprow0 --input
     * @param tempcol0 --input
     * @param moves --input
     * @param move --input*/
    private void getMoves(ArrayList<Move> moves, int tempcol0, int temprow0
            , Move move, HashMap<Integer, PieceColor> pieces) {

        char col0 = _colIndex.get(tempcol0);
        char row0 = _rowIndex.get(temprow0);

        for (int i : rFactors) {
            for (int j : cFactors) {
                boolean con = i == 0 && j == 0;
                int tempcol1 = tempcol0 + j;
                int temprow1 = temprow0 + i;

                if (!con && validPiece(tempcol1, temprow1)) {
                    char col1 = _colIndex.get(tempcol1);
                    char row1 = _rowIndex.get(temprow1);
                    int k1 = index(col1, row1);

                    if (!_pieces.get(k1).isPiece()) {
                        if (isLegalMove(col0, row0, col1, row1, pieces)) {
                            Move mov = move(col0, row0, col1, row1);
                            moves.add(mov);
                        }
                    }
                }
            }
        }
    }

    /** Return true iff MOV is legal on the current board. */
    boolean isLegalMove(Move mov) {
        if (mov == null) {
            return false;
        }
        char col0 = mov.col0();
        char col1 = mov.col1();
        char row0 = mov.row0();
        char row1 = mov.row1();
        return isLegalMove(col0, row0, col1, row1, _pieces);
    }

    /** If a move is legal.
     * @param row1 --input
     * @param row0 --input
     * @param col1 --input
     * @param col0 --input
     * @param pieces --input
     * @return --boolean */
    @SuppressWarnings("unchecked")
    private boolean isLegalMove(char col0, char row0, char col1, char row1
            , HashMap<Integer, PieceColor> pieces) {

        // false if invalid start/end
        if (!validSquare(col0, row0) || !validSquare(col1, row1)) {
//            System.out.println("Problem 1");
            return false;
        }

        // get start/end index
        int k0 = index(col0, row0);
        int k1 = index(col1, row1);

        // false if start is not White/Black
        if ((!pieces.get(k0).isPiece()) || pieces.get(k1).isPiece()) {
//            System.out.println("Problem 2");
//            System.out.println(board().charAt(12));
//            System.out.println(board().charAt(17));
//            System.out.print(col0);System.out.print(row0);
//            System.out.print("-");
//            System.out.print(col1);System.out.print(row1);
            return false;
        }

        int intcol0 = _axisIndex.get(col0);
        int intcol1 = _axisIndex.get(col1);
        int introw0 = _axisIndex.get(row0);
        int introw1 = _axisIndex.get(row1);

        // false if not move by one unit step
        int diffcol = Math.abs(intcol0 - intcol1);
        int diffrow = Math.abs(introw0 - introw1);
        boolean con1 = diffcol == 1 && diffrow == 0;
        boolean con2 = diffcol == 0 && diffrow == 1;
        boolean con3 = diffcol == 1 && diffrow == 1;
        boolean condition = con1 || con2 || con3;
        if (!condition) {
//            System.out.println("Problem 3");
            return false;
        }

        // false if move back, or, move at very end line
        if (pieces.get(k0).equals(WHITE)) {
            introw0 = _axisIndex.get(row0);
            introw1 = _axisIndex.get(row1);
            if (introw1 < introw0) {
//                System.out.println("Problem 4");
                return false;
            }

            if (introw0 == 5) {
//                System.out.println("Problem 5");
                return false;
            }
        } else if (pieces.get(k0).equals(BLACK)) {
            introw0 = _axisIndex.get(row0);
            introw1 = _axisIndex.get(row1);
            if (introw1 > introw0) {
//                System.out.println("Problem 6");
                return false;
            }

            if (introw0 == 1) {
//                System.out.println("Problem 7");
                return false;
            }
        }

        // false if illegal diagonal move
        if (k0 % 2 == 1 && k1 % 2 == 1) {
//            System.out.println("Problem 8");
            return false;
        }

        // false if illegally move between two points
        List pair = new ArrayList();
        pair.add(k0);
        pair.add(k1);
        if (_movedNotJumped.contains(pair)) {
//            System.out.println("Problem 9");
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

        // check horizontal/vertical jumps
        if (checkJumpPossible(k, lintcol, lintrow, rintcol, rintrow)) {
            return true;
        }
        if (checkJumpPossible(k, uintcol, uintrow, dintcol, dintrow)) {
            return true;
        }

        // if at allow-diagonal place, consider diagonals
        if (k % 2 == 0) {
            if (checkJumpPossible(k, luintcol, luintrow, rdintcol, rdintrow)) {
                return true;
            }
            if (checkJumpPossible(k, ruintcol, ruintrow, ldintcol, ldintrow)) {
                return true;
            }
        }
        return false;
    }

    /** Helper jumpPossible. Check Possible.
     * @param col0 --input
     * @param col1 --input
     * @param row1 --input
     * @param row0 --input
     * @param k --input
     * @return --if two pieces have can-jump relation */
    private boolean checkJumpPossible(int k, int col0, int row0
            , int col1, int row1) {

        // false if invalid two pieces
        if (validPiece(col0, row0) && validPiece(col1, row1)) {
            char lcol = _colIndex.get(col0);
            char rcol = _colIndex.get(col1);
            char lrow = _rowIndex.get(row0);
            char rrow = _rowIndex.get(row1);

            int l = index(lcol, lrow);
            int r = index(rcol, rrow);

            // conditions if two pieces have can-jump relations
            boolean con1 = _pieces.get(l).isPiece()
                    && _pieces.get(l).opposite().equals(_pieces.get(k))
                    && !_pieces.get(r).isPiece();
            boolean con2 = _pieces.get(r).isPiece()
                    && _pieces.get(r).opposite().equals(_pieces.get(k))
                    && !_pieces.get(l).isPiece();
            if (con1 || con2) {
                return true;
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

    /** Make the Move MOV on this Board, assuming it is legal. */
    @SuppressWarnings("unchecked")
    void makeMove(Move mov) {

        // ignore "null" moves
        if (mov == null) {
            return;
        }

        // record current map for further "undo"
        boardList.add(reverseBoard());

        // assert mov is not null since we further have things
        // like takeTurn that don't want to run for any "null" move.
        while (mov != null) {
            int position0 = mov.fromIndex();
            int position1 = mov.toIndex();

            // isJump, or, isMove?
            if (mov.isJump()) {

                // recursively check if legal jump
                if (isLegalJump(mov)) {
                    int jumped = mov.jumpedIndex();
                    PieceColor type0 = _pieces.get(position0);

                    // operate the move
                    insertPiece(position1, type0);
                    removePiece(position0);
                    removePiece(jumped);
                    _movedNotJumped = new ArrayList<>();
                } else {
                    System.out.println("This is an illegal jump.");
                    return;
                }
            } else {

                // check if legal move
                if (isLegalMove(mov)) {
                    PieceColor type0 = _pieces.get(position0);

                    // operate the move
                    insertPiece(position1, type0);
                    removePiece(position0);

                    // add the move to forbidden list(_movedNotJumped)
                    List pair = new ArrayList();
                    pair.add(position1);
                    pair.add(position0);
                    _movedNotJumped.add(pair);
                } else {
                    System.out.println("This is an illegal move.");
                    return;
                }
            }
            mov = mov.jumpTail();
        }
        takeTurn();

        setChanged();
        notifyObservers();
    }

    /** Record not-retrievable move targets. */
    private List<List> _movedNotJumped = new ArrayList<>();

    /** checkGameOver.*/
    void checkGameOver() {
        List<Move> moves = getMoves();
        List<Move> movs = getJustMoves();

        // Check if the current player can jump or move,
        // if cannot, gameOver and the other wins.

        if (_whoseMove.equals(WHITE)) {
            for (Move mov : moves) {
                if (moveBy(mov).equals(WHITE)) {
                    return;
                }
            }
            if (jumpPossible()) {

                for (Move mv : movs) {
                    if (moveBy(mv).equals(WHITE)) {
                        return;
                    }
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
            if (jumpPossible()) {
                for (Move mv : movs) {
                    if (moveBy(mv).equals(BLACK)) {
                        return;
                    }
                }
            }
            _gameOver = true;
            _winner = WHITE;
            return;
        }
    }

    /** Check if moves the right piece (piece of current player's) */
    boolean checkMoveMyPiece(Move mov) {
        if (mov == null) {
            return true;
        }
        // true if start piece is current player's
        int k0 = mov.fromIndex();
        PieceColor start = _pieces.get(k0);

        return start.equals(_whoseMove);
    }

    /** Get moves for current player.
     * @param moves --input
     * @param player --input
     * @return */
    @SuppressWarnings("unchecked")
    ArrayList<Move> getMyMoves(ArrayList<Move> moves
            , PieceColor player) {
        ArrayList<Move> result = new ArrayList<>();
        for (Move mov : moves) {
            if (moveBy(mov).equals(player)) {
                List pair = new ArrayList();
                pair.add(mov.fromIndex());
                pair.add(mov.toIndex());
                if (!_movedNotJumped.contains(pair)) {
                    result.add(mov);
                }
            }
        }
        return result;
    }

    /** Determine move by.
     * @param mov --input
     * @return */
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

        if (legend) {
            string.append(rowIndex);
        }
        string.append(" ");

        for (int i = 0; i <= MAX_INDEX; i++) {
            Character name = board().charAt(i);
            if (i % 5 == 0 && i != 0) {
                string.append("\n");
                rowIndex -= 1;
                if (legend) {
                    string.append(rowIndex);
                }
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

    /** Added by Wayne, take turn for _whoseMove. */
    private void takeTurn() {
        if (_whoseMove.equals(WHITE)) {
            _whoseMove = BLACK;
        } else if (_whoseMove.equals(BLACK)) {
            _whoseMove = WHITE;
        } else {
            throw new Error("The Player is neither"
                    + " white or black. --Board.takeTurn()");
        }
    }

    /** Player that is on move. */
    private PieceColor _whoseMove;

    /** Set true when game ends. */
    private boolean _gameOver;

    /** Convenience value giving values of pieces at each ordinal position. */
    static final PieceColor[] PIECE_VALUES = PieceColor.values();

    /** Added by Wayne, track moves' changes by remembering the board. */
    private List<String> boardList = new ArrayList<>();

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

    /** Added by Wayne, create HashMap for convenience tracking
     *  piece content. */
    private HashMap<Integer, PieceColor> _pieces = new HashMap<>();

    /** Added by Wayne, get _pieces.*/
    private HashMap<Integer, PieceColor> pieces() {
        return _pieces;
    }

    /** Added by Wayne, get current state.*/
    private String state() {
        return _state;
    }

    /** Added by Wayne, indicating board state.*/
    private String _state;

    /** Added by Wayne, winner. */
    private PieceColor _winner = EMPTY;

    /** Added by Wayne, show winner.*/
    public PieceColor winner() {
        return _winner;
    }

    /** Added by Wayne, get board map.*/
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

    /** Reversed board. For better fitting undo need
     * for setPiece in correct direction. */
    public String reverseBoard() {
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
