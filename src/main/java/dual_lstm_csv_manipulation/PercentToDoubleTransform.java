package dual_lstm_csv_manipulation;

import org.datavec.api.transform.schema.Schema;
import org.datavec.api.transform.transform.BaseTransform;
import org.datavec.api.writable.DoubleWritable;
import org.datavec.api.writable.Writable;

import java.util.Arrays;
import java.util.List;

public class PercentToDoubleTransform extends BaseTransform {
    private final String columnName;

    public PercentToDoubleTransform(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public List<Writable> map(List<Writable> writables) {
        // Obtener el valor del String de la columna
        String value = writables.get(0).toString().trim();
        double result = Double.parseDouble(value.substring(0, value.length() - 1).replace(',','.')) * 0.01;
        // Devolver el resultado como DoubleWritable
        return Arrays.asList(new DoubleWritable(result));
    }

    @Override
    public Object map(Object o) {
        return null;
    }

    @Override
    public Object mapSequence(Object o) {
        return null;
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public String outputColumnName() {
        return "";
    }

    @Override
    public String[] outputColumnNames() {
        return new String[0];
    }

    @Override
    public String[] columnNames() {
        return new String[0];
    }

    @Override
    public String columnName() {
        return "";
    }

    @Override
    public Schema transform(Schema schema) {
        return null;
    }
}
