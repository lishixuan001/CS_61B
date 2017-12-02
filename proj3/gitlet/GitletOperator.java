package gitlet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

import static gitlet.Utils.*;
import static gitlet.Commit.*;
import static gitlet.Branch.*;
import static gitlet.Command.Type.*;
import static gitlet.Staged.*;
import static gitlet.GitletException.error;
import static java.io.File.*;


/** Real functional operating class for Gitlet system.
 *  @author Shixuan (Wayne) Li
 */
class GitletOperator {

    /** Another way to start an operator. */
    GitletOperator() {
        new GitletOperator(null);
    }
    /** Initialize the Gitlet System for running. */
    GitletOperator(String input) {
        _input = input;
        _blobs = new Blob();
        _staged = new Staged();
        _workArea = new WorkArea();
        _branch = Branch.restore();
    }

    /** Process the user commands. */
    void process() {
        doCommand(_input);
    }
    /** Process if it's "commit" or "find". */
    void process(String cmnd, String[] operands) {

        // FIXME --Test what command is translated
//        System.out.println("=====================");
//        System.out.println("Command Type: " + cmnd.toUpperCase());
//        System.out.println("Command Operands: " + Arrays.toString(operands));
//        System.out.println("=====================");

        if (cmnd.equals("commit")) {
            doCommit(operands);
        } else if (cmnd.equals("find")) {
            doFind(operands);
        }
    }

    /** Perform the next command from our input source. */
    private void doCommand(String input) {
        try {
            // Get command from Player
            Command cmnd =
                    Command.parseCommand(input);

            // FIXME --Test what command is translated
//            System.out.println("=====================");
//            System.out.println("Command Type: " + cmnd.commandType());
//            System.out.println("Command Operands: " + Arrays.toString(cmnd.operands()));
//            System.out.println("=====================");

            // Acknowledge and run command
            _commands.get(cmnd.commandType()).accept(cmnd.operands());
        } catch (GitletException excp) {
            throw new Error("Error --Main.doCommand");
        }
    }

    /** Function for "init". */
    private void doInit(String[] unused) {
        if (isInitialized()) {
            SystemExit("A Gitlet version-control system already exists in the current directory.");
        }
        new File(_gitletPath).mkdir();
        _blobs.init();
        _staged.init();
        new Branch().init();
        new Commit().init();
    }

    /** Function for "add [file name]". */
    private void doAdd(String[] operands) {
        doTest(operands);
        String filename = operands[0];

        File f = new File(PATH_WORKING + filename);
        if (!f.exists()) {
            SystemExit("File does not exist.");
        }

        Doc file = new Doc(filename, PATH_WORKING);
        _staged.add(file);
    }

    /** Function for "commit [message]". */
    private void doCommit(String[] operands) {
        doTest(operands);
        String msg = operands[0];
        Commit newCommit = new Commit(msg);
        newCommit.createCommit();
    }

    /** Function for "rm [file name]". */
    private void doRm(String[] operands) {
        doTest(operands);

        String filename = operands[0];
        Doc doc = new Doc(filename, PATH_WORKING);

        if (_staged.hasFileHash(doc.myHash())) {
            _staged.deleteByHash(filename);
        }

        if (nextCommitListContains(doc.myHash())) {
            deleteFromNextCommitList(doc.myHash());
        }

        deleteFromWorking(filename);

        addToRemovedHashs(doc.myHash());
        addToRemovedNames(doc.myName());
    }

    /** Function for "log". */
    private void doLog(String[] unused) {
        doTest(unused);
        Commit headCommit = Commit.restore(currentLatestCommit());
        while(true) {
            System.out.println("===");
            System.out.println("commit " + headCommit.myHash());
            if (headCommit.isMerged()) {
                String originCommit = headCommit.myParents()[0].substring(0, 7);
                String mergedInCommit = headCommit.myParents()[1].substring(0, 7);
                System.out.println("Merge: " + originCommit + " " + mergedInCommit);
            }
            System.out.println("Date: " + headCommit.myDate());
            System.out.println(headCommit.myMessage());
            System.out.println();

            if (headCommit.hasParents()) {
                headCommit = Commit.restore(headCommit.myParents()[0]);
            } else {
                return;
            }
        }
    }

    /** Function for "global-log". */
    private void doGlobalLog(String[] unused) {
        doTest(unused);
        for (String hash : getAllDirectorysFrom(PATH_COMMITS)) {
            Commit headCommit = Commit.restore(hash);
            System.out.println("===");
            System.out.println("commit " + headCommit.myHash());
            if (headCommit.isMerged()) {
                String originCommit = headCommit.myParents()[0].substring(0, 7);
                String mergedInCommit = headCommit.myParents()[1].substring(0, 7);
                System.out.println("Merge: " + originCommit + " " + mergedInCommit);
            }
            System.out.println("Date: " + headCommit.myDate());
            System.out.println(headCommit.myMessage());
            System.out.println();
        }
    }

