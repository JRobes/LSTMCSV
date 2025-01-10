package dual_lstm_csv_manipulation;

import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.transform.transform.BaseTransform;
import org.datavec.api.writable.DoubleWritable;
import org.datavec.api.writable.Text;
import org.datavec.api.writable.Writable;
import org.datavec.api.records.reader.impl.collection.CollectionRecordReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataVecCustomTransformExample {
    public static void main(String[] args) {
        // 1. Definir el esquema de entrada
        Schema inputSchema = new Schema.Builder()
                .addColumnString("number_with_suffix") // Columna original como String
                .build();

        // 2. Crear el proceso de transformación con una transformación personalizada
        TransformProcess transformProcess = new TransformProcess.Builder(inputSchema)
                .transform(new SuffixToDoubleTransform("number_with_suffix"))
                .build();

        // Ejemplo de datos de entrada
        List<List<Writable>> inputData = new ArrayList<>();
        inputData.add(Arrays.asList(new Text("12.5K")));
        inputData.add(Arrays.asList(new Text("3.4M")));
        inputData.add(Arrays.asList(new Text("100")));
        inputData.add(Arrays.asList(new Text("45.8K")));
        inputData.add(Arrays.asList(new Text("1.2M")));

        // 3. Aplicar la transformación a los datos de entrada
        CollectionRecordReader recordReader = new CollectionRecordReader(inputData);
        List<List<Writable>> outputData = new ArrayList<>();

        while (recordReader.hasNext()) {
            List<Writable> record = recordReader.next();
            List<Writable> transformedRecord = transformProcess.execute(record);
            outputData.add(transformedRecord);
        }

        // 4. Mostrar los resultados transformados
        for (List<Writable> record : outputData) {
            System.out.println(record.get(0)); // Mostrar el valor transformado
        }
    }

    // Clase de transformación personalizada que convierte un String con sufijo a Double escalado
    public static class SuffixToDoubleTransform extends BaseTransform {

        private final String columnName;

        public SuffixToDoubleTransform(String columnName) {
            this.columnName = columnName;
        }

        @Override
        public List<Writable> map(List<Writable> writables) {
            // Obtener el valor del String de la columna
            String value = writables.get(0).toString().trim();
            double result;

            if (value.endsWith("K")) {
                result = Double.parseDouble(value.substring(0, value.length() - 1)) * 1000;
            } else if (value.endsWith("M")) {
                result = Double.parseDouble(value.substring(0, value.length() - 1)) * 1000000;
            } else {
                result = Double.parseDouble(value); // Sin sufijo, se convierte directamente a Double
            }

            // Devolver el resultado como DoubleWritable
            return Arrays.asList(new DoubleWritable(result));
        }

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
}

