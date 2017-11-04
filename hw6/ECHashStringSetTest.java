import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ECHashStringSetTest {

    /** Basic test of adding, checking, and removing two elements from a heap */
    @Test
    public void GeneralTest() {
        ECHashStringSet Set = new ECHashStringSet();
        assertEquals(Set.bucketlength(),10);

        for (int i = 0; i < 1000; i++) {
            String s = StringUtils.randomString(10);
            Set.put(s);
        }

    }

    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(ECHashStringSetTest.class));
    }
}
