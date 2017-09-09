
public class IntDList {

    protected DNode _front, _back;

    public IntDList() {
        _front = _back = null;
    }

    public IntDList(Integer... values) {
        _front = _back = null;
        for (int val : values) {
            insertBack(val);
        }
    }

    /** Returns the first item in the IntDList. */
    public int getFront() {
        return _front._val;
    }

    /** Returns the last item in the IntDList. */
    public int getBack() {
        return _back._val;
    }

    /** Return value #I in this list, where item 0 is the first, 1 is the
     *  second, ...., -1 is the last, -2 the second to last.... */
    public int get(int i) {

        DNode n = null;
        if (i >= 0){
            n = _front;
            while (n != null && i != 0){
                n = n._next;
                i -= 1;
            }
        }
        else {
            n = _back;
            while (n != null && i != -1){
                n = n._prev;
                i += 1;
            }
        }
        if (n != null) {
            return n._val;
        } else {
            return 0;
        }

        // Your code here
    }

    /** The length of this list. */
    public int size() {
        DNode n = _front;
        int size = 0;

        while(n != null){
            n = n._prev;
            size += 1;
        }
        return size;
           // Your code here
    }

    /** Adds D to the front of the IntDList. */
    public void insertFront(int d) {
        DNode new_node = new DNode(d);
        DNode ori_node = _front;

        new_node._prev = ori_node;
        if(ori_node != null)
            ori_node._next = new_node;

        _front = new_node;
        if(_back == null)
            _back = new_node;
        // Your code here
    }

    /** Adds D to the back of the IntDList. */
    public void insertBack(int d) {
        DNode new_node = new DNode(d);
        DNode ori_node = _back;

        new_node._next = ori_node;
        if(ori_node != null)
            ori_node._prev = new_node;

        _back = new_node;
        if(_front == null)
            _front = new_node;
        // Your code here
    }

    /** Removes the last item in the IntDList and returns it.
     * This is an extra challenge problem. */
    public int deleteBack() {
        return 0;
        }
        // Your code here



    /** Returns a string representation of the IntDList in the form
     *  [] (empty list) or [1, 2], etc.
     * This is an extra challenge problem. */
        public String toString() { return "0";

        // Your code here
    }

    /* DNode is a "static nested class", because we're only using it inside
     * IntDList, so there's no need to put it outside (and "pollute the
     * namespace" with it. This is also referred to as encapsulation.
     * Look it up for more information! */
    protected static class DNode {
        protected DNode _prev;
        protected DNode _next;
        protected int _val;

        private DNode(int val) {
            this(null, val, null);
        }

        private DNode(DNode prev, int val, DNode next) {
            _prev = prev;
            _val = val;
            _next = next;
        }
    }

}
