package gitlet;

import java.io.File;
import java.util.Set;
import java.util.Date;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ArrayList;

import static gitlet.CommitUtilis.*;
import static gitlet.Doc._nameFolder;
import static gitlet.Utils.*;
import static gitlet.Staged.*;
import static gitlet.GitletOperator.*;

/** Commit Area in .gitlet/Commit. Representing each commit.
 *  @author Shixuan (Wayne) Li
 */
public class Commit {

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
        _isMerged = getIfMerged();
    }

    /** Mostly for restore use, since can set all parameters.
     * @param id -- commit hash
     * @param parents -- parents
     * @param message -- log message
     * @param files -- contained files
     * @param branches -- branches it got pointed to
     * @param timeStamp -- time the commit is created. */
    Commit(String id, String[] parents, String[] timeStamp, String[] message,
           String[] files, String[] branches, boolean isMerged) {
        _parents = parents;
        _timeStamp = timeStamp[0];
        _message = message[0];
        _files = files;
        _branches = new HashSet<>(Arrays.asList(branches));
        _myHash = id;
        _myPath = PATH_COMMITS + _myHash + "/";
        _isMerged = isMerged;
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
    public void createCommit() {
        createCommit(false);
    }

    /** Create commit (Record the commit information).
     * @param InitOrMerge -- if is init.*/
    public void createCommit(boolean InitOrMerge) {

        if (!InitOrMerge) {
            if (_staged.isEmptyForCommit() && _staged.isEmptyRemovedFile()) {
                SystemExit("No changes added to the commit.");
            }

            String[] lastFiles = readFrom(PATH_COMMITS + currentHeadCommit() + "/" + _filesFolder);

            if (lastFiles == null || lastFiles.length <= 0) {
                if (_files == null) {
                    SystemExit("No changes added to the commit.");
                }
            }
            else {
                if (Arrays.equals(_files, lastFiles)) {
                    SystemExit("No changes added to the commit.");
                }
            }
        }

        new File(_myPath).mkdir();
        writeInto(_myPath + _parentFolder, false, _parents);
        writeInto(_myPath + _timeStampFolder, false, _timeStamp);
        writeInto(_myPath + _messageFolder, false, _message);
        writeInto(_myPath + _filesFolder, false, _files);
        writeInto(_myPath + _branchesFolder, false, SetToStrings(_branches));
        writeInto(_myPath + _isMergedFolder, false, String.valueOf(_isMerged));

        for (String file : getAllDirectorysFrom(PATH_STAGED)) {
            _blobs.add(file);
        }

        _staged.clearRemovedFiles();
        _branch.addCommit(_myHash);
        _branch.changeMyHeadCommitTo(_myHash);
    }


    /** Change the commit's attribute to an merged commit. */
    public void tagAsMerged() {
        _isMerged = true;
        writeInto(_myPath + _isMergedFolder, false, String.valueOf(true));
    }

    /** Read file and get if isMerged.
     * @return -- Check result. */
    private boolean getIfMerged() {
        String[] isMerged = readFrom(_myPath + _isMergedFolder);
        if (isMerged == null) {
            return false;
        }
        return Boolean.parseBoolean(isMerged[0]);
    }

    /** Add parent branch to this commit.
     * @param branch -- name of the branch. */
    public void addParent(String branch) {
        ArrayList<String> currentParents = StringsToList(_parents);
        currentParents.add(branch);
        _parents = ListToStrings(currentParents);
        writeInto(_myPath + _parentFolder, true, branch);
    }

    /** Create String formatted Date.
     * @param date -- date.
     * @return -- String formatted date. */
    public String getDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    /** Check if contains file with filename.
     * @param filename -- file name.
     * @return -- check result. */
    public boolean containsFileName(String filename) {
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
    public boolean containsFileHash(String filehash) {
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
    public boolean containsBranch(String branchname) {
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
    public String getHashByName(String filename) {
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
    public String[] myFiles() {
        return _files;
    }

    /** Get my branches.
     * @return my branches. */
    public Set<String> myBranches() {
        return _branches;
    }

    /** Check if this commit has parents.
     * @return -- check result. */
    public boolean hasParents() {
        return _parents != null && _parents.length > 0;
    }

    /** Get my parents.
     * @return -- my parents. */
    public String[] myParents() {
        return _parents;
    }

    /** My hash id name.
     * @return my hash. */
    public String myHash() {
        return _myHash;
    }

    /** Get String format date.
     * @return -- my date. */
    public String myDate() {
        return _timeStamp;
    }

    /** Get String format message.
     * @return -- my message. */
    String myMessage() {
        return _message;
    }

    /** Get if this commit is created by merging.
     * @return -- if is merged. */
    public boolean isMerged() {
        return _isMerged;
    }

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

    /** Auto-collect file hashes in staged area.
     * @return -- applied to get files that need to be commited. */
    private String[] getFilesFromStaged() {
        ArrayList<String> files = new ArrayList<>();
        String[] parentfiles = readFrom(PATH_COMMITS + currentHeadCommit() + "/" + _filesFolder);
        if (parentfiles != null) {
            for (String parentFile : parentfiles) {
                String parentFileName = _blobs.getNameOf(parentFile);
                if (!_staged.existFileNameInRemoved(parentFileName) && !_staged.hasFileName(parentFileName)) {
                    files.add(parentFile);
                }
            }
        }
        for (String stagedFile : getAllDirectorysFrom(PATH_STAGED)) {
            String stagedFileName = _staged.getNameByHash(stagedFile);
            if (!_staged.existFileNameInRemoved(stagedFileName)) {
                files.add(stagedFile);
            }
        }
        if (files.size() <= 0) {
            return null;
        }
        return ListToStrings(files);
    }



    /** Restore a Commit by commit id.
     * @param id -- input
     * @return -- restored commit. */
    public Commit restoreCommit(String id) {
        String path = PATH_COMMITS + id + "/";
        File file = new File(path);
        if (file.exists()) {
            String[] parents = readFrom(path + _parentFolder),
                    timeStamp = readFrom(path + _timeStampFolder),
                    message = readFrom(path + _messageFolder),
                    files = readFrom(path + _filesFolder),
                    branches = readFrom(path + _branchesFolder);
            boolean isMerged = Boolean.parseBoolean(readFrom(path + _isMergedFolder)[0]);
            return new Commit(id, parents, timeStamp, message, files, branches, isMerged);
        } else {
            return null;
        }
    }

    /** Initial Date. */
    static final Date INIT_DATE = new Date(0);
    /** Message for initial commit. */
    static final String INIT_MESSAGE = "initial commit";
    /** Convenience for folders. */
    static final String _parentFolder = "parents.txt",
            _messageFolder = "message.txt",
            _timeStampFolder = "timeStamp.txt",
            _filesFolder = "files.txt",
            _branchesFolder = "branches.txt",
            _isMergedFolder = "isMerged.txt";

}
