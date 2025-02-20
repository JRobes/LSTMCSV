package dual_lstm_csv_manipulation;

import org.datavec.api.writable.Writable;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import java.util.List;

public class DataPreparation implements IDataPreparation{
    @Override
    public DataSetIterator prepareData(double[][] rawData, int sequenceLength, int batchSize) {
        int numFeatures = rawData[0].length - 1; // Excluyendo la columna de la etiqueta
        int numSamples = rawData.length - sequenceLength + 1;

        INDArray features = Nd4j.create(new int[]{numSamples, numFeatures, sequenceLength}, 'f');
        INDArray labels = Nd4j.create(new int[]{numSamples, 1, sequenceLength}, 'f');

        for (int i = 0; i < numSamples; i++) {
            for (int j = 0; j < sequenceLength; j++) {
                for (int k = 0; k < numFeatures; k++) {
                    features.putScalar(new int[]{i, k, j}, rawData[i + j][k + 1]); // k + 1 para omitir la columna de la etiqueta
                }
                labels.putScalar(new int[]{i, 0, j}, rawData[i + j][0]); // La primera columna es la etiqueta
            }
        }

        // Convertir a DataSet
        DataSet dataSet = new DataSet(features, labels);

        return new CustomDataSetIterator(dataSet.asList(), batchSize);
    }

    @Override
    public List<List<Writable>> getDataAsWritable() {
        return null;
    }

}
