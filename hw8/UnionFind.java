import java.util.Arrays;

/** A partition of a set of contiguous integers that allows (a) finding whether
 *  two integers are in the same partition set and (b) replacing two partitions
 *  with their union.  At any given time, for a structure partitioning
 *  the integers 1-N, each partition is represented by a unique member of that
 *  partition, called its representative.
 *  @author Shixuan (Wayne) Li
 */
public class UnionFind {

    /** A union-find structure consisting of the sets { 1 }, { 2 }, ... { N }.
     */
    public UnionFind(int N) {
        _id = new int[N];
        initialId(_id);
        _length = N;
    }

    private void initialId(int[] id) {
        for (int i = 0; i < id.length; i++) {
            id[i] = i;
        }
    }

    /** Return the representative of the partition currently containing V.
     *  Assumes V is contained in one of the partitions.  */
    public int find(int v) {
        return _id[v];
    }

    /** Return true iff U and V are in the same partition. */
    public boolean samePartition(int u, int v) {
        return find(u) == find(v);
    }

    /** Union U and V into a single partition, returning its representative. */
    public int union(int u, int v) {
        if (u != v) {
            for (int i = 0; i < _id.length; i++) {
                if (_id[i] == find(u)) {
                    _id[i] = find(v);
                }
            }
        }
        _length -= 1;
        return find(v);
    }

    private int[] _id;
    private int _length;
}
