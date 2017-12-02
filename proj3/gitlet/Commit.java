package gitlet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static gitlet.Utils.*;
import static gitlet.GitletOperator.*;

public class Commit {

    /** Only for init!. For convenience creating Commits. */
    Commit() {
        _timeStamp = getDate(new Date());
    }

    /** Auto-search parent form Branches directory.
     * Auto-get file hashes in staged area.
     * Auto-detect current branch. */
    Commit(String message) {
        new Commit(getParents(), message, getFiles(), getCurrentBranch());
    }

    /** Create a new commit.
     * Nothing can be further changed except for "_branches". */
    Commit(String[] parents, String message, String[] files, String currentBranch) {
        _parents = parents;
        _timeStamp = getDate(new Date());
        _message = message;
        _files = files;
        _branches.add(currentBranch);
        _myHash = getHashName();
        _myPath = PATH_COMMITS + _myHash + "/";
    }

    /** Mostly for restore use, since can set all parameters. */
    Commit(String id, String[] parents, String[] timeStamp, String[] message, String[] files, String[] branches) {
        _parents = parents;
        _timeStamp = timeStamp[0];
        _message = message[0];
        _files = files;
        _branches = new HashSet<>(Arrays.asList(branches));
        _myHash = id;
        _myPath = PATH_COMMITS + _myHash + "/";
    }

    /** Restore a Commit by commit id. */
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

    void createCommit() {
        createCommit(false);
    }

    /** Create commit (Record the commit information). */
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

        try {
            for (Doc doc : _staged.files()) {
                Files.move(new File(PATH_STAGED + doc.myHash()).toPath(),
                        new File(PATH_BLOBS + doc.myHash()).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        _branch.addCommit(_myHash);
    }

    /** Check if this commit has parents. */
    boolean hasParents() {
        return _parents != null && _parents.length > 0;
    }

    /** Get my parents. */
    String[] myParents() {
        return _parents;
    }

    /** Get String format date. */
    String myDate() {
        return _timeStamp;
    }

    /** Get String format message. */
    String myMessage() {
        return _message;
    }

    /** Get if this commit is created by merging. */
    boolean isMerged() {
        return _isMerged;
    }

    /** Search if there is a commit in .gitlet/Commits with the message. */
    static boolean hasCommitWithMsg(String message) {
        for (String commitHash : getAllDirectorysFrom(PATH_COMMITS)) {
            Commit commit = Commit.restore(commitHash);
            if (commit.myMessage().equals(message)) {
                return true;
            }
        }
        return false;
    }

    /** Get hashs of the commit with the message. */
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

    /** My hash id name. */
    String myHash() {
        return _myHash;
    }

    /** Get hash name for this commit. */
    private String getHashName() {
        return sha1(_parents.toString(), _timeStamp, _message, _files.toString());
    }

    /** Create String formatted Date. */
    static String getDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    /** Auto-detect the parent for this commit. */
    private String[] getParents() {
        return new String[] {currentLatestCommit()};
    }

    /** Auto-collect file hashes in staged area. */
    private String[] getFiles() {
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
    private static String _parentFolder = "parents.txt",
                   _timeStampFolder = "timeStamp.txt",
                   _messageFolder = "message.txt",
                   _filesFolder = "files.txt",
                   _branchesFolder = "branches.txt";
    /** If this committed is created by merging. */
    private static boolean _isMerged = false;



}
