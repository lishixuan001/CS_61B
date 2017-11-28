package qirkat;

import java.io.File;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

import static qirkat.PieceColor.*;
import static qirkat.Game.State.*;
import static qirkat.Command.Type.*;
import static qirkat.GameException.error;

/** Controls the play of the game.
 *  @author Shixuan (Wayne) Li
 */
class Game {

    /** States of play. */
    static enum State {
        SETUP, PLAYING;
    }

    /** A new Game, using BOARD to play on, reading initially from
     *  BASESOURCE and using REPORTER for error and informational messages. */
    Game(Board board, CommandSource baseSource, Reporter reporter) {
        _inputs.addSource(baseSource);
        _board = board;
        _constBoard = _board.constantView();
        _reporter = reporter;
    }

    /** Run a session of Qirkat gaming. */
    void process() {
        doClear(null);
        while (true) {
            // SetUp state, free moves
            while (_state == SETUP) {
                doCommand();
                if (_moved) {
                    takeTurn();
                    _moved = false;
                }
            }

            // Start the game by checking gameOver
            _board.checkGameOver();
            System.out.println(_board.toString());

            // Start state
            while (_state != SETUP && !_board.gameOver()) {

                // Check gameOver
                _board.checkGameOver();
                if (_board.winner().isPiece()) {
                    break;
                }

                // Get move from Player
                Move move = null;
                if (_whoseMove.equals(WHITE)) {

                    // If White Player
                    if (_whiteIsManual) {

                        // If Manual, get Command
                        Command cmnd = getMoveCmnd(_white.myPrompt());
                        if (cmnd == null) {
                            _moved = false;
                        } else {
                            move = _white.myMove(cmnd);
                            if (move == null) {
                                _moved = false;
                            } else {
                                _moved = true;
                            }
                        }
                    } else {

                        // If auto, get move
                        move = _white.myMove(null);
                        _moved = true;
                    }
                } else if (_whoseMove.equals(BLACK)) {

                    // If Black Player
                    if (_blackIsManual) {

                        // If Manual, get Command
                        Command cmnd = getMoveCmnd(_black.myPrompt());
                        if (cmnd == null) {
                            _moved = false;
                        } else {
                            move = _black.myMove(cmnd);
                            if (move == null) {
                                _moved = false;
                            } else {
                                _moved = true;
                            }
                        }
                    } else {

                        // If auto, get move
                        move = _black.myMove(null);
                        _moved = true;
                    }
                }

                // Make the move, and show the map
                if (_state == PLAYING) {

                    // Check if move piece of current player's
                    if (!_board.checkMoveMyPiece(move)) {
                        _moved = false;
                        String title = "Notice";
                        String msg = "Note! Please move your own piece!";
                        _reporter.errMsg(title, msg);
                    } else {
                        // Check if illegal move
                        if (!(_board.isLegalMove(move) || _board.isLegalJump(move))) {
                            _moved = false;
                            String title = "Notice";
                            String msg = "Note! Illegal Move!";
                            _reporter.errMsg(title, msg);
                        } else {
                            _board.recordLastBoard(_board.board());
                            _board.makeMove(move);
                            System.out.println(_board.toString());
                        }
                    }
                }

                // If a move is made, takeTurn
                if (_moved) {
                    _board.recordLastMove(move);
                    takeTurn();
                    _moved = false;
                }
            }

            // If gameOver, report Winner
            if (_state == PLAYING) {
                reportWinner();
            }

            // Get state back to SetUp
            _state = SETUP;
        }
    }

    /** Return a read-only view of my game board. */
    Board board() {
        return _constBoard;
    }

    /** Perform the next command from our input source. */
    void doCommand() {
        try {
            // Get command from Player
            Command cmnd =
                Command.parseCommand(_inputs.getLine("qirkat: "));
            // Acknowledge and run command
            _commands.get(cmnd.commandType()).accept(cmnd.operands());
        } catch (GameException excp) {
            _reporter.errMsg("Error", excp.getMessage());
        }
    }

    /** Read and execute commands until encountering a move or until
     *  the game leaves playing state due to one of the commands. Return
     *  the terminating move command, or null if the game first drops out
     *  of playing mode. If appropriate to the current input source, use
     *  PROMPT to prompt for input. */
    Command getMoveCmnd(String prompt) {
        // Work for getting command from user, when started game
        while (_state == PLAYING) {
            try {
                // Get command from Player
                Command cmnd = Command.parseCommand(_inputs.getLine(prompt));
                // Acknowledge command type and run
                switch (cmnd.commandType()) {
                case PIECEMOVE:
                    return cmnd;
                default:
                    _commands.get(cmnd.commandType()).accept(cmnd.operands());
                }
            } catch (GameException excp) {
                _reporter.errMsg(excp.getMessage());
            }
        }
        return null;
    }

