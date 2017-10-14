import java.io.*;

public class P1 {

    public static void main(String... ignored) {

        FileReader file = null;

        try {
             file = new FileReader("1/example.inp");
        } catch (Exception e) {
            e.printStackTrace();
        }

        BufferedReader readImage =  new BufferedReader(file);

        String imageLine="";
        String str="";

        try {
            imageLine = readImage.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        str += " " + imageLine;
        System.out.println(str);

        for (String item : imageLine)






//        while(( imageLine = readImage.readLine()) != null){
//            str += " "+ imageLine;
//            System.out.println(str);
//        }
//
//        String[] numbersArray = str.split(" ");
//        System.out.println(numbersArray);

    }


}
