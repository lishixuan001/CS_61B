package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import static gitlet.Doc.*;
import static gitlet.Staged.*;
import static gitlet.GitletOperator.*;

public class Blob{

    Blob() {
        _files = getAllDocs();
    }

    /** Get all files in Blobs. */
    private ArrayList<String> getAllDocs() {
        ArrayList<String> result = new ArrayList<>();
        File[] files = getFilesInFile(PATH_BLOBS);
        if (files != null) {
            ArrayList<String> hashs = getAllDirectorysFrom(files);
            result.addAll(hashs);
        }
        return result;
    }

    /** Init in init mode. */
    void init() {
        new File(PATH_BLOBS).mkdir();
    }

    /** Get my files' hashs. */
    static ArrayList<String> myFiles() {
        ArrayList<String> result = new ArrayList<>();
        result.addAll(_files);
        return result;
    }

    /** Check if has file by hash. */
    boolean hasFileHash(String fileHash) {
        for (String hash: _files) {
            if (hash.equals(fileHash)) {
                return true;
            }
        }
        return false;
    }

    /** Check and add from Staged Area. */
    void add(Doc doc) {
        try {
            Files.move(new File(PATH_STAGED + doc.myHash()).toPath(),
                    new File(PATH_BLOBS + doc.myHash()).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Get name of a hash. */
    String getNameOf(String hash) {
        if (hasFileHash(hash)) {
            return readFrom(PATH_BLOBS + hash + "/" + _nameFolder)[0];
        } else {
            return null;
        }
    }

    /** Checkout file based on filename to WorkingArea. Assume exist. */
    void checkOutByHash(String hash) {
        File source = new File(PATH_BLOBS + hash + _contentFolder + getNameOf(hash));
        File target = new File(PATH_WORKING + getNameOf(hash));
        if (target.exists()) {
            target.delete();
        }
        copyFiles(source, target);
    }

    /** All files inside Staged Area. */
    private static ArrayList<String> _files = new ArrayList<>();

}
