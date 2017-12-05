package gitlet;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

import static gitlet.Branch._commitsFolder;
import static gitlet.Commit.*;
import static gitlet.Utils.*;
import static gitlet.Command.Type.*;
import static gitlet.Staged.*;

/** Operator form Gitlet System.
 *  @author Shixuan (Wayne) Li
 */
public class GitletOperator {

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
        _branch = new Branch().restoreBranch();
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
            _staged.addToRemovedNames(filename);
        }
    }

    /** Function for "log". */
    private void doLog(String[] unused) {
        doTest(unused);
        Commit headCommit = new Commit().restoreCommit(currentHeadCommit());
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
                headCommit = new Commit().restoreCommit(headCommit.myParents()[0]);
            } else {
                return;
            }
        }
    }

    /** Function for "global-log". */
    private void doGlobalLog(String[] unused) {
        doTest(unused);
        for (String hash : getAllDirectorysFrom(PATH_COMMITS)) {
            Commit headCommit = new Commit().restoreCommit(hash);
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
                    if (!fileInWorking.exists() && !_staged.existFileNameInRemoved(fileName)) {
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
        Commit commit = new Commit().restoreCommit(currentCommit);
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

        commitId = fullLengthIdOf(commitId);
        if (commitId == null || !existCommit(commitId)) {
            SystemExit("No commit with that id exists.");
        }

        Commit commit = new Commit().restoreCommit(commitId);

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
        rewriteCurrentBranch(branchName);
        Commit commit = new Commit().restoreCommit(currentHeadCommit());
        for (File file : getFilesInFile(PATH_WORKING)) {
            String fileName = file.getName();
            if (!fileName.equals(_gitletPath)) {
                if (!isTrackedByBranch(fileName, currentBranch)) {
                    if (isTrackedByCommit(fileName, commit.myHash())) {
                        SystemExit("There is an untracked file in the way; delete it or add it first.");
                    }
                }
            }
        }
        for (File file : getFilesInFile(PATH_WORKING)) {
            if (!file.getName().equals( _gitletPath)) {
                deleteFile(file);
            }
        }
        for (String hash : commit.myFiles()) {
            _blobs.checkOutByHash(hash);
        }
    }

    /** Function for "reset [commit id]". */
    private void doReset(String[] operands) {
        doTest(operands);
        String commitId = operands[0];

        commitId = fullLengthIdOf(commitId);
        if (commitId == null || !existCommit(commitId)) {
            SystemExit("No commit with that id exists.");
        }
        String currentBranch = getCurrentBranch();
        Commit commit = new Commit().restoreCommit(commitId);
        for (File file : getFilesInFile(PATH_WORKING)) {
            String fileName = file.getName();
            if (!fileName.equals(_gitletPath)) {
                if (!isTrackedByBranch(fileName, currentBranch)) {
                    if (isTrackedByCommit(fileName, commitId)) {
                        SystemExit("There is an untracked file in the way; delete it or add it first.");
                    }
                }
            }
        }
        for (File file : getFilesInFile(PATH_WORKING)) {
            if (!file.getName().equals( _gitletPath)) {
                if (isTrackedByCommit(file.getName(), commitId)) {
                    deleteFile(file);
                }
            }
        }
        for (String hash : commit.myFiles()) {
            String filename = _blobs.getNameOf(hash);
            File source = new File(PATH_BLOBS + hash + _contentFolder + filename);
            File target = new File(PATH_WORKING + filename);
            copyFiles(source, target);
        }
        for (String stagedFile : getAllDirectorysFrom(PATH_STAGED)) {
            deleteFile(new File(PATH_STAGED + stagedFile));
        }
        _branch.changeMyHeadCommitTo(commitId);
    }

    /** Function for "merge [branch name]". */
    private void doMerge(String[] operands) {
        doTest(operands);

        String givenBranchName = operands[0];
        String currentBranch = getCurrentBranch();
        String[] removed = readFrom(_removedNames);
        boolean conflictOccur = false;
        if (!_staged.isEmpty() || removed.length >= 1) {
            SystemExit("You have uncommitted changes.");
        }
        for (File file : getFilesInFile(PATH_WORKING)) {
            String fileName = file.getName();
            if (fileName.equals(_gitletPath)) {
                continue;
            }
            if (!isTrackedByBranch(fileName, currentBranch)) {
                SystemExit("There is an untracked file in the way; delete it or add it first.");
            }
        }
        if (!hasBranchName(givenBranchName)) {
            SystemExit("A branch with that name does not exist.");
        }
        if (currentBranch.equals(givenBranchName)) {
            SystemExit("Cannot merge a branch with itself.");
        }

        String splitCommitHash = getSplitCommit(currentBranch, givenBranchName);
        Commit splitCommit = new Commit().restoreCommit(splitCommitHash);
        Commit lastCommitOfCurrent = new Commit().restoreCommit(_branch.myLatestCommit());
        Branch givenBranch = new Branch().restoreBranch(givenBranchName);
        Commit lastCommitOfGiven = new Commit().restoreCommit(givenBranch.myLatestCommit());

        if (splitCommitHash.equals(lastCommitOfGiven.myHash())) {
            SystemExit("Given branch is an ancestor of the current branch.");
        }
        if (splitCommitHash.equals(lastCommitOfCurrent.myHash())) {
            File source = new File(PATH_BRANCHES + givenBranch.myName() + "/" + _commitsFolder);
            File target = new File(PATH_BRANCHES + _branch.myName() + "/" + _commitsFolder);
            if (target.exists()) {
                deleteFile(target);
            }
            copyFiles(source, target);
            _branch = new Branch().restoreBranch();
            _branch.changeMyHeadCommitTo(_branch.myLatestCommit());
            SystemExit("Current branch fast-forwarded.");
        }

        for (String fileHash : lastCommitOfGiven.myFiles()) {
            String fileName = _blobs.getNameOf(fileHash);

            // Condition 2-1
            boolean existedAndBothModifiedSameWay = splitCommit.containsFileName(fileName)
                    && !splitCommit.containsFileHash(fileHash)
                    && lastCommitOfCurrent.containsFileHash(fileHash);
            if (existedAndBothModifiedSameWay) {
                continue;
            }

            // Condition 5
            boolean newFileExistOnlyInGiven = !splitCommit.containsFileName(fileName)
                    && !lastCommitOfCurrent.containsFileName(fileName);
            if (newFileExistOnlyInGiven) {
                File fileShouldBeCreated = new File(PATH_WORKING + fileName);
                if (fileShouldBeCreated.exists()) {
                    fileShouldBeCreated.delete();
                }
                _blobs.checkOutByHash(fileHash);
                doAdd(new String[] {fileName});
            }

            boolean existedButModifiedInDiffWays = splitCommit.containsFileName(fileName)
                    && !splitCommit.containsFileHash(fileHash)
                    && lastCommitOfCurrent.containsFileName(fileName)
                    && !lastCommitOfCurrent.containsFileHash(fileHash)
                    && !lastCommitOfCurrent.containsFileHash(splitCommit.getHashByName(fileName));
            boolean newFileButModifiedInDiffWays = !splitCommit.containsFileName(fileName)
                    && lastCommitOfCurrent.containsFileName(fileName)
                    && !lastCommitOfCurrent.containsFileHash(fileHash);
            if (existedButModifiedInDiffWays || newFileButModifiedInDiffWays) {
                conflictOccur = true;
                writeInto(PATH_WORKING + fileName, false, "<<<<<<< HEAD");
                String currentFileHash = lastCommitOfCurrent.getHashByName(fileName);
                writeInto(PATH_WORKING + fileName, true, readFrom(PATH_BLOBS + currentFileHash + _contentFolder + fileName));
                writeInto(PATH_WORKING + fileName, true, "=======");
                writeInto(PATH_WORKING + fileName, true, readFrom(PATH_BLOBS + fileHash + _contentFolder + fileName));
                writeInto(PATH_WORKING + fileName, true, ">>>>>>>");
                doAdd(new String[] {fileName});
            }
        }

        for (String fileHash : splitCommit.myFiles()) {
            String fileName = _blobs.getNameOf(fileHash);

            // Condition 1
            boolean existedButModifiedGivenAndUnchangedCurr = lastCommitOfGiven.containsFileName(fileName)
                    && !lastCommitOfGiven.containsFileHash(fileHash)
                    && lastCommitOfCurrent.containsFileName(fileName)
                    && lastCommitOfCurrent.containsFileHash(fileHash);
            if (existedButModifiedGivenAndUnchangedCurr) {
                File fileShouldBeUpdated = new File(PATH_WORKING + fileName);
                if (fileShouldBeUpdated.exists()) {
                    fileShouldBeUpdated.delete();
                }
                _blobs.checkOutByHash(lastCommitOfGiven.getHashByName(fileName));
                doAdd(new String[] {fileName});
            }

            // Condition 2-2
            boolean existedButBothDeleted = !lastCommitOfGiven.containsFileName(fileName)
                    && !lastCommitOfCurrent.containsFileName(fileName);
            if (existedButBothDeleted) {
                continue;
            }

            // Condition 3
            boolean existedUnchangedGivenButModifiedCurr = lastCommitOfGiven.containsFileHash(fileHash)
                    && lastCommitOfCurrent.containsFileName(fileName)
                    && !lastCommitOfCurrent.containsFileHash(fileHash);
            if (existedUnchangedGivenButModifiedCurr) {
                continue;
            }

            // Condition 6
            boolean existedButDeletedGivenAndUnchangedCurr = !lastCommitOfGiven.containsFileName(fileName)
                    && lastCommitOfCurrent.containsFileHash(fileHash);
            if (existedButDeletedGivenAndUnchangedCurr) {
                doRm(new String[] {fileName});
            }

            // Condition 7
            boolean existedUnchangedGivenButDeletedCurr = lastCommitOfGiven.containsFileHash(fileHash)
                    && !lastCommitOfCurrent.containsFileName(fileName);
            if (existedUnchangedGivenButDeletedCurr) {
                continue;
            }

            boolean existedButGivenModifedAndCurrDeleted = lastCommitOfGiven.containsFileName(fileName)
                    && !lastCommitOfGiven.containsFileHash(fileHash)
                    && !lastCommitOfCurrent.containsFileName(fileName);
            boolean existedButCurrModifedAndGivenDeleted = !lastCommitOfGiven.containsFileName(fileName)
                    && lastCommitOfCurrent.containsFileName(fileName)
                    && !lastCommitOfCurrent.containsFileHash(fileHash);
            if (existedButCurrModifedAndGivenDeleted || existedButGivenModifedAndCurrDeleted) {
                conflictOccur = true;
                File conflictFile = new File(PATH_WORKING + fileName);
                if (!conflictFile.exists()) {
                    try {
                        conflictFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                writeInto(PATH_WORKING + fileName, false, "<<<<<<< HEAD");
                if (lastCommitOfCurrent.containsFileName(fileName)) {
                    String currentHash = lastCommitOfCurrent.getHashByName(fileName);
                    writeInto(PATH_WORKING + fileName, true, readFrom(PATH_BLOBS + currentHash + _contentFolder + fileName));
                    writeInto(PATH_WORKING + fileName, true, "=======");
                } else {
                    writeInto(PATH_WORKING + fileName, true, "=======");
                }
                if (lastCommitOfGiven.containsFileName(fileName)) {
                    String givenHash = lastCommitOfGiven.getHashByName(fileName);
                    writeInto(PATH_WORKING + fileName, true, readFrom(PATH_BLOBS + givenHash + _contentFolder + fileName));
                    writeInto(PATH_WORKING + fileName, true, ">>>>>>>");
                } else {
                    writeInto(PATH_WORKING + fileName, true, ">>>>>>>");
                }
                doAdd(new String[] {fileName});
            }
        }

        for (String fileHash : lastCommitOfCurrent.myFiles()) {
            String fileName = _blobs.getNameOf(fileHash);

            // Condition 4
            boolean newFileExistOnlyInCurr = !splitCommit.containsFileName(fileName)
                    && !lastCommitOfGiven.containsFileName(fileName);
            if (newFileExistOnlyInCurr) {
                continue;
            }
        }

        Commit mergedCommit = new Commit(String.format("Merged %s into %s.", givenBranchName, currentBranch));
        mergedCommit.createCommit(true);
        mergedCommit.tagAsMerged();
        mergedCommit.addParent(lastCommitOfGiven.myHash());

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
            deleteFile(new File(_gitletPath));
        } else {
            SystemExit("This directory hasn't been initialized --Wayne's doClean");
        }
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

    /* **********************************
     *        File R/W/Cr/Cp/De         *
     ********************************** */

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

    /** WriteInto with input as "File". */
    static void writeInto(File file, boolean ifAppend, String... strs) {
        writeInto(file.getPath(), ifAppend, strs);
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

    /** readFrom with input as "File". */
    static String[] readFrom(File file) {
        return readFrom(file.getPath());
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

    /** Delete files and directories for doClean. */
    static void deleteFile(File f){
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                deleteFile(c);
        }
        f.delete();
    }

    /* **********************************
     *          Commit-Related          *
     ********************************** */

    /** Check the existence of a commit with id.
     * @param id -- hash of the commit.
     * @return -- check result. */
    public boolean existCommit(String id) {
        for (String hash : getAllDirectorysFrom(PATH_COMMITS)) {
            if (hash.equals(id)) {
                return true;
            }
        }
        return false;
    }

    /** Search if there is a commit in .gitlet/Commits with the message.
     * @param message -- message to be searched.
     * @return -- check result. */
    public boolean hasCommitWithMsg(String message) {
        for (String commitHash : getAllDirectorysFrom(PATH_COMMITS)) {
            Commit commit = new Commit().restoreCommit(commitHash);
            if (commit.myMessage().equals(message)) {
                return true;
            }
        }
        return false;
    }

    /** Add branch to the commit.
     * @param hash -- hash of the commit
     * @param branch -- name of the branch. */
    public void addBranchTo(String hash, String branch) {
        writeInto(PATH_COMMITS + hash + "/" + _branchesFolder, true, branch);
    }

    /** Delete branch from the commit.
     * @param hash -- hash of the commit
     * @param branch -- name of the branch. */
    public void deleteBranchFrom(String hash, String branch) {
        File commit = new File(PATH_COMMITS + hash + "/" + _branchesFolder);
        String[] currentBranchs = readFrom(commit);
        clearFile(commit);
        for (String currentbranch : currentBranchs) {
            if (!currentbranch.equals(branch)) {
                writeInto(commit, true, currentbranch);
            }
        }
    }

    /** Get hashs of the commit with the message.
     * @param message -- message to be searched.
     * @return -- commits' hashes as a searched result. */
    public ArrayList<String> getCommitsWithMsg(String message) {
        ArrayList<String> result = new ArrayList<>();
        for (String commitHash : getAllDirectorysFrom(PATH_COMMITS)) {
            Commit commit = new Commit().restoreCommit(commitHash);
            if (commit.myMessage().equals(message)) {
                result.add(commitHash);
            }
        }
        return result;
    }

    /** Restore a Commit with 7-digit id. Assume exist.
     * @param id -- 7-digit version commit id.
     * @return -- full length version of the id. */
    public String fullLengthIdOf(String id) {
        String fullId = null;
        int length = id.length();
        for (String hash : getAllDirectorysFrom(PATH_COMMITS)) {
            String partHash = hash.substring(0, length);
            if (partHash.equals(id)) {
                fullId = hash;
            }
        }
        return fullId;
    }

    /** Check if a file name is tracked by the commit. Assume exist commit.
     * @param filename -- input
     * @return check result. */
    public boolean isTrackedByCommit(String filename, String commitHash) {
        String[] filesInCommit = readFrom(PATH_COMMITS + commitHash + "/" + _filesFolder);
        if (filesInCommit == null) {
            return false;
        }
        for (String commitFile : filesInCommit) {
            String commitFileName = _blobs.getNameOf(commitFile);
            if (commitFileName.equals(filename)) {
                return true;
            }
        }
        return false;
    }

    /* **********************************
     *          Branch-Related          *
     ********************************** */

    /** See if there already exist a branch with the name.
     * @param branchName -- input.
     * @return -- check result. */
    public boolean hasBranchName(String branchName) {
        for (String branch : getAllDirectorysFrom(PATH_BRANCHES)) {
            if (branch.equals(branchName)) {
                return true;
            }
        }
        return false;
    }

    /** Delete branch.
     * @param branchName -- branch name to be deleted. */
    public void deleteBranch(String branchName) {
        File branch = new File(PATH_BRANCHES + branchName);
        deleteFile(branch);
    }

    /** Check if a file name is ever tracked.
     * @param fileName -- input
     * @return -- check result. */
    public boolean isEverTracked(String fileName) {
        for (String fileHash : getAllDirectorysFrom(PATH_BLOBS)) {
            if (_blobs.getNameOf(fileHash).equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    /** Check if a file with filename in WorkingArea is tracked by the branch.
     * @param filename -- file name as input.
     * @param branchName -- branch name as input.
     * @return -- check result. */
    public boolean isTrackedByBranch(String filename, String branchName) {
        Branch branch = new Branch().restoreBranch(branchName);
        if (branch == null) {
            return false;
        }
        ArrayList<String> commits = branch.myCommits();
        if (commits == null) {
            return false;
        }
        for (String commitHash : commits) {
            Commit commit = new Commit().restoreCommit(commitHash);
            if (commit.containsFileName(filename)) {
                return true;
            }
        }
        return false;
    }

    /** Get the split commit of the two branches.
     * @param branchName1 -- branch name of the first branch as input.
     * @param branchName2 -- branch name of the second branch as input.
     * @return -- the hash of the split commit of the two branches. */
    public String getSplitCommit(String branchName1, String branchName2) {
        Branch branch1 = new Branch().restoreBranch(branchName1);
        String commitHash1 = branch1.myLatestCommit();

        while (true) {
            Commit commit1 = new Commit().restoreCommit(commitHash1);
            if (commit1.containsBranch(branchName2) && !commit1.isMerged()) {
                return commit1.myHash();
            }
            if (commit1.hasParents()) {
                commitHash1 = commit1.myParents()[0];
            } else {
                break;
            }
        }
        return null;
    }

    /* **********************************
     *         Global-Attributes        *
     ********************************** */

    /** Convenience for re-writing currentBranch.txt. */
    static void rewriteCurrentBranch(String branchName) {
        writeInto(PATH_CURRENTBRANCH, false, branchName);
        _branch = new Branch().restoreBranch(branchName);
    }

    /** Get the current(head) commit for current branch. */
    static String currentHeadCommit() {
        return _branch.myHeadCommit();
    }

    /** Delete from Working directory. */
    static void deleteFromWorking(String filename) {
        new File(PATH_WORKING + filename).delete();
    }

    /* **********************************
     *         Static-Utilities         *
     ********************************** */

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
    static final String INIT_COMMIT = sha1(INIT_MESSAGE, DATE_FORMAT.format(INIT_DATE));

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
