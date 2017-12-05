package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static gitlet.Doc.NAME_FOLDER;
import static gitlet.GitletOperator.*;

/** Staged Area in .gitlet/Staged.
 *  @author Shixuan (Wayne) Li
 */
class Staged {

    /** Get the staged area ready. */
    Staged() {
        _files = getAllDocs();
    }

    /** Init the Staged Area in init mode. */
    void init() {
        try {
            new File(PATH_STAGED).mkdir();
            new File(REMOVED_NAMES).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Add file folder with name.txt and content in Staged place. Assume exist.
     * @param doc -- doc to be added. */
    void add(Doc doc) {
        String hash = doc.myHash();
        String name = doc.myName();

        if (_blobs.hasFileHash(hash)) {
            if (_staged.hasFileName(name)) {
                _staged.deleteByName(name);
            }
        } else {
            if (_staged.hasFileName(name)) {
                _staged.deleteByName(name);
            }
            copyOverFromWorking(doc);
            _files.add(new Doc(name, hash,
                    PATH_STAGED + hash + CONTENT_FOLDER));
        }

        if (existFileNameInRemoved(name)) {
            deleteFromRemovedNames(name);
        }
    }

    /** Copy over file from Working place.
     * @param doc -- doc to be copied over. */
    private void copyOverFromWorking(Doc doc) {
        try {
            new File(PATH_STAGED + doc.myHash()).mkdir();
            File name = new File(PATH_STAGED
                    + doc.myHash() + "/" + NAME_FOLDER);
            name.createNewFile();
            writeInto(name, false, doc.myName());

            new File(PATH_STAGED + doc.myHash()
                    + "/" + CONTENT_FOLDER).mkdir();

            File source = new File(doc.myPath());
            File target = new File(PATH_STAGED
                    + doc.myHash() + CONTENT_FOLDER + doc.myName());
            copyFiles(source, target);

            Doc newFile = new Doc(doc.myName(), doc.myHash(), target.getPath());
            _files.add(newFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Show if Staged is empty without those in removed(marked) files.
     * @return -- check result */
    boolean isEmptyForCommit() {
        if (_staged.isEmpty()) {
            return true;
        }
        for (Doc doc : _staged.files()) {
            if (!existFileNameInRemoved(doc.myName())) {
                return false;
            }
        }
        return true;
    }

    /* **********************************
     *          Access-Methods          *
     ********************************** */

    /** Get files in Staged.
     * @return -- return files in Staged. */
    ArrayList<Doc> files() {
        return _files;
    }

    /* **********************************
     *        Parameter-Building        *
     ********************************** */

    /** Get all files in Staged. (Get names(hash) of all directories).
     * @return -- gather all files in Staged. */
    private ArrayList<Doc> getAllDocs() {
        ArrayList<Doc> docs = new ArrayList<>();
        ArrayList<String> hashs = getAllDirectorysFrom(PATH_STAGED);
        if (!hashs.isEmpty()) {
            for (String hash : hashs) {
                String name =
                        readFrom(PATH_STAGED + hash + "/" + NAME_FOLDER)[0];
                docs.add(new Doc(name, hash,
                        PATH_STAGED + hash + CONTENT_FOLDER));
            }
        }
        return docs;
    }

    /* **********************************
     *              Methods             *
     ********************************** */

    /** Show if Staged has new files.
     * @return -- if is empty. */
    boolean isEmpty() {
        ArrayList<String> files = getAllDirectorysFrom(PATH_STAGED);
        return files.isEmpty();
    }

    /** Get file name by hash. Assume exist.
     * @param hash -- input
     * @return -- name of file. */
    String getNameByHash(String hash) {
        String[] name = readFrom(PATH_STAGED + hash + "/" + NAME_FOLDER);
        if (name == null) {
            return null;
        }
        return name[0];
    }

    /** Check if has a filename in staged.
     * @param filename -- input
     * @return -- check result. */
    boolean hasFileName(String filename) {
        ArrayList<String> files = getAllDirectorysFrom(PATH_STAGED);
        if (files.size() <= 0) {
            return false;
        }
        for (String hash : files) {
            String[] name = readFrom(PATH_STAGED + hash + "/" + NAME_FOLDER);
            if (name == null) {
                return false;
            }
            if (name[0].equals(filename)) {
                return true;
            }
        }
        return false;
    }

    /** Check if has file by hash.
     * @param fileHash -- file hash.
     * @return -- check result. */
    boolean hasFileHash(String fileHash) {
        for (String hash : getAllDirectorysFrom(PATH_STAGED)) {
            if (hash.equals(fileHash)) {
                return true;
            }
        }
        return false;
    }

    /** Delete file in Staged by hash.
     * @param hash -- hash of file to be deleted. */
    private void deleteByHash(String hash) {
        File folder = new File(PATH_STAGED + hash);
        if (folder.exists()) {
            deleteFile(folder);
            _files.remove(hash);
        }
    }

    /** Delete file in Staged by name. Assume exist.
     * @param filename -- name of file to be deleted. */
    void deleteByName(String filename) {
        ArrayList<String> files = getAllDirectorysFrom(PATH_STAGED);
        for (String hash : files) {
            String[] name = readFrom(PATH_STAGED + hash + "/" + NAME_FOLDER);
            if (name == null) {
                return;
            }
            if (name[0].equals(filename)) {
                _staged.deleteByHash(hash);
                _files.remove(hash);
                return;
            }
        }
    }

    /* **********************************
     *          About-Removed           *
     ********************************** */

    /** Check if removed is empty.
     * @return -- check result. */
    boolean isEmptyRemovedFile() {
        String[] existedNames = readFrom(REMOVED_NAMES);
        if (existedNames == null) {
            return true;
        }
        return false;
    }

    /** Clear the _removedFiles. */
    void clearRemovedFiles() {
        clearFile(REMOVED_NAMES);
    }

    /** Write into REMOVED_NAMES.
     * @param filename -- filename of the removed.*/
    void addToRemovedNames(String filename) {
        writeInto(REMOVED_NAMES, true, filename);
    }

    /** Delete name from removed names. Assume exist.
     * @param filename -- filename to be deleted from the removed. */
    void deleteFromRemovedNames(String filename) {
        String[] existedNames = readFrom(REMOVED_NAMES);
        if (existedNames == null) {
            return;
        }
        clearRemovedFiles();
        for (String name : existedNames) {
            if (!name.equals(filename)) {
                writeInto(REMOVED_NAMES, true, name);
            }
        }
    }

    /** Check if a file name exist in RemovedNames.
     * @param filename -- input.
     * @return -- check result. */
    boolean existFileNameInRemoved(String filename) {
        String[] existedNames = readFrom(REMOVED_NAMES);
        if (existedNames == null) {
            return false;
        }
        for (String name : existedNames) {
            if (name.equals(filename)) {
                return true;
            }
        }
        return false;
    }

    /** All files inside Staged Area. */
    private ArrayList<Doc> _files = new ArrayList<>();
    /** Convenience showing content folder. */
    static final String CONTENT_FOLDER = "/content/";
    /** Removed names. */
    static final String REMOVED_NAMES = PATH_STAGED + "removedNames.txt";

}
