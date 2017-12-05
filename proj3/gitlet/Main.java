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

        if (args.length <= 0) {
            doSystemExit("Please enter a command.");
        }

        String command = args[0];
        if (!isValidCommand(command)) {
            doSystemExit("No command with that name exists.");
        }

        if (command.equals("commit")) {
            if (args.length == 1 || args[1].length() <= 0) {
                doSystemExit("Please enter a commit message.");
            }
            GitletOperator operator = new GitletOperator();
            operator.process(command, new String[] {args[1]});

        } else if (command.equals("find")) {
            GitletOperator operator = new GitletOperator();
            String[] operands;
            if (args.length == 1) {
                operands = new String[] {""};
            } else {
                operands = new String[] {args[1]};
            }
            operator.process(command, operands);
        } else {
            StringBuilder input = new StringBuilder();
            for (String arg : args) {
                input.append(arg).append(" ");
            }
            GitletOperator operator = new GitletOperator(input.toString());
            operator.process();
        }
    }

    /** Check if is valid command.
     * @param command -- command
     * @return -- check result. */
    static boolean isValidCommand(String command) {
        for (String cmnd : VALID_COMMANDS) {
            if (cmnd.equals(command)) {
                return true;
            }
        }
        return false;
    }

    /** Collection of all correct commands. */
    private static final String[] VALID_COMMANDS = new String[] {
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
        "fetch",
        "add-remote",
        "rm-remote",
        "push",
        "pull",
        "--help",
        "clean",
    };

}
