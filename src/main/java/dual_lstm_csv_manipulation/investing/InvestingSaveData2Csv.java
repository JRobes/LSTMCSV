package dual_lstm_csv_manipulation.investing;

import java.io.*;

public class InvestingSaveData2Csv {
    public static void main(String[] args) {
        String inputFile = "input.csv";  // Archivo de entrada
        String outputFile = "output.csv"; // Archivo de salida

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            boolean isFirstLine = true;

            String res = null;
            br.readLine();
            while ((line = br.readLine()) != null) {
                res = line.substring(1, line.length() - 1);
                System.out.println(res);
                String other = res.replaceAll("\",\"", ";");
                System.out.println(other);
                //String[] fields = res.split("\",\"");
                //double[] changes = new double[fields.length-1];
                //changes[0] = Double.parseDouble(fields[0]);
                //for(int i = 1; i < fields.length; i++){
                    //System.out.println();

                    //fields[i].replace("\",", "\";");
                   // System.out.println(changes[i]);
               // }
                String ss = "";
                //for(int i = 0; i < changes.length; i++){
                  //   ss = ss + changes[i] + ",";
                //}

                //System.out.println("WWW"  + ss);
               //


                //System.out.println(res);

                /*
                // Dividir la línea en columnas
                String[] fields = line.split(",");

                // Modificar una columna (por ejemplo, agregar un prefijo a la segunda columna)
                if (fields.length > 1 && !isFirstLine) { // Evitar modificar la cabecera
                    fields[1] = "MOD-" + fields[1];
                }

                // Escribir la línea modificada en el nuevo archivo
                bw.write(String.join(",", fields));
                bw.newLine();

                isFirstLine = false; // Marcar que ya hemos procesado la cabecera

                 */
            }

            //System.out.println("Archivo modificado guardado como " + outputFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