    /** Return random integer between 0 (inclusive) and MAX>0 (exclusive). */
    int nextRandom(int max) {
        return _randoms.nextInt(max);
    }

    /** Report a move, using a message formed from FORMAT and ARGS as
     *  for String.format. */
    void reportMove(String format, Object... args) {
        _reporter.moveMsg(format, args);
    }

    /** Report an error, using a message formed from FORMAT and ARGS as
     *  for String.format. */
    void reportError(String format, Object... args) {
        _reporter.errMsg(format, args);
    }

    /** Perform the command 'clear'. */
    void doClear(String[] unused) {
        // Clear the game
        _state = SETUP;
        _whiteIsManual = true;
        _blackIsManual = false;
        _whoseMove = WHITE;
        _moved = false;
        _board.clear();
        _winner = null;
        _constBoard = _board.constantView();
        _white = new Manual(this, WHITE);
        _black = new AI(this, BLACK);
    }

    /** Set some Player to Manual mode.
     * Perform the command 'auto OPERANDS[0]'. */
    void doAuto(String[] operands) {
        // Keep game state to SetUp
        _state = SETUP;
        // Get Player name
        String player = operands[0].toUpperCase();

        if (player.equals("WHITE")) {
            _white = new AI(this, WHITE);
            _whiteIsManual = false;
        } else if (player.equals("BLACK")) {
            _black = new AI(this, BLACK);
            _blackIsManual = false;
        } else {
            throw error("Invalid command "
                    + "while setting player to AI. --Game.doAuto()");
        }
    }

    /** Set some Player to Manual mode.
     * Perform the command 'manual OPERANDS[0]'. */
    void doManual(String[] operands) {
        // Keep game state in SetUp
        _state = SETUP;
        // Get Player name
        String player = operands[0].toUpperCase();

        if (player.equals("WHITE")) {
            _white = new Manual(this, WHITE);
            _whiteIsManual = true;
        } else if (player.equals("BLACK")) {
            _black = new Manual(this, BLACK);
            _blackIsManual = true;
        } else {
            throw error("Invalid command while"
                    + " setting player to Manual. --Game.doManual()");
        }
    }

    /** Print the game map.
     * Perform the command 'dump'. */
    void doDump(String[] unused) {
        System.out.println("===");
        System.out.println(_board.toString());
        System.out.println("===");
    }

    /** Works only in SetUp state, make moves.
     * Perform the move OPERANDS[0]. */
    void doMove(String[] operands) {
        String string = operands[0];
        Move mov = Move.parseMove(string);
        if (mov == null) {
            return;
        }
        if (mov.isJump()) {
            if (_board.isLegalJump(mov)) {
                _board.makeMove(mov);
                _moved = true;
            } else {
                String title = "Alert";
                String msg = "This is an illegal jump --doMove";
                _reporter.errMsg(title, msg);
            }
        } else {
            if (_board.isLegalMove(mov)) {
                _board.makeMove(mov);
                _moved = true;
            } else {
                String title = "Alert";
                String msg = "This is an illegal move --doMove";
                _reporter.errMsg(title, msg);
            }
        }
    }

    /** Set the game map. "set white ----- ---w- ---b- ---bb --w--"
     * Perform the command 'set OPERANDS[0] OPERANDS[1]'. */
    void doSet(String[] operands) {

        // first clear, set back all parameters
        _board.clear();

        // then do setting
        String string = operands[0].toUpperCase();
        if (string.equals("WHITE")) {
            _board.setPieces(operands[1], WHITE);
            _whoseMove = WHITE;
        } else if (string.equals("BLACK")) {
            _board.setPieces(operands[1], BLACK);
            _whoseMove = BLACK;
        } else {
            throw new Error("Wrong Input for 'doSet'.'");
        }
    }

    /** Run commands in the file.
     * Perform the command 'load OPERANDS[0]'. */
    void doLoad(String[] operands) {
        try {
            // Read the file and get commands
            File file = new File(operands[0]);
            Reader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            // Run each command
            while ((line = bufferedReader.readLine()) != null) {
                Command cmnd = Command.parseCommand(line);
                _commands.get(cmnd.commandType()).accept(cmnd.operands());
            }
        } catch (IOException e) {
            throw error("Cannot open file %s", operands[0]);
        }
    }

    /** Start the game.
     * Perform the command 'start'. */
    void doStart(String[] unused) {
        _state = PLAYING;
    }

    /** Exit the program. */
    void doQuit(String[] unused) {
        Main.reportTotalTimes();
        System.exit(0);
    }

