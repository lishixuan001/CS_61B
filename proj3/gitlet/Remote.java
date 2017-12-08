package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static gitlet.Branch.HEADCOMMIT_FOLDER;
import static gitlet.GitletOperator.*;

/** Connected remote in .gitlet/Remote. Representing Remote.
 *  @author Shixuan (Wayne) Li
 */
public class Remote {

    /** This should occur only in init mode. */
    Remote() {
    }

    /** Create a new remote.
     * @param remoteName -- remote name
     * @param remoteDirectory -- remote directory. */
    Remote(String remoteName, String remoteDirectory) {
        _myName = remoteName;
        _myDirectory = remoteDirectory;
        _branches = getMyBranches();
        _commits = getMyCommits();
        _blobs = getMyBlobs();
        _currentBranch = getMyCurrentBranch();
    }

    /** Initialize the Remote folder and necessary parts. */
    public void init() {
        try {
            new File(PATH_REMOTE).mkdir();
            new File(REMOTE_LIST).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* **********************************
     *          Access-Methods          *
     ********************************** */

    /** Get my name.
     * @return -- name of remote. */
    String myName() {
        return _myName;
    }

    /** Get my directory.
     * @return -- directory of remote. */
    String myDirectory() {
        return _myDirectory;
    }

    /** Get my branchs.
     * @return -- branches in remote. */
    String[] myBranches() {
        return _branches;
    }

    /** Get my commits.
     * @return -- commits in remote. */
    String[] myCommits() {
        return _commits;
    }

    /** Get my head commits.
     * @return -- head commit in remote. */
    String myHeadCommit() {
        return _currentBranch.myHeadCommit();
    }

    /* **********************************
     *        Parameter-Building        *
     ********************************** */

    /** Collect the branch names in the remote.
     * @return -- branches in remote. */
    private String[] getMyBranches() {
        ArrayList<String> branches =
                getAllDirectorysFrom(_myDirectory + PATH_BRANCHES);
        return doListToStrings(branches);
    }

    /** Collect commit hashes in the remote.
     * @return -- commits in remote. */
    private String[] getMyCommits() {
        ArrayList<String> commits = new ArrayList<>();
        commits.addAll(getAllDirectorysFrom(_myDirectory + PATH_COMMITS));
        return doListToStrings(commits);
    }

    /** Collect files in Blobs in the remote.
     * @return -- files in blobs of remote. */
    private String[] getMyBlobs() {
        ArrayList<String> files =
                getAllDirectorysFrom(_myDirectory + PATH_BLOBS);
        return doListToStrings(files);
    }

    /** Collect current branch in the remote.
     * @return -- current branch of remote. */
    private Branch getMyCurrentBranch() {
        String[] currentBranchName =
                readFrom(_myDirectory + PATH_BRANCHES + HEADCOMMIT_FOLDER);
        if (currentBranchName == null) {
            return null;
        }
        return new Branch().restoreRemoteBranch(_myDirectory, currentBranchName[0]);
    }


    /* **********************************
     *              Methods             *
     ********************************** */

    /** Check if the remote has a branch name.
     * @param branchName -- branch name
     * @return -- check result. */
    boolean hasBranch(String branchName) {
        for (String branch : _branches) {
            if (branch.equals(branchName)) {
                return true;
            }
        }
        return false;
    }

    /** Check if has the file in remote's Blobs.
     * @param fileHash -- file hash
     * @return -- check result. */
    boolean hasFile(String fileHash) {
        for (String file : _blobs) {
            if (file.equals(fileHash)) {
                return true;
            }
        }
        return false;
    }

    /** Create a branch in remote from init. Update _branches.
     * @param branchName -- branch name.*/
    void createBranch(String branchName) {
        Branch newBranch = new Branch(_myDirectory, branchName);
        newBranch.createBranch();
    }

    /** Choose the branch in remote.
     *  @param branchName -- branch name. */
    void chooseBranch(String branchName) {
        _currentBranch = new Branch().restoreRemoteBranch(_myDirectory, branchName);
    }

    /** Add Commit. Copying local commit to remote.
     * @param localCommit -- commit to added over. */
    void addCommit(Commit localCommit) {

        File sourceCommit = new File(localCommit.myPath());
        File targetCommit =
                new File(_myDirectory + PATH_COMMITS + localCommit.myHash());
        copyFiles(sourceCommit, targetCommit);

        for (String file : localCommit.myFiles()) {
            if (!hasFile(file)) {
                File sourceFile = new File(PATH_BLOBS + file);
                File targetFile = new File(_myDirectory + PATH_BLOBS + file);
                copyFiles(sourceFile, targetFile);
            }
        }

        _currentBranch.addCommit(localCommit.myHash());
        _currentBranch.changeMyHeadCommitTo(localCommit.myHash());
    }


    /** Name of the remote. */
    private String _myName;
    /** Directory of the remote. */
    private String _myDirectory;
    /** Branch names contained in remote. */
    private String[] _branches;
    /** Files in Blobs contained in remote. */
    private String[] _blobs;
    /** Current branch in remote. */
    private Branch _currentBranch;
    /** Commit hashes contained in the remote. */
    private String[] _commits;

}
