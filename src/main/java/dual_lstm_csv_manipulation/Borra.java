package dual_lstm_csv_manipulation;

import java.util.Arrays;

public class Borra {
    public static void main( String[] args ){
        System.out.println(Math.round(0.4));
        int a = (int) Math.round(0.8);
        System.out.println(a);
        //initialize 3-d array
        int[][][] myArray = { { { 1, 2, 3 }, { 4, 5, 6 } },  { { 1, 4, 9 }, { 16, 25, 36 } },
                { { 1, 8, 27 }, { 64, 125, 216 } }, { { 100, 200, 300 }, { 400, 500, 600 } } };
        System.out.println("3x2x3 array is given below:");
        //print the 3-d array
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 3; k++) {
                    System.out.print(myArray[i][j][k] + "\t");
                }
                System.out.println();
                System.out.println("TT: " + myArray[i][j].length);
            }
            System.out.println();
            System.out.println(myArray[i].length);
        }
        System.out.println(myArray.length);
    }
}
