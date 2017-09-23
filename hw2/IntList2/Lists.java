/* NOTE: The file Utils.java contains some functions that may be useful
 * in testing your answers. */

/** HW #2, Problem #1. */

/** List problem.
 *  @author Wayne Li
 */
class Lists {
    /** Return the list of lists formed by breaking up L into "natural runs":
     *  that is, maximal strictly ascending sublists, in the same order as
     *  the original.  For example, if L is (1, 3, 7, 5, 4, 6, 9, 10, 10, 11),
     *  then result is the four-item list
     *            ((1, 3, 7), (5), (4, 6, 9, 10), (10, 11)).
     *  Destructive: creates no new IntList items, and may modify the
     *  original list pointed to by L. */
    static IntList2 naturalRuns(IntList L) {
        if (L == null) {
            return null;
        }

        IntList subList = L;
        IntList copyList = L;
        IntList tailList = L.tail;
        int headValue = L.head;

        while (true) {
            if (L == null || headValue > L.head) {
                break;
            }
            headValue = L.head;
            copyList = L;
            tailList = L.tail;

            L = L.tail;

            if (tailList == null) {
                break;
            }
            if (headValue >= L.head) {
                break;
            }
        }

        if (copyList != null) {
            copyList.tail = null;
        }
        return new IntList2(subList, naturalRuns(tailList));
    }
}
