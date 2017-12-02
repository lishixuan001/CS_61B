package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static gitlet.GitletOperator.*;
import static gitlet.Doc.*;
import static gitlet.Staged.*;

public class Blob{

    Blob() {
        _myAreaPath = PATH_BLOBS;
        _files = getAllDocs();
    }

    /** Get all files in Blobs. */
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

    /** Init in init mode. */
    void init() {
        new File(PATH_BLOBS).mkdir();
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

    /** Check and add from Staged Area. */
    void add(Doc doc) {
        // FIXME

        // Add from Staged to Blobs

        // Add the new doc to _files

    }

    /** All files inside Staged Area. */
    private static ArrayList<Doc> _files = new ArrayList<>();
    /** My Area's general path. */
    private static String _myAreaPath;

}
