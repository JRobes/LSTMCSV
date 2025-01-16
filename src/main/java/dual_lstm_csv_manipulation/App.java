package dual_lstm_csv_manipulation;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.transform.MathOp;
import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.condition.ConditionOp;
import org.datavec.api.transform.condition.column.CategoricalColumnCondition;
import org.datavec.api.transform.condition.column.DoubleColumnCondition;
import org.datavec.api.transform.filter.ConditionFilter;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.transform.transform.condition.ConditionalReplaceValueTransform;
import org.datavec.api.transform.transform.string.ReplaceStringTransform;
import org.datavec.api.transform.transform.time.DeriveColumnsFromTimeTransform;
import org.datavec.api.transform.transform.time.StringToTimeTransform;
import org.datavec.api.writable.DoubleWritable;
import org.datavec.api.writable.Writable;
import org.datavec.api.split.FileSplit;
import org.datavec.local.transforms.LocalTransformExecutor;
import org.datavec.spark.functions.RecordReaderFunction;
import org.datavec.spark.transform.SparkTransformExecutor;
import org.datavec.spark.transform.misc.StringToWritablesFunction;
import org.datavec.spark.transform.misc.WritablesToStringFunction;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.nd4j.common.io.ClassPathResource;
import org.nd4j.common.io.Resource;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    static String path = "C:\\Users\\jrobes\\IdeaProjects\\LSTMCSV\\ACX-short.csv";

    public static void main( String[] args ) throws IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {
        System.out.println( "Hello World!" );


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
                //Let's remove some column we don't need
                //.removeColumns("CustomerID","MerchantID")
                //.doubleColumnsMathOp("Var", MathOp.Subtract, "Max","Min")

                //Now, suppose we only want to analyze transactions involving merchants in USA or Canada. Let's filter out
                // everthing except for those countries.
                //Here, we are applying a conditional filter. We remove all of the examples that match the condition
                // The condition is "MerchantCountryCode" isn't one of {"USA", "CAN"}
                //.filter(new ConditionFilter(
                //        new CategoricalColumnCondition("MerchantCountryCode", ConditionOp.NotInSet, new HashSet<>(Arrays.asList("USA","CAN")))))

                //Let's suppose our data source isn't perfect, and we have some invalid data: negative dollar amounts that we want to replace with 0.0
                //For positive dollar amounts, we don't want to modify those values
                //Use the ConditionalReplaceValueTransform on the "TransactionAmountUSD" column:
                //.conditionalReplaceValueTransform(
                //        "TransactionAmountUSD",     //Column to operate on
                //        new DoubleWritable(0.0),    //New value to use, when the condition is satisfied
                //        new DoubleColumnCondition("TransactionAmountUSD",ConditionOp.LessThan, 0.0)) //Condition: amount < 0.0

                //Finally, let's suppose we want to parse our date/time column in a format like "2016/01/01 17:50.000"
                //We use JodaTime internally, so formats can be specified as follows: http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html
                //.stringToTimeTransform("DateTimeString","YYYY-MM-DD HH:mm:ss.SSS", DateTimeZone.UTC)

                //However, our time column ("DateTimeString") isn't a String anymore. So let's rename it to something better:
                //.renameColumn("DateTimeString", "DateTime")
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
                .transform(new ReplaceStringTransform("Per", replacements))


                //.transform(new SuffixToDoubleTransform("Vol"))
                //.transform(new PercentToDoubleTransform("Per"))
                //At this point, we have our date/time format stored internally as a long value (Unix/Epoch format): milliseconds since 00:00.000 01/01/1970
                //Suppose we only care about the hour of the day. Let's derive a new column for that, from the DateTime column
                //.transform(new DeriveColumnsFromTimeTransform.Builder("DateTime")
                //        .addIntegerDerivedColumn("HourOfDay", DateTimeFieldType.hourOfDay())
                //        .build())

                //We no longer need our "DateTime" column, as we've extracted what we need from it. So let's remove it
                //.removeColumns("Per")

                //We've finished with the sequence of operations we want to do: let's create the final TransformProcess object
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
            System.out.println(recordReader.next());
        }

        // Paso 5: Aplicar el TransformProcess localmente
        List<List<Writable>> transformedData = LocalTransformExecutor.execute(originalData, tp);

        // Paso 6: Imprimir los datos transformados
        for (List<Writable> record : transformedData) {
            System.out.println(record);
        }

        // Paso 7: Convertir los datos transformados en un DataSetIterator
        int batchSize = 10;
        int labelIndex = 1; // Índice de la columna de etiquetas después de la transformación
        int numClasses = 3;  // Número de clases para la clasificación

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
