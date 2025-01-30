package dual_lstm_csv_manipulation;

import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class CustomDataSetIterator implements DataSetIterator {
    private List<DataSet> dataSets;
    private Iterator<DataSet> iterator;
    private int batchSize;

    public CustomDataSetIterator(List<DataSet> dataSets, int batchSize) {
        this.dataSets = dataSets;
        this.batchSize = batchSize;
        this.iterator = dataSets.iterator();
    }

    @Override
    public DataSet next(int num) {
        // Implementación para devolver un lote de datos
        List<DataSet> batch = new ArrayList<>();
        int count = 0;
        while (iterator.hasNext() && count < num) {
            batch.add(iterator.next());
            count++;
        }
        return DataSet.merge(batch);
    }

    @Override
    public int inputColumns() {
        return dataSets.get(0).getFeatures().columns();
    }

    @Override
    public int totalOutcomes() {
        return dataSets.get(0).getLabels().columns();
    }

    @Override
    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return false;
    }

    @Override
    public void reset() {
        iterator = dataSets.iterator();
    }

    @Override
    public int batch() {
        return batchSize;
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<String> getLabels() {
        return null; // Implementa esto si es necesario
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public DataSet next() {
        return next(batchSize);
    }
}