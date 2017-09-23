/* NOTE: The file ArrayUtil.java contains some functions that may be useful
 * in testing your answers. */

/** HW #2 */

/** Array utilities.
 *  @author Wayne Li
 */
class Arrays {
    /* C. */
    /** Returns a new array consisting of the elements of A followed by the
     *  the elements of B. */
    static int[] catenate(int[] A, int[] B) {

        int lengthA = A.length;
        int lengthB = B.length;
        int lengthAB = lengthA + lengthB;

        int[] resultArray = new int[lengthAB];
        System.arraycopy(A, 0, resultArray, 0, lengthA);
        System.arraycopy(B, 0, resultArray, lengthA, lengthB);

        return resultArray;
    }

    /** Returns the array formed by removing LEN items from A,
     *  beginning with item #START. */
    static int[] remove(int[] A, int start, int len) {

        int lengthA = A.length;
        int lengthArray = lengthA - len;

        int[] theArray = new int[lengthArray];
        System.arraycopy(A, 0, theArray, 0, start);

        int numLen = lengthA - len - start;
        System.arraycopy(A, start + len, theArray, start, numLen);

        return theArray;
    }

    /* E. */
    /** Returns the array of arrays formed by breaking up A into
     *  maximal ascending lists, without reordering.
     *  For example, if A is {1, 3, 7, 5, 4, 6, 9, 10}, then
     *  returns the three-element array
     *  {{1, 3, 7}, {5}, {4, 6, 9, 10}}. */
    static int[][] naturalRuns(int[] A) {

        int[][] resultArray = new int[0][];
        int item = A[0];
        int[] temp;
        int begin = 0;
        Arrays useThis = new Arrays();

        for (int i = 0; i <= A.length; i += 1) {

            if (i == A.length || A[i] < item) {
                temp = new int[i - begin];
                System.arraycopy(A, begin, temp, 0, i - begin);
                resultArray = useThis.addArray(resultArray, temp);
                begin = i;
                if (i == A.length) {
                    break;
                }
            }
            item = A[i];
        }

        return resultArray;
    }

    /**This is an helper function.
     * @param resultArray ** input1
     * @param temp **input2
     * @return
     * */
    private int[][] addArray(int[][] resultArray, int[] temp) {
        int lengthArray = resultArray.length;
        int[][] newArray = new int[lengthArray + 1][];
        System.arraycopy(resultArray, 0, newArray, 0, lengthArray);
        newArray[lengthArray] = temp;
        return  newArray;
    }

}























