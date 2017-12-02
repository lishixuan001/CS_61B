package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static gitlet.Doc._nameFolder;
import static gitlet.GitletOperator.*;
import static gitlet.GitletOperator.PATH_STAGED;

class Area {

    Area(String AreaPath) {
        _myAreaPath = AreaPath;
    }

    /** Get all files in Blobs. */
    List<Doc> getAllDocs() {
        List<Doc> docs = new ArrayList<>();
        File[] files = getFilesInFile(_myAreaPath);
        List<String> hashs = getAllDirectorysFrom(files);
        for (String hash : hashs) {
            String name = readFrom(_myAreaPath + hash + "/" + _nameFolder)[0];
            docs.add(new Doc(name, hash, _myAreaPath + hash + "/"));
        }
        return docs;
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

    /** Add file foler with name.txt and content in AreaPath place. */
    void addFile(Doc doc) {
        // FIXME

        // get hash and create folder

        // create name.txt and write down name

        // copy and paste the file????
    }

    /** All files inside Staged Area. */
    private List<Doc> _files = new ArrayList<>();
    /** My Area's general path. */
    private String _myAreaPath;
}
