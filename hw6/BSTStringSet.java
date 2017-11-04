import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * Implementation of a BST based String Set.
 * @author Shixuan (Wayne) Li
 */
public class BSTStringSet implements SortedStringSet, Iterable<String> {
    /** Creates a new empty set. */
    public BSTStringSet() {
        root = null;
    }

    @Override
    public boolean contains(String s) {
        Node last = find(s);
        return last != null && s.equals(last.s);
    }

    @Override
    public void put(String s) {
        Node last = find(s);
        if (last == null) {
            root = new Node(s);
        } else {
            int c = s.compareTo(last.s);
            if (c < 0) {
                last.left = new Node(s);
            } else if (c > 0) {
                last.right = new Node(s);
            }
        }
    }

    @Override
    public List<String> asList() {
        ArrayList<String> result = new ArrayList<>();
        for (String label : this) {
            result.add(label);
        }
        return result;
    }

    @Override
    public Iterator<String> iterator() {
        return new BSTIterator(root);
    }

    @Override
    public Iterator<String> iterator(String low, String high) {
        return new BSTBoundIterator(low, high);
    }

    /** Return either the node in this BST that contains S, or, if
     *  there is no such node, the node that should be the parent
     *  of that node, or null if neither exists. */
    private Node find(String s) {
        if (root == null) {
            return null;
        }
        Node p;
        p = root;
        while (true) {
            int c = s.compareTo(p.s);
            Node next;
            if (c < 0) {
                next = p.left;
            } else if (c > 0) {
                next = p.right;
            } else {
                return p;
            }
            if (next == null) {
                return p;
            } else {
                p = next;
            }
        }
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

    /** An iterator over BSTs. */
    private static class BSTIterator implements Iterator<String> {
        /** Stack of nodes to be delivered.  The values to be delivered
         *  are (a) the label of the top of the stack, then (b)
         *  the labels of the right child of the top of the stack inorder,
         *  then (c) the nodes in the rest of the stack (i.e., the result
         *  of recursively applying this rule to the result of popping
         *  the stack. */
        private Stack<Node> _toDo = new Stack<>();
        private Node node;
        private String left;
        private String right;

        /** A new iterator over the labels in NODE. */
        BSTIterator(Node node) {
            addTree(node);
        }

        @Override
        public boolean hasNext() {
            return !_toDo.empty();
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            Node node = _toDo.pop();
            addTree(node.right);
            return node.s;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /** Add the relevant subtrees of the tree rooted at NODE. */
        private void addTree(Node node) {
            while (node != null) {
                _toDo.push(node);
                node = node.left;
            }
        }
    }

    /** An iterator over BSTs. */
    private class BSTBoundIterator implements Iterator<String> {
        /** Stack of nodes to be delivered.  The values to be delivered
         *  are (a) the label of the top of the stack, then (b)
         *  the labels of the right child of the top of the stack inorder,
         *  then (c) the nodes in the rest of the stack (i.e., the result
         *  of recursively applying this rule to the result of popping
         *  the stack. */
        private Stack<Node> _toDo = new Stack<>();
        private Node _node;
        private String _low;
        private String _high;

        /** A new iterator over the labels in NODE. */
        BSTBoundIterator(String low, String high) {
            this._low = low;
            this._high = high;
            _node = root;
        }

        @Override
        public boolean hasNext() {
            boolean condition1 = !_toDo.empty();
            boolean condition2 = _node != null && _node.s.compareTo(_high) <= 0;
            return condition1 || condition2;
        }

        private boolean cancontinue() {
            return _node != null && _node.s.compareTo(_low) >= 0;
        }
        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            addTree();
            Node node = _toDo.pop();
            _node = node.right;
            return node.s;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /** Add the relevant subtrees of the tree rooted at NODE. */
        private void addTree() {
            while (cancontinue()) {
                _toDo.push(_node);
                _node = _node.left;
            }
        }
    }

    public void printInOrder() {
        printInOrder(root);
    }

    private void printInOrder(Node node) {
        if (node != null) {
            printInOrder(node.left);
            System.out.print(node.s);
            printInOrder(node.right);
        }
    }

    /** Root node of the tree. */
    private Node root;
}
