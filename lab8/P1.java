import java.io.*;
import java.util.ArrayList;

public class P1 {

    public static void main(String... ignored) {

        int min = Integer.MAX_VALUE;
        int max = -1;

        FileReader file = null;
        ArrayList<Integer> countX = new ArrayList<>();

        try {
            file = new FileReader("1/example.inp");
        } catch (Exception e) {
            e.printStackTrace();
        }

        BufferedReader readImage = new BufferedReader(file);

        String imageLine = "";
        String str = "";

        int indexTurn = 0;
        int indexImage = 0;


        while(true) {
            int count = 0;
            try {
                imageLine = readImage.readLine();
                if (imageLine == null) {
                    indexTurn += 1;
                    indexImage += 1;
                } else if (indexTurn == 1) {
                    indexTurn -= 1;
                    min = Integer.MAX_VALUE;
                    max = -1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (indexTurn == 0) {
                String[] numbersArray = imageLine.split(" ");
                for (int i = 0; i < numbersArray.length; i++) {
                    if (numbersArray[i] == "X") {
                        countX.add(i);
                        count += 1;
                    }
                }
                if (count < min) {
                    min = count;
                } else if (count > max) {
                    max = count;
                }
            }

            if (indexTurn == 1) {
                System.out.println("Image " + indexImage + ": " + (max - min));
                System.out.println();
            } else if (indexTurn == 2) {
                break;
            }

        }

    }

}
