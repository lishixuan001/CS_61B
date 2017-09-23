import org.junit.Test;
import static org.junit.Assert.*;

/** Tests for MatrixUtils
 *  @author Wayne Li
 */

public class MatrixUtilsTest {

    @Test
    public void testAccumulateVertical() {
        double[] aIn1 = {1000000, 1000000, 1000000, 1000000};
        double[] aIn2 = {1000000, 75990, 30003, 1000000};
        double[] aIn3 = {1000000, 30002, 103046, 1000000};

        double[][] inPut = {aIn1, aIn2, aIn3};

        double[] aOut1 = {1000000, 1000000, 1000000, 1000000};
        double[] aOut2 = {2000000, 1075990, 1030003, 2000000};
        double[] aOut3 = {2075990, 1060005, 1133049, 2030003};

        double[][] outPut = {aOut1, aOut2, aOut3};
        double[][] outArray = MatrixUtils.accumulateVertical(inPut);
        assertArrayEquals(outArray, outPut);
    }

    @Test
    public void testAccumulate() {
        double[] aIn1 = {1000000, 1000000, 1000000, 1000000};
        double[] aIn2 = {1000000, 75990, 30003, 1000000};
        double[] aIn3 = {1000000, 30002, 103046, 1000000};

        double[][] iP = {aIn1, aIn2, aIn3};

        double[] aOut1 = {1000000, 1000000, 1000000, 1000000};
        double[] aOut2 = {2000000, 1075990, 1030003, 2000000};
        double[] aOut3 = {2075990, 1060005, 1133049, 2030003};

        double[][] outPut = {aOut1, aOut2, aOut3};
        double[][] iP2 = MatrixUtils.transpose(iP);
        double[][] outPut2 = MatrixUtils.transpose(outPut);

        double[][] oA;
        oA = MatrixUtils.accumulate(iP, MatrixUtils.Orientation.VERTICAL);
        double[][] oA2;
        oA2 = MatrixUtils.accumulate(iP2, MatrixUtils.Orientation.HORIZONTAL);

        assertArrayEquals(oA, outPut);
        assertArrayEquals(oA2, outPut2);
    }

    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(MatrixUtilsTest.class));
    }
}
