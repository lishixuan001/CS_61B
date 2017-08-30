public class hw0{
  private static Integer max(int[] a){
    int a_length = a.length;
    int index = a[0];
    for (int i = 0; i < a_length; i++){
      if (a[i] > index){
        index = a[i];
      }
    }
    return index;
  }

  private static boolean threeSum(int[] a){
    int a_length = a.length;
    for (int f = 0; f < a_length; f++){
      for (int g = 0; g < a_length; g++){
        for (int h = 0; h < a_length; h++){
          int number = a[f] + a[g] + a[h];
          if (number == 0){
            return true;
          }
        }
      }
    }
    return false;
  }

  private static boolean threeSumDistinct(int[] a){
    static

  }


  public static void main(String[] args){
    int[] inputmax = {1, 2, 3, 4};
    int resultmax = max(inputmax);
    System.out.println("Answer for max is: " + resultmax);
    int[] inputthree = {-6, 3, 10, 200};
    boolean resultthree = threeSum(inputthree);
    System.out.println("Answer for threeSum is: " + resultthree);
  }
}
