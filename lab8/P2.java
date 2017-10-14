import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class P2 {



    private boolean existed(String array, int num) {
        return true;
    }

    public static void main(String... ignored) {
        System.out.print("Case 1: ");
        FileReader file = null;

        try {
            file = new FileReader("2/example.inp");
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
//            System.out.print(itemsArray[i] + " ");
        }

        String treeLine2 = null;
        try {
            treeLine2 = readTree.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] itemsArray2 = treeLine2.split("");
        for (int i = 0; i < itemsArray2.length; i++) {
//            System.out.print(itemsArray2[i] + " ");
            if (i % 2 == 0) {
                System.out.print(itemsArray[i]);
            } else {
                System.out.print(itemsArray2[i]);
            }

        }


    }


}
