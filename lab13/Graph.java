import java.util.*;

public class Graph {

    private LinkedList<Edge>[] adjLists;
    private int vertexCount;
    private static final int INFTY = Integer.MAX_VALUE;

    // Initialize a graph with the given number of vertices and no edges.
    @SuppressWarnings("unchecked")
    public Graph(int numVertices) {
        adjLists = (LinkedList<Edge>[]) new LinkedList[numVertices];
        for (int k = 0; k < numVertices; k++) {
            adjLists[k] = new LinkedList<Edge>();
        }
        vertexCount = numVertices;
    }

    // Add to the graph a directed edge from vertex v1 to vertex v2,
    // with the given edge information. If the edge already exists,
    // replaces the current edge with a new edge with edgeInfo.
    public void addEdge(int v1, int v2, int edgeWeight) {
        if (!isAdjacent(v1, v2)) {
            LinkedList<Edge> v1Neighbors = adjLists[v1];
            v1Neighbors.add(new Edge(v1, v2, edgeWeight));
        } else {
            LinkedList<Edge> v1Neighbors = adjLists[v1];
            for (Edge e : v1Neighbors) {
                if (e.to() == v2) {
                    e.edgeWeight = edgeWeight;
                }
            }
        }
    }

    // Add to the graph an undirected edge from vertex v1 to vertex v2,
    // with the given edge information. If the edge already exists,
    // replaces the current edge with a new edge with edgeInfo.
    public void addUndirectedEdge(int v1, int v2, int edgeWeight) {
        addEdge(v1, v2, edgeWeight);
        addEdge(v2, v1, edgeWeight);
    }

    // Return true if there is an edge from vertex "from" to vertex "to";
    // return false otherwise.
    public boolean isAdjacent(int from, int to) {
        for (Edge e : adjLists[from]) {
            if (e.to() == to) {
                return true;
            }
        }
        return false;
    }

    // Returns a list of all the neighboring  vertices 'u'
    // such that the edge (VERTEX, 'u') exists in this graph.
    public List<Integer> neighbors(int vertex) {
        ArrayList<Integer> neighbors = new ArrayList<>();
        for (Edge e : adjLists[vertex]) {
            neighbors.add(e.to());
        }
        return neighbors;
    }

    // Runs Dijkstra's algorithm starting from vertex 'startVertex' and returns
    // an integer array consisting of the shortest distances from 'startVertex'
    // to all other vertices.
    public int[] dijkstras(int startVertex) {
        int[] result = new int[vertexCount];

        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < vertexCount; i++) {
            map.put(i, INFTY);
        }
        map.put(startVertex, 0);

        helper(startVertex, map);

        for (int i = 0; i < vertexCount; i++) {
            result[i] = map.get(i);
        }

        return result;
    }

    private void helper(int start, HashMap<Integer, Integer> map) {

        if (adjLists[start].isEmpty()) {
            return;
        }

        for (Edge e : adjLists[start]) {
            int value = map.get(e.from) + e.edgeWeight;
            if (value < map.get(e.to)) {
                map.put(e.to, value);
                helper(e.to, map);
            }
        }
    }

    // Returns the Edge object corresponding to the listed vertices, v1 and v2.
    // You may find this helpful to implement!
    private Edge getEdge(int v1, int v2) {
        return null;
    }

    private class Edge {

        private int from;
        private int to;
        private int edgeWeight;

        public Edge(int from, int to, int weight) {
            this.from = from;
            this.to = to;
            this.edgeWeight = weight;
        }

        public int to() {
            return to;
        }

        public int info() {
            return edgeWeight;
        }

        public String toString() {
            return "(" + from + "," + to + ",dist=" + edgeWeight + ")";
        }

    }

    public static void main(String[] args) {
        // Put some tests here!

        Graph g1 = new Graph(5);
        g1.addEdge(0, 1, 1);
        g1.addEdge(0, 2, 1);
        g1.addEdge(0, 4, 1);
        g1.addEdge(1, 2, 1);
        g1.addEdge(2, 0, 1);
        g1.addEdge(2, 3, 1);
        g1.addEdge(4, 3, 1);

        Graph g2 = new Graph(5);
        g2.addEdge(0, 1, 1);
        g2.addEdge(0, 2, 1);
        g2.addEdge(0, 4, 1);
        g2.addEdge(1, 2, 1);
        g2.addEdge(2, 3, 1);
        g2.addEdge(4, 3, 1);

        System.out.println("test1");
        int[] test1 = g1.dijkstras(1);
        for (int i : test1) {
            System.out.println(i);
        }

        System.out.println("test2");
        int[] test2 = g1.dijkstras(0);
        for (int i : test2) {
            System.out.println(i);
        }
    }
}