    /** Function for "find [commit message]". */
    private void doFind(String[] operands) {
        doTest(operands);
        String message = operands[0];

        if (hasCommitWithMsg(message)) {
            for (String commit : getCommitsWithMsg(message)) {
                System.out.println(commit);
            }
        } else {
            SystemExit("Found no commit with that message.");
        }
    }

    /** Function for "status". */
    private void doStatus(String[] unused) {
        doTest(unused);

        // Branches
        System.out.println("=== Branches ===");
        ArrayList<String> branches = new ArrayList<>();
        branches.addAll(getAllDirectorysFrom(PATH_BRANCHES));
        Collections.sort(branches);
        String currentBranch = getCurrentBranch();
        for (String branch : branches) {
            if (branch.equals(currentBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();

        // Staged files
        System.out.println("=== Staged Files ===");
        ArrayList<String> stageds = new ArrayList<>();
        stageds.addAll(getAllDirectorysFrom(PATH_STAGED));
        Collections.sort(stageds);
        for (String file : stageds) {
            System.out.println(file);
        }
        System.out.println();

        // Removed files
        System.out.println("=== Removed Files ===");
        ArrayList<String> removed = new ArrayList<>();
        removed.addAll(StringsToList(readFrom(_removedNames)));
        Collections.sort(removed);
        for (String file : removed) {
            System.out.println(file);
        }
        System.out.println();

        // FIXME -- Do these for Extra Credits
        // Gather all files in Staged and in nextCommit.txt

        // for the gathered commits:
        // if not in Staged: if in nextCommit.txt: modified/deleted
        //               else: should not happen
        // else: if in Staged: modified

        // Untracked files
    }

    /** Function for "branch [branch name]". */
    private void doBranch(String[] operands) {
        doTest(operands);
        String branchName = operands[0];
        if (hasBranchName(branchName)) {
            SystemExit("A branch with that name already exists.");
        }
        Branch newBranch = new Branch(branchName);
        newBranch.createBranch();

        addBranchTo(currentLatestCommit(), branchName);
    }

    /** Function for "rm-branch [branch name]". */
    private void doRmBranch(String[] operands) {
        doTest(operands);
        String branchName = operands[0];
        if (getCurrentBranch().equals(branchName)) {
            SystemExit("Cannot remove the current branch.");
        }
        if (!hasBranchName(branchName)) {
            SystemExit("A branch with that name does not exist.");
        }
        File commits = new File(PATH_BRANCHES + branchName + "/" + _commitsFolder);
        for (String commit : readFrom(commits)) {
            deleteBranchFrom(commit, branchName);
        }

        deleteBranch(branchName);
    }

    /** Function for "checkout -- [file name]". */
    private void doCheckoutF(String[] operands) {
        doTest(operands);
        String filename = operands[0];
        String latestCommit = currentLatestCommit();
        Commit commit = Commit.restore(latestCommit);
        if (!commit.contains(filename)) {
            SystemExit("File does not exist in that commit.");
        }
        _blobs.checkOutByHash(commit.getHashByName(filename));
    }

    /** Function for "checkout [commit id] -- [file name]". */
    private void doCheckoutCF(String[] operands) {
        doTest(operands);
        String commitId = operands[0];
        String filename = operands[1];

        if (commitId.length() <= 7) {
            commitId = fullLengthIdOf(commitId);
        }

        if (!existCommit(commitId)) {
            SystemExit("No commit with that id exists.");
        }

        Commit commit = Commit.restore(commitId);

        if (!commit.contains(filename)) {
            SystemExit("File does not exist in that commit.");
        }
        _blobs.checkOutByHash(commit.getHashByName(filename));
    }

    /** Function for "checkout [branch name]". */
    private void doCheckoutB(String[] operands) {
        doTest(operands);
        String branchName = operands[0];

        if (!hasBranchName(branchName)) {
            SystemExit("No such branch exists.");
        }
        String currentBranch = getCurrentBranch();
        if (currentBranch.equals(branchName)) {
            SystemExit("No need to checkout the current branch.");
        }
        for (File file : getFilesInFile(PATH_WORKING)) {
            if (!isTrackedBy(file.getName(), currentBranch)) {
                SystemExit("There is an untracked file in the way; delete it or add it first.");
            }
        }

        for (File file : getFilesInFile(PATH_WORKING)) {
            if (!isTrackedBy(file.getName(), branchName)) {
                delete(file);
            }
        }

        rewriteCurrentBranch(branchName);
        Commit commit = Commit.restore(currentLatestCommit());
        for (String hash : commit.myFiles()) {
            _blobs.checkOutByHash(hash);
        }
    }

    /** Function for "reset [commit id]". */
    private void doReset(String[] operands) {
        // Use "restore" in Commit.java
        // if return null, means not found
        // there are totally two possibilities of error
        doTest(operands);
    }

    /** Function for "merge [branch name]". */
    private void doMerge(String[] operands) {
        doTest(operands);
    }

    /** Function for "help". */
    private void doHelp(String[] unused) {
        doTest(unused);
    }

    /** Function as the third input command check,
     * this work if has wrong type operands with previously
     * checked correct command. */
    private void doError(String[] unused) {
        SystemExit("Incorrect operands.");
    }


    /** Test if current directory is initialized. */
    private void doTest(String[] operands){
        if (!isInitialized()) {
            SystemExit("Not in an initialized Gitlet directory.");
        }
    }

    /** Clean up the .gitlet (remove). */
    private void doClean(String[] unused) {
        if (isInitialized()) {
            delete(new File(_gitletPath));
        } else {
            SystemExit("This directory hasn't been initialized --Wayne's doClean");
        }
    }

    /** Delete files and directories for doClean. */
    private void delete(File f){
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        f.delete();
    }

    /** Check if current environment is initialized.
     * Must exist and must be directory. */
    private boolean isInitialized(){
        File initedPath = new File(_gitletPath);
        return initedPath.exists() && initedPath.isDirectory();
    }

    /** This should never occur, if this happen, need to check. */
    private void doEOF(String[] unused) {
        SystemExit("EOF error occurred which should not happen at all times, fix it!");
    }

    /** Get name of the current branch. */
    static String getCurrentBranch() {
        String[] readResult = readFrom(PATH_CURRENTBRANCH);
        if (readResult != null) {
            return readResult[0];
        } else {
            return null;
        }
    }

    /** Clear a file. */
    static void clearFile(String file) {
        try {
            PrintWriter pw = new PrintWriter(file);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Clear a file with input "File". */
    static void clearFile(File file) {
        clearFile(file.getPath());
    }

    /** Convenience for writing objects into file. */
    static void writeInto(String file, boolean ifAppend, String... strs) {
        try {
            FileWriter fw = new FileWriter(file, ifAppend);
            BufferedWriter bw = new BufferedWriter(fw);
            if (strs != null) {
                for (String str : strs) {
                    if (str != null) {
                        bw.write(str);
                        bw.newLine();
                    }
                }
                bw.flush();
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** WriteInto with input as "File". */
    static void writeInto(File file, boolean ifAppend, String... strs) {
        writeInto(file.getPath(), ifAppend, strs);
    }

    /** Convenience for reading Objects from file. */
    static String[] readFrom(String file) {
        ArrayList<String> lst = new ArrayList<>();
        String strLine;
        try {
            FileInputStream fstream = new FileInputStream(file);
            InputStreamReader istream = new InputStreamReader(fstream);
            BufferedReader br = new BufferedReader(istream);
            while ((strLine = br.readLine()) != null)   {
                lst.add(strLine);
            }
            br.close();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ListToStrings(lst);
    }

    /** readFrom with input as "File". */
    static String[] readFrom(File file) {
        return readFrom(file.getPath());
    }

    /** Convenience for re-writing currentBranch.txt. */
    static void rewriteCurrentBranch(String branchName) {
        writeInto(PATH_CURRENTBRANCH, false, branchName);
    }

    /** Get Files in File. */
    static File[] getFilesInFile(String path) {
        return new File(path).listFiles();
    }

    /** Get all directories from File[]. */
    static ArrayList<String> getAllDirectorysFrom(File[] files) {
        ArrayList<String> result = new ArrayList<>();
        if (files == null) {
            return result;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                result.add(file.getName());
            }
        }
        return result;
    }

    /** Convenience way of get all directories from a certain path. */
    static ArrayList<String> getAllDirectorysFrom(String path) {
        return getAllDirectorysFrom(getFilesInFile(path));
    }

    /** Get all plain files from File[]. */
    static ArrayList<String> getAllPlainsFrom(File[] files) {
        ArrayList<String> result = new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) {
                result.add(file.getName());
            }
        }
        return result;
    }

    /** Method converting ArrayList<String> to String[]. */
    static String[] ListToStrings(ArrayList<String> lst) {
        return lst.toArray(new String[lst.size()]);
    }

    /** Method converting Set<String> to String[]. */
    static String[] SetToStrings(Set<String> lst) {
        return lst.toArray(new String[lst.size()]);
    }

    /** Method converting String[] to ArrayList<String>.*/
    static ArrayList<String> StringsToList(String[] str) {
        return new ArrayList<>(Arrays.asList(str));
    }

    /** Get the hash of th commit of the current branch. */
    static String currentLatestCommit() {
        String currentBranchName = getCurrentBranch();
        Branch currentBranch = Branch.restore(currentBranchName);
        if (currentBranch == null) { return null; }
        return currentBranch.myLatestCommit();
    }

    /** Copy File from one place to another. Do not use in same directory. */
    static void copyFiles(File sourceLocation, File targetLocation) {
        if (sourceLocation.isDirectory()) {
            copyDirectory(sourceLocation, targetLocation);
        } else {
            copyPlains(sourceLocation, targetLocation);
        }
    }

    /** Copy Directories form source to target. */
    private static void copyDirectory(File source, File target){
        if (!target.exists()) {
            target.mkdir();
        }

        for (String f : source.list()) {
            copyFiles(new File(source, f), new File(target, f));
        }
    }

    /** Copy Plain files form source to target. */
    private static void copyPlains(File source, File target){
        try {
            InputStream instream = new FileInputStream(source);
            OutputStream outstream = new FileOutputStream(target);

            byte[] buf = new byte[1024];
            int length;
            while ((length = instream.read(buf)) > 0) {
                outstream.write(buf, 0, length);
            }
            instream.close();
            outstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Delete from Working directory. */
    static void deleteFromWorking(String filename) {
        new File(PATH_WORKING + filename).delete();
    }

    /** Delete directory. */
    static void deleteDirectory(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDirectory(f);
            }
        }
        file.delete();
    }

    /** Make system exit with a message. */
    static void SystemExit(String msg) {
        System.out.println(msg);
        System.exit(0);
    }

    /** Mapping of command types to methods that process them. */
    private static final HashMap<Command.Type, Consumer<String[]>> _commands =
            new HashMap<>();
    {
        _commands.put(INIT, this::doInit);
        _commands.put(ADD, this::doAdd);
//        _commands.put(COMMIT, this::doCommit);
        _commands.put(RM, this::doRm);
        _commands.put(LOG, this::doLog);
        _commands.put(GLOBALLOG, this::doGlobalLog);
//        _commands.put(FIND, this::doFind);
        _commands.put(STATUS, this::doStatus);
        _commands.put(BRANCH, this::doBranch);
        _commands.put(RMBRANCH, this::doRmBranch);
        _commands.put(CHECKOUTF, this::doCheckoutF);
        _commands.put(CHECKOUTCF, this::doCheckoutCF);
        _commands.put(CHECKOUTB, this::doCheckoutB);
        _commands.put(RESET, this::doReset);
        _commands.put(MERGE, this::doMerge);
        _commands.put(CLEAN, this::doClean);
        _commands.put(HELP, this::doHelp);
        _commands.put(ERROR, this::doError);
        _commands.put(EOF, this::doEOF);
    }

    /** The user input command and operands as a String. */
    private String _input;
    /** The current Blobs Area. */
    static Blob _blobs;
    /** The current Staged Area. */
    static Staged _staged;
    /** The current Work Area. */
    static WorkArea _workArea;
    /** The current branch. */
    static Branch _branch;

    /** The File name of the directory that saves Gitlet System files. */
    static final String _gitletPath = ".gitlet/";
    /** Default Date Format. */
    static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
    /** Initialized branch -> *master */
    static final String DEFAULT_BRANCH = "master";
    /** Default hash for "initial commit"*/
    static final String INIT_COMMIT = sha1(INIT_MESSAGE, getDate(INIT_DATE));

    /** Convenience for directory on Working Area. */
    static final String PATH_WORKING = "./";
    /** Convenience for directory on .gitlet/Commits. */
    static final String PATH_COMMITS = _gitletPath + "Commits/";
    /** Convenience for directory on .gitlet/Blobs/. */
    static final String PATH_BLOBS = _gitletPath + "Blobs/";
    /** Convenience for directory on .gitlet/Staged/. */
    static final String PATH_STAGED = _gitletPath + "Staged/";
    /** Convenience for directory on .gitlet/Branches/. */
    static final String PATH_BRANCHES = _gitletPath + "Branches/";
    /** Convenience for directory on .gitlet/Branches/currentBranch.txt. */
    static final String PATH_CURRENTBRANCH = PATH_BRANCHES + "currentBranch.txt";

}
