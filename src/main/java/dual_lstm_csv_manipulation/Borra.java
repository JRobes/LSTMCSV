package dual_lstm_csv_manipulation;

import au.com.bytecode.opencsv.CSVReader;
import org.nd4j.common.io.ClassPathResource;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Borra {
    public static void main( String[] args ){
        // Parámetros
        int numSamples = 40;
        int numFeatures = 3; // Segunda, tercera y cuarta columna (features)
        int sequenceLength = 7; // Longitud de la secuencia para LSTM

        // Leer el archivo CSV
        String csvFile = "test.csv";

        CSVReader reader = null;
        try {
            String classPath = new ClassPathResource("").getFile().getPath();
            Path path2LevelsUp = Paths.get(classPath).getParent().getParent();
            String absCsv = path2LevelsUp.toAbsolutePath() + File.separator + csvFile;

            reader = new CSVReader(new FileReader(absCsv));
            List<String[]> data = reader.readAll();
            // Inicializar arrays para features y labels
            double[][][] features = new double[numSamples - sequenceLength][sequenceLength][numFeatures];
            double[][] labels = new double[numSamples - sequenceLength][1];

            // Procesar los datos
            for (int i = 0; i < numSamples - sequenceLength; i++) {
                for (int j = 0; j < sequenceLength; j++) {
                    // Leer las features (columnas 2, 3 y 4)
                    features[i][j][0] = Double.parseDouble(data.get(i + j)[1]); // Segunda columna
                    features[i][j][1] = Double.parseDouble(data.get(i + j)[2]); // Tercera columna
                    features[i][j][2] = Double.parseDouble(data.get(i + j)[3]); // Cuarta columna
                }
                // Leer el target (segunda columna, que es el target)
                labels[i][0] = Double.parseDouble(data.get(i + sequenceLength)[1]);
            }

            // Convertir a INDArray
            INDArray featureArray = Nd4j.create(features);
            INDArray labelArray = Nd4j.create(labels);

            // Imprimir formas para verificar
            System.out.println("Forma de features: " + Arrays.toString(featureArray.shape())); // [numSamples - sequenceLength, sequenceLength, numFeatures]
            System.out.println("Forma de labels: " + Arrays.toString(labelArray.shape())); // [numSamples - sequenceLength, 1]

            System.out.println("Forma de features: \n" + featureArray); // [numSamples - sequenceLength, sequenceLength, numFeatures]
            //System.out.println("Forma de labels: " + Arrays.toString(labelArray.shape()));
            System.out.println("Forma de labels: \n" + labelArray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }








        /*
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

        */
    }
}
