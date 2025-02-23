package dual_lstm_csv_manipulation;

import org.datavec.api.writable.Writable;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.util.List;

public interface IDataPreparation {
    //public DataSetIterator prepareData(double[][] rawData, int sequenceLength, int batchSize);
    public List<List<Writable>> getDataAsWritable();
}
