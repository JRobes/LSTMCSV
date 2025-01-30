package dual_lstm_csv_manipulation;

import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

public interface IDataPreparation {
    public DataSetIterator prepareData(double[][] rawData, int sequenceLength, int batchSize);
}
