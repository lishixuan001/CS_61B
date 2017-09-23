import org.junit.Test;
import static org.junit.Assert.*;

/** Test for Arrays
 *  @author Wayne Li
 */

public class ArraysTest {

    @Test
    public void testCatenate() {
        int[] A = {1, 2, 3};
        int[] B = {4, 5, 6};
        int[] result = {1, 2, 3, 4, 5, 6};

        int[] resultArray = Arrays.catenate(A, B);
        assertArrayEquals(result, resultArray);
    }

    @Test
    public void testRemove() {
        int[] A = {1, 2, 3, 4, 5, 6};
        int[] result = {1, 2, 6};

        int[] resultArray = Arrays.remove(A, 2, 3);
        assertArrayEquals(result, resultArray);
    }

    @Test
    public void testNatual() {
        int[] A = {1, 3, 7, 5, 4, 6, 9, 10};
        int[][] outPut = {{1, 3, 7}, {5}, {4, 6, 9, 10}};

        int[][] resultArray = Arrays.naturalRuns(A);
        assertArrayEquals(resultArray, outPut);
    }

    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(ArraysTest.class));
    }
}
