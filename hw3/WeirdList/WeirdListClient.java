/** Functions to increment and sum the elements of a WeirdList. */
class WeirdListClient {

    /** Return the result of adding N to each element of L. */
    static WeirdList add(WeirdList L, int n) {
        return L.map(x -> x + n);
    }

    /** Return the sum of the elements in L. */
    static int sum(WeirdList L) {
        Helper result = new Helper(0);
        L.map(result);
        return result.feedback();
    }

    /* As with WeirdList, you'll need to add an additional class or
     * perhaps more for WeirdListClient to work. Again, you may put
     * those classes either inside WeirdListClient as private static
     * classes, or in their own separate files.

     * You are still forbidden to use any of the following:
     *       if, switch, while, for, do, try, or the ?: operator.
     */
    public static class Helper implements IntUnaryFunction {
        private int result = 0;

        public Helper(int input) {
            this.result = input;
        }

        @Override
        public int apply(int x) {
            this.result += x;
            return x;
        }

        public int feedback() {
            return this.result;
        }
    }

}
