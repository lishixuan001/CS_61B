package gitlet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.Pattern.*;


/** All things to do with parsing commands.
 *  @author Shixuan (Wayne) Li
 */
class Command {

    /** Command types.
     *  ERROR indicates a parse error in the command.
     *  All other commands are upper-case versions of what the
     *  programmer writes. */
    enum Type {
        /* Start-up state only. */
        INIT, LOG, STATUS, CLEAN,
        GLOBALLOG("global-log"),
        ADD("add\\s+(\\S+)"),
        RM("rm\\s+(\\S+)"),
        BRANCH("branch\\s+(\\S+)"),
        RMBRANCH("rm-branch\\s+(\\S+)"),
        RESET("reset\\s+(\\S+)"),
        MERGE("merge\\s+(\\S+)"),
        CHECKOUTF("checkout\\s--\\s+(\\S+)"),
        CHECKOUTCF("checkout\\s+(\\S+)\\s--\\s+(\\S+)"),
        CHECKOUTB("checkout\\s+(\\S+)"),
        HELP("--help"),
        /* Special "commands" internally generated. */
        /** Syntax error in command. */
        ERROR(".*"),
        /** End of input stream. */
        EOF;

        /** PATTERN is a regular expression string giving the syntax of
         *  a command of the given type.  It matches the entire command,
         *  assuming no leading or trailing whitespace.  The groups in
         *  the pattern capture the operands (if any). */
        Type(String pattern) {
            _pattern = Pattern.compile(pattern + "$");
        }

        /** A Type whose pattern is the lower-case version of its name. */
        Type() {
            _pattern = Pattern.compile(this.toString().toLowerCase() + "$");
        }

        /** The Pattern descrbing syntactically correct versions of this
         *  type of command. */
        private final Pattern _pattern;

    }

    /** A new Command of type TYPE with OPERANDS as its operands. */
    Command(Type type, String... operands) {
        _type = type;
        _operands = operands;
    }

    /** Return the type of this Command. */
    Type commandType() {
        return _type;
    }

    /** Returns this Command's operands. */
    String[] operands() {
        return _operands;
    }

    /** Parse COMMAND, returning the command and its operands. */
    static Command parseCommand(String command) {
        if (command == null) {
            return new Command(Type.EOF);
        }
        command = command.trim();
        for (Type type : Type.values()) {
            Matcher mat = type._pattern.matcher(command);
            if (mat.matches()) {

                String[] operands = new String [mat.groupCount()];
                for (int i = 1; i <= operands.length; i += 1) {
                    operands[i - 1] = mat.group(i);
                }
                return new Command(type, operands);
            }
        }
        throw new Error("This error should not occur --Command.parseCommand");
    }

    /** The command name. */
    private final Type _type;
    /** Command arguments. */
    private final String[] _operands;
}
