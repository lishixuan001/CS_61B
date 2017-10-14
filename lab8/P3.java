import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class P3 {

    public String method(int num) {
        int i = 0;
        ArrayList<Integer> result = new ArrayList<>();
        while (i < num) {
            int number = i % 4;
            if (number == 0) {
                number = 1;
            }
            result.add(number);
            i += 1;
        }
        return result.toArray(new String);
    }

    public static void main(String... ignored) {
        FileReader file = null;
        String test = "1";
        test = test + "2";
        System.out.print(test);

        test2.add(2);
        test2.

        try {
            file = new FileReader("3/example.inp");
        } catch (Exception e) {
            e.printStackTrace();
        }

        BufferedReader readTree = new BufferedReader(file);

        String treeLine = "";

        try {
            treeLine = readTree.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] itemsArray = treeLine.split("");
        for (int i = 0; i < itemsArray.length; i++) {
            System.out.print(this.method(itemsArray[i]));
        }



    }


}
