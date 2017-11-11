package qirkat;

import static qirkat.PieceColor.*;
import static qirkat.Command.Type.*;

/** A Player that receives its moves from its Game's getMoveCmnd method.
 *  @author Shixuan (Wayne) Li
 */
class Manual extends Player {

    /** A Player that will play MYCOLOR on GAME, taking its moves from
     *  GAME. */
    Manual(Game game, PieceColor myColor) {
        super(game, myColor);
        _prompt = myColor + ": ";
    }

    @Override
    Move myMove(Command cmnd) {
        String string = cmnd.operands()[0];
        Move move = Move.parseMove(string);
        return move;
    }

    @Override
    /** Added by Wayne, show prompt. */
    public String myPrompt() {
        return _prompt;
    }

    /** Identifies the player serving as a source of input commands. */
    private String _prompt;
}

