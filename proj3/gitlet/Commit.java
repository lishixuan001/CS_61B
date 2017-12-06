package gitlet;

import java.io.File;
import java.util.Set;
import java.util.Date;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ArrayList;

import static gitlet.Utils.*;
import static gitlet.GitletOperator.*;

/** Commit Area in .gitlet/Commit. Representing each commit.
 *  @author Shixuan (Wayne) Li
 */
public class Commit {

    /** Only for init!. For convenience creating Commits. */
    Commit() {
        _timeStamp = getDate(new Date());
    }

    /** Auto-search parent form Branches directory.
     * Auto-get file hashes in staged area.
     * Auto-detect current branch.
     * @param message -- log message. */
    Commit(String message) {
        _parents = getParents();
        _timeStamp = getDate(new Date());
        _message = message;
        _files = getFilesFromStaged();
        _branches.add(getCurrentBranch());
        _myHash = getHashName();
        _myPath = PATH_COMMITS + _myHash + "/";
        _isMerged = getIfMerged();
    }

    /** Mostly for restore use, since can set all parameters.
     * @param id -- commit hash
     * @param parents -- parents
     * @param message -- log message
     * @param files -- contained files
     * @param branches -- branches it got pointed to
     * @param timeStamp -- time the commit is created.
     * @param isMerged -- if is a merged commit */
    private Commit(String id, String[] parents, String[] timeStamp,
                   String[] message, String[] files,
                   String[] branches, boolean isMerged) {
        _parents = parents;
        _timeStamp = timeStamp[0];
        _message = message[0];
        _files = files;
        _branches = new HashSet<>(Arrays.asList(branches));
        _myHash = id;
        _myPath = PATH_COMMITS + _myHash + "/";
        _isMerged = isMerged;
    }

    /** Restore a Commit by commit id.
     * @param id -- input
     * @return -- restored commit. */
    Commit restoreCommit(String id) {
        String path = PATH_COMMITS + id + "/";
        File file = new File(path);
        if (file.exists()) {
            String[] parents = readFrom(path + PARENT_FOLDER),
                    timeStamp = readFrom(path + TIMESTAMP_FOLDER),
                    message = readFrom(path + MESSAGE_FOLDER),
                    files = readFrom(path + FILES_FOLDER),
                    branches = readFrom(path + BRANCHES_FOLDER);
            String[] isMergedString = readFrom(path + ISMERGED_FOLDER);
            if (isMergedString == null) {
                return null;
            }
            boolean isMerged = Boolean.parseBoolean(isMergedString[0]);
            return new Commit(id, parents, timeStamp,
                    message, files, branches, isMerged);
        } else {
            return null;
        }
    }

    /** Initialized commit (the first commit). */
    public void init() {
        new File(PATH_COMMITS).mkdir();
        _parents = null;
        _timeStamp = getDate(INIT_DATE);
        _message = INIT_MESSAGE;
        _files = null;
        _branches.add(DEFAULT_BRANCH);
        _myHash = INIT_COMMIT;
        _myPath = PATH_COMMITS + _myHash + "/";
        _isMerged = false;
        createCommit(true);
    }

    /** Create a new commit. */
    void createCommit() {
        createCommit(false);
    }

    /** Create commit (Record the commit information).
     * @param initOrMerge -- if is init.*/
    void createCommit(boolean initOrMerge) {

        if (!initOrMerge) {
            if (myStaged().isEmptyForCommit()
                    && myStaged().isEmptyRemovedFile()) {
                doSystemExit("No changes added to the commit.");
            }

            String[] lastFiles = readFrom(PATH_COMMITS
                    + currentHeadCommit() + "/" + FILES_FOLDER);

            if (lastFiles == null || lastFiles.length <= 0) {
                if (_files == null) {
                    doSystemExit("No changes added to the commit.");
                }
            } else {
                if (Arrays.equals(_files, lastFiles)) {
                    doSystemExit("No changes added to the commit.");
                }
            }
        }

        new File(_myPath).mkdir();
        writeInto(_myPath + PARENT_FOLDER, false, _parents);
        writeInto(_myPath + TIMESTAMP_FOLDER, false, _timeStamp);
        writeInto(_myPath + MESSAGE_FOLDER, false, _message);
        writeInto(_myPath + FILES_FOLDER, false, _files);
        writeInto(_myPath + BRANCHES_FOLDER, false, doSetToStrings(_branches));
        writeInto(_myPath + ISMERGED_FOLDER, false, String.valueOf(_isMerged));

        for (String fileHash : getAllDirectorysFrom(PATH_STAGED)) {
            addFileToBlobs(fileHash);
        }

        clearRemovedInMyStaged();
        addCommitToMyBranch(_myHash);
        changeHeadCommitForMyBranch(_myHash);
    }

    /* **********************************
     *          Access-Methods          *
     ********************************** */

    /** Get my files.
     * @return -- my files. */
    String[] myFiles() {
        return _files;
    }

    /** Get my branches.
     * @return my branches. */
    Set<String> myBranches() {
        return _branches;
    }

    /** Check if this commit has parents.
     * @return -- check result. */
    boolean hasParents() {
        return _parents != null && _parents.length > 0;
    }

    /** Get my parents.
     * @return -- my parents. */
    String[] myParents() {
        return _parents;
    }

    /** My hash id name.
     * @return my hash. */
    String myHash() {
        return _myHash;
    }

    /** Get String format date.
     * @return -- my date. */
    String myDate() {
        return _timeStamp;
    }

