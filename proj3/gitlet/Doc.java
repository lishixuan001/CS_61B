package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static gitlet.Utils.*;
import static gitlet.GitletOperator.*;

/** Representing a full doc file.
 *  @author Shixuan (Wayne) Li
 */
class Doc {

    /** Create Doc Class by filename and filepath. */
    Doc(String filename, String filepath) {
        _myName = filename;
        _myPath = filepath + _myName;
        _myHash = getHash();
    }

    /** Do it if you know the hash. */
    Doc(String filename, String filehash, String filepath) {
        _myName = filename;
        _myPath = filepath + _myName;
        _myHash = filehash;
    }

    /** Get my name. */
    String myName() {
        return _myName;
    }

    /** Get my hash. */
    String myHash() {
        return _myHash;
    }

    /** Get my path. */
    String myPath() {
        return _myPath;
    }

    /** Get the hash id base on 'this' file content. */
    private String getHash() {
        File file = new File(_myPath);
        byte[] data = readContents(file);
        return sha1(_myName, data);
    }

    /** Set the local parameters. */
    private String _myName, _myPath, _myHash;
    /** Convenience for name.txt folder. */
    static final String _nameFolder = "name.txt";

}
