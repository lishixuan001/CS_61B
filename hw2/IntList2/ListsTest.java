import org.junit.Test;
import static org.junit.Assert.*;

/** Test Lists
 *
 *  @author Wayne Li
 */

public class ListsTest {

    @Test
    public void testLists() {
        int[] inPut0 = {1, 3, 7, 5, 4, 6, 9, 10, 10, 11};
        IntList inPut = IntList.list(inPut0);
        System.out.println(inPut);

        int[] outPut10 = {1, 3, 7};
        int[] outPut20 = {5};
        int[] outPut30 = {4, 6, 9, 10};
        int[] outPut40 = {10, 11};
        IntList outPut1 = IntList.list(outPut10);
        IntList outPut2 = IntList.list(outPut20);
        IntList outPut3 = IntList.list(outPut30);
        IntList outPut4 = IntList.list(outPut40);
        IntList2 outPut = IntList2.list(outPut1, outPut2, outPut3, outPut4);
        System.out.println(outPut);

        IntList2 result = Lists.naturalRuns(inPut);
        System.out.println(result);

        assertEquals(result, outPut);
    }

    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(ListsTest.class));
    }
}
