package gitlet;

import java.io.File;

import static gitlet.Utils.*;

/** Representing a full doc file.
 *  @author Shixuan (Wayne) Li
 */
class Doc {

    /** Create Doc Class by filename and filepath.
     * @param filename -- file name
     * @param filepath -- file path */
    Doc(String filename, String filepath) {
        _myName = filename;
        _myPath = filepath + _myName;
        _myHash = getHash();
    }

    /** Do it if you know the hash.
     * @param filename -- file name
     * @param filehash -- file hash
     * @param filepath -- file path */
    Doc(String filename, String filehash, String filepath) {
        _myName = filename;
        _myPath = filepath + _myName;
        _myHash = filehash;
    }

    /** Get my name.
     * @return -- my name. */
    String myName() {
        return _myName;
    }

    /** Get my hash.
     * @return -- my hash. */
    String myHash() {
        return _myHash;
    }

    /** Get my path.
     * @return -- my path. */
    String myPath() {
        return _myPath;
    }

    /** Get the hash id base on 'this' file content.
     * @return -- generated hash. */
    private String getHash() {
        File file = new File(_myPath);
        byte[] data = readContents(file);
        return sha1(_myName, data);
    }

    /** Set the local parameters. */
    private String _myName, _myPath, _myHash;
    /** Convenience for name.txt folder. */
    static final String NAME_FOLDER = "name.txt";

}
