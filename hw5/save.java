// REPLACE THIS STUB WITH THE CORRECT SOLUTION.
// The current contents of this file are merely to allow things to compile
// out of the box. It bears scant relation to a proper solution (for one thing,
// a hash table should not be a SortedStringSet.)

import java.util.List;
import java.util.LinkedList;

/** A set of String values.
 *  @author Shixuan (Wayne) Li
 */
class ECHashStringSet implements StringSet {

    private static final int INIT_SIZE = 10;
    private static final int MAX_LOAD = 5;

    public ECHashStringSet() {
        _size = 0;
        _bucket = new LinkedList[INIT_SIZE];
        for (int i = 0; i < INIT_SIZE; i += 1) {
            _bucket[i] = new LinkedList<String>();
        }
    }

    public int size() {
        return _size;
    }

    public int bucketlength() {
        return _bucket.length;
    }

    public int maxsize() {
        return bucketlength() * MAX_LOAD;
    }

    public void resize() {
        LinkedList<String>[] _oldbucket = _bucket;
        _bucket = new LinkedList[_oldbucket.length * 2];
        _size = 0;

        for(LinkedList<String> list : _oldbucket) {
            if (list != null) {
                for (String s : list) {
                    this.put(s);
                }
            }
        }
    }

    private int stringToBucket(String s) {
        int hashCode = s.hashCode();
        return ((hashCode >>> 1) | (hashCode & 1)) % _bucket.length;
    }

    @Override
    public void put(String s) {
        if (s != null) {
            _size += 1;

            if (size() > maxsize()) {
                resize();
            }

            int position = stringToBucket(s);

            if (_bucket[position] == null) {
                _bucket[position] = new LinkedList();
            } else {
                _bucket[position].add(s);
            }
        }
    }

    @Override
    public boolean contains(String s) {
        int position = stringToBucket(s);

        if (_bucket[position] == null) {
            return false;
        }
        else {
            return _bucket[position].contains(s);
        }
    }

    @Override
    public List<String> asList() {
        return null;
    }

    private LinkedList<String>[] _bucket;
    private int _size;
}
