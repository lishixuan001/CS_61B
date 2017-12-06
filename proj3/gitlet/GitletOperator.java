package gitlet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Set;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;

import static gitlet.Branch.COMMITS_FOLDER;
import static gitlet.Commit.*;
import static gitlet.Utils.*;
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
            COMMANDS.get(cmnd.commandType()).accept(cmnd.operands());
        } catch (GitletException excp) {
            throw new Error("Error --Main.doCommand");
        }
    }

    /** Function for "init".
     * @param unused  -- none. */
    private void doInit(String[] unused) {
        if (isInitialized()) {
            doSystemExit("A Gitlet version-control "
                    + "system already exists in the current directory.");
        }
        new File(GITLET_PATH).mkdir();
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
            doSystemExit("File does not exist.");
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

        if (!_staged.hasFileName(filename)
                && !isTrackedByCommit(filename, currentHeadCommit())) {
            doSystemExit("No reason to remove the file.");
        }

        if (_staged.hasFileName(filename)) {
            _staged.deleteByName(filename);
        }

        if (isTrackedByCommit(filename, currentHeadCommit())) {
            deleteFromWorking(filename);
            _staged.addToRemovedNames(filename);
        }
    }

    /** Function for "log".
     * @param unused -- unused. */
    private void doLog(String[] unused) {
        doTest(unused);
        Commit headCommit = new Commit().restoreCommit(currentHeadCommit());
        while (true) {
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
                headCommit = new Commit().restoreCommit(
                        headCommit.myParents()[0]);
            } else {
                return;
            }
        }
    }

    /** Function for "global-log".
     * @param unused -- unused. */
    private void doGlobalLog(String[] unused) {
        doTest(unused);
        for (String hash : getAllDirectorysFrom(PATH_COMMITS)) {
            Commit headCommit = new Commit().restoreCommit(hash);
            System.out.println("===");
            System.out.println("commit " + headCommit.myHash());
            if (headCommit.isMerged()) {
                String originCommit =
                        headCommit.myParents()[0].substring(0, 7);
                String mergedInCommit =
                        headCommit.myParents()[1].substring(0, 7);
                System.out.println("Merge: " + originCommit
                        + " " + mergedInCommit);
            }
            System.out.println("Date: " + headCommit.myDate());
            System.out.println(headCommit.myMessage());
            System.out.println();
        }
    }

    /** Function for "find [commit message]".
     * @param operands -- input. */
    private void doFind(String[] operands) {
        doTest(operands);
        String message = operands[0];

        if (hasCommitWithMsg(message)) {
            for (String commit : getCommitsWithMsg(message)) {
                System.out.println(commit);
            }
        } else {
            doSystemExit("Found no commit with that message.");
        }
    }

    /** Function for "status".
     * @param unused -- unused. */
    private void doStatus(String[] unused) {
        doTest(unused);

        HashMap<String, String> staged = new HashMap<>();
        ArrayList<String> stagedNames = new ArrayList<>();
        ArrayList<String> stagedHashs = getAllDirectorysFrom(PATH_STAGED);
        for (String stagedHash : stagedHashs) {
            String stagedName = _staged.getNameByHash(stagedHash);
            stagedNames.add(stagedName);
            staged.put(stagedName, stagedHash);
        }

        doStatusBranches();
        doStatusStagedFiles(stagedNames);
        doStatusRemovedFiles();
        doStatusModifiedFiles(staged, stagedNames);
        doStatusUntrackedFiles();
    }

    /** Helper function showing Branches part for doStatus. */
    private void doStatusBranches() {
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
    }

    /** Helper function showing Staged Files part for doStatus.
     * @param stagedNames -- all staged file names. */
    private void doStatusStagedFiles(ArrayList<String> stagedNames) {
        System.out.println("=== Staged Files ===");
        Collections.sort(stagedNames);

        for (String name : stagedNames) {
            System.out.println(name);
        }
        System.out.println();
    }

    /** Helper function showing Removed Files part for doStatus. */
    private void doStatusRemovedFiles() {
        System.out.println("=== Removed Files ===");
        ArrayList<String> removed = new ArrayList<>();
        removed.addAll(doStringsToList(readFrom(REMOVED_NAMES)));
        Collections.sort(removed);

        for (String file : removed) {
            System.out.println(file);
        }
        System.out.println();
    }

    /** Helper function showing Modified Files part for doStatus.
     * @param staged -- HashMap(fileName, fileHash)
     * @param stagedNames -- all staged file names*/
    private void doStatusModifiedFiles(HashMap<String,
            String> staged, ArrayList<String> stagedNames) {
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
        String[] filesInCurrentCommit = readFrom(PATH_COMMITS
                + currentHeadCommit() + "/" + FILES_FOLDER);
        if (filesInCurrentCommit != null) {
            for (String fileHash : filesInCurrentCommit) {
                String fileName = _blobs.getNameOf(fileHash);
                if (!_staged.hasFileName(fileName)) {
                    File fileInWorking = new File(PATH_WORKING
                            + fileName);
                    if (!fileInWorking.exists()
                            && !_staged.existFileNameInRemoved(fileName)) {
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
    }

    /** Helper function showing Untracked Files part for doStatus. */
    private void doStatusUntrackedFiles() {
        System.out.println("=== Untracked Files ===");
        for (File file : getFilesInFile(PATH_WORKING)) {
            String fileName = file.getName();
            if (fileName.equals(GITLET_PATH)) {
                continue;
            }
            if (!isEverTracked(fileName) && !_staged.hasFileName(fileName)) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }

    /** Function for "branch [branch name]".
     * @param operands -- input */
    private void doBranch(String[] operands) {
        doTest(operands);
        String branchName = operands[0];
        if (hasBranchName(branchName)) {
            doSystemExit("A branch with that name already exists.");
        }
        Branch newBranch = new Branch(branchName);
        newBranch.createBranch();
        addBranchTo(currentHeadCommit(), branchName);
    }

    /** Function for "rm-branch [branch name]".
     * @param operands -- input */
    private void doRmBranch(String[] operands) {
        doTest(operands);
        String branchName = operands[0];
        if (getCurrentBranch().equals(branchName)) {
            doSystemExit("Cannot remove the current branch.");
        }
        if (!hasBranchName(branchName)) {
            doSystemExit("A branch with that name does not exist.");
        }
        File commits =
                new File(PATH_BRANCHES + branchName + "/" + COMMITS_FOLDER);
        for (String commit : readFrom(commits)) {
            deleteBranchFrom(commit, branchName);
        }
        deleteBranch(branchName);
    }

    /** Function for "checkout -- [file name]".
     * @param operands -- input */
    private void doCheckoutF(String[] operands) {
        doTest(operands);
        String filename = operands[0];
        String currentCommit = currentHeadCommit();
        Commit commit = new Commit().restoreCommit(currentCommit);
        if (!commit.containsFileName(filename)) {
            doSystemExit("File does not exist in that commit.");
        }
        _blobs.checkOutByHash(commit.getHashByName(filename));
    }

    /** Function for "checkout [commit id] -- [file name]".
     * @param operands -- input */
    private void doCheckoutCF(String[] operands) {
        doTest(operands);
        String commitId = operands[0];
        String filename = operands[1];

        commitId = fullLengthIdOf(commitId);
        if (commitId == null || !existCommit(commitId)) {
            doSystemExit("No commit with that id exists.");
        }

        Commit commit = new Commit().restoreCommit(commitId);

        if (!commit.containsFileName(filename)) {
            doSystemExit("File does not exist in that commit.");
        }
        _blobs.checkOutByHash(commit.getHashByName(filename));
    }

    /** Function for "checkout [branch name]".
     * @param operands -- input */
    private void doCheckoutB(String[] operands) {
        doTest(operands);
        String branchName = operands[0];

        if (!hasBranchName(branchName)) {
            doSystemExit("No such branch exists.");
        }
        String currentBranch = getCurrentBranch();
        if (currentBranch.equals(branchName)) {
            doSystemExit("No need to checkout the current branch.");
        }
        rewriteCurrentBranch(branchName);
        Commit commit = new Commit().restoreCommit(currentHeadCommit());
        for (File file : getFilesInFile(PATH_WORKING)) {
            String fileName = file.getName();
            if (!fileName.equals(GITLET_PATH)) {
                if (!isTrackedByBranch(fileName, currentBranch)) {
                    if (isTrackedByCommit(fileName, commit.myHash())) {
                        doSystemExit("There is an untracked file "
                                + "in the way; delete it or add it first.");
                    }
                }
            }
        }
        for (File file : getFilesInFile(PATH_WORKING)) {
            if (!file.getName().equals(GITLET_PATH)) {
                deleteFile(file);
            }
        }
        for (String hash : commit.myFiles()) {
            _blobs.checkOutByHash(hash);
        }
    }

    /** Function for "reset [commit id]".
     * @param operands -- input */
    private void doReset(String[] operands) {
        doTest(operands);
        String commitId = operands[0];

        commitId = fullLengthIdOf(commitId);
        if (commitId == null || !existCommit(commitId)) {
            doSystemExit("No commit with that id exists.");
        }
        String currentBranch = getCurrentBranch();
        Commit commit = new Commit().restoreCommit(commitId);
        for (File file : getFilesInFile(PATH_WORKING)) {
            String fileName = file.getName();
            if (!fileName.equals(GITLET_PATH)) {
                if (!isTrackedByBranch(fileName, currentBranch)) {
                    if (isTrackedByCommit(fileName, commitId)) {
                        doSystemExit("There is an untracked file "
                                + "in the way; delete it or add it first.");
                    }
                }
            }
        }
        for (File file : getFilesInFile(PATH_WORKING)) {
            if (!file.getName().equals(GITLET_PATH)) {
                if (isTrackedByCommit(file.getName(), commitId)) {
                    deleteFile(file);
                }
            }
        }
        for (String hash : commit.myFiles()) {
            String filename = _blobs.getNameOf(hash);
            File source = new File(PATH_BLOBS + hash
                    + CONTENT_FOLDER + filename);
            File target = new File(PATH_WORKING + filename);
            copyFiles(source, target);
        }
        for (String stagedFile : getAllDirectorysFrom(PATH_STAGED)) {
            deleteFile(new File(PATH_STAGED + stagedFile));
        }
        _branch.changeMyHeadCommitTo(commitId);
    }

    /** Function for "merge [branch name]".
     * @param operands -- input */
    private void doMerge(String[] operands) {
        doTest(operands);

        boolean conflictOccur;
        String givenBranchName = operands[0];
        String currentBranch = getCurrentBranch();

        doMergeCheckFailureCases(currentBranch, givenBranchName);

        String splitCommitHash =
                getSplitCommit(currentBranch, givenBranchName);
        Branch givenBranch =
                new Branch().restoreBranch(givenBranchName);
        Commit splitCommit =
                new Commit().restoreCommit(splitCommitHash);
        Commit lastCommitOfCurrent =
                new Commit().restoreCommit(_branch.myLatestCommit());
        Commit lastCommitOfGiven =
                new Commit().restoreCommit(givenBranch.myLatestCommit());

        doMergeCheckSpecialMerges(givenBranchName, splitCommitHash,
                lastCommitOfCurrent.myHash(), lastCommitOfGiven.myHash());

        boolean conflictByGiven = checkByGivenBranchSide(splitCommit,
                lastCommitOfCurrent, lastCommitOfGiven);

        boolean conflictBySplit = checkBySplitCommitSide(splitCommit,
                lastCommitOfCurrent, lastCommitOfGiven);

        Commit mergedCommit = new Commit(String.format(
                "Merged %s into %s.", givenBranchName, currentBranch));
        mergedCommit.createCommit(true);
        mergedCommit.tagAsMerged();
        mergedCommit.addParent(lastCommitOfGiven.myHash());

        conflictOccur = conflictByGiven || conflictBySplit;

        if (conflictOccur) {
            doSystemExit("Encountered a merge conflict.");
        }
    }

    /** Helper function for doMerge. Checking failure cases.
     * @param currentBranch -- currentBranchName
     * @param givenBranchName -- givenBranchName */
    private void doMergeCheckFailureCases(String currentBranch,
                                          String givenBranchName) {
        String[] removed = readFrom(REMOVED_NAMES);

        if (!_staged.isEmpty() || removed.length >= 1) {
            doSystemExit("You have uncommitted changes.");
        }
        for (File file : getFilesInFile(PATH_WORKING)) {
            String fileName = file.getName();
            if (fileName.equals(GITLET_PATH)) {
                continue;
            }
            if (!isTrackedByBranch(fileName, currentBranch)) {
                doSystemExit("There is an untracked "
                        + "file in the way; delete it or add it first.");
            }
        }
        if (!hasBranchName(givenBranchName)) {
            doSystemExit("A branch with that name does not exist.");
        }
        if (currentBranch.equals(givenBranchName)) {
            doSystemExit("Cannot merge a branch with itself.");
        }
    }

    /** Helper function for doMerge. Checking failure cases.
     * @param givenBranchName -- givenBranchName
     * @param splitCommitHash -- splitCommitHash
     * @param lastCommitOfCurrentHash -- lastCommitOfCurrentHash
     * @param lastCommitOfGivenHash -- lastCommitOfGivenHash */
    private void doMergeCheckSpecialMerges(String givenBranchName,
                                           String splitCommitHash,
                                           String lastCommitOfCurrentHash,
                                           String lastCommitOfGivenHash) {

        if (splitCommitHash.equals(lastCommitOfGivenHash)) {
            doSystemExit("Given branch is an ancestor of the current branch.");
        }
        if (splitCommitHash.equals(lastCommitOfCurrentHash)) {
            File source =
                    new File(PATH_BRANCHES
                            + givenBranchName + "/" + COMMITS_FOLDER);
            File target =
                    new File(PATH_BRANCHES
                            + _branch.myName() + "/" + COMMITS_FOLDER);
            if (target.exists()) {
                deleteFile(target);
            }
            copyFiles(source, target);
            _branch = new Branch().restoreBranch();
            _branch.changeMyHeadCommitTo(_branch.myLatestCommit());
            doSystemExit("Current branch fast-forwarded.");
        }
    }

    /** Helper function for doMerge.
     * Checking merge conditions by given branch's side.
     * @param splitCommit -- splitCommit
     * @param lastCommitOfCurrent -- lastCommitOfCurrent
     * @param lastCommitOfGiven -- lastCommitOfGiven
     * @return -- if a conflict occurs. */
    private boolean checkByGivenBranchSide(Commit splitCommit,
                                        Commit lastCommitOfCurrent,
                                        Commit lastCommitOfGiven) {
        boolean conflictOccur = false;
        for (String fileHash : lastCommitOfGiven.myFiles()) {
            String fileName = _blobs.getNameOf(fileHash);

            boolean newFileExistOnlyInGiven =
                    !splitCommit.containsFileName(fileName)
                            && !lastCommitOfCurrent.containsFileName(fileName);
            if (newFileExistOnlyInGiven) {
                File fileShouldBeCreated =
                        new File(PATH_WORKING + fileName);
                if (fileShouldBeCreated.exists()) {
                    fileShouldBeCreated.delete();
                }
                _blobs.checkOutByHash(fileHash);
                doAdd(new String[]{fileName});
            }

            boolean existedButModifiedInDiffWays =
                    splitCommit.containsFileName(fileName)
                            && !splitCommit.containsFileHash(fileHash)
                            && lastCommitOfCurrent.containsFileName(fileName)
                            && !lastCommitOfCurrent.containsFileHash(fileHash)
                            && !lastCommitOfCurrent.containsFileHash(
                            splitCommit.getHashByName(fileName));
            boolean newFileButModifiedInDiffWays =
                    !splitCommit.containsFileName(fileName)
                            && lastCommitOfCurrent.containsFileName(fileName)
                            && !lastCommitOfCurrent.containsFileHash(fileHash);
            if (existedButModifiedInDiffWays
                    || newFileButModifiedInDiffWays) {
                conflictOccur = true;
                doConflictContionOne(fileName,
                        fileHash, lastCommitOfCurrent);
            }
        }
        return conflictOccur;
    }

    /** Helper for doCheckBySplitCommitSide, operate when a conflict
     *  that given and current modified in different ways.
     *  @param fileName -- file name
     *  @param fileHash -- file hash
     *  @param lastCommitOfCurrent -- lastCommitOfCurrent */
    private void doConflictContionOne(String fileName,
                                      String fileHash,
                                      Commit lastCommitOfCurrent) {
        writeInto(PATH_WORKING + fileName, false, "<<<<<<< HEAD");
        String currentFileHash =
                lastCommitOfCurrent.getHashByName(fileName);
        writeInto(PATH_WORKING + fileName, true,
                readFrom(PATH_BLOBS + currentFileHash
                        + CONTENT_FOLDER + fileName));
        writeInto(PATH_WORKING + fileName, true, "=======");
        writeInto(PATH_WORKING + fileName, true,
                readFrom(PATH_BLOBS + fileHash
                        + CONTENT_FOLDER + fileName));
        writeInto(PATH_WORKING + fileName, true, ">>>>>>>");
        doAdd(new String[] {fileName});
    }

    /** Helper function for doMerge.
     * Checking merge conditions by given branch's side.
     * @param splitCommit -- splitCommit
     * @param lastCommitOfCurrent -- lastCommitOfCurrent
     * @param lastCommitOfGiven -- lastCommitOfGiven
     * @return -- if a conflict occurs. */
    private boolean checkBySplitCommitSide(Commit splitCommit,
                                           Commit lastCommitOfCurrent,
                                           Commit lastCommitOfGiven) {
        boolean conflictOccur = false;
        for (String fileHash : splitCommit.myFiles()) {
            String fileName = _blobs.getNameOf(fileHash);

            boolean existedButModifiedGivenAndUnchangedCurr =
                    lastCommitOfGiven.containsFileName(fileName)
                            && !lastCommitOfGiven.containsFileHash(fileHash)
                            && lastCommitOfCurrent.containsFileName(fileName)
                            && lastCommitOfCurrent.containsFileHash(fileHash);
            if (existedButModifiedGivenAndUnchangedCurr) {
                File fileShouldBeUpdated =
                        new File(PATH_WORKING + fileName);
                if (fileShouldBeUpdated.exists()) {
                    fileShouldBeUpdated.delete();
                }
                _blobs.checkOutByHash(
                        lastCommitOfGiven.getHashByName(fileName));
                doAdd(new String[] {fileName});
            }

            boolean existedButDeletedGivenAndUnchangedCurr =
                    !lastCommitOfGiven.containsFileName(fileName)
                            && lastCommitOfCurrent.containsFileHash(fileHash);
            if (existedButDeletedGivenAndUnchangedCurr) {
                doRm(new String[] {fileName});
            }

            boolean existedButGivenModifedAndCurrDeleted =
                    lastCommitOfGiven.containsFileName(fileName)
                            && !lastCommitOfGiven.containsFileHash(fileHash)
                            && !lastCommitOfCurrent.containsFileName(fileName);
            boolean existedButCurrModifedAndGivenDeleted =
                    !lastCommitOfGiven.containsFileName(fileName)
                            && lastCommitOfCurrent.containsFileName(fileName)
                            && !lastCommitOfCurrent.containsFileHash(fileHash);
            if (existedButCurrModifedAndGivenDeleted
                    || existedButGivenModifedAndCurrDeleted) {
                conflictOccur = true;
                doConflictContionTwo(fileName,
                        lastCommitOfCurrent, lastCommitOfGiven);
            }
        }
        return conflictOccur;
    }

    /** Helper for doCheckBySplitCommitSide, operate when a conflict
     *  that one is modified and one deleted occurs.
     *  @param fileName -- file name
     *  @param lastCommitOfCurrent -- lastCommitOfCurrent
     *  @param lastCommitOfGiven -- lastCommitOfGiven */
    private void doConflictContionTwo(String fileName,
                                      Commit lastCommitOfCurrent,
                                      Commit lastCommitOfGiven) {
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
            String currentHash =
                    lastCommitOfCurrent.getHashByName(fileName);
            writeInto(PATH_WORKING + fileName, true,
                    readFrom(PATH_BLOBS + currentHash
                            + CONTENT_FOLDER + fileName));
            writeInto(PATH_WORKING + fileName, true, "=======");
        } else {
            writeInto(PATH_WORKING + fileName, true, "=======");
        }
        if (lastCommitOfGiven.containsFileName(fileName)) {
            String givenHash =
                    lastCommitOfGiven.getHashByName(fileName);
            writeInto(PATH_WORKING + fileName, true,
                    readFrom(PATH_BLOBS + givenHash
                            + CONTENT_FOLDER + fileName));
            writeInto(PATH_WORKING + fileName, true, ">>>>>>>");
        } else {
            writeInto(PATH_WORKING + fileName, true, ">>>>>>>");
        }
        doAdd(new String[] {fileName});
    }

    /** Function for add-remote [remote name] [name of remote]/.gitlet.
     * @param operands -- input */
    private void doAddRemote(String[] operands) {

        String remoteName = operands[0];
        String remoteDirectory = operands[1];
        System.out.println("[" + remoteName + "]");
        System.out.println("[" + remoteDirectory + "]");
    }

    /** Function for rm-remote [remote name].
     * @param operands -- input */
    private void doRmRemote(String[] operands) {

        String remoteName = operands[0];
        System.out.println("[" + remoteName + "]");
    }

    /** Function for "help".
     * @param unused -- unused */
    private void doHelp(String[] unused) {
        doTest(unused);
    }

    /** Function as the third input command check,
     * this work if has wrong type operands with previously
     * checked correct command.
     * @param unused -- unused */
    private void doError(String[] unused) {
        doSystemExit("Incorrect operands.");
    }


    /** Test if current directory is initialized.
     * @param operands -- input */
    private void doTest(String[] operands) {
        if (!isInitialized()) {
            doSystemExit("Not in an initialized Gitlet directory.");
        }
    }

    /** Clean up the .gitlet (remove).
     * @param unused -- unused */
    private void doClean(String[] unused) {
        if (isInitialized()) {
            deleteFile(new File(GITLET_PATH));
        } else {
            doSystemExit("This directory hasn't been"
                    + " initialized --Wayne's doClean");
        }
    }

    /** Check if current environment is initialized.
     * Must exist and must be directory.
     * @return -- check result. */
    private boolean isInitialized() {
        File initedPath = new File(GITLET_PATH);
        return initedPath.exists() && initedPath.isDirectory();
    }

    /** This should never occur, if this happen, need to check.
     * @param unused -- unused */
    private void doEOF(String[] unused) {
        doSystemExit("EOF error occurred which should"
                + " not happen at all times, fix it!");
    }

    /* **********************************
     *        File R/W/Cr/Cp/De         *
     ********************************** */

    /** Clear a file.
     * @param file -- file path */
    static void clearFile(String file) {
        try {
            PrintWriter pw = new PrintWriter(file);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Clear a file with input "File".
     * @param file -- File */
    private static void clearFile(File file) {
        clearFile(file.getPath());
    }

    /** WriteInto with input as "File".
     * @param file -- File
     * @param ifAppend -- if append or overwrite
     * @param strs -- content */
    static void writeInto(File file, boolean ifAppend, String... strs) {
        writeInto(file.getPath(), ifAppend, strs);
    }

    /** Convenience for writing objects into file.
     * @param file -- file path
     * @param ifAppend -- if append or overwrite
     * @param strs -- content */
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

    /** readFrom with input as "File".
     * @param file -- File
     * @return -- content */
    private static String[] readFrom(File file) {
        return readFrom(file.getPath());
    }

    /** Convenience for reading Objects from file.
     * @param file -- file path
     * @return -- content*/
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
        return doListToStrings(lst);
    }

    /** Copy File from one place to another. Do not use in same directory.
     * @param sourceLocation -- source File
     * @param targetLocation -- target File */
    static void copyFiles(File sourceLocation, File targetLocation) {
        if (sourceLocation.isDirectory()) {
            copyDirectory(sourceLocation, targetLocation);
        } else {
            copyPlains(sourceLocation, targetLocation);
        }
    }

    /** Copy Directories form source to target.
     * @param source -- source File
     * @param target -- target File */
    private static void copyDirectory(File source, File target) {
        if (!target.exists()) {
            target.mkdir();
        }

        String[] sourceFileList = source.list();
        if (sourceFileList != null) {
            for (String f : sourceFileList) {
                copyFiles(new File(source, f), new File(target, f));
            }
        }
    }

    /** Copy Plain files form source to target.
     * @param source -- source File
     * @param target -- target File */
    private static void copyPlains(File source, File target) {
        try {
            InputStream instream = new FileInputStream(source);
            OutputStream outstream = new FileOutputStream(target);

            byte[] buf = new byte[PACE];
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

    /** Delete files and directories for doClean.
     * @param file -- File */
    static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File c : subFiles) {
                    deleteFile(c);
                }
            }
        }
        file.delete();
    }

    /* **********************************
     *          Commit-Related          *
     ********************************** */

    /** Check the existence of a commit with id.
     * @param id -- hash of the commit.
     * @return -- check result. */
    private boolean existCommit(String id) {
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
    private boolean hasCommitWithMsg(String message) {
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
    private void addBranchTo(String hash, String branch) {
        writeInto(PATH_COMMITS + hash + "/" + BRANCHES_FOLDER,
                true, branch);
    }

    /** Delete branch from the commit.
     * @param hash -- hash of the commit
     * @param branch -- name of the branch. */
    private void deleteBranchFrom(String hash, String branch) {
        File commit = new File(PATH_COMMITS + hash + "/"
                + BRANCHES_FOLDER);
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
    private ArrayList<String> getCommitsWithMsg(String message) {
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
    private String fullLengthIdOf(String id) {
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
     * @param filename -- file name
     * @param commitHash -- commit hash
     * @return check result. */
    private boolean isTrackedByCommit(String filename, String commitHash) {
        String[] filesInCommit = readFrom(PATH_COMMITS
                + commitHash + "/" + FILES_FOLDER);
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
    private boolean hasBranchName(String branchName) {
        for (String branch : getAllDirectorysFrom(PATH_BRANCHES)) {
            if (branch.equals(branchName)) {
                return true;
            }
        }
        return false;
    }

    /** Delete branch.
     * @param branchName -- branch name to be deleted. */
    private void deleteBranch(String branchName) {
        File branch = new File(PATH_BRANCHES + branchName);
        deleteFile(branch);
    }

    /** Check if a file name is ever tracked.
     * @param fileName -- input
     * @return -- check result. */
    private boolean isEverTracked(String fileName) {
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
    private boolean isTrackedByBranch(String filename, String branchName) {
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
    private String getSplitCommit(String branchName1, String branchName2) {
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

    /** Get name of the current branch.
     * @return -- current branch */
    static String getCurrentBranch() {
        String[] readResult = readFrom(PATH_CURRENTBRANCH);
        if (readResult != null) {
            return readResult[0];
        } else {
            return null;
        }
    }

    /** Convenience for re-writing currentBranch.txt.
     * @param branchName -- new branch name */
    static void rewriteCurrentBranch(String branchName) {
        writeInto(PATH_CURRENTBRANCH, false, branchName);
        _branch = new Branch().restoreBranch(branchName);
    }

    /** Get the current(head) commit for current branch.
     * @return -- current head commit */
    static String currentHeadCommit() {
        return _branch.myHeadCommit();
    }

    /** Delete from Working directory.
     * @param filename -- file name */
    private static void deleteFromWorking(String filename) {
        new File(PATH_WORKING + filename).delete();
    }

    /* **********************************
     *         Static-Utilities         *
     ********************************** */

    /** Get Files in File.
     * @param path -- file path
     * @return -- sub-Files*/
    static File[] getFilesInFile(String path) {
        return new File(path).listFiles();
    }

    /** Get all directories from File[].
     * @param files -- given Files
     * @return -- list of sub directories */
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

    /** Convenience way of get all directories from a certain path.
     * @param path -- given file path
     * @return -- list of sub directories*/
    static ArrayList<String> getAllDirectorysFrom(String path) {
        return getAllDirectorysFrom(getFilesInFile(path));
    }

    /** Method converting ArrayList<String> to String[].
     * @param lst -- input
     * @return -- ouput */
    static String[] doListToStrings(ArrayList<String> lst) {
        return lst.toArray(new String[lst.size()]);
    }

    /** Method converting Set<String> to String[].
     * @param lst -- input
     * @return -- ouput */
    static String[] doSetToStrings(Set<String> lst) {
        return lst.toArray(new String[lst.size()]);
    }

    /** Method converting String[] to ArrayList<String>.
     * @param strs -- input
     * @return -- ouput */
    static ArrayList<String> doStringsToList(String[] strs) {
        return new ArrayList<>(Arrays.asList(strs));
    }

    /** Make system exit with a message.
     * @param msg -- message */
    static void doSystemExit(String msg) {
        System.out.println(msg);
        System.exit(0);
    }

    /** Mapping of command types to methods that process them. */
    private static final HashMap<Command.Type, Consumer<String[]>> COMMANDS =
            new HashMap<>();
    {
        COMMANDS.put(INIT, this::doInit);
        COMMANDS.put(ADD, this::doAdd);
        COMMANDS.put(RM, this::doRm);
        COMMANDS.put(LOG, this::doLog);
        COMMANDS.put(GLOBALLOG, this::doGlobalLog);
        COMMANDS.put(STATUS, this::doStatus);
        COMMANDS.put(BRANCH, this::doBranch);
        COMMANDS.put(RMBRANCH, this::doRmBranch);
        COMMANDS.put(CHECKOUTF, this::doCheckoutF);
        COMMANDS.put(CHECKOUTCF, this::doCheckoutCF);
        COMMANDS.put(CHECKOUTB, this::doCheckoutB);
        COMMANDS.put(RESET, this::doReset);
        COMMANDS.put(MERGE, this::doMerge);
        COMMANDS.put(ADDREMOTE, this::doAddRemote);
        COMMANDS.put(RMREMOTE, this::doRmRemote);
        COMMANDS.put(CLEAN, this::doClean);
        COMMANDS.put(HELP, this::doHelp);
        COMMANDS.put(ERROR, this::doError);
        COMMANDS.put(EOF, this::doEOF);
    }


    /** My Current Branch.
     * @return -- _branch. */
    static Branch myBranch() {
        return _branch;
    }

    /** Set Current Branch.
     * @param branch -- branch to be set. */
    static void setMyBranch(Branch branch) {
        _branch = branch;
    }

    /** Add Commit to Current Branch.
     * @param commitId -- commit to be added. */
    static void addCommitToMyBranch(String commitId) {
        _branch.addCommit(commitId);
    }

    /** Change Head Commit for Current Branch.
     * @param commitId -- head commit to be set. */
    static void changeHeadCommitForMyBranch(String commitId) {
        _branch.changeMyHeadCommitTo(commitId);
    }

    /** My Staged Area.
     * @return -- _staged. */
    static Staged myStaged() {
        return _staged;
    }

    /** Clear Removed Files for My Staged. */
    static void clearRemovedInMyStaged() {
        _staged.clearRemovedFiles();
    }

    /** Delete by Name in My Staged.
     * @param fileName -- file name to be deleted. */
    static void deleteByNameInMyStaged(String fileName) {
        _staged.deleteByName(fileName);
    }

    /** Delete by Hash in My Staged.
     * @param fileHash -- file hash to be deleted. */
    static void deleteByHashInMyStaged(String fileHash) {
        _staged.deleteByHash(fileHash);
    }

    /** My Blob Area.
     * @return -- _blobs. */
    static Blob myBlobs() {
        return _blobs;
    }

    /** Add File to Blobs.
     * @param fileHash -- file hash in Staged to be added. */
    static void addFileToBlobs(String fileHash) {
        _blobs.add(fileHash);
    }

    /** The user input command and operands as a String. */
    private String _input;
    /** The current branch. */
    private static Branch _branch;
    /** The current Staged Area. */
    private static Staged _staged;
    /** The current Blobs Area. */
    private static Blob _blobs;

    /** Pace for the method copyFile. */
    private static final int PACE = 1024;
    /** The File name of the directory that saves Gitlet System files. */
    private static final String GITLET_PATH = ".gitlet";
    /** Default Date Format. */
    static final DateFormat DATE_FORMAT =
            new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
    /** Initialized branch -> *master. */
    static final String DEFAULT_BRANCH = "master";
    /** Default hash for "initial commit". */
    static final String INIT_COMMIT =
            sha1(INIT_MESSAGE, DATE_FORMAT.format(INIT_DATE));

    /** Convenience for directory on Working Area. */
    static final String PATH_WORKING = "./";
    /** Convenience for directory on .gitlet/Commits. */
    static final String PATH_COMMITS = GITLET_PATH + "/" + "Commits/";
    /** Convenience for directory on .gitlet/Blobs/. */
    static final String PATH_BLOBS = GITLET_PATH + "/" + "Blobs/";
    /** Convenience for directory on .gitlet/Staged/. */
    static final String PATH_STAGED = GITLET_PATH + "/" + "Staged/";
    /** Convenience for directory on .gitlet/Branches/. */
    static final String PATH_BRANCHES = GITLET_PATH + "/" + "Branches/";
    /** Convenience for directory on .gitlet/Branches/currentBranch.txt. */
    static final String PATH_CURRENTBRANCH =
            PATH_BRANCHES + "currentBranch.txt";

}
