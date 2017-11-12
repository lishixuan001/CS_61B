package qirkat;

import java.util.ArrayList;

import static qirkat.PieceColor.*;

/** A Player that computes its own moves.
 *  @author Shixuan (Wayne) Li
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 8;
    /** A position magnitude indicating a win (for white if positive, black
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. */
    AI(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove(Command cmnd) {
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();

        StringBuilder msg = new StringBuilder();
        msg.append(pieceColorToString(myColor()));
        msg.append(" moves ");

        String mov = move.toString();
        msg.append(mov);
        msg.append(".");

        System.out.println(msg.toString());
        return move;
    }

    /** PieceColor to String.
     * @param piececolor --input
     * @return */
    private String pieceColorToString(PieceColor piececolor) {
        String string = "";
        if (piececolor.equals(WHITE)) {
            string = "White";
        } else if (piececolor.equals(BLACK)) {
            string = "Black";
        }
        return string;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        return findMove(b, MAX_DEPTH, -INFTY, INFTY);
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private Move findMove(Board board, int depth, int alpha, int beta) {
        board.checkGameOver();
        ArrayList<Move> moves = board.getMoves();
        PieceColor currentPlayer = board.whoseMove();
        ArrayList<Move> myMoves = board.getMyMoves(moves, currentPlayer);
        Move mov = filter(myMoves);
        return mov;
    }

    /** Filter the moves.
     * @param moves --input
     * @return */
    private Move filter(ArrayList<Move> moves) {
        Move first = null;
        for (Move mov : moves) {
            int s = mov.length();
            if (first == null) {
                first = mov;
            } else {
                if (s >= first.length()) {
                    first = mov;
                }
            }
        }
        return first;
    }
}