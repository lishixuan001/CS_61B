package gitlet;

import static gitlet.Command.Type.*;
import static gitlet.GitletOperator.*;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Shixuan (Wayne) Li
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {

        // Test if has command, if not report corresponding message and exit
        if (args.length <= 0) {
            SystemExit("Please enter a command.");
        }

        // Test if has correct command, if not report corresponding message and exit
        String _command = args[0];
        if (!isValidCommand(_command)) {
            SystemExit("No command with that name exists.");
        }

        // Test has correct operands, if not report corresponding message and exit
        // This would be tested in Command.java


        // Exception for "commit" and "find"
        if (_command.equals("commit")) {
            if (args.length == 1) {
                SystemExit("Please enter a commit message.");
            }
            GitletOperator operator = new GitletOperator();
            operator.process(_command, new String[] {args[1]});

        } else if (_command.equals("find")) {
            GitletOperator operator = new GitletOperator();
            String[] operands;
            if (args.length == 1) {
                operands = new String[] {""};
            } else {
                operands = new String[] {args[1]};
            }
            operator.process(_command, operands);
        } else {
            // Get the input as one String command
            StringBuilder input = new StringBuilder();
            for (String arg : args) {
                input.append(arg).append(" ");
            }
            GitletOperator operator = new GitletOperator(input.toString());
            operator.process();
        }


    }

    static boolean isValidCommand(String command) {
        for (String cmnd : _commands) {
            if (cmnd.equals(command)) {
                return true;
            }
        }
        return false;
    }

    static final String[] _commands = new String[] {
            "init",
            "add",
            "commit",
            "rm",
            "log",
            "global-log",
            "find",
            "status",
            "branch",
            "rm-branch",
            "checkout",
            "reset",
            "merge",
            "--help",
            "clean",
    };

}
