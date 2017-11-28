package qirkat;

import java.util.ArrayList;

import static qirkat.PieceColor.*;

/** A Player that computes its own moves.
 *  @author Shixuan (Wayne) Li
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 8;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. */
    AI(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove(Command cmnd) {
        // Get the move
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();

        // Show the move "White moves c2-a2-a4"
        StringBuilder msg = new StringBuilder();
        msg.append(pieceColorToString(myColor()));
        msg.append(" moves ");

        String mov = move.toString();
        msg.append(mov);
        msg.append(".");

        System.out.println(msg.toString());
        return move;
    }

    /** Transform PieceColor to String.
     * @param piececolor --input
     * @return --change PieceColor to a string */
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
        // Take "White wins." as highest board score.
        // Take "Black wins." as lowest board score.
        if (myColor() == WHITE) {
            // White -> Maximizing overall board score
            findMax(b, MAX_DEPTH, -INFTY, INFTY);
        } else {
            // Black-> Minimizing overall board score
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
        int response;

        board.checkGameOver();
        PieceColor winner = board.winner();

        // If end of depth searching |or| gameOver
        if (depth == 0 || winner.isPiece()) {
            return flatMax(board, alpha, beta, depth);
        }

        // Get all possible moves on board
        ArrayList<Move> moves = board.getMoves();
        PieceColor currentPlayer = board.whoseMove();

        // Get moves by current Player
        ArrayList<Move> myMoves = board.getMyMoves(moves, currentPlayer);

        // If Empty (in case the condition that there are jumps
        // but none of them are from the current Player) -- prevent returning "null" move
        if (myMoves.isEmpty()) {

            // If jumpPossible, get all "moves "
            // (which means the previously collected moves are all "jumps")
            if (board.jumpPossible()) {
                moves = board.getJustMoves();

                // Get all moves from current Player
                myMoves = board.getMyMoves(moves, currentPlayer);
            }
        }

        // A filter than select preferred moves from myMoves
        myMoves = filter(myMoves);

        // Evaluating each move -> get score for each move
        for (Move mov : myMoves) {

            // Do the move, mimic response, them undo back
            board.makeMove(mov);
            response = findMin(board, depth - 1, alpha, beta);
            board.undo();

            // Pruning. If know there is winning possibility, do the winning one.
            // If know guaranteed lost, try to do the most-step one.

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
     * @return --Return minimum possible score */
    private int findMin(Board board, int depth, int alpha, int beta) {
        Move best = null;
        int bestScore = INFTY;
        int response;

        board.checkGameOver();
        PieceColor winner = board.winner();

        // If end of depth searching |or| gameOver
        if (depth == 0 || winner.isPiece()) {
            return flatMin(board, alpha, beta, depth);
        }

        // Get all possible moves on board
        ArrayList<Move> moves = board.getMoves();
        PieceColor currentPlayer = board.whoseMove();

        // Get moves by current Player
        ArrayList<Move> myMoves = board.getMyMoves(moves, currentPlayer);

        // If Empty (in case the condition that there are jumps
        // but none of them are from the current Player) -- prevent returning "null" move
        if (myMoves.isEmpty()) {

            // If jumpPossible, get all "moves "
            // (which means the previously collected moves are all "jumps")
            if (board.jumpPossible()) {
                moves = board.getJustMoves();

                // Get all moves from current Player
                myMoves = board.getMyMoves(moves, currentPlayer);
            }
        }

        // A filter than select preferred moves from myMoves
        myMoves = filter(myMoves);

        // Evaluating each move -> get score for each move
        for (Move mov : myMoves) {

            // Do the move, mimic response, them undo back
            board.makeMove(mov);
            response = findMax(board, depth - 1, alpha, beta);
            board.undo();

            // Pruning
            if (response <= bestScore) {
                best = mov;
                bestScore = response;
                beta = Math.min(beta, bestScore);
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
     * @return --Maximum return for an end of search */
    private int flatMax(Board board, int alpha, int beta, int depth) {
        board.checkGameOver();
        PieceColor winner = board.winner();
        Move best = null;
        int bestScore = -INFTY;

        // If gameOver.
        // "White wins." -> Maximum | "Black wins." -> Minimum
        if (winner.isPiece()) {
            if (winner.equals(WHITE)) {
                bestScore = INFTY / (9 - depth);
            } else if (winner.equals(BLACK)) {
                bestScore = -INFTY / (9 - depth);
            }
            return bestScore;
        }

        // Get all possible moves on board
        ArrayList<Move> moves = board.getMoves();
        PieceColor currentPlayer = board.whoseMove();

        // Get moves by current Player
        ArrayList<Move> myMoves = board.getMyMoves(moves, currentPlayer);

        // If Empty (in case the condition that there are jumps
        // but none of them are from the current Player) -- prevent returning "null" move
        if (myMoves.isEmpty()) {

            // If jumpPossible, get all "moves "
            // (which means the previously collected moves are all "jumps")
            if (board.jumpPossible()) {
                moves = board.getJustMoves();

                // Get all moves from current Player
                myMoves = board.getMyMoves(moves, currentPlayer);
            }
        }

        // A filter than select preferred moves from myMoves
        myMoves = filter(myMoves);

        // Evaluating each move -> get score for each move
        for (Move mov : myMoves) {

            // Do the move, get score, them undo back
            board.makeMove(mov);
            int score = staticScore(board, depth);
            board.undo();

            // Pruning
            if (score >= bestScore) {
                best = mov;
                bestScore = score;
                alpha = Math.max(alpha, bestScore);
                if (alpha >= beta) {
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
     * @return --Minimum return for an end of search */
    private int flatMin(Board board, int alpha, int beta, int depth) {

        board.checkGameOver();
        PieceColor winner = board.winner();
        Move best = null;
        int bestScore = INFTY;

        // If gameOver.
        // "White wins." -> Maximum | "Black wins." -> Minimum
        if (winner.isPiece()) {
            if (winner.equals(WHITE)) {
                bestScore = INFTY / (9 - depth);
            } else if (winner.equals(BLACK)) {
                bestScore = -INFTY / (9 - depth);
            }
            return bestScore;
        }

        // Get all possible moves on board
        ArrayList<Move> moves = board.getMoves();
        PieceColor currentPlayer = board.whoseMove();

        // Get moves by current Player
        ArrayList<Move> myMoves = board.getMyMoves(moves, currentPlayer);

        // If Empty (in case the condition that there are jumps
        // but none of them are from the current Player) -- prevent returning "null" move
        if (myMoves.isEmpty()) {

            // If jumpPossible, get all "moves "
            // (which means the previously collected moves are all "jumps")
            if (board.jumpPossible()) {
                moves = board.getJustMoves();

                // Get all moves from current Player
                myMoves = board.getMyMoves(moves, currentPlayer);
            }
        }

        // A filter than select preferred moves from myMoves
        myMoves = filter(myMoves);

        // Evaluating each move -> get score for each move
        for (Move mov : myMoves) {

            // Do the move, get score, them undo back
            board.makeMove(mov);
            int score = staticScore(board, depth);
            board.undo();

            // Pruning
            if (score <= bestScore) {
                best = mov;
                bestScore = score;
                beta = Math.min(alpha, bestScore);
                if (alpha >= beta) {
                    break;
                }
            }
        }
        _lastFoundMove = best;
        return bestScore;
    }

    /** Return a heuristic value for BOARD.*/
    private int staticScore(Board board, int depth) {
        int result;

        ArrayList<Integer> arrayDiffAmount;
        ArrayList<Integer> arrayDiffJumpLength;
        ArrayList<Integer> arrayPositionScores;

        int whiteLongest = 0;
        int blackLongest = 0;
        int scoreJumpLength = 0;
        int scoreExpectedNext = 0;

        int diffJumpLength;
        int scoreAmount, scorePositions;
        int whiteAmount, blackAmount, diffAmount;
        int scoreWhitePosition, scoreBlackPosition;

        // Scoring by piece amount difference
        arrayDiffAmount = diffWhiteBlackPiece(board);
        assert !arrayDiffAmount.isEmpty();
        whiteAmount = arrayDiffAmount.get(0);
        blackAmount = arrayDiffAmount.get(1);
        diffAmount = arrayDiffAmount.get(2);
        scoreAmount = diffAmount * PIECE_POINT;

        // Scoring by jump-able length sum
        arrayDiffJumpLength = diffWhiteBlackJumpAble(board);
        if (arrayDiffJumpLength != null) {
            whiteLongest = arrayDiffJumpLength.get(0);
            blackLongest = arrayDiffJumpLength.get(1);
            diffJumpLength = arrayDiffJumpLength.get(2);
            scoreJumpLength = diffJumpLength * JUMPABLE_POINT;
        }

        // if jumps eliminate amount difference (expected next step)
        if (whiteLongest == blackAmount && board.whoseMove().equals(WHITE)) {
            scoreExpectedNext = INFTY / (9 - depth);
        } else if (blackLongest == whiteAmount && board.whoseMove().equals(BLACK)) {
            scoreExpectedNext = -INFTY / (9 - depth);
        }

        // Position Points, the more to the middle, the higher the score
        arrayPositionScores = positionsEvaluation(board);
        scoreWhitePosition = arrayPositionScores.get(0);
        scoreBlackPosition = arrayPositionScores.get(1);
        scorePositions = scoreWhitePosition - scoreBlackPosition;

        // Sum up scores, and return
        result = scoreAmount + scoreJumpLength + scoreExpectedNext + scorePositions;
        return result;
    }

    /** Difference amount of White/Black pieces on board.
     * @param board --input
     * @return --Format -> {white, black, difference}. */
    private ArrayList<Integer> diffWhiteBlackPiece(Board board) {
        ArrayList<Integer> result = new ArrayList<>();
        String string = board.board();
        int black = 0;
        int white = 0;

        // count White/Black piece amounts
        for (int i = 0; i < string.length(); i++) {
            char piece = string.charAt(i);
            if (piece == 'b') {
                black += 1;
            } else if (piece == 'w') {
                white += 1;
            }
        }

        int difference = white - black;

        // return in format
        result.add(white);
        result.add(black);
        result.add(difference);
        return result;
    }

    /** Difference amount of White/Black jump-able length.
     * @param board --input
     * @return --Format -> {white, black, difference}*/
    private ArrayList<Integer> diffWhiteBlackJumpAble(Board board) {
        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<Move> jumps;
        ArrayList<Move> whiteJumps;
        ArrayList<Move> blackJumps;
        int whiteJumpLength = 0;
        int blackJumpLength = 0;
        int wbest = 0;
        int bbest = 0;

        // if not jump-possible, return 0
        if (!board.jumpPossible()) {
            return null;
        }

        // if jump-possible, return all jumps
        jumps = board.getMoves();

        // Count White/Black all jump-able length
        // Find out longest White/Black moves.
        whiteJumps = board.getMyMoves(jumps, WHITE);
        blackJumps = board.getMyMoves(jumps, BLACK);
        for (Move wmov : whiteJumps) {
            whiteJumpLength += wmov.length();
            if (wmov.length() >= wbest) {
                wbest = wmov.length();
            }
        }
        for (Move bmov : blackJumps) {
            blackJumpLength += bmov.length();
            if (bmov.length() >= wbest) {
                wbest = bmov.length();
            }
        }

        int difference = whiteJumpLength - blackJumpLength;

        // return in format
        result.add(wbest);
        result.add(bbest);
        result.add(difference);
        return result;
    }

    /** Return scores for White/Black based on their positions.
     * @param board --input
     * @return --Format -> {wscore, bscore}*/
    private ArrayList<Integer> positionsEvaluation(Board board) {
        ArrayList<Integer> result = new ArrayList<>();
        int wscore = 0;
        int bscore = 0;

        // get board string
        String string = board.board();

        // for pieces on odd index points, add points
        for (int i = 0; i < string.length(); i += 2) {
            char piece = string.charAt(i);
            if (piece == 'w') { wscore += POSITION_POINT; }
            else if (piece == 'b') { bscore -= POSITION_POINT; }
        }

        // collect scores in format, and return
        result.add(wscore);
        result.add(bscore);
        return result;
    }

    /** Filter the moves.
     * @param moves --input
     * @return --Filtered moves -> fully functional */
    private ArrayList<Move> filter(ArrayList<Move> moves) {

        // fully functional condition
        return moves;
    }

    /** Filter the moves.
     * @param moves --input
     * @return --Filtered moves -> top two longest moves */
    private ArrayList<Move> filterBackUp(ArrayList<Move> moves) {

        // use condition -> selects top two longest jumps/moves
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
        if (second != null) {
            result.add(second);
        }
        return result;
    }

    @Override
    public void setLevel(String level) {
        switch (level) {
            case "one":
                PIECE_POINT = 0;
                JUMPABLE_POINT = 0;
                POSITION_POINT = 0;
                break;
            case "two":
                PIECE_POINT = 500;
                JUMPABLE_POINT = 0;
                POSITION_POINT = 5;
                break;
            case "three":
                PIECE_POINT = 500;
                JUMPABLE_POINT = 500;
                POSITION_POINT = 5;
                break;
        }
    }

    /** Point value for Piece Count Difference. */
    private int PIECE_POINT = 0;
    /** Point value for Piece Count Difference. */
    private int JUMPABLE_POINT = 0;
    /** Point value for Piece Position on board. */
    private int POSITION_POINT = 0;

}
