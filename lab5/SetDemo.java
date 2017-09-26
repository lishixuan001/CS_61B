import java.util.Set;
import java.util.HashSet;

/**This is the class.
 * @author Wayne Li
 * */
public class SetDemo {

    /**This is the main.
     * @param args **input.*/
    public static void main(String[] args) {
        Set<String> resultSet = new HashSet<String>();
        resultSet.add("papa");
        resultSet.add("bear"
        resultSet.add("mama");
        resultSet.add("bear");
        resultSet.add("baby");
        resultSet.add("bear");
        System.out.println(resultSet);
    }
}
