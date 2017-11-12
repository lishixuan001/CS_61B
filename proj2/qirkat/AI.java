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
        if (myColor() == WHITE) {
            findMax(b, MAX_DEPTH, -INFTY, INFTY);
        } else {
            findMin(b, MAX_DEPTH, -INFTY, INFTY);
        }
        return _lastFoundMove;
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
    private int findMax(Board board, int depth, int alpha, int beta) {
        Move best = null;
        int bestScore = -INFTY;

        board.checkGameOver();
        PieceColor winner = board.winner();

        if (depth == 0 || winner.isPiece()) {
            Board next = new Board(board);
            return flatMax(next, alpha, beta);
        }

        ArrayList<Move> moves = board.getMoves();
        PieceColor currentPlayer = board.whoseMove();
        ArrayList<Move> myMoves = board.getMyMoves(moves, currentPlayer);

        myMoves = filter(myMoves);

        for (Move mov : myMoves) {
            Board next = new Board(board);
            next.makeMove(mov);
            int response = findMin(next, depth - 1, alpha, beta);

            next.undo();
            if (response >= bestScore) {
                best = mov;
                bestScore = response;
                alpha = Math.max(alpha, bestScore);
                if (alpha >= beta) {
                    break;
                }
            }
        }

        _lastFoundMove = best;
        return bestScore;
    }

    /** Find the minimum.
     * @param alpha --input
     * @param beta --input
     * @param board --input
     * @param depth --input
     * @return */
    private int findMin(Board board, int depth, int alpha, int beta) {
        Move best = null;
        int bestScore = INFTY;

        board.checkGameOver();
        PieceColor winner = board.winner();

        if (depth == 0 || winner.isPiece()) {
            Board next = new Board(board);
            return flatMin(next, alpha, beta);
        }

        ArrayList<Move> moves = board.getMoves();
        PieceColor currentPlayer = board.whoseMove();
        ArrayList<Move> myMoves = board.getMyMoves(moves, currentPlayer);

        myMoves = filter(myMoves);

        for (Move mov : myMoves) {
            Board next = new Board(board);
            next.makeMove(mov);
            int response = findMax(next, depth - 1, alpha, beta);
            next.undo();
            if (response <= bestScore) {
                best = mov;
                bestScore = response;
                beta = Math.min(alpha, bestScore);
                if (alpha >= beta) {
                    break;
                }
            }
        }

        _lastFoundMove = best;
        return bestScore;
    }


    /** Last condition for finding maximized possible move/jump.
     * @param board --input
     * @param beta --input
     * @param alpha --input
     * @return */
    private int flatMax(Board board, int alpha, int beta) {
        board.checkGameOver();
        PieceColor winner = board.winner();
        Move best = null;
        int bestScore = -INFTY;

        if (winner.isPiece()) {
            if (winner.equals(WHITE)) {
                bestScore = INFTY;
            } else if (winner.equals(BLACK)) {
                bestScore = -INFTY;
            }
            return bestScore;
        }
        ArrayList<Move> moves = board.getMoves();
        PieceColor currentPlayer = board.whoseMove();
        ArrayList<Move> myMoves = board.getMyMoves(moves, currentPlayer);

        myMoves = filter(myMoves);

        for (Move mv : myMoves) {
            Board bd = new Board(board);
            bd.makeMove(mv);
            int score = staticScore(bd);
            bd.undo();
            if (score >= bestScore) {
                best = mv;
                bestScore = score;
                alpha = Math.max(alpha, bestScore);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        _lastFoundMove = best;
        return bestScore;
    }

    /** Last condition for finding maximized possible move/jump.
     * @param alpha --input
     * @param beta --input
     * @param board --input
     * @return */
    private int flatMin(Board board, int alpha, int beta) {

        board.checkGameOver();
        PieceColor winner = board.winner();
        Move best = null;
        int bestScore = INFTY;

        if (winner.isPiece()) {
            if (winner.equals(WHITE)) {
                bestScore = INFTY;
            } else if (winner.equals(BLACK)) {
                bestScore = -INFTY;
            }
            return bestScore;
        }

        ArrayList<Move> moves = board.getMoves();
        PieceColor currentPlayer = board.whoseMove();
        ArrayList<Move> myMoves = board.getMyMoves(moves, currentPlayer);

        myMoves = filter(myMoves);

        for (Move mv : myMoves) {
            Board bd = new Board(board);
            bd.makeMove(mv);
            int score = staticScore(bd);
            bd.undo();
            if (score <= bestScore) {
                best = mv;
                bestScore = score;
                beta = Math.min(alpha, bestScore);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        _lastFoundMove = best;
        return bestScore;
    }

    /** Return a heuristic value for BOARD.*/
    private int staticScore(Board board) {
        String string = board.board();
        int black = 0;
        int white = 0;
        for (int i = 0; i < string.length(); i++) {
            char piece = string.charAt(i);
            if (piece == 'b') {
                black += 1;
            } else if (piece == 'w') {
                white += 1;
            }
        }
        return white - black;
    }

    /** Filter the moves.
     * @param moves --input
     * @return */
    private ArrayList<Move> filter(ArrayList<Move> moves) {
        ArrayList<Move> result = new ArrayList<>();
        Move first = null;
        Move second = null;
        for (Move mov : moves) {
            int s = mov.length();
            if (first == null) {
                first = mov;
            } else if (second == null) {
                second = mov;
            } else {
                if (s >= first.length()) {
                    Move temp = first;
                    first = mov;
                    second = temp;
                } else if (s >= second.length()) {
                    second = mov;
                }
            }
        }
        if (first != null) {
            result.add(first);
        }
        return result;
    }
}