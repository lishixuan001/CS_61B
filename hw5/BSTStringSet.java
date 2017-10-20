import java.util.List;

/**
 * Implementation of a BST based String Set.
 * @author Shixuan (Wayne) Li
 */
public class BSTStringSet implements StringSet {
    /** Creates a new empty set. */
    public BSTStringSet() {
        root = null;
    }

    @Override
    public void put(String s) {
        root = helpput(s, root);
    }

    private Node helpput(String s, Node root) {
        if (root == null) {
            return new Node(s);
        }
        int compare = s.compareTo(root.s);
        if (compare < 0) {
            root.left = helpput(s, root.left);
        } else if (compare > 0) {
            root.right = helpput(s, root.right);
        } else {
            return root;
        }
        return root;
    }

    @Override
    public boolean contains(String s) {
        return helpcontains(s, root);
    }

    private boolean helpcontains(String s, Node root) {
        if (root == null) {
            return false;
        }
        int compare = s.compareTo(root.s);
        if (compare < 0) {
            return helpcontains(s, root.left);
        } else if (compare > 0) {
            return helpcontains(s, root.right);
        } else {
            return true;
        }
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
