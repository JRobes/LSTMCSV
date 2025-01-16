package dual_lstm_csv_manipulation;

import org.datavec.api.transform.schema.Schema;
import org.datavec.api.transform.transform.BaseTransform;
import org.datavec.api.writable.DoubleWritable;
import org.datavec.api.writable.Text;
import org.datavec.api.writable.Writable;

import java.util.Arrays;
import java.util.List;

public class SuffixTransform extends BaseTransform {

    private final String columnName;

    public SuffixTransform(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public List<Writable> map(List<Writable> writables) {
        // Obtener el valor del String de la columna
        String value = writables.get(0).toString().trim();
        String result;
        Double result1;

        if (value.endsWith("K")) {
            result1 = Double.parseDouble(value.substring(0, value.length() - 1)) * 1000;
            result = String.valueOf(result1);
        } else if (value.endsWith("M")) {
            result1 = Double.parseDouble(value.substring(0, value.length() - 1).replace(',','.')) * 1000000;
            result = String.valueOf(result1);
        } else {
            result1 = Double.parseDouble(value.replace(',','.')); // Sin sufijo, se convierte directamente a Double
            result = String.valueOf(result1);
        }

        // Devolver el resultado como DoubleWritable
        return Arrays.asList(new Text(result));
    }
//new DouWritable(result)
    @Override
    public Object map(Object input) {
        throw new UnsupportedOperationException("Single object mapping not supported for this transform.");
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