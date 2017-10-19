import java.util.List;

/**
 * Implementation of a BST based String Set.
 * @author
 */
public class BSTStringSet implements StringSet {
    /** Creates a new empty set. */
    public BSTStringSet() {
        root = null;
    }

    @Override
    public void put(String s) {
        // FIXME
    }

    @Override
    public boolean contains(String s) {
        return false; // FIXME
    }

    @Override
    public List<String> asList() {
        return null; // FIXME
    }

    /** Represents a single Node of the tree. */
    private static class Node {
        /** String stored in this Node. */
        private String s;
        /** Left child of this Node. */
        private Node left;
        /** Right child of this Node. */
        private Node right;

        /** Creates a Node containing SP. */
        public Node(String sp) {
            s = sp;
        }
    }

    /** Root node of the tree. */
    private Node root;
}
