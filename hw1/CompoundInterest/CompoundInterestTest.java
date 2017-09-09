import static org.junit.Assert.*;
import org.junit.Test;

public class CompoundInterestTest {

    @Test
    public void testNumYears() {
        /** Sample assert statement for comparing integers. */

        assertEquals(-1, CompoundInterest.numYears(2014));
        assertEquals(0, CompoundInterest.numYears(2015));
        assertEquals(1, CompoundInterest.numYears(2016));
        assertEquals(2, CompoundInterest.numYears(2017));
    }

    @Test
    public void testFutureValue() {
        double tolerance = 0.01;
        assertEquals(12.544, CompoundInterest.futureValue(10, 12, 2017), tolerance);
        assertEquals(7.744, CompoundInterest.futureValue(10, -12, 2017), tolerance);
        assertEquals(10, CompoundInterest.futureValue(10, 0, 2017), tolerance);
        assertEquals(10, CompoundInterest.futureValue(10, -12, 2015), tolerance);
    }

    @Test
    public void testFutureValueReal() {
        double tolerance = 0.01;
        assertEquals(10, CompoundInterest.futureValueReal(10, 12, 2015, 3), tolerance);
        assertEquals(11.8026496, CompoundInterest.futureValueReal(10, 12, 2017, 3), tolerance);
        assertEquals(11.321, CompoundInterest.futureValueReal(10, 12, 2017, 5), tolerance);
        assertEquals(7.2863, CompoundInterest.futureValueReal(10, -12, 2017, 3), tolerance);
    }


    @Test
    public void testTotalSavings() {
        double tolerance = 0.01;
        assertEquals(16550, CompoundInterest.totalSavings(5000, 2017, 10), tolerance);
        assertEquals(5000, CompoundInterest.totalSavings(5000, 2015, 10), tolerance);
        assertEquals(13240, CompoundInterest.totalSavings(4000, 2017, 10), tolerance);
        assertEquals(15000, CompoundInterest.totalSavings(5000, 2017, 0), tolerance);
        assertEquals(13550, CompoundInterest.totalSavings(5000, 2017, -10), tolerance);

    }

    @Test
    public void testTotalSavingsReal() {
        double tolerance = 0.01;
        assertEquals(13405.5, CompoundInterest.totalSavingsReal(5000, 2017, 10, 10), tolerance);
        assertEquals(5000, CompoundInterest.totalSavingsReal(5000, 2015, 10, 10), tolerance);
        assertEquals(10724.4, CompoundInterest.totalSavingsReal(4000, 2017, 10, 10), tolerance);
        assertEquals(12150, CompoundInterest.totalSavingsReal(5000, 2017, 0, 10), tolerance);
        assertEquals(16395.5, CompoundInterest.totalSavingsReal(5000, 2017, -10, -10), tolerance);
        assertEquals(10975.5, CompoundInterest.totalSavingsReal(5000, 2017, -10, 10), tolerance);
    }


    /* Run the unit tests in this file. */
    public static void main(String... args) {
        System.exit(ucb.junit.textui.runClasses(CompoundInterestTest.class));
    }
}