    /** One player surrender. */
    void doSurrender(String[] unused) {
        PieceColor currentPlayer = _whoseMove;
        reportWinnerBySurrender(currentPlayer.opposite());
        doClear(null);
    }

    /** Set AI Level. */
    void setAI(String[] operands) {

        String level = operands[0];
        String title = "Notice";
        StringBuilder msg = new StringBuilder();
        if (_state == PLAYING) {
            msg.append("Since game has started, we'll pause the game for changing AI settings.\n");
        }
        msg.append(String.format("Already set AIs to level %s", level));
        _reporter.moveMsg(title, msg.toString());

        _state = SETUP;

        if (!_whiteIsManual) {
            _white.setLevel(level);
        }
        if (!_blackIsManual) {
            _black.setLevel(level);
        }
    }

    /** Execute 'seed OPERANDS[0]' command, where the operand is a string
     *  of decimal digits. Silently substitutes another value if
     *  too large. */
    void doSeed(String[] operands) {
        try {
            _randoms.setSeed(Long.parseLong(operands[0]));
        } catch (NumberFormatException e) {
            _randoms.setSeed(Long.MAX_VALUE);
        }
    }

    /** Perform a 'help' command. */
    void doHelp(String[] unused) {
        InputStream helpIn =
                Game.class.getClassLoader().getResourceAsStream("qirkat/help.txt");
        if (helpIn == null) {
            System.err.println("No help available.");
        } else {
            try {
                BufferedReader r
                        = new BufferedReader(new InputStreamReader(helpIn));
                while (true) {
                    String line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    System.out.println(line);
                }
                r.close();
            } catch (IOException e) {
                return;
            }
        }
    }

    /** Execute the artificial 'error' command. */
    void doError(String[] unused) {
        throw error("Command not understood");
    }

    /** Report the assigned winner when one player "surrender". */
    void reportWinnerBySurrender(PieceColor player) {
        StringBuilder msg = new StringBuilder();

        String winner = pieceColorToString(player);
        String loser = pieceColorToString(player.opposite());

        msg.append(loser);
        msg.append(" surrendered.");
        msg.append("\n");
        msg.append(winner);
        msg.append(" wins!");

        String title = "Congratulations";
        _reporter.outcomeMsg(title, msg.toString());
    }

    /** Report "White wins." or "Black wins."
     * Report the outcome of the current game. */
    void reportWinner() {
        StringBuilder msg = new StringBuilder();

        _winner = _board.winner();
        String winner = pieceColorToString(_winner);
        msg.append(winner);
        msg.append(" wins!");

        String title = "Congratulations";
        _reporter.outcomeMsg(title, msg.toString());
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

    /** Mapping of command types to methods that process them. */
    private final HashMap<Command.Type, Consumer<String[]>> _commands =
        new HashMap<>();
    {
        _commands.put(AUTO, this::doAuto);
        _commands.put(CLEAR, this::doClear);
        _commands.put(DUMP, this::doDump);
        _commands.put(HELP, this::doHelp);
        _commands.put(MANUAL, this::doManual);
        _commands.put(PIECEMOVE, this::doMove);
        _commands.put(SEED, this::doSeed);
        _commands.put(SETBOARD, this::doSet);
        _commands.put(START, this::doStart);
        _commands.put(LOAD, this::doLoad);
        _commands.put(QUIT, this::doQuit);
        _commands.put(ERROR, this::doError);
        _commands.put(EOF, this::doQuit);
        _commands.put(SURRENDER, this::doSurrender);
        _commands.put(SETAI, this::setAI);
    }

    /** Input source. */
    private final CommandSources _inputs = new CommandSources();

    /** My board and its read-only view. */
    private Board _board, _constBoard;

    /** Indicate which players are manual players (as opposed to AIs). */
    private boolean _whiteIsManual, _blackIsManual;

    /** Current game state. */
    private State _state;

    /** Used to send messages to the user. */
    private Reporter _reporter;

    /** Source of pseudo-random numbers (used by AIs). */
    private Random _randoms = new Random();

    /** Added by Wayne, create players. */
    private Player _white, _black;

    /** Added by Wayne, show whose turn. */
    private PieceColor _whoseMove;

    /** Added by Wayne, show if there was a move. */
    private boolean _moved;

    /** Added by Wayne, winner. */
    private PieceColor _winner;

    /** Added by Wayne, take turn for _whoseMove. */
    private void takeTurn() {
        if (_whoseMove.equals(WHITE)) {
            _whoseMove = BLACK;
        } else if (_whoseMove.equals(BLACK)) {
            _whoseMove = WHITE;
        } else {
            throw new Error("The Player is neither "
                    + "white or black. --Game.takeTurn()");
        }
    }
}
