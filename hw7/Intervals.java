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
        int result = 0;
        intervals.sort(new helperComparator());
        ArrayList<int[]> helper = new ArrayList<>();
        helper.add(intervals.get(0));

        for (int i = 0; i < intervals.size(); i++) {

            int[] current = intervals.get(i);
            int front = current[0];
            int back = current[1];

            int[] compared = helper.get(helper.size() - 1);
            int cfront = compared[0];
            int cback = compared[1];

            if (front < cback && back > cback) {
                helper.remove(helper.size() - 1);
                helper.add(new int[]{ cfront, back});
            } else if (front > cback) {
                helper.add(intervals.get(i));
            }
        }

        for (int[] i : helper) {
            result += i[1] - i[0];
        }
        return result;
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
