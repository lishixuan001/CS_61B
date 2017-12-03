package gitlet;

import java.io.File;
import java.util.Set;
import java.util.Date;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ArrayList;

import static gitlet.Utils.*;
import static gitlet.Staged.*;
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
        new Commit(getParents(), message,
                getFilesFromStaged(), getCurrentBranch());
    }

    /** Create a new commit.
     * Nothing can be further changed except for "_branches".
     * @param parents -- parents
     * @param message -- log message
     * @param files -- contained files
     * @param currentBranch -- branch it appends to. */
    Commit(String[] parents, String message, String[] files,
           String currentBranch) {
        _parents = parents;
        _timeStamp = getDate(new Date());
        _message = message;
        _files = files;
        _branches.add(currentBranch);
        _myHash = getHashName();
        _myPath = PATH_COMMITS + _myHash + "/";
        if (_parents.length >= 2) {
            _isMerged = true;
        }
    }

    /** Mostly for restore use, since can set all parameters.
     * @param id -- commit hash
     * @param parents -- parents
     * @param message -- log message
     * @param files -- contained files
     * @param branches -- branches it got pointed to
     * @param timeStamp -- time the commit is created. */
    Commit(String id, String[] parents, String[] timeStamp, String[] message,
           String[] files, String[] branches) {
        _parents = parents;
        _timeStamp = timeStamp[0];
        _message = message[0];
        _files = files;
        _branches = new HashSet<>(Arrays.asList(branches));
        _myHash = id;
        _myPath = PATH_COMMITS + _myHash + "/";
        if (_parents.length >= 2) {
            _isMerged = true;
        }
    }

    /** Restore a Commit by commit id.
     * @param id -- input
     * @return -- restored commit. */
    static Commit restore(String id) {
        String path = PATH_COMMITS + id + "/";
        File file = new File(path);
        if (file.exists()) {
            String[] parents = readFrom(path + _parentFolder),
                    timeStamp = readFrom(path + _timeStampFolder),
                    message = readFrom(path + _messageFolder),
                    files = readFrom(path + _filesFolder),
                    branches = readFrom(path + _branchesFolder);
            return new Commit(id, parents, timeStamp, message, files, branches);

        } else {
            return null;
        }
    }

    /** Initialized commit (the first commit). */
    void init() {
        new File(PATH_COMMITS).mkdir();
        _parents = null;
        _timeStamp = getDate(INIT_DATE);
        _message = INIT_MESSAGE;
        _files = null;
        _branches.add(DEFAULT_BRANCH);
        _myHash = INIT_COMMIT;
        _myPath = PATH_COMMITS + _myHash + "/";
        createCommit(true);
    }

    /** Restore a Commit with 7-digit id. Assume exist.
     * @param id -- 7-digit version commit id.
     * @return -- full length version of the id. */
    static String fullLengthIdOf(String id) {
        String fullId = null;
        for (String hash : getAllDirectorysFrom(PATH_COMMITS)) {
            String partHash = hash.substring(0, 7);
            if (partHash.equals(id)) {
                fullId = hash;
            }
        }
        return fullId;
    }

    /** Create a new commit. */
    void createCommit() {
        createCommit(false);
    }

    /** Create commit (Record the commit information).
     * @param isInit -- if is init.*/
    private void createCommit(boolean isInit) {

        if (_staged.isEmpty() && !isInit) {
            SystemExit("No changes added to the commit.");
        }

        new File(_myPath).mkdir();
        writeInto(_myPath + _parentFolder, false, _parents);
        writeInto(_myPath + _timeStampFolder, false, _timeStamp);
        writeInto(_myPath + _messageFolder, false, _message);
        writeInto(_myPath + _filesFolder, false, _files);
        writeInto(_myPath + _branchesFolder, false, SetToStrings(_branches));

        for (Doc doc : _staged.files()) {
            _blobs.add(doc);
        }

        clearRemovedFiles();
        _branch.addCommit(_myHash);
        _branch.changeMyHeadCommitTo(_myHash);
    }

    /** Add branch to the commit.
     * @param hash -- hash of the commit
     * @param branch -- name of the branch. */
    static void addBranchTo(String hash, String branch) {
        writeInto(PATH_COMMITS + hash + "/" + _branchesFolder, true, branch);
    }

    /** Delete branch from the commit.
     * @param hash -- hash of the commit
     * @param branch -- name of the branch. */
    static void deleteBranchFrom(String hash, String branch) {
        File commit = new File(PATH_COMMITS + hash + "/" + _branchesFolder);
        String[] currentBranchs = readFrom(commit);
        clearFile(commit);
        for (String currentbranch : currentBranchs) {
            if (!currentbranch.equals(branch)) {
                writeInto(commit, true, currentbranch);
            }
        }
    }

    /** Check the existence of a commit with id.
     * @param id -- hash of the commit.
     * @return -- check result. */
    static boolean existCommit(String id) {
        for (String hash : getAllDirectorysFrom(PATH_COMMITS)) {
            if (hash.equals(id)) {
                return true;
            }
        }
        return false;
    }

    /** Change the commit's attribute to an merged commit. */
    void tagAsMerged() {
        _isMerged = true;
    }

    /** Add parent branch to this commit.
     * @param branch -- name of the branch. */
    void addParent(String branch) {
        ArrayList<String> currentParents = StringsToList(_parents);
        currentParents.add(branch);
        _parents = ListToStrings(currentParents);
        writeInto(_myPath + _parentFolder, true, branch);
    }

    /** Search if there is a commit in .gitlet/Commits with the message.
     * @param message -- message to be searched.
     * @return -- check result. */
    static boolean hasCommitWithMsg(String message) {
        for (String commitHash : getAllDirectorysFrom(PATH_COMMITS)) {
            Commit commit = Commit.restore(commitHash);
            if (commit.myMessage().equals(message)) {
                return true;
            }
        }
        return false;
    }

    /** Get hashs of the commit with the message.
     * @param message -- message to be searched.
     * @return -- commits' hashes as a searched result. */
    static ArrayList<String> getCommitsWithMsg(String message) {
        ArrayList<String> result = new ArrayList<>();
        for (String commitHash : getAllDirectorysFrom(PATH_COMMITS)) {
            Commit commit = restore(commitHash);
            if (commit.myMessage().equals(message)) {
                result.add(commitHash);
            }
        }
        return result;
    }

    /** Check if contains file with filename.
     * @param filename -- file name.
     * @return -- check result. */
    boolean containsFileName(String filename) {
        String[] hashs = readFrom(_myPath + _filesFolder);
        if (hashs == null) {
            return false;
        }
        for (String hash : hashs) {
            if (_blobs.hasFileHash(hash)) {
                String name = _blobs.getNameOf(hash);
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
        String[] hashs = readFrom(_myPath + _filesFolder);
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
        String[] branchs = readFrom(_myPath + _branchesFolder);
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

    /** Get the hash of the file in this commit by its name. Assume exist.
     * @param filename -- file name
     * @return -- hash of the file. */
    String getHashByName(String filename) {
        String[] hashs = readFrom(_myPath + _filesFolder);
        for (String hash : hashs) {
            if (_blobs.hasFileHash(hash)) {
                String name = _blobs.getNameOf(hash);
                if (name.equals(filename)) {
                    return hash;
                }
            }
        }
        return null;
    }

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

    /** Get hash name for this commit.
     * @return -- created hash name for this commit. */
    private String getHashName() {
        return sha1(_parents.toString(), _timeStamp, _message,
                _files.toString());
    }

    /** Create String formatted Date.
     * @param date -- date.
     * @return -- String formatted date. */
    static String getDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    /** Auto-detect the parent for this commit.
     * @return -- applied to see the parent of a new commit. */
    private String[] getParents() {
        return new String[] {currentHeadCommit()};
    }

    /** Auto-collect file hashes in staged area.
     * @return -- applied to get files that need to be commited. */
    private String[] getFilesFromStaged() {
        return ListToStrings(_staged.getNextCommitFiles());
    }

    /** Initial Date. */
    static final Date INIT_DATE = new Date(0);
    /** Message for initial commit. */
    static final String INIT_MESSAGE = "initial commit";
    /** Hash name of the commit. */
    private static String _myHash;
    /** Hashes of the parents. */
    private static String[] _parents;
    /** Timestamp of the commit. */
    private static String _timeStamp;
    /** Log message with the commit. */
    private static String _message;
    /** Hashes of the tracked files. */
    private static String[] _files;
    /** Branches that exist at this commit. */
    private static Set<String> _branches = new HashSet<>();
    /** The path of this commit. */
    private static String _myPath;
    /** Convenience for folders. */
    static final String _parentFolder = "parents.txt",
            _messageFolder = "message.txt",
            _timeStampFolder = "timeStamp.txt",
            _filesFolder = "files.txt",
            _branchesFolder = "branches.txt";
    /** If this committed is created by merging. */
    private boolean _isMerged = false;



}
