import java.util.Arrays;
import java.util.Comparator;

/** Minimal spanning tree utility.
 *  @author Shixuan (Wayne) Li
 */
public class MST {

    /** Given an undirected, weighted, connected graph whose vertices are
     *  numbered 1 to V, and an array E of edges, returns a list of edges
     *  in E that form a minimal spanning tree of the input graph.
     *  Each edge in E is a three-element int array of the form (u, v, w),
     *  where 0 < u < v <= V are vertex numbers, and 0 <= w is the weight
     *  of the edge. The result is an array containing edges from E.
     *  Neither E nor the arrays in it may be modified.  There may be
     *  multiple edges between vertices.  The objects in the returned array
     *  are a subset of those in E (they do not include copies of the
     *  original edges, just the original edges themselves.) */
    public static int[][] mst(int V, int[][] E) {

        int[][] H = Arrays.copyOf(E, E.length);
        // Filter out the loops
        for (int[] item : E) {
            if (item[0] == item[1]) {
                H = remove(H, item);
            }
        }

        int[][] K = Arrays.copyOf(H, H.length);
        // Filter out the ones with parallel routes
        for (int i = 0; i < H.length; i++) {
            for (int j = i + 1; j < H.length; j++) {
                int[] itemi = E[i];
                int[] itemj = E[j];
                if (itemi[0] == itemj[0] && itemi[1] == itemj[1]) {
                    if (itemi[2] < itemj[2]) {
                        K = remove(K, itemj);
                    } else {
                        K = remove(K, itemi);
                    }
                }
            }
        }

        // use Comparator
        Arrays.sort(K, EDGE_WEIGHT_COMPARATOR);

        return K;
    }

    public static int[][] remove(int[][] symbols, int[] c)
    {
        for (int i = 0; i < symbols.length; i++)
        {
            if (symbols[i] == c)
            {
                int[][] copy = new int[symbols.length-1][];
                System.arraycopy(symbols, 0, copy, 0, i);
                System.arraycopy(symbols, i+1, copy, i, symbols.length-i-1);
                return copy;
            }
        }
        return symbols;
    }

    /** An ordering of edges by weight. */
    private static final Comparator<int[]> EDGE_WEIGHT_COMPARATOR =
        new Comparator<int[]>() {
            @Override
            public int compare(int[] e0, int[] e1) {
                return e0[2] - e1[2];
            }
        };

}
