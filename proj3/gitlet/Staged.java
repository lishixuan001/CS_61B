package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static gitlet.Doc._nameFolder;
import static gitlet.GitletOperator.*;

class Staged {

    Staged() {
        _files = getAllDocs();
        _nextCommit = getNextCommitList();
    }

    /** Init the Staged Area in init mode. */
    void init() {
        try {
            new File(PATH_STAGED).mkdir();
            new File(_nextCommitFile).createNewFile();
            new File(_removedHashs).createNewFile();
            new File(_removedNames).createNewFile();
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
        ArrayList<String> hashs = getAllDirectorysFrom(PATH_STAGED);
        if (!hashs.isEmpty()) {
            for (String hash : hashs) {
                String name = readFrom(PATH_STAGED + hash + "/" + _nameFolder)[0];
                docs.add(new Doc(name, hash, PATH_STAGED + hash + _contentFolder));
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
        ArrayList<String> files = getAllDirectorysFrom(PATH_STAGED);
        return files.isEmpty();
    }

    /** Add file folder with name.txt and content in Staged place. */
    void add(Doc doc) {
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
            new File(PATH_STAGED + doc.myHash()).mkdir();
            File name = new File(PATH_STAGED + doc.myHash() + "/" + _nameFolder);
            name.createNewFile();
            writeInto(name, false, doc.myName());

            new File(PATH_STAGED + doc.myHash() + "/" + _contentFolder).mkdir();

            File source = new File(doc.myPath());
            File target = new File(PATH_STAGED + doc.myHash() + _contentFolder + doc.myName());
            copyFiles(source, target);

            Doc newFile = new Doc(doc.myName(), doc.myHash(), target.getPath());
            _files.add(newFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Delete file in Staged by hash. */
    void deleteByHash(String hash) {
        File folder = new File(PATH_STAGED + hash);
        if (folder.exists()) {
            folder.delete();
        }
    }

    /** Clear the _removedFiles. */
    static void clearRemovedFiles() {
        clearFile(_removedHashs);
        clearFile(_removedNames);
    }

    /** Write into _removedNames. */
    static void addToRemovedNames(String filename) {
        writeInto(_removedNames, true, filename);
    }

    /** Write into _removedHashs. */
    static void addToRemovedHashs(String filehash) {
        writeInto(_removedHashs, true, filehash);
    }


    /** See if the nextCommit.txt has the hash. */
    static boolean nextCommitListContains(String hash) {
        ArrayList<String> files = getAllDirectorysFrom(_nextCommitFile);
        for (String myhash : files) {
            if (myhash.equals(hash)) {
                return true;
            }
        }
        return false;
    }

    /** Delete the hash from the nextCommit.txt. */
    static void deleteFromNextCommitList(String hash) {
        ArrayList<String> currentList = getAllDirectorysFrom(_nextCommitFile);
        clearFile(_nextCommitFile);
        for (String currenthash : currentList) {
            if (!currenthash.equals(hash)) {
                writeInto(_nextCommitFile, true, currenthash);
            }
        }
    }

    /** All files inside Staged Area. */
    private static ArrayList<Doc> _files = new ArrayList<>();
    /** Recording the commit-files for next commit. */
    private static ArrayList<String> _nextCommit = new ArrayList<>();
    /** Convenience showing content folder. */
    static final String _contentFolder = "/content/";
    /** Convenience for .gitlet/Staged/nextCommit.txt. */
    static final String _nextCommitFile = PATH_STAGED + "nextCommit.txt";
    /** Removed family : removed hashs and names. */
    static final String _removedHashs = PATH_STAGED + "removedHashs.txt";
    static final String _removedNames = PATH_STAGED + "removedNames.txt";

}
