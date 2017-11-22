import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/** HW #8, Problem 3.
 *  @author
  */
public class Intervals {
    /** Assuming that INTERVALS contains two-element arrays of integers,
     *  <x,y> with x <= y, representing intervals of ints, this returns the
     *  total length covered by the union of the intervals. */
    public static int coveredLength(List<int[]> intervals) {
        // REPLACE WITH APPROPRIATE STATEMENTS.
        intervals.sort(new helperComparator());
        ArrayList<int[]> helper = new ArrayList<>();
        helper.add(intervals.get(0));

        for (int i = 0; i < intervals.size(); i++) {
            int middle = intervals.get(i)[0];
            int front = helper.get(helper.size() - 1)[1];
            int back = intervals.get(i)[1];
            if (middle < front && back < front) {
                int[] rec = new int[]{ helper.get(helper.size() - 1)[0], back}
                helper.remove(helper.size() - 1);
                helper.add(rec);
            } else if (middle > front) {
                helper.add(intervals.get(i));
            }
        }
        int second = 0;
        for (int[] i : helper) {
            second += i[1] - i[0];
        }
        return second;
    }

    static class helperComparator implements Comparator<int[]> {
        public int compare(int[] a, int[] b) {
            if (a[0] == b[0]) {
                return a[1] - b[1];
            } else {
                return a[0] - b[0];
            }
        }
    }

    /** Test intervals. */
    static final int[][] INTERVALS = {
        {19, 30},  {8, 15}, {3, 10}, {6, 12}, {4, 5},
    };
    /** Covered length of INTERVALS. */
    static final int CORRECT = 23;

    /** Performs a basic functionality test on the coveredLength method. */
    @Test
    public void basicTest() {
        assertEquals(CORRECT, coveredLength(Arrays.asList(INTERVALS)));
    }

    /** Runs provided JUnit test. ARGS is ignored. */
    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(Intervals.class));
    }

}
