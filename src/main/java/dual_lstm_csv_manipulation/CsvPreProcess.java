package dual_lstm_csv_manipulation;
import java.io.*;
import java.util.*;

public class CsvPreProcess {

    public static void processCsv(String inputFilePath, String outputFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            br.readLine();//Header
            while ((line = br.readLine()) != null) {
                // Separar las columnas por punto y coma
                String[] columns = line.split(";");

                // Verificar que la fila tenga al menos 7 columnas
                if (columns.length < 7) {
                    System.err.println("Fila con formato incorrecto: " + line);
                    continue;
                }

                // Procesar columnas 2 a 5: eliminar separador de miles y cambiar separador decimal
                for (int i = 1; i <= 4; i++) {
                    if(columns[i].isEmpty()) {
                        System.err.println("Valor vacio en columna: " + i);
                        continue;
                    }
                    columns[i] = columns[i].replace(".", "").replace(",", ".");
                }

                // Procesar columna 6: manejar multiplicadores K y M
                try {
                    columns[5] = processMultiplier(columns[5]);
                } catch (Exception e) {
                    System.out.println("Excepcion !!!!!!");
                    continue;

                }

                // Procesar columna 7: eliminar formato de porcentaje
                columns[6] = columns[6].replace("%", "").replace(",", ".").trim();

                double num = Double.parseDouble(columns[6]) /100;
                columns[6] = String.valueOf(num);
                // Construir la línea de salida con coma como separador
                String outputLine = String.join(",", columns);
                System.out.println(outputLine);
                bw.write(outputLine);
                bw.newLine();
            }

            System.out.println("Procesamiento completado. Archivo de salida: " + outputFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método auxiliar para manejar multiplicadores K y M en la columna 6
    private static String processMultiplier(String value) throws Exception{

        value = value.trim().replace(",", ".");  // Cambiar separador decimal a punto
        double number;
        //System.out.println(value);

        if (value.endsWith("K")) {
            number = Double.parseDouble(value.substring(0, value.length() - 1)) * 1000;
        } else if (value.endsWith("M")) {
            number = Double.parseDouble(value.substring(0, value.length() - 1)) * 1000000;
        } else {

            number = Double.parseDouble(value);
        }

        return String.valueOf(number);
    }

    public static void main(String[] args) {
        // Ruta del archivo CSV de entrada y salida
        String inputFile = "ruta/del/archivo.csv";
        String outputFile = "ruta/del/archivo_procesado.csv";

        processCsv(inputFile, outputFile);
    }
}

