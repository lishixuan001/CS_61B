package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.GitletOperator.*;

public class Remote {

    /** This should occur only in init mode. */
    Remote() {
    }

    /** Create a new remote. */
    private Remote(String remoteName, String remoteDirectory) {
        _myName = remoteName;
        _myDirectory = remoteDirectory;
    }

    /** Restore the current remote conditions. */
    Remote restoreRemote() {
        String[] currentRemoteInfo = getCurrentRemoteInfo();
        if (currentRemoteInfo == null
                || currentRemoteInfo.length <= 0) {
            return new Remote();
        }
        String currentRemoteName = currentRemoteInfo[0];
        String currentRemoteDirectory = currentRemoteInfo[1];
        return new Remote (currentRemoteName, currentRemoteDirectory);
    }

    /** Initialize the Remote folder and necessary parts. */
    public void init() {
        try {
            new File(PATH_REMOTE).mkdir();
            new File(CURRENT_REMOTE).createNewFile();
            new File(REMOTE_LIST).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Get my name. */
    String myName() {
        return _myName;
    }

    /** Get my directory. */
    String myDirectory() {
        return _myDirectory;
    }

    /** Name of the remote. */
    private String _myName;
    /** Directory of the remote. */
    private String _myDirectory;



}
