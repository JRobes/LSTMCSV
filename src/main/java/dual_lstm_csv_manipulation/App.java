package dual_lstm_csv_manipulation;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.collection.CollectionRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.transform.MathOp;
import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.analysis.DataAnalysis;
import org.datavec.api.transform.analysis.columns.ColumnAnalysis;
import org.datavec.api.transform.condition.column.NullWritableColumnCondition;
import org.datavec.api.transform.filter.FilterInvalidValues;
import org.datavec.api.transform.join.Join;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.transform.transform.string.ReplaceStringTransform;
import org.datavec.api.writable.LongWritable;
import org.datavec.api.writable.Writable;
import org.datavec.api.split.FileSplit;
import org.datavec.local.transforms.AnalyzeLocal;
import org.datavec.local.transforms.LocalTransformExecutor;
import org.joda.time.DateTimeZone;
import org.nd4j.common.io.ClassPathResource;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */
public class App 
{
    private static Logger log = LoggerFactory.getLogger(App.class);
    static String fileName = "ACX-short.csv";
    static String fileNameIBEX = "IBEX35-short.csv";
    //static String path = "C:\\Users\\jrobes\\IdeaProjects\\LSTMCSV\\ACX-short.csv";
    //static String path = "C:\\Users\\COTERENA\\IdeaProjects\\LSTMCSV\\ACX-short.csv";
    public static void main( String[] args ) throws IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {

        System.out.println( "Hello World!" );
        log.warn("Hello from the logger");
        String classPath = new ClassPathResource("").getFile().getPath();
        //System.out.println(classPath);

        //Path path2 = Paths.get(classPath);
        Path path2LevelsUp = Paths.get(classPath).getParent().getParent(); // Retrocede dos niveles

        System.out.println("Dos niveles arriba: " + path2LevelsUp.toAbsolutePath() + File.separator + fileName);
        String path = path2LevelsUp.toAbsolutePath() + File.separator + fileName;
        String pathIBEX = path2LevelsUp.toAbsolutePath() + File.separator + fileNameIBEX;

        Schema inputDataSchema = new Schema.Builder()
                .addColumnString("DateTimeString", ".*", 1, null)
                .addColumnString("Last", ".*", 1, null)
                .addColumnString("Open", ".*", 1, null)
                .addColumnString("Max", ".*", 1, null)
                .addColumnString("Min", ".*", 1, null)
                .addColumnString("Vol", ".*", 1, null)
                .addColumnString("Per", ".*", 1, null)
                .build();

        System.out.println("Input data schema details:");
        //System.out.println(inputDataSchema);

        // Crear el TransformProcess
        //Map<String, String> replacements = new HashMap<>();
        //replacements.put(",", "."); // Reemplazar ',' por '.'

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


        Schema outputSchema = tp.getFinalSchema();
        Schema outputSchemaIBEX = tpIBEX.getFinalSchema();

        System.out.println("\n\nSchemas after transforming data:");
        //System.out.println(outputSchema);
        //System.out.println(outputSchemaIBEX);

        // Paso 3: Leer los datos con CSVRecordReader
        int skipNumLines = 1; // Saltar la primera línea (cabecera)
        RecordReader recordReader = new CSVRecordReader(skipNumLines, ',', '"');
        recordReader.initialize(new FileSplit(new File(path)));

        RecordReader recordReaderIBEX = new CSVRecordReader(skipNumLines, ',', '"');
        recordReaderIBEX.initialize(new FileSplit(new File(pathIBEX)));

        System.out.println("Original data");
        // Paso 4: Almacenar los registros en una lista
        List<List<Writable>> originalData = new ArrayList<>();
        while (recordReader.hasNext()) {
            originalData.add(recordReader.next());
        }

        List<List<Writable>> originalDataIBEX = new ArrayList<>();
        while (recordReaderIBEX.hasNext()) {
            originalDataIBEX.add(recordReaderIBEX.next());
        }

        // Definir la join
        Join join = new Join.Builder(Join.JoinType.Inner)
                .setJoinColumns("Date")
                .setSchemas(outputSchema, outputSchemaIBEX)
                .build();


        // Paso 5: Aplicar el TransformProcess localmente
        List<List<Writable>> transformedData = LocalTransformExecutor.execute(originalData, tp);
        List<List<Writable>> transformedDataIBEX = LocalTransformExecutor.execute(originalDataIBEX, tpIBEX);


        // Paso 6: Imprimir los datos transformados
        System.out.println("Datos transformados...\nNúmero de filas: " + transformedData.size());
        for (List<Writable> record : transformedData) {
            System.out.println(record);
        }
        System.out.println();
        System.out.println();
        System.out.println("Datos transformados IBEX...\nNúmero de filas: " + transformedDataIBEX.size());
        for (List<Writable> record : transformedDataIBEX) {
            System.out.println(record);
        }






        //List<List<Writable>> joinedData = LocalTransformExecutor.executeJoin(join, transformedData, transformedDataIBEX);
        List<List<Writable>> joinedData = new ArrayList<>();

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

        System.out.println();
        System.out.println();
        System.out.println("Datos Unidos...\nNúmero de filas: " + joinedData.size());
        for (List<Writable> record : joinedData) {
            System.out.println(record);
        }
        int numFeatures = 5;
        int numLabels = 1;

        double[][] featureMatrix = new double[joinedData.size()][numFeatures];
        double[][] labelMatrix = new double[transformedData.size()][numLabels];

        for (int rowIndex = 0; rowIndex < joinedData.size(); rowIndex++) {
            List<Writable> row = joinedData.get(rowIndex);

            // Extraer características
            for (int colIndex = 0; colIndex < numFeatures; colIndex++) {
                featureMatrix[rowIndex][colIndex] = row.get(colIndex).toDouble();
            }
            // Extraer etiquetas
            for (int colIndex = 0; colIndex < numLabels; colIndex++) {
                labelMatrix[rowIndex][colIndex] = row.get(numFeatures + colIndex).toDouble();
            }

        }

        INDArray featureArray = Nd4j.create(featureMatrix);
        INDArray labelArray = Nd4j.create(labelMatrix);


        //INDArray featureArray = Nd4j.create(joinedData);
        DataSet dataSet = new DataSet(featureArray, labelArray);

        // Imprimir el DataSet
        System.out.println("Features:");
        System.out.println(dataSet.getFeatures());
        System.out.println("Labels:");
        System.out.println(dataSet.getLabels());


        //NormalizerMinMaxScaler preProcessor = new NormalizerMinMaxScaler();
        //preProcessor.fit(dataSet);




/*

        Collections.sort(transformedData, new Comparator<List<Writable>>() {
            @Override
            public int compare(List<Writable> o1, List<Writable> o2) {
                Writable date1 = o1.get(0);
                Writable date2 = o2.get(0);

                Long n1 = (Long)date1.toLong();
                Long n2 = (Long)date2.toLong();
                return n1.compareTo(n2);

            }

        });
*/

/*
        // Analizar los datos
        RecordReader recordReaderNew = new CollectionRecordReader(transformedData);
        DataAnalysis dataAnalysis = AnalyzeLocal.analyze(outputSchema, recordReaderNew);
        System.out.println("Análisis del conjunto de datos:");
        System.out.println(dataAnalysis);
        ColumnAnalysis salaryAnalysis = dataAnalysis.getColumnAnalysis("Diff");
        System.out.println("Análisis de la columna 'Diff':");
        System.out.println(salaryAnalysis);

*/

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

    public static Map<Writable, List<Writable>> convertList2Map(List<List<Writable>> list) {
        Map<Writable, List<Writable>> map = new HashMap<>();
        for (List<Writable> record : list) {
            map.put(record.get(0), record);
        }
        return map;
    }
}
