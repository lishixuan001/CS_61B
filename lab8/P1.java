

public class P1 {

    public static void main(String... ignored) {

        BufferedReader readTxt=new BufferedReader(new FileReader(new File("text.txt")));
        String textLine="";
        String str="";
        while(( textLine=readTxt.readLine())!=null){
            str+=" "+ textLine;
        }
        String[] numbersArray=str.split(" ");

    }


}
