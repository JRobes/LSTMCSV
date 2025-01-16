package dual_lstm_csv_manipulation;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.transform.MathOp;
import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.transform.transform.string.ReplaceStringTransform;
import org.datavec.api.writable.Writable;
import org.datavec.api.split.FileSplit;
import org.datavec.local.transforms.LocalTransformExecutor;
import org.joda.time.DateTimeZone;
import org.nd4j.common.io.ClassPathResource;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.io.IOException;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    //static String path = "C:\\Users\\jrobes\\IdeaProjects\\LSTMCSV\\ACX-short.csv";
    static String path = "C:\\Users\\COTERENA\\IdeaProjects\\LSTMCSV\\ACX-short.csv";
    public static void main( String[] args ) throws IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {
        System.out.println( "Hello World!" );
        String customerInfoPath = new ClassPathResource("").getFile().getPath();
        System.out.println(customerInfoPath);


        Path path2 = Paths.get(customerInfoPath);
        Path twoLevelsUp = path2.getParent().getParent(); // Retrocede dos niveles
        System.out.println("Dos niveles arriba: " + twoLevelsUp.toAbsolutePath());

        //CsvPreProcess.processCsv("pre-ACX.csv", "out.csv");
        char quote = '\"';
        char delim = ',';
        char delet = 'r';
        //RecordReader rr = new CSVRecordReader(2, delim, quote);
        //RecordReader rr = new CSVRecordReader(2, ',');

        Schema inputDataSchema = new Schema.Builder()
                //We can define a single column
                .addColumnString("DateTimeString")
                .addColumnString("Last")
                .addColumnString("Open")
                .addColumnString("Max")
                .addColumnString("Min")
                .addColumnsString("Vol", "Per")
                .build();

        System.out.println("Input data schema details:");
        System.out.println(inputDataSchema);

        // Crear el TransformProcess
        Map<String, String> replacements = new HashMap<>();
        replacements.put(",", "."); // Reemplazar ',' por '.'

        TransformProcess tp = new TransformProcess.Builder(inputDataSchema)
                .transform(new ReplaceStringTransform("Last", replacements))
                .convertToDouble("Last")
                .transform(new ReplaceStringTransform("Open", replacements))
                .convertToDouble("Open")
                .transform(new ReplaceStringTransform("Max", replacements))
                .convertToDouble("Max")
                .transform(new ReplaceStringTransform("Min", replacements))
                .convertToDouble("Min")
                .transform(new ReplaceStringTransform("Vol", replacements))
                .transform(new SuffixTransform("Vol"))
                .convertToDouble("Vol")
                .transform(new ReplaceStringTransform("Per", replacements))
                .transform(new PercentTransform("Per"))
                .convertToDouble("Per")
                .doubleColumnsMathOp("Diff", MathOp.Subtract, "Max", "Min")
                .stringToTimeTransform("DateTimeString","DD.MM.YYYY", DateTimeZone.UTC)
                .renameColumn("DateTimeString", "Date")
                .build();
        System.out.println("\n\nAntes de tp.getFinalSchema:");
        Schema outputSchema = tp.getFinalSchema();

        System.out.println("\n\nSchema after transforming data:");
        System.out.println(outputSchema);

        // Paso 3: Leer los datos con CSVRecordReader
        int skipNumLines = 1; // Saltar la primera línea (cabecera)
        RecordReader recordReader = new CSVRecordReader(skipNumLines, ',', '"');
        recordReader.initialize(new FileSplit(new File(path)));

        System.out.println("Original data");
        // Paso 4: Almacenar los registros en una lista
        List<List<Writable>> originalData = new ArrayList<>();
        while (recordReader.hasNext()) {
            originalData.add(recordReader.next());
            //System.out.println(recordReader.next());
        }

        // Paso 5: Aplicar el TransformProcess localmente
        List<List<Writable>> transformedData = LocalTransformExecutor.execute(originalData, tp);

        // Paso 6: Imprimir los datos transformados
        System.out.println("Datos transformados..." + transformedData.size());
        for (List<Writable> record : transformedData) {
            System.out.println(record);
        }

        // Paso 7: Convertir los datos transformados en un DataSetIterator
        //int batchSize = 10;
        //int labelIndex = 1; // Índice de la columna de etiquetas después de la transformación
       // int numClasses = 3;  // Número de clases para la clasificación

        //RecordReader transformedRecordReader = new CSVRecordReader(skipNumLines);
        //transformedRecordReader.initialize(new FileSplit(new File(path)));

        //DataSetIterator iterator = new RecordReaderDataSetIterator(transformedRecordReader, batchSize, labelIndex, numClasses);

        // Paso 8: Normalizar los datos
        //NormalizerStandardize normalizer = new NormalizerStandardize();
        //normalizer.fit(iterator);  // Calcular estadísticas de normalización
        //iterator.setPreProcessor(normalizer);

        // Paso 9: Leer y procesar los datos
        //while (iterator.hasNext()) {
         //   DataSet dataSet = iterator.next();
         //   System.out.println(dataSet);
       // }




        System.out.println("\n\nDONE");

    }
}
