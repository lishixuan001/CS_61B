import java.io.Reader;
import java.io.IOException;

/** Translating Reader: a stream that is a translation of an
 *  existing reader.
 *  @author Wayne Li
 */
public class TrReader extends Reader {
    private Reader _str;
    private String _from;
    private String _to;
    private char charactor;
    private int checkIndex;
    /** A new TrReader that produces the stream of characters produced
     *  by STR, converting all characters that occur in FROM to the
     *  corresponding characters in TO.  That is, change occurrences of
     *  FROM.charAt(0) to TO.charAt(0), etc., leaving other characters
     *  unchanged.  FROM and TO must have the same length. */
    public TrReader(Reader str, String from, String to) {
        // FILL IN
        this._str = str;
        this._from = from;
        this._to = to;
    }

    public void close() throws IOException {
        this._str.close();
    }

    @Override
    public int read(char[] buffer, int offset, int len) throws IOException {
        int result = this._str.read(buffer, offset, len);
        for (int i = offset; i < offset + len; i += 1) {
            charactor = buffer[i];
            checkIndex = this._from.indexOf(charactor);
            if (checkIndex == -1) {
                buffer[i] = charactor;
            } else {
                buffer[i] = this._to.charAt(checkIndex);
            }
        }
        return Math.min(len, result);
    }


    // FILL IN
    // NOTE: Until you fill in the right methods, the compiler will
    //       reject this file, saying that you must declare TrReader
    //     abstract.  Don't do that; define the right methods instead!
}


