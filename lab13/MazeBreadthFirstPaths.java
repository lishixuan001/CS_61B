import java.util.LinkedList;
import java.util.Observable;
import java.util.Queue;

/**
 *  @author Josh Hug
 */

public class MazeBreadthFirstPaths extends MazeExplorer {
    /* Inherits public fields:
    public int[] distTo;
    public int[] edgeTo;
    public boolean[] marked;
    */
    private int s;
    private int t;
    private Maze maze;
    private boolean targetFound = false;
    private static final int INFTY = Integer.MAX_VALUE;
    private Queue<Integer> queue = new LinkedList<>();

    public MazeBreadthFirstPaths(Maze m, int sourceX, int sourceY, int targetX, int targetY) {
        super(m);
        maze = m;
        s = maze.xyTo1D(sourceX, sourceY);
        t = maze.xyTo1D(targetX, targetY);

        marked = new boolean[maze.V()];
        distTo = new int[maze.V()];
        edgeTo = new int[maze.V()];

        for (int k = 0; k < maze.V(); k++) {
            distTo[k] = INFTY;
        }

        // Set up the start point
        distTo[s] = 0;
        marked[s] = true;
        queue.offer(s);
    }

    /** Conducts a breadth first search of the maze starting at the source. */
    private void bfs() {
        // TODO: Stop when reach end
        while(!queue.isEmpty()) {
            int k = queue.poll();
            if (k == t) { return; }
            for (int p : maze.adj(k)) {
                if (!marked[p]) {
                    edgeTo[p] = k;
                    distTo[p] = distTo[k] + 1;
                    marked[p] = true;
                    queue.offer(p);
                    announce();
                }
            }
        }

    }


    @Override
    public void solve() {
        bfs();
    }
}

