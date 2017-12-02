package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gitlet.GitletOperator.*;

public class Branch {

    /** This should occur only in init mode. */
    Branch() {
        new Branch(getCurrentBranch());
    }

    /** Auto check current head Commit for current branch. */
    Branch(String name) {
        new Branch(name, currentLatestCommit());
    }

    /** Create a branch. */
    Branch(String name, String headCommit) {
        _name = name;
        if (headCommit != null) {
            _commits.add(headCommit);
        }
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
            _commits.add(INIT_COMMIT);
            _myPath = PATH_BRANCHES + _name + "/";
            createBranch();
            _branch = Branch.restore(DEFAULT_BRANCH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /** Create branch. */
    void createBranch() {
        new File(_myPath).mkdir();
        writeInto(_myPath + _commitsFolder, false, ListToStrings(_commits));
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


    /** Name of this branch. */
    private static String _name;
    /** Path of this branch. */
    private static String _myPath;
    /** Commits through this branch. */
    private static ArrayList<String> _commits = new ArrayList<>();
    /** Convenience showing commits.txt. */
    private static String _commitsFolder = "commits.txt";
}
