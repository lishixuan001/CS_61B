import java.util.Iterator;
import utils.Filter;

/** A kind of Filter that lets through every other VALUE element of
 *  its input sequence, starting with the first.
 *  @author You
 */
class AlternatingFilter<Value> extends Filter<Value> {

    /** A filter of values from INPUT that lets through every other
     *  value. */
    AlternatingFilter(Iterator<Value> input) {
        super(input);
        allow = true;
    }

    @Override
    /** can be made simpler, but this is for maximum pedagogical clarity */
    protected boolean keep() {
        if (allow == true) {
            allow = false;
            return true;
        } else {
            allow = true;
            return false;
        }
    }

    boolean allow;

}
