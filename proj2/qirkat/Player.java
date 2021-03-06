package qirkat;

/** A generic Qirkat Player.
 *  @author Shixuan (Wayne) Li
 */
abstract class Player {

    /** A Player that will play MYCOLOR in GAME. */
    Player(Game game, PieceColor myColor) {
        _game = game;
        _myColor = myColor;
    }

    /** Return my pieces' color. */
    PieceColor myColor() {
        return _myColor;
    }

    /** Return the game I am playing in. */
    Game game() {
        return _game;
    }

    /** Return a view of the board I am playing on. */
    Board board() {
        return _game.board();
    }


    /** Return a legal move for me. Assumes that
     *  board.whoseMove() == myColor and that !board.gameOver().
     *  @param cmnd --input
     *  @return */
    abstract Move myMove(Command cmnd);

    /** Added by Wayne, show prompt.
     * @return */
    public String myPrompt() {
        return _prompt;
    }

    /** Change Player Level. */
    public void setLevel(String level) {
        return;
    }

    /** The game I am playing in. */
    private final Game _game;
    /** The color of my pieces. */
    private final PieceColor _myColor;
    /** Added by Wayne. */
    private String _prompt;
}
