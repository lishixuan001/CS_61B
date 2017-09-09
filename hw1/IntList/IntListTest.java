import static org.junit.Assert.*;
import org.junit.Test;

public class IntListTest {

    /** Sample test that verifies correctness of the IntList.list static
     *  method. The main point of this is to convince you that
     *  assertEquals knows how to handle IntLists just fine.
     */

    @Test
    public void testList() {
        IntList one = new IntList(1, null);
        IntList twoOne = new IntList(2, one);
        IntList threeTwoOne = new IntList(3, twoOne);

        IntList x = IntList.list(3, 2, 1);
        assertEquals(threeTwoOne, x);
    }

    /** Do not use the new keyword in your tests. You can create
     *  lists using the handy IntList.list method.
     *
     *  Make sure to include test cases involving lists of various sizes
     *  on both sides of the operation. That includes the empty list, which
     *  can be instantiated, for example, with
     *  IntList empty = IntList.list().
     *
     *  Keep in mind that dcatenate(A, B) is NOT required to leave A untouched.
     *  Anything can happen to A.
     */

    @Test
    public void testDcatenate() {
        IntList a = new IntList(1, null);
        IntList b = new IntList(2, null);
        IntList c = new IntList(1, b);

        assertEquals(IntList.dcatenate(IntList.list(), a), a);
        assertEquals(IntList.dcatenate(a, b), c);

    }

    /** Tests that subtail works properly. Again, don't use new.
     *
     *  Make sure to test that subtail does not modify the list.
     */

    @Test
    public void testSubtail() {
        IntList a = new IntList(1, null);
        IntList b = new IntList(2, a);
        IntList c = new IntList(3, b);


        IntList test1 = IntList.subTail(c, -1);
        assertEquals(null, test1);

        IntList test2 = IntList.subTail(c, 1);
        assertEquals(test2, b);

        IntList test3 = IntList.subTail(c, 5);
        assertEquals(null, test3);

    }

    /** Tests that sublist works properly. Again, don't use new.
     *
     *  Make sure to test that sublist does not modify the list.
     */

    @Test
    public void testSublist() {
        IntList a = new IntList(1, null);
        IntList b = new IntList(2, a);
        IntList c = new IntList(3, b);
        IntList d = new IntList(2, null);

        IntList test1 = IntList.sublist(c, -1, 1);
        assertEquals(null, test1);

        IntList test2 = IntList.sublist(c, 1,1);
        assertEquals(test2, d);

        IntList test3 = IntList.sublist(c, 5, 1);
        assertEquals(null, test3);
    }

    /** Tests that dSublist works properly. Again, don't use new.
     *
     *  As with testDcatenate, it is not safe to assume that list passed
     *  to dSublist is the same after any call to dSublist
     */

    @Test
    public void testDsublist() {
        IntList a = new IntList(1, null);
        IntList b = new IntList(2, a);
        IntList c = new IntList(3, b);
        IntList input = new IntList (3, b);

        IntList test1 = IntList.dsublist(input, -1, 1);
        assertEquals(null, test1);

        IntList input2 = new IntList (3, b);

        IntList test2 = IntList.sublist(input2, 1,2);
        assertEquals(test2, b);

        IntList input3 = new IntList (3, b);

        IntList test3 = IntList.sublist(input3, 1,5);
        assertEquals(test3, b);
    }


    /* Run the unit tests in this file. */
    public static void main(String... args) {
        System.exit(ucb.junit.textui.runClasses(IntListTest.class));
    }
}
