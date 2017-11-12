package qirkat;

/* Author: P. N. Hilfinger */

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
 *  @author Shixuan (Wayne) Li [import graph.B;]
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
            while (_state == SETUP) {
                doCommand();
                if (_moved) {
                    takeTurn();
                    _moved = false;
                }
            }
            _board.checkGameOver();
            while (_state != SETUP && !_board.gameOver()) {
                _board.checkGameOver();
                if (_board.winner().isPiece()) {
                    break;
                }
                Move move = null;
                if (_whoseMove.equals(WHITE)) {
                    if (_whiteIsManual) {
                        Command cmnd = getMoveCmnd(_white.myPrompt());
                        if (cmnd == null) {
                            _moved = false;
                        } else {
                            move = _white.myMove(cmnd);
                            _moved = true;
                        }
                    } else {
                        move = _white.myMove(null);
                        _moved = true;
                    }
                } else if (_whoseMove.equals(BLACK)) {
                    if (_blackIsManual) {
                        Command cmnd = getMoveCmnd(_black.myPrompt());
                        if (cmnd == null) {
                            _moved = false;
                        } else {
                            move = _black.myMove(cmnd);
                            _moved = true;
                        }
                    } else {
                        move = _black.myMove(null);
                        _moved = true;
                    }
                }
                if (_state == PLAYING) {
                    _board.makeMove(move);

                }
                if (_moved) {
                    takeTurn();
                    _moved = false;
                }
            }
            if (_state == PLAYING) {
                reportWinner();
            }
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
            Command cmnd =
                Command.parseCommand(_inputs.getLine("qirkat: "));
            _commands.get(cmnd.commandType()).accept(cmnd.operands());
        } catch (GameException excp) {
            _reporter.errMsg(excp.getMessage());
        }
    }

    /** Read and execute commands until encountering a move or until
     *  the game leaves playing state due to one of the commands. Return
     *  the terminating move command, or null if the game first drops out
     *  of playing mode. If appropriate to the current input source, use
     *  PROMPT to prompt for input. */
    Command getMoveCmnd(String prompt) {
        while (_state == PLAYING) {
            try {
                Command cmnd = Command.parseCommand(_inputs.getLine(prompt));
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

    /** Perform the command 'auto OPERANDS[0]'. */
    void doAuto(String[] operands) {
        _state = SETUP;
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

    /** Perform the command 'clear'. */
    void doClear(String[] unused) {
        _state = SETUP;
        _white = new Manual(this, WHITE);
        _whiteIsManual = true;
        _black = new AI(this, BLACK);
        _blackIsManual = false;
        _whoseMove = WHITE;
        _moved = false;
        _board.clear();
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

    /** Perform the command 'load OPERANDS[0]'. */
    void doLoad(String[] operands) {
        try {
            File file = new File(operands[0]);
            Reader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Command cmnd = Command.parseCommand(line);
                _commands.get(cmnd.commandType()).accept(cmnd.operands());
            }
        } catch (IOException e) {
            throw error("Cannot open file %s", operands[0]);
        }
    }

    /** Perform the command 'manual OPERANDS[0]'. */
    void doManual(String[] operands) {
        _state = SETUP;
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

    /** Exit the program. */
    void doQuit(String[] unused) {
        Main.reportTotalTimes();
        System.exit(0);
    }

    /** Perform the command 'start'. */
    void doStart(String[] unused) {
        _state = PLAYING;
    }

    /** Perform the move OPERANDS[0]. */
    void doMove(String[] operands) {
        String string = operands[0];
        Move mov = Move.parseMove(string);
        _board.makeMove(mov);
        _moved = true;
    }

    /** Perform the command 'set OPERANDS[0] OPERANDS[1]'. */
    void doSet(String[] operands) {
        String string = operands[0].toUpperCase();
        if (string.equals("WHITE")) {
            _board.setPieces(operands[1], WHITE);
        } else if (string.equals("BLACK")) {
            _board.setPieces(operands[1], BLACK);
        } else {
            throw new Error("Wrong Input for 'doSet'.'");
        }
    }

    /** Perform the command 'dump'. */
    void doDump(String[] unused) {
        System.out.println("===");
        System.out.println(_board.toString());
        System.out.println("===");
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

    /** Execute the artificial 'error' command. */
    void doError(String[] unused) {
        throw error("Command not understood");
    }

    /** Report the outcome of the current game. */
    void reportWinner() {
        StringBuilder msg = new StringBuilder();
        String winner = "";
        _winner = _board.winner();
        if (_winner.equals(WHITE)) {
            winner = "White";
        } else if (_winner.equals(BLACK)) {
            winner = "Black";
        }
        msg.append(winner);
        msg.append(" wins.");

        _reporter.outcomeMsg(msg.toString());
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
