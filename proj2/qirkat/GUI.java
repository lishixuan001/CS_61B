package qirkat;

import ucb.gui2.TopLevel;
import ucb.gui2.LayoutSpec;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Observable;
import java.util.Observer;

import java.awt.event.MouseEvent;
import static com.sun.java.accessibility.util.AWTEventMonitor.addMouseListener;
import static qirkat.PieceColor.*;

/** The GUI for the Qirkat game.
 *  @author Shixuan (Wayne) Li
 */
class GUI extends TopLevel implements Observer, Reporter {

    /* The implementation strategy applied here is to make it as
     * unnecessary as possible for the rest of the program to know that it
     * is interacting with a GUI as opposed to a terminal.
     *
     * To this end, we first have made Board observable, so that the
     * GUI gets notified of changes to a Game's board and can interrogate
     * it as needed, while the Game and Board themselves need not be aware
     * that it is being watched.
     *
     * Second, instead of creating a new API by which the GUI communicates
     * with a Game, we instead simply arrange to make the GUI's input look
     * like that from a terminal, so that we can reuse all the machinery
     * in the rest of the program to interpret and execute commands.  The
     * GUI simply composes commands (such as "start" or "clear") and
     * writes them to a Writer that (using the Java library's PipedReader
     * and PipedWriter classes) provides input to the Game using exactly the
     * same API as would be used to read from a terminal. Thus, a simple
     * Manual player can handle all commands and moves from the GUI.
     *
     * See also Main.java for how this might get set up.
     */

    /** Minimum size of board in pixels. */
    private static final int MIN_SIZE = 300;

    /** A new display observing MODEL, with TITLE as its window title.
     *  It uses OUTCOMMANDS to send commands to a game instance, using the
     *  same commands as the text format for Qirkat. */
    GUI(String title, Board model, Writer outCommands) {
        super(title, true);
        addMenuButton("Game->Start", this::start);
        addMenuButton("Game->Clear", this::clear);
//        addMenuButton("Game->Undo", this::undo);
        addMenuButton("Game->Surrender", this::surrender);
        addMenuButton("Game->Save", this::save);
        addMenuButton("Game->Import", this::setImport);
//        addMenuButton("Game->Set", this::set);
        addMenuButton("Game->Quit", this::quit);
        addMenuButton("Options->Players->White Manual", this::setWhiteManual);
        addMenuButton("Options->Players->Black Manual", this::setBlackManual);
        addMenuButton("Options->Players->White Auto", this::setWhiteAuto);
        addMenuButton("Options->Players->Black Auto", this::setBlackAuto);
        addMenuButton("Options->Seed...", this::setSeed);
        addMenuButton("Options->Clean Choices", this::cleanChoices);
        addMenuButton("Info->Help", this::help);
        _model = model;
        _widget = new BoardWidget(model);
        _out = new PrintWriter(outCommands, true);
        add(_widget,
            new LayoutSpec("height", "1",
                           "width", "REMAINDER",
                           "ileft", 5, "itop", 5, "iright", 5,
                           "ibottom", 5));
        setMinimumSize(MIN_SIZE, MIN_SIZE);
        _widget.addObserver(this);
        _model.addObserver(this);
    }

    /** Start the game. */
    private synchronized void start(String unused) {
        _out.printf("start%n");
    }

    /** Clear the board. */
    private synchronized void clear(String unused) {
        _out.printf("clear%n");
    }

    /** Surrender. */
    private synchronized void surrender(String unused) {
        _out.printf("surrender%n");
    }

    /** Undo. */
    private synchronized void undo(String unused) {
        _out.printf("undo%n");
    }

    /** Execute the "Quit" button function. */
    private synchronized void quit(String unused) {
        _out.printf("quit%n");
    }

