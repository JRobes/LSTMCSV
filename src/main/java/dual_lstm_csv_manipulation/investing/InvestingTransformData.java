package dual_lstm_csv_manipulation.investing;

import dual_lstm_csv_manipulation.IDataPreparation;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.transform.MathOp;
import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.condition.column.NullWritableColumnCondition;
import org.datavec.api.transform.filter.FilterInvalidValues;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.writable.Writable;
import org.datavec.local.transforms.LocalTransformExecutor;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InvestingTransformData implements IDataPreparation {
    private final Schema inputDataSchema;
    private String[] paths;
    private int typeOfTransform = 0;
    public InvestingTransformData(int typeOfTransform, String...paths){
        this.typeOfTransform = typeOfTransform;
        this.paths = paths;
        this.inputDataSchema = new Schema.Builder()
                .addColumnString("DateTimeString", ".*", 1, null)
                .addColumnString("Last", ".*", 1, null)
                .addColumnString("Open", ".*", 1, null)
                .addColumnString("Max", ".*", 1, null)
                .addColumnString("Min", ".*", 1, null)
                .addColumnString("Vol", ".*", 1, null)
                .addColumnString("Per", ".*", 1, null)
                .build();

    }

    public List<List<Writable>> transform01()  {
        List<List<Writable>> joinedData = new ArrayList<>();

        TransformProcess tp = new TransformProcess.Builder(inputDataSchema)
                .filter(new FilterInvalidValues("DateTimeString", "Last", "Open", "Max","Min", "Vol","Per"))
                .transform(new InvestingNumberTransform("Last"))
                .convertToDouble("Last")
                .transform(new InvestingNumberTransform("Open"))
                .convertToDouble("Open")
                .transform(new InvestingNumberTransform("Max"))
                .convertToDouble("Max")
                .transform(new InvestingNumberTransform("Min"))
                .convertToDouble("Min")
                .transform(new InvestingNumberTransform("Vol"))
                .convertToDouble("Vol")
                .transform(new PercentTransform("Per"))
                .convertToDouble("Per")
                .doubleColumnsMathOp("Diff", MathOp.Subtract, "Max", "Min")
                .stringToTimeTransform("DateTimeString","DD.MM.YYYY", DateTimeZone.UTC)
                .renameColumn("DateTimeString", "Date")
                .removeColumns("Open", "Max", "Min")
                .filter(new NullWritableColumnCondition("Last"))
                .build();

        TransformProcess tpIBEX = new TransformProcess.Builder(inputDataSchema)
                .removeColumns("Open", "Max","Min", "Vol","Per")
                .filter(new FilterInvalidValues("DateTimeString", "Last"))
                .transform(new InvestingNumberTransform("Last"))
                .convertToDouble("Last")
                .stringToTimeTransform("DateTimeString","DD.MM.YYYY", DateTimeZone.UTC)
                .renameColumn("DateTimeString", "Date")
                .build();

        try{
            int skipNumLines = 1; // Saltar la primera línea (cabecera)
            RecordReader recordReader = new CSVRecordReader(skipNumLines, ',', '"');
            recordReader.initialize(new FileSplit(new File(paths[0])));

            RecordReader recordReaderIBEX = new CSVRecordReader(skipNumLines, ',', '"');
            recordReaderIBEX.initialize(new FileSplit(new File(paths[1])));
            // Paso 4: Almacenar los registros en una lista
            List<List<Writable>> originalData = new ArrayList<>();
            while (recordReader.hasNext()) {
                originalData.add(recordReader.next());
            }
            List<List<Writable>> originalDataIBEX = new ArrayList<>();
            while (recordReaderIBEX.hasNext()) {
                originalDataIBEX.add(recordReaderIBEX.next());
            }
            // Paso 5: Aplicar el TransformProcess localmente
            List<List<Writable>> transformedData = LocalTransformExecutor.execute(originalData, tp);
            List<List<Writable>> transformedDataIBEX = LocalTransformExecutor.execute(originalDataIBEX, tpIBEX);


            //List<List<Writable>> joinedData = new ArrayList<>();
            for (List<Writable> leftRow : transformedData) {
                for (List<Writable> rightRow : transformedDataIBEX) {
                    if (leftRow.get(0).toString().equals(rightRow.get(0).toString())) { // Comparar la columna "id"
                        List<Writable> joinedRow = new ArrayList<>(leftRow);
                        joinedRow.addAll(rightRow.subList(1, rightRow.size())); // Excluir la columna "id" duplicada
                        joinedData.add(joinedRow);
                    }
                }
            }



        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }

        //SE REVIERTEN LOS DATOS
        //Collections.reverse(joinedData);

        return joinedData;

    }

    @Override
    public List<List<Writable>> getDataAsWritable() {
        if(typeOfTransform != 0){
            System.out.println("InvestingTransformData.getDataAsWritable()...No implementada la transformación de los datos. ");
            return null;
        }
        return transform01();
    }
}
