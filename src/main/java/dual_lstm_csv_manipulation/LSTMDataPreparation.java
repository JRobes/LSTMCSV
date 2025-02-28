package dual_lstm_csv_manipulation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.text.DecimalFormat;
import java.util.Arrays;

public class LSTMDataPreparation {
    public static void main(String[] args) {
        int numSamples = 15;      // Cantidad total de filas
        int numFeatures = 3;       // Cantidad de columnas (features)
        int sequenceLength = 5;    // Pasos de tiempo por secuencia

        // Simulación de datos (100 muestras con 3 características)
        double[][] data = new double[numSamples][numFeatures];
        for (int i = 0; i < numSamples; i++) {
            for (int j = 0; j < numFeatures; j++) {
                data[i][j] = (double)Math.round(Math.random() * 100d) / 100d; // Datos aleatorios para ejemplo
                System.out.print(data[i][j] + "\t");
            }
            System.out.println();
        }

        // Determinar cantidad de secuencias
        int numSequences = numSamples - sequenceLength + 1; // 100 - 5 + 1 = 96

        // Crear array para almacenar secuencias
        double[][][] sequences = new double[numSequences][numFeatures][sequenceLength];

        // Extraer secuencias
        for (int i = 0; i < numSequences; i++) {
            for (int j = 0; j < sequenceLength; j++) {
                for (int k = 0; k < numFeatures; k++) {
                    sequences[i][k][j] = data[i + j][k]; // [batch, features, timeStep]
                }
            }
        }

        // Convertir a INDArray (necesario para DL4J)
        INDArray trainData = Nd4j.create(sequences);
        System.out.println("··························");
        // Mostrar dimensiones
        System.out.println("Forma del trainData: " + Arrays.toString(trainData.shape()));
        // Debería imprimir: [96, 3, 5] → [miniBatchSize, numFeatures, sequenceLength]

        System.out.println(trainData);

    }
}
