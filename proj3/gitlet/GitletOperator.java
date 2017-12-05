package gitlet;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

import static gitlet.Blob.*;
import static gitlet.Utils.*;
import static gitlet.Commit.*;
import static gitlet.Branch.*;
import static gitlet.Command.Type.*;
import static gitlet.Staged.*;

/** Operator form Gitlet System.
 *  @author Shixuan (Wayne) Li
 */
class GitletOperator {

    /** Another way to start an operator. */
    GitletOperator() {
        new GitletOperator(null);
    }
    /** Initialize the Gitlet System for running.
     * @param input -- user input. */
    GitletOperator(String input) {
        _input = input;
        _blobs = new Blob();
        _staged = new Staged();
        _branch = Branch.restore();
    }

    /** Process the user commands. */
    void process() {
        doCommand(_input);
    }
    /** Process if it's "commit" or "find".
     * @param cmnd -- command
     * @param operands -- operands. */
    void process(String cmnd, String[] operands) {
        if (cmnd.equals("commit")) {
            doCommit(operands);
        } else if (cmnd.equals("find")) {
            doFind(operands);
        }
    }

    /** Perform the next command from our input source.
     * @param input -- input. */
    private void doCommand(String input) {
        try {
            Command cmnd = Command.parseCommand(input);
            _commands.get(cmnd.commandType()).accept(cmnd.operands());
        } catch (GitletException excp) {
            throw new Error("Error --Main.doCommand");
        }
    }

    /** Function for "init".
     * @param unused  -- none. */
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

    /** Function for "add [file name]".
     * @param operands  -- file name. */
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

    /** Function for "commit [message]".
     * @param operands -- commit message. */
    private void doCommit(String[] operands) {
        doTest(operands);
        String msg = operands[0];
        Commit newCommit = new Commit(msg);
        newCommit.createCommit();
    }

    /** Function for "rm [file name]".
     * @param operands -- file name. */
    private void doRm(String[] operands) {
        doTest(operands);

        String filename = operands[0];

        if (!_staged.hasFileName(filename) &&
                !isTrackedByCommit(filename, currentHeadCommit())) {
            SystemExit("No reason to remove the file.");
        }

        if (_staged.hasFileName(filename)) {
            _staged.deleteByName(filename);
        }

        if (isTrackedByCommit(filename, currentHeadCommit())) {
            deleteFromWorking(filename);
            addToRemovedNames(filename);
        }
    }