    /** Save current board as a file, later we can continue playing. */
    private synchronized void save(String unused) {
        String resp =
                getTextInput("Save As", "Save Game","question", "File Name");
        if (resp == null) {
            return;
        }
        try {
            String filename = "./SavedMaps/" + resp + ".txt";
            Path path = Paths.get(filename);

            if (Files.exists(path)) {
                String title = "Error";
                String msg = "File name already exist, please try again.";
                errMsg(title, msg);
            } else {
                Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(filename), "utf-8"));
                String map = _model.reverseBoard();
                String nextmove = _model.whoseMove().toString();
                writer.write(nextmove + " " + map);
                writer.close();
            }
        } catch (UnsupportedEncodingException e) {
            errMsg("Error", "UnsupportedEncodingException --GUI.save");
        } catch (FileNotFoundException e) {
            errMsg("Error", "FileNotFoundException --GUI.save");
        } catch (IOException e) {
            errMsg("Error", "IOException --GUI.save");
        }
    }

    /** Import and continue the map. */
    private synchronized void setImport(String unused) {
        String resp =
                getTextInput("Get Map", "Import Game", "question", "Map Name");
        if (resp == null) {
            return;
        }
        try {
            String filename = "./SavedMaps/" + resp + ".txt";
            Path path = Paths.get(filename);
            if (Files.notExists(path)) {
                String title = "Error";
                String msg = "File not found, please try again.";
                errMsg(title, msg);
            } else {
                BufferedReader br = new BufferedReader(new FileReader(filename));
                String line = br.readLine();
                _out.printf("set %s%n", line);
            }
        } catch (FileNotFoundException e) {
            errMsg("Error", "FileNotFoundException --GUI.setImport");
        } catch (IOException e) {
            errMsg("Error", "IOException --GUI.setImport");
        }
    }

    /** Execute Seed... command. */
    private synchronized void setSeed(String unused) {
        String resp =
            getTextInput("Random Seed", "Get Seed", "question", "");
        if (resp == null) {
            return;
        }
        try {
            long s = Long.parseLong(resp);
            _out.printf("seed %d%n", s);
        } catch (NumberFormatException excp) {
            return;
        }
    }

    /** Set White to Manual. */
    private synchronized void setWhiteManual(String unused) {
        _out.printf("manual White%n");
    }

    /** Set Black to Manual. */
    private synchronized void setBlackManual(String unused) {
        _out.printf("manual Black%n");
    }

    /** Set White to Auto. */
    private synchronized void setWhiteAuto(String unused) {
        _out.printf("auto White%n");
    }

    /** Set Black to auto. */
    private synchronized void setBlackAuto(String unused) {
        _out.printf("auto Black%n");
    }

    /** Call for help. */
    private synchronized void help(String unused) {
        _out.printf("help%n");
        String title = "Help";
        String msg = "Game Instructions not complete";
        displayText(title, msg);
    }

    /** Display text in file NAME in a box titled TITLE. */
    private void displayText(String name, String title) {
        InputStream input =
            Game.class.getClassLoader().getResourceAsStream(name);
        if (input != null) {
            try {
                BufferedReader r
                    = new BufferedReader(new InputStreamReader(input));
                char[] buffer = new char[1 << 15];
                int len = r.read(buffer);
                showMessage(new String(buffer, 0, len), title, "plain");
                r.close();
            } catch (IOException e) {
                /* Ignore IOException */
            }
        }
    }

    /** Clean choices in the graphical map. */
    private void cleanChoices(String unused) {
        _widget.clearString();
    }

    @Override
    public void errMsg(String title, Object... args) {
        reportMsg(title, (String) args[0]);
    }

    @Override
    public void outcomeMsg(String title, Object... args) {
        reportMsg(title, (String) args[0]);
    }

    @Override
    public void moveMsg(String title, Object... args) {
        reportMsg(title, (String) args[0]);
    }

    private void reportMsg(String title, String msg) {
        JOptionPane pane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = pane.createDialog(null, title);
        dialog.setModal(false);
        dialog.setVisible(true);

        new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        }).start();
    }

    @Override
    public void update(Observable obs, Object arg) {

        if (obs == _model) {
            // Update the gui only for result (ignore AI trials)
            _widget.update(_model, arg);
        } else if (obs == _widget) {
            if (_model.whoseMove().equals(WHITE)) {
                System.out.println("White moves " + (String) arg);
            }
            _out.printf(arg + "%n");
        }
    }

    /** Contains the drawing logic for the Qirkat model. */
    private BoardWidget _widget;
    /** The model of the game. */
    private Board _model;
    /** Output sink for sending commands to a game. */
    private PrintWriter _out;
    /** Move selected by clicking. */
    private Move _selectedMove;
}
