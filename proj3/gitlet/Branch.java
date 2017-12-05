package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static gitlet.Commit.*;
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
    Branch(String name, String[] commits) {
        _name = name;
        _commits = StringsToList(commits);
        _myPath = PATH_BRANCHES + _name + "/";
        _headCommit = getMyHeadCommit();
    }

    /** Restore a branch.
     * @param branchName -- branchName to be restored.
     * @return -- restored branch. */
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

    /** Restore current branch.
     * @return -- restored current branch. */
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
    void changeMyHeadCommitTo(String commit) {
        _headCommit = commit;
        writeInto(_myPath + _headCommitFolder, false, commit);
    }

    /** My Commits.
     * @return -- my commits in this branch. */
    ArrayList<String> myCommits() {
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
    String myHeadCommit() {
        return _headCommit;
    }

    /** Delete branch.
     * @param branchName -- branch name to be deleted. */
    static void deleteBranch(String branchName) {
        File branch = new File(PATH_BRANCHES + branchName);
        deleteDirectory(branch);
    }

    /** My branch name.
     * @return -- name of this branch. */
    String myName() {
        return _name;
    }

    /** Get the id of my latest commit.
     * @return -- hash of the latest commit of this branch. */
    String myLatestCommit() {
        if (_commits.isEmpty()) {
            return null;
        }
        return _commits.get(_commits.size() - 1);
    }

    /** Add commit to this branch.
     * @param id -- commit id to be added to this branch. */
    void addCommit(String id) {
        _commits.add(id);
        writeInto(_myPath + _commitsFolder, true, id);
    }

    /** See if there already exist a branch with the name.
     * @param branchName -- input.
     * @return -- check result. */
    static boolean hasBranchName(String branchName) {
        for (String branch : getAllDirectorysFrom(PATH_BRANCHES)) {
            if (branch.equals(branchName)) {
                return true;
            }
        }
        return false;
    }

    /** Check if a file with filename in WorkingArea is tracked by the branch.
     * @param filename -- file name as input.
     * @param branchName -- branch name as input.
     * @return -- check result. */
    static boolean isTrackedByBranch(String filename, String branchName) {
        Branch branch = Branch.restore(branchName);
        if (branch == null) {
            return false;
        }
        ArrayList<String> commits = branch.myCommits();
        if (commits == null) {
            return false;
        }
        for (String commitHash : commits) {
            Commit commit = Commit.restore(commitHash);
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
    static String getSplitCommit(String branchName1, String branchName2) {
        Branch branch1 = Branch.restore(branchName1);
        String commitHash1 = branch1.myLatestCommit();

        // FIXME -- DELETE
//        System.out.println(commit1.myBranches());
//        System.out.println(commit1.containsBranch(branchName2));

        while (true) {
            Commit commit1 = Commit.restore(commitHash1);
            if (commit1.containsBranch(branchName2) && !commit1.isMerged()) {

                System.out.println(commit1.myHash());
                System.out.println(commit1.myBranches());
                System.out.println(commit1.containsBranch(branchName2));

                return commit1.myHash();
            }
            if (commit1.hasParents()) {
//                commit1 = Commit.restore(commit1.myParents()[0]);
                commitHash1 = commit1.myParents()[0];
            } else {
                break;
            }
        }
        return null;
    }

    /** Name of this branch. */
    private static String _name;
    /** Path of this branch. */
    private static String _myPath;
    /** Commits through this branch. */
    private ArrayList<String> _commits = new ArrayList<>();
    /** Head commit for this branch. */
    private String _headCommit;
    /** Convenience showing commits.txt. */
    static final String _commitsFolder = "commits.txt";
    /** Convenience showing headCommit.txt. */
    static final String _headCommitFolder = "headCommit.txt";
}
