public class LeapYear {
    
    public static void main(String... args) {
        int year = 2000;
        String not = (((year % 400) == 0) || 
                      ((year % 4) == 0 && !(year % 100 == 0))) ?
            "" : "not ";
        System.out.printf("%d is %sa leap year.%n", year, not);
    }

}
