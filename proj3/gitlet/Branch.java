package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import gitlet.Commit;
import static gitlet.GitletOperator.*;

/** Branch Area in .gitlet/Branch. Representing branch.
 *  @author Shixuan (Wayne) Li
 */
public class Branch {

    /** Name of this branch. */
    private String _name;
    /** Path of this branch. */
    private String _myPath;
    /** Commits through this branch. */
    private ArrayList<String> _commits = new ArrayList<>();
    /** Head commit for this branch. */
    private String _headCommit;

    /** This should occur only in init mode. */
    public Branch() {
        _name = getCurrentBranch();
    }

    /** Create a new branch.
     * @param name -- branch name. */
    public Branch(String name) {
        _name = name;
        _commits = _branch.myCommits();
        _myPath = PATH_BRANCHES + _name + "/";
        _headCommit = _branch.myHeadCommit();
    }

    /** Mostly for restoring.
     * @param name -- branch name.
     * @param commits -- commits in the branch. */
    public Branch(String name, String[] commits) {
        _name = name;
        _commits = StringsToList(commits);
        _myPath = PATH_BRANCHES + _name + "/";
        _headCommit = getMyHeadCommit();
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

    /** Get the head commit for this branch by reading file.
     * @return -- hash of the head commit. */
    private String getMyHeadCommit() {
        String[] head = readFrom(_myPath + _headCommitFolder);
        if (head != null && head.length > 0) {
            return head[0];
        } else {
            return null;
        }
    }

    /** Change my head commit to.
     * @param commit -- hash of commit to be set as head. */
    public void changeMyHeadCommitTo(String commit) {
        _headCommit = commit;
        writeInto(_myPath + _headCommitFolder, false, commit);
    }

    /** My Commits.
     * @return -- my commits in this branch. */
    public ArrayList<String> myCommits() {
        return _commits;
    }

    /** Create branch. */
    void createBranch() {
        new File(_myPath).mkdir();
        writeInto(_myPath + _commitsFolder, false, ListToStrings(_commits));
        writeInto(_myPath + _headCommitFolder, false, _headCommit);
    }

    /** My head commit.
     * @return -- return the hash of the head commit of this branch. */
    public String myHeadCommit() {
        return _headCommit;
    }



    /** My branch name.
     * @return -- name of this branch. */
    public String myName() {
        return _name;
    }

    /** Get the id of my latest commit.
     * @return -- hash of the latest commit of this branch. */
    public String myLatestCommit() {
        if (_commits.isEmpty()) {
            return null;
        }
        return _commits.get(_commits.size() - 1);
    }

    /** Add commit to this branch.
     * @param id -- commit id to be added to this branch. */
    public void addCommit(String id) {
        _commits.add(id);
        writeInto(_myPath + _commitsFolder, true, id);
    }

    /** Restore a branch.
     * @param branchName -- branchName to be restored.
     * @return -- restored branch. */
    public Branch restoreBranch(String branchName) {
        String path = PATH_BRANCHES + branchName + "/";
        File file = new File(path);
        if (file.exists()) {
            String[] commits = readFrom(path + _commitsFolder);
            return new Branch(branchName, commits);
        } else {
            return null;
        }
    }

    /** Restore current branch.
     * @return -- restored current branch. */
    public Branch restoreBranch() {
        return restoreBranch(getCurrentBranch());
    }

    /** Convenience showing commits.txt. */
    static final String _commitsFolder = "commits.txt";
    /** Convenience showing headCommit.txt. */
    static final String _headCommitFolder = "headCommit.txt";

}