    /** Function for "log". */
    private void doLog(String[] unused) {
        doTest(unused);
        Commit headCommit = Commit.restore(currentHeadCommit());
        while(true) {
            System.out.println("===");
            System.out.println("commit " + headCommit.myHash());
            if (headCommit.isMerged()) {
                System.out.print("Merge:");
                for (String commit : headCommit.myParents()) {
                    System.out.print(" " + commit.substring(0, 7));
                }
                System.out.println();
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

        System.out.println("=== Staged Files ===");
        HashMap<String, String> staged = new HashMap<>();
        ArrayList<String> stagedNames = new ArrayList<>();
        ArrayList<String> stagedHashs = getAllDirectorysFrom(PATH_STAGED);
        for (String stagedHash : stagedHashs) {
            String stagedName = _staged.getNameByHash(stagedHash);
            stagedNames.add(stagedName);
            staged.put(stagedName, stagedHash);
        }
        Collections.sort(stagedNames);

        for (String name : stagedNames) {
            System.out.println(name);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        ArrayList<String> removed = new ArrayList<>();
        removed.addAll(StringsToList(readFrom(_removedNames)));
        Collections.sort(removed);

        for (String file : removed) {
            System.out.println(file);
        }
        System.out.println();

        ArrayList<String> modified = new ArrayList<>();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String name : stagedNames) {
            File fileInWorking = new File(PATH_WORKING + name);
            if (!fileInWorking.exists()) {
                modified.add(name + " (deleted)");
            } else {
                Doc docInWorking = new Doc(name, PATH_WORKING);
                if (!docInWorking.myHash().equals(staged.get(name))) {
                    modified.add(name + " (modified)");
                }
            }
        }
        String[] filesInCurrentCommit = readFrom(PATH_COMMITS + currentHeadCommit() + "/" + _filesFolder);
        if (filesInCurrentCommit != null) {
            for (String fileHash : filesInCurrentCommit) {
                String fileName = _blobs.getNameOf(fileHash);
                if (!_staged.hasFileName(fileName)) {
                    File fileInWorking = new File(PATH_WORKING +fileName);
                    if (!fileInWorking.exists() && !existFileNameInRemoved(fileName)) {
                        modified.add(fileName + " (deleted)");
                    } else {
                        if (fileInWorking.exists()) {
                            Doc docInWorking = new Doc(fileName, PATH_WORKING);
                            if (!docInWorking.myHash().equals(fileHash)) {
                                modified.add(fileName + " (modified)");
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(modified);

        for (String name : modified) {
            System.out.println(name);
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        for (File file : getFilesInFile(PATH_WORKING)) {
            String fileName = file.getName();
            if (fileName.equals(_gitletPath)) {
                continue;
            }
            if (!isEverTracked(fileName) && !_staged.hasFileName(fileName)) {
                System.out.println(fileName);
            }
        }
        System.out.println();
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
        addBranchTo(currentHeadCommit(), branchName);
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
        String currentCommit = currentHeadCommit();
        Commit commit = Commit.restore(currentCommit);
        if (!commit.containsFileName(filename)) {
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

        if (commitId == null || !existCommit(commitId)) {
            SystemExit("No commit with that id exists.");
        }

        Commit commit = Commit.restore(commitId);

        if (!commit.containsFileName(filename)) {
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
            if (!isTrackedByBranch(file.getName(), currentBranch)) {
                SystemExit("There is an untracked file in the way; delete it or add it first.");
            }
        }
        for (File file : getFilesInFile(PATH_WORKING)) {
            if (!isTrackedByBranch(file.getName(), branchName)) {
                delete(file);
            }
        }
        rewriteCurrentBranch(branchName);
        Commit commit = Commit.restore(currentHeadCommit());
        for (String hash : commit.myFiles()) {
            _blobs.checkOutByHash(hash);
        }
    }

    /** Function for "reset [commit id]". */
    private void doReset(String[] operands) {
        doTest(operands);
        String commitId = operands[0];

        if (commitId.length() <= 7) {
            commitId = fullLengthIdOf(commitId);
        }
        if (commitId == null || !existCommit(commitId)) {
            SystemExit("No commit with that id exists.");
        }
        String currentBranch = getCurrentBranch();
        Commit commit = Commit.restore(commitId);
        for (File file : getFilesInFile(PATH_WORKING)) {
            String fileName = file.getName();
            if (fileName.equals(_gitletPath)) {
                continue;
            }
            Doc doc = new Doc(fileName, PATH_WORKING);
            if (!isTrackedByBranch(fileName, currentBranch)) {
                // FIXME -- IF WILL BE MODIFIED OR DELETED

                boolean willBeDeleted = !commit.containsFileName(fileName);
                boolean willBeModified = commit.containsFileName(fileName) &&
                        commit.containsFileHash(doc.myHash());
                if (willBeDeleted || willBeModified) {
                    SystemExit("There is an untracked file in the way; delete it or add it first.");
                }
            }
        }
        for (File file : getFilesInFile(PATH_WORKING)) {
            if (!file.getName().equals( _gitletPath)) {
                delete(file);
            }
        }
        for (String hash : commit.myFiles()) {
            String filename = _blobs.getNameOf(hash);
            File source = new File(PATH_BLOBS + hash + _contentFolder + filename);
            File target = new File(PATH_WORKING + filename);
            copyFiles(source, target);
        }
    }

    /** Function for "merge [branch name]". */
    private void doMerge(String[] operands) {
        doTest(operands);
        String givenBranchName = operands[0];
        String currentBranch = getCurrentBranch();
        String[] removed = readFrom(_removedNames);
        boolean conflictOccur = false;
        for (File file : getFilesInFile(PATH_WORKING)) {
            String fileName = file.getName();
            if (fileName.equals(_gitletPath)) {
                continue;
            }
            if (!isTrackedByBranch(fileName, currentBranch)) {
                SystemExit("There is an untracked file in the way; delete it or add it first.");
            }
        }
        if (!_staged.isEmpty() || removed != null) {
            SystemExit("You have uncommitted changes.");
        }
        if (hasBranchName(givenBranchName)) {
            SystemExit("A branch with that name does not exist.");
        }
        if (currentBranch.equals(givenBranchName)) {
            SystemExit("Cannot merge a branch with itself.");
        }

        String splitCommitHash = getSplitCommit(currentBranch, givenBranchName);
        Branch givenBranch = Branch.restore(givenBranchName);
        Commit splitCommit = Commit.restore(splitCommitHash);
        Commit lastCommitOfCurrent = Commit.restore(_branch.myLatestCommit());
        Commit lastCommitOfGiven = Commit.restore(givenBranch.myLatestCommit());

        if (splitCommitHash.equals(lastCommitOfGiven)) {
            SystemExit("Given branch is an ancestor of the current branch.");
        }
        if (splitCommitHash.equals(lastCommitOfCurrent)) {
            File source = new File(PATH_BRANCHES + givenBranch.myName() + "/" + _commitsFolder);
            File target = new File(PATH_BRANCHES + _branch.myName() + "/" + _commitsFolder);
            if (target.exists()) {
                delete(target);
            }
            copyFiles(source, target);
            _branch = Branch.restore();
            _branch.changeMyHeadCommitTo(_branch.myLatestCommit());
            SystemExit("Current branch fast-forwarded.");
        }

        for (String fileHash : lastCommitOfGiven.myFiles()) {
            String fileName = _blobs.getNameOf(fileHash);
            boolean existedButModifiedAndCurrNotChanged = splitCommit.containsFileName(fileName)
                    && !splitCommit.containsFileHash(fileHash)
                    && lastCommitOfCurrent.containsFileHash(splitCommit.getHashByName(fileName));
            boolean newFileAndCurrNotHave = !splitCommit.containsFileName(fileName)
                    && !splitCommit.containsFileHash(fileHash)
                    && !lastCommitOfCurrent.containsFileHash(fileHash)
                    && !lastCommitOfCurrent.containsFileName(fileName);
            if (existedButModifiedAndCurrNotChanged || newFileAndCurrNotHave) {
                _blobs.checkOutByHash(fileHash);
                doAdd(new String[] {fileName});
            }

            boolean existedButModifiedInDiffWays = !splitCommit.containsFileHash(fileHash)
                    && splitCommit.containsFileName(fileName)
                    && !lastCommitOfCurrent.containsFileHash(fileHash)
                    && lastCommitOfCurrent.containsFileName(fileName);
            boolean newFileButModifiedInDiffWays = !splitCommit.containsFileHash(fileHash)
                    && !splitCommit.containsFileName(fileName)
                    && !lastCommitOfCurrent.containsFileHash(fileHash)
                    && lastCommitOfCurrent.containsFileName(fileName);
            if (existedButModifiedInDiffWays || newFileButModifiedInDiffWays) {
                conflictOccur = true;
                _blobs.checkOutByHash(fileHash);
                writeInto(PATH_WORKING + fileName, false, "<<<<<<< HEAD");
                String currentFileHash = lastCommitOfCurrent.getHashByName(fileName);
                writeInto(PATH_WORKING + fileName, true, PATH_BLOBS + currentFileHash + _contentFolder + fileName);
                writeInto(PATH_WORKING + fileName, true, "=======");
                writeInto(PATH_WORKING + fileName, true, readFrom(PATH_BLOBS + fileHash + _contentFolder + fileName));
                writeInto(PATH_WORKING + fileName, true, ">>>>>>>");
                doAdd(new String[] {fileName});
            }
        }

        for (String fileHash : splitCommit.myFiles()) {
            String fileName = _blobs.getNameOf(fileHash);
            boolean existedNotModifiedCurrButDeletedInGiven = !lastCommitOfGiven.containsFileHash(fileHash)
                    && !lastCommitOfGiven.containsFileName(fileName)
                    && lastCommitOfCurrent.containsFileHash(fileHash)
                    && lastCommitOfCurrent.containsFileName(fileName);
            if (existedNotModifiedCurrButDeletedInGiven) {
                File fileShouldBeDeleted = new File(PATH_WORKING + fileName);
                if (fileShouldBeDeleted.exists()) {
                    fileShouldBeDeleted.delete();
                }
                addToRemovedNames(fileName);
            }

            boolean existedButGiveModifedAndCurrDeleted = !lastCommitOfGiven.containsFileHash(fileHash)
                    && lastCommitOfGiven.containsFileName(fileName)
                    && !lastCommitOfCurrent.containsFileHash(fileHash)
                    && !lastCommitOfCurrent.containsFileName(fileName);
            boolean existedButCurrModifedAndGivenDeleted = !lastCommitOfCurrent.containsFileHash(fileHash)
                    && lastCommitOfCurrent.containsFileName(fileName)
                    && !lastCommitOfGiven.containsFileHash(fileHash)
                    && !lastCommitOfGiven.containsFileName(fileName);
            if (existedButCurrModifedAndGivenDeleted || existedButGiveModifedAndCurrDeleted) {
                conflictOccur = true;
                writeInto(PATH_WORKING + fileName, false, "<<<<<<< HEAD");
                if (lastCommitOfCurrent.containsFileName(fileName)) {
                    writeInto(PATH_WORKING + fileName, true, readFrom(PATH_BLOBS + lastCommitOfCurrent.getHashByName(fileName) + _contentFolder + fileName));
                } else {
                    writeInto(PATH_WORKING + fileName, true, "");
                }
                writeInto(PATH_WORKING + fileName, true, "=======");
                if (lastCommitOfGiven.containsFileName(fileName)) {
                    writeInto(PATH_WORKING + fileName, true, readFrom(PATH_BLOBS + lastCommitOfGiven.getHashByName(fileName) + _contentFolder + fileName));
                } else {
                    writeInto(PATH_WORKING + fileName, true, "");
                }
                writeInto(PATH_WORKING + fileName, true, ">>>>>>>");
                doAdd(new String[] {fileName});
            }
        }

        doCommit(new String[] {String.format("Merged %s into %s.", givenBranchName, currentBranch)});
        Commit mergedCommit = Commit.restore(_branch.myLatestCommit());
        mergedCommit.tagAsMerged();
        mergedCommit.addParent(givenBranchName);

        if (conflictOccur) {
            SystemExit("Encountered a merge conflict.");
        }
    }

    /** Function for add-remote [remote name] [name of remote directory]/.gitlet. */
    private void doAddRemote(String[] operands) {
        // FIXME -- EXTRA CREDIT
        String remoteName = operands[0];
        String remoteDirectory = operands[1];
        System.out.println("[" + remoteName + "]");
        System.out.println("[" + remoteDirectory + "]");
    }

    /** Function for rm-remote [remote name]. */
    private void doRmRemote(String[] operands) {
        // FIXME -- EXTRA CREDIT
        String remoteName = operands[0];
        System.out.println("[" + remoteName + "]");
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
        _branch = Branch.restore(branchName);
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

    /** Get the current(head) commit for current branch. */
    static String currentHeadCommit() {
        return _branch.myHeadCommit();
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
        _commands.put(RM, this::doRm);
        _commands.put(LOG, this::doLog);
        _commands.put(GLOBALLOG, this::doGlobalLog);
        _commands.put(STATUS, this::doStatus);
        _commands.put(BRANCH, this::doBranch);
        _commands.put(RMBRANCH, this::doRmBranch);
        _commands.put(CHECKOUTF, this::doCheckoutF);
        _commands.put(CHECKOUTCF, this::doCheckoutCF);
        _commands.put(CHECKOUTB, this::doCheckoutB);
        _commands.put(RESET, this::doReset);
        _commands.put(MERGE, this::doMerge);
        _commands.put(ADDREMOTE, this::doAddRemote);
        _commands.put(RMREMOTE, this::doRmRemote);
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
    /** The current branch. */
    static Branch _branch;

    /** The File name of the directory that saves Gitlet System files. */
    private static final String _gitletPath = ".gitlet";
    /** Default Date Format. */
    static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
    /** Initialized branch -> *master */
    static final String DEFAULT_BRANCH = "master";
    /** Default hash for "initial commit"*/
    static final String INIT_COMMIT = sha1(INIT_MESSAGE, getDate(INIT_DATE));

    /** Convenience for directory on Working Area. */
    static final String PATH_WORKING = "./";
    /** Convenience for directory on .gitlet/Commits. */
    static final String PATH_COMMITS = _gitletPath + "/" + "Commits/";
    /** Convenience for directory on .gitlet/Blobs/. */
    static final String PATH_BLOBS = _gitletPath + "/" + "Blobs/";
    /** Convenience for directory on .gitlet/Staged/. */
    static final String PATH_STAGED = _gitletPath + "/" + "Staged/";
    /** Convenience for directory on .gitlet/Branches/. */
    static final String PATH_BRANCHES = _gitletPath + "/" + "Branches/";
    /** Convenience for directory on .gitlet/Branches/currentBranch.txt. */
    static final String PATH_CURRENTBRANCH = PATH_BRANCHES + "currentBranch.txt";

}
