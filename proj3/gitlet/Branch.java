package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static gitlet.Commit.*;
import static gitlet.GitletOperator.*;

public class Branch {

    /** This should occur only in init mode. */
    Branch() {
        _name = getCurrentBranch();
    }

    /** Create a new branch. */
    Branch(String name) {
        _name = name;
        _commits = _branch.myCommits();
        _myPath = PATH_BRANCHES + _name + "/";
    }

    /** Mostly for restoring. */
    Branch(String name, String[] commits) {
        _name = name;
        _commits = StringsToList(commits);
        _myPath = PATH_BRANCHES + _name + "/";
    }

    /** Restore a branch. */
    static Branch restore(String branchName) {
        String path = PATH_BRANCHES + branchName + "/";
        File file = new File(path);
        if (file.exists()) {
            String[] commits = readFrom(path + _commitsFolder);
            return new Branch(branchName, commits);
        } else {
            return null;
        }
    }

    /** Restore current branch. */
    static Branch restore() {
        return restore(getCurrentBranch());
    }

    /** Create initialize mode branch -> master. */
    void init() {
        try {
            new File(PATH_BRANCHES).mkdir();
            new File(PATH_CURRENTBRANCH).createNewFile();
            rewriteCurrentBranch(DEFAULT_BRANCH);

            _name = DEFAULT_BRANCH;
            _myPath = PATH_BRANCHES + _name + "/";
            createBranch();
            _commits.add(INIT_COMMIT);
            _branch = Branch.restore(DEFAULT_BRANCH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** My Commits. */
   ArrayList<String> myCommits() {
        return _commits;
    }

    /** Create branch. */
    void createBranch() {
        new File(_myPath).mkdir();
        writeInto(_myPath + _commitsFolder, false, ListToStrings(_commits));
    }

    /** Delete branch. */
    static void deleteBranch(String branchName) {
        File branch = new File(PATH_BRANCHES + branchName);
        deleteDirectory(branch);
    }

    /** My hash id name. */
    String myHash() {
        return _name;
    }

    /** Get the id of my latest commit. */
    String myLatestCommit() {
        if (_commits.isEmpty()) {
            return null;
        }
        return _commits.get(_commits.size() - 1);
    }

    /** Add commit to this branch. */
    void addCommit(String id) {
        _commits.add(id);
        writeInto(_myPath + _commitsFolder, true, id);
    }

    /** See if there already exist a branch with the name. */
    static boolean hasBranchName(String branchName) {
        for (String branch : getAllDirectorysFrom(PATH_BRANCHES)) {
            if (branch.equals(branchName)) {
                return true;
            }
        }
        return false;
    }

    /** Check if a file with filename in WorkingArea is tracked by the branch. */
    static boolean isTrackedBy(String filename, String branch) {
        for (String hash : getAllDirectorysFrom(PATH_COMMITS)) {
            for (String file : readFrom(PATH_COMMITS + hash + "/" + _filesFolder)) {
                if (_blobs.getNameOf(file).equals(filename)) {
                    for (String bran : readFrom(PATH_COMMITS + hash + "/" + _branchesFolder)) {
                        if (bran.equals(branch)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    /** Name of this branch. */
    private static String _name;
    /** Path of this branch. */
    private static String _myPath;
    /** Commits through this branch. */
    private ArrayList<String> _commits = new ArrayList<>();
    /** Convenience showing commits.txt. */
    static String _commitsFolder = "commits.txt";
}
