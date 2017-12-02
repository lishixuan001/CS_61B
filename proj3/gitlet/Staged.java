package gitlet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static gitlet.Doc._nameFolder;
import static gitlet.GitletOperator.*;
import static gitlet.GitletOperator.PATH_BLOBS;

class Staged {

    Staged() {
        _myAreaPath = PATH_STAGED;
        _files = getAllDocs();
        _nextCommit = getNextCommitList();
    }

    /** Init the Staged Area in init mode. */
    void init() {
        try {
            new File(PATH_STAGED).mkdir();
            new File(_nextCommitFile).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Get files in Staged. */
    ArrayList<Doc> files() {
        return _files;
    }

    /** Get all files in Staged. (Get names(hash) of all directories). */
    private ArrayList<Doc> getAllDocs() {
        ArrayList<Doc> docs = new ArrayList<>();
        File[] files = getFilesInFile(_myAreaPath);
        if (files != null) {
            ArrayList<String> hashs = getAllDirectorysFrom(files);
            for (String hash : hashs) {
                String name = readFrom(_myAreaPath + hash + "/" + _nameFolder)[0];
                docs.add(new Doc(name, hash, _myAreaPath + hash + _contentFolder));
            }
        }
        return docs;
    }

    /** Read file of .gitlet/Staged/nextCommit.txt. */
    private ArrayList<String> getNextCommitList() {
        ArrayList<String> result = new ArrayList<>();
        File file = new File(_nextCommitFile);
        if (file.exists()) {
            String[] fileList = readFrom(file);
            result = StringsToList(fileList);
        }
        return result;
    }

    /** Get the next commit's file list. */
    ArrayList<String> getNextCommitFiles() {
        return _nextCommit;
    }

    /** Check if has file by hash. */
    boolean hasFileHash(String fileHash) {
        for (Doc file : _files) {
            if (file.myHash().equals(fileHash)) {
                return true;
            }
        }
        return false;
    }

    /** Show if Staged has new files. */
    boolean isEmpty() {
        ArrayList<String> files = getAllDirectorysFrom(_myAreaPath);
        return files.isEmpty();
    }

    /** Add file folder with name.txt and content in Staged place. */
    void add(Doc doc) {
        // see if blob has the hash
        if (_blobs.hasFileHash(doc.myHash())) {
            if (_staged.hasFileHash(doc.myHash())) {
                _staged.deleteByHash(doc.myHash());
            }
            if (!_nextCommit.contains(doc.myHash())) {
                addToNextCommit(doc.myHash());
            }
        } else {
            addToNextCommit(doc.myHash());
            copyOverFromWorking(doc);
        }
    }

    /** Add to the _nextCommit list, and rewrite the file to record. */
    private void addToNextCommit(String hash) {
        _nextCommit.add(hash);
        rewriteNextCommitList();
    }

    /** This follows when _nextCommit changes. */
    private void rewriteNextCommitList() {
        writeInto(_nextCommitFile, false, ListToStrings(_nextCommit));
    }

    /** Copy over file from Working place. */
    private void copyOverFromWorking(Doc doc) {
        try {
            new File(_myAreaPath + doc.myHash()).mkdir();
            File name = new File(_myAreaPath + doc.myHash() + "/" + _nameFolder);
            name.createNewFile();
            writeInto(name, false, doc.myName());

            new File(_myAreaPath + doc.myHash() + "/" + _contentFolder).mkdir();

            File source = new File(doc.myPath());
            File target = new File(_myAreaPath + doc.myHash() + _contentFolder + doc.myName());
            copyFiles(source, target);

            Doc newFile = new Doc(doc.myName(), doc.myHash(), target.getPath());
            _files.add(newFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Delete file in Staged by hash. */
    void deleteByHash(String hash) {
        File folder = new File(_myAreaPath + hash);
        if (folder.exists()) {
            folder.delete();
        }
    }

    /** All files inside Staged Area. */
    private static ArrayList<Doc> _files = new ArrayList<>();
    /** My Area's general path. */
    private static String _myAreaPath;
    /** Recording the commit-files for next commit. */
    private static ArrayList<String> _nextCommit = new ArrayList<>();
    /** Convenience showing content folder. */
    static final String _contentFolder = "/content/";
    /** Convenience for .gitlet/Staged/nextCommit.txt. */
    static final String _nextCommitFile = PATH_STAGED + "nextCommit.txt";

}
