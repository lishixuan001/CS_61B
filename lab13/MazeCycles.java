import java.util.Observable;
/**
 *  @author Josh Hug
 */

public class MazeCycles extends MazeExplorer {
    /* Inherits public fields:
    public int[] distTo;
    public int[] edgeTo;
    public boolean[] marked;
    */
    private int cycle = -1;
    int[] tempedgeTo = edgeTo.clone();

    public MazeCycles(Maze m) {
        super(m);
    }

    @Override
    public void solve() {
        announce();

        helper(0);
        if (cycle == -1) {
            return;
        }

        announce();
    }

    private void helper(int v) {
        marked[v] = true;

        for (int w : maze.adj(v)) {
            if (marked[w] && tempedgeTo[v] != w) {
                tempedgeTo[w] = v;
                cycle = w;
                edgeTo[cycle] = tempedgeTo[cycle];
                int node = tempedgeTo[cycle];
                while (node != cycle) {
                    edgeTo[node] = tempedgeTo[node];
                    node = tempedgeTo[node];
                }
                return;
            }
            if (!marked[w]) {
                tempedgeTo[w] = v;
                announce();
                helper(w);
                if (cycle != -1) {
                    return;
                }
            }
        }
    }
}