    /** Get String format message.
     * @return -- my message. */
    String myMessage() {
        return _message;
    }

    /** Get if this commit is created by merging.
     * @return -- if is merged. */
    boolean isMerged() {
        return _isMerged;
    }

    /* **********************************
     *        Parameter-Building        *
     ********************************** */

    /** Get hash name for this commit.
     * @return -- created hash name for this commit. */
    private String getHashName() {
        if (_files == null) {
            return sha1(_parents.toString(), _timeStamp, _message);
        } else {
            return sha1(_parents.toString(), _timeStamp, _message,
                    _files.toString());
        }
    }

    /** Auto-detect the parent for this commit.
     * @return -- applied to see the parent of a new commit. */
    private String[] getParents() {
        return new String[] {currentHeadCommit()};
    }

    /** Create String formatted Date.
     * @param date -- date.
     * @return -- String formatted date. */
    private String getDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    /** Read file and get if isMerged.
     * @return -- Check result. */
    private boolean getIfMerged() {
        String[] isMerged = readFrom(_myPath + ISMERGED_FOLDER);
        return isMerged != null && Boolean.parseBoolean(isMerged[0]);
    }

    /** Auto-collect file hashes in staged area.
     * @return -- applied to get files that need to be commited. */
    private String[] getFilesFromStaged() {
        ArrayList<String> files = new ArrayList<>();
        String[] parentfiles = readFrom(PATH_COMMITS
                + currentHeadCommit() + "/" + FILES_FOLDER);
        if (parentfiles != null) {
            for (String parentFile : parentfiles) {
                String parentFileName = myBlobs().getNameOf(parentFile);
                if (!myStaged().existFileNameInRemoved(parentFileName)
                        && !myStaged().hasFileName(parentFileName)) {
                    files.add(parentFile);
                }
            }
        }
        for (String stagedFile : getAllDirectorysFrom(PATH_STAGED)) {
            String stagedFileName = myStaged().getNameByHash(stagedFile);
            if (!myStaged().existFileNameInRemoved(stagedFileName)) {
                files.add(stagedFile);
            }
        }
        if (files.size() <= 0) {
            return null;
        }
        return doListToStrings(files);
    }

    /** Get the hash of the file in this commit by its name. Assume exist.
     * @param filename -- file name
     * @return -- hash of the file. */
    String getHashByName(String filename) {
        String[] hashs = readFrom(_myPath + FILES_FOLDER);
        for (String hash : hashs) {
            if (myBlobs().hasFileHash(hash)) {
                String name = myBlobs().getNameOf(hash);
                if (name.equals(filename)) {
                    return hash;
                }
            }
        }
        return null;
    }

    /** Change the commit's attribute to an merged commit. */
    void tagAsMerged() {
        _isMerged = true;
        writeInto(_myPath + ISMERGED_FOLDER, false, String.valueOf(true));
    }

    /** Add parent branch to this commit.
     * @param branch -- name of the branch. */
    void addParent(String branch) {
        ArrayList<String> currentParents = doStringsToList(_parents);
        currentParents.add(branch);
        _parents = doListToStrings(currentParents);
        writeInto(_myPath + PARENT_FOLDER, true, branch);
    }

    /* **********************************
     *              Methods             *
     ********************************** */

    /** Check if contains file with filename.
     * @param filename -- file name.
     * @return -- check result. */
    boolean containsFileName(String filename) {
        String[] hashs = readFrom(_myPath + FILES_FOLDER);
        if (hashs == null) {
            return false;
        }
        for (String hash : hashs) {
            if (myBlobs().hasFileHash(hash)) {
                String name = myBlobs().getNameOf(hash);
                if (name.equals(filename)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Check if contains file with filehash.
     * @param filehash -- file hash.
     * @return -- check result. */
    boolean containsFileHash(String filehash) {
        String[] hashs = readFrom(_myPath + FILES_FOLDER);
        if (hashs == null) {
            return false;
        }
        for (String hash : hashs) {
            if (hash.equals(filehash)) {
                return true;
            }
        }
        return false;
    }

    /** Check if contains branch with branchname.
     * @param branchname -- branch name
     * @return -- check result. */
    boolean containsBranch(String branchname) {
        String[] branchs = readFrom(_myPath + BRANCHES_FOLDER);
        if (branchs == null) {
            return false;
        }
        for (String branch : branchs) {
            if (branch.equals(branchname)) {
                return true;
            }
        }
        return false;
    }

    /** Hash name of the commit. */
    private String _myHash;
    /** Hashes of the parents. */
    private String[] _parents;
    /** Timestamp of the commit. */
    private String _timeStamp;
    /** Log message with the commit. */
    private String _message;
    /** Hashes of the tracked files. */
    private String[] _files;
    /** Branches that exist at this commit. */
    private Set<String> _branches = new HashSet<>();
    /** The path of this commit. */
    private String _myPath;
    /** If this committed is created by merging. */
    private boolean _isMerged = false;

    /** Initial Date. */
    static final Date INIT_DATE = new Date(0);
    /** Message for initial commit. */
    static final String INIT_MESSAGE = "initial commit";
    /** Convenience for folders. */
    static final String PARENT_FOLDER = "parents.txt",
            MESSAGE_FOLDER = "message.txt",
            TIMESTAMP_FOLDER = "timeStamp.txt",
            FILES_FOLDER = "files.txt",
            BRANCHES_FOLDER = "branches.txt",
            ISMERGED_FOLDER = "isMerged.txt";

}
