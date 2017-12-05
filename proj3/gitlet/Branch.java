package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static gitlet.GitletOperator.*;

/** Branch Area in .gitlet/Branch. Representing branch.
 *  @author Shixuan (Wayne) Li
 */
public class Branch {

    /** This should occur only in init mode. */
    Branch() {
        _name = getCurrentBranch();
    }

    /** Create a new branch.
     * @param name -- branch name. */
    Branch(String name) {
        _name = name;
        _commits = _branch.myCommits();
        _myPath = PATH_BRANCHES + _name + "/";
        _headCommit = _branch.myHeadCommit();
    }

    /** Mostly for restoring.
     * @param name -- branch name.
     * @param commits -- commits in the branch. */
    private Branch(String name, String[] commits) {
        _name = name;
        _commits = doStringsToList(commits);
        _myPath = PATH_BRANCHES + _name + "/";
        _headCommit = getMyHeadCommit();
    }

    /** Restore current branch.
     * @return -- restored current branch. */
    Branch restoreBranch() {
        return restoreBranch(getCurrentBranch());
    }

    /** Restore a branch.
     * @param branchName -- branchName to be restored.
     * @return -- restored branch. */
    Branch restoreBranch(String branchName) {
        String path = PATH_BRANCHES + branchName + "/";
        File file = new File(path);
        if (file.exists()) {
            String[] commits = readFrom(path + COMMITS_FOLDER);
            return new Branch(branchName, commits);
        } else {
            return null;
        }
    }

    /** Create initialize mode branch -> master. */
    public void init() {
        try {
            new File(PATH_BRANCHES).mkdir();
            new File(PATH_CURRENTBRANCH).createNewFile();
            rewriteCurrentBranch(DEFAULT_BRANCH);

            _name = DEFAULT_BRANCH;
            _myPath = PATH_BRANCHES + _name + "/";
            createBranch();
            _commits.add(INIT_COMMIT);
            _branch = restoreBranch(DEFAULT_BRANCH);
            _headCommit = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Create branch. */
    void createBranch() {
        new File(_myPath).mkdir();
        writeInto(_myPath + COMMITS_FOLDER, false, doListToStrings(_commits));
        writeInto(_myPath + HEADCOMMIT_FOLDER, false, _headCommit);
    }

    /* **********************************
     *          Access-Methods          *
     ********************************** */

    /** My branch name.
     * @return -- name of this branch. */
    String myName() {
        return _name;
    }

    /** My Commits.
     * @return -- my commits in this branch. */
    ArrayList<String> myCommits() {
        return _commits;
    }

    /** My head commit.
     * @return -- return the hash of the head commit of this branch. */
    String myHeadCommit() {
        return _headCommit;
    }

    /** Get the id of my latest commit.
     * @return -- hash of the latest commit of this branch. */
    String myLatestCommit() {
        if (_commits.isEmpty()) {
            return null;
        }
        return _commits.get(_commits.size() - 1);
    }


    /** Get the head commit for this branch by reading file.
     * @return -- hash of the head commit. */
    private String getMyHeadCommit() {
        String[] head = readFrom(_myPath + HEADCOMMIT_FOLDER);
        if (head != null && head.length > 0) {
            return head[0];
        } else {
            return null;
        }
    }

    /* **********************************
     *        Parameter-Building        *
     ********************************** */

    /** Change my head commit to.
     * @param commit -- hash of commit to be set as head. */
    void changeMyHeadCommitTo(String commit) {
        _headCommit = commit;
        writeInto(_myPath + HEADCOMMIT_FOLDER, false, commit);
    }

    /** Add commit to this branch.
     * @param id -- commit id to be added to this branch. */
    void addCommit(String id) {
        _commits.add(id);
        writeInto(_myPath + COMMITS_FOLDER, true, id);
    }


    /** Name of this branch. */
    private String _name;
    /** Path of this branch. */
    private String _myPath;
    /** Commits through this branch. */
    private ArrayList<String> _commits = new ArrayList<>();
    /** Head commit for this branch. */
    private String _headCommit;

    /** Convenience showing commits.txt. */
    static final String COMMITS_FOLDER = "commits.txt";
    /** Convenience showing headCommit.txt. */
    static final String HEADCOMMIT_FOLDER = "headCommit.txt";

}
