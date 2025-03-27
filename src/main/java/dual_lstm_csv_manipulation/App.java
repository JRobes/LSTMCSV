package dual_lstm_csv_manipulation;

import dual_lstm_csv_manipulation.investing.InvestingTransformData;

import dual_lstm_csv_manipulation.paths.GetSoucePaths;
import dual_lstm_csv_manipulation.paths.IAbsPaths;
import org.datavec.api.writable.Writable;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.ui.model.storage.FileStatsStorage;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *https://stackoverflow.com/questions/61202337/error-labels-and-preoutput-must-have-equal-shapes
 *https://deeplearning4j.konduit.ai/deeplearning4j/reference/recurrent-layers?_ga=2.119259401.1085070398.1742465562-23555796.1732280399
 */
public class App 
{
    private static final Logger log = LoggerFactory.getLogger(App.class);
    static String fileName = "ACX-short.csv";
    //static String fileName = "ACX-2015-2025.csv";
    static String fileNameIBEX = "IBEX35-short.csv";
    //static String path = "C:\\Users\\jrobes\\IdeaProjects\\LSTMCSV\\ACX-short.csv";
    //static String path = "C:\\Users\\COTERENA\\IdeaProjects\\LSTMCSV\\ACX-short.csv";
    static double percentOfTraining = 0.7;
    //LONGITUD DE SECUENCIA
    static int sequenceLength = 3;
    //INDICA CUAL ES LA COLUMNA QUE TIENE EL LABEL (EMPIEZA POR 0)
    static int columnOfLabels = 0;
    //EL NUMERO DE FEATURES ESTÁ DEFINIDO POR EL MNUMERO DE COLUMNAS DEL ARCHIVO DE ENTRADA
    public static void main( String[] args ) throws IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {

        System.out.println( "Hello World!" );
        String[] sourcePaths = new String[]{fileName, fileNameIBEX};

        IAbsPaths absPaths = new GetSoucePaths(sourcePaths);
        String[] absolutePaths = absPaths.getAbsPaths();

        IDataPreparation itd = new InvestingTransformData(1, absolutePaths);
        List<List<Writable>> joinedData = itd.getDataAsWritable();

        System.out.println();
        System.out.println();
        //System.out.println("Datos Unidos...\nNúmero de filas: " + joinedData.size());
        List<String[]> data = new ArrayList<>();
        for (List<Writable> record : joinedData) {
            Object[] ee = record.toArray();
            String[] stringArray = Arrays.stream(ee)
                    .map(Object::toString) // Convierte cada elemento a String
                    .toArray(String[]::new);
            data.add(stringArray);
        }


        //REVERTIR LOS DATOS,
        Collections.reverse(data);

        generateFeaturesAndLabelfile(absolutePaths[0], data);
        //System.out.println("DATA NUM DE FEATURES EN ARCHIVO: " + data.get(0).length);
        int numFeatures = data.get(0).length;
        int numLabels = 1;
        int foreseenDays = 1;


        int numSamples = data.size();

        int numberOfTrainingItems = (int)Math.round(percentOfTraining * joinedData.size());
        List<String[]> trainingData = data.subList(0, numberOfTrainingItems);
        List<String[]> testData = data.subList(numberOfTrainingItems -  sequenceLength, data.size());

        System.out.println("-------------------------------------------");
        System.out.println("Número total de filas:\t\t\t\t" + joinedData.size());
        System.out.println("% de Training:\t\t\t\t\t\t" + percentOfTraining);
        System.out.println("Numero de Items de entrenamiento:\t" + numberOfTrainingItems);
        System.out.println("Numero de Items de test:\t\t\t" + (joinedData.size() - numberOfTrainingItems));
        System.out.println("Numero de training Items:\t\t\t" + trainingData.size());
        System.out.println("Numero de test Items:\t\t\t\t" + testData.size());
        System.out.println("-------------------------------------------");

        System.out.println("@@@@@@@@@@@@@@@@ Data entero @@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        //for(String[] e : data){
        //    System.out.println(Arrays.toString(e));
        //}

        System.out.println("@@@@@@@@@@@@@@@@ Training data @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        for(String[] e : trainingData){
            System.out.println(Arrays.toString(e));
        }
        System.out.println("@@@@@@@@@@@@@@@@ Test data @@@@@@@@@@@@@@@@@@@@@@@@@@@");
        for(String[] e : testData){
            System.out.println(Arrays.toString(e));
        }



        System.out.println("@@@@@@@@@@@@@@@@ GET FEATURES AND DATA  - TRAIN @@@@@@@@@@@@@@@@@@@@@@@@@@@");

        DataSet trainDataSet = getFeaturesAndLabels4(trainingData, columnOfLabels, sequenceLength);
        System.out.println("@@@@@@@@@@@@@@@@ GET FEATURES AND DATA  - TEST @@@@@@@@@@@@@@@@@@@@@@@@@@@");
        DataSet testDataSet  = getFeaturesAndLabels4(testData,     columnOfLabels, sequenceLength);
        NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler();
        normalizer.fit(trainDataSet);           //Collect the statistics (mean/stdev) from the training data. This does not modify the input data
        normalizer.transform(trainDataSet);     //Apply normalization to the training data
        normalizer.transform(testDataSet);      //Apply normalization to the test data. This is using statistics calculated from the *training* set




        /*

        NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler();
        normalizer.fit(trainingDataSet); //Collect the statistics (mean/stdev) from the training data. This does not modify the input data
        for (DataSetIterator iter = trainingDataSet; iter.hasNext(); ) {
            DataSet it = iter.next();
            normalizer.transform(it);
        }
        DataSetIterator testDataSet = getFeaturesAndLabels3(testData, columnOfLabels, sequenceLength);
        for (DataSetIterator iter = testDataSet; iter.hasNext(); ) {
            DataSet it = iter.next();
            normalizer.transform(it);
        }
        System.out.println("#####################");
        System.out.println(testDataSet);
*/





        /*

        //INDArrays
        INDArray[] trainingFeaturesAndLabels = getFeaturesAndLabels(trainingData, sequenceLength, numFeatures);
        INDArray[] testFeaturesAndLabels = getFeaturesAndLabels(testData, sequenceLength, numFeatures);
        System.out.println("-----------------------------------------------------");


        DataSet trainDataSet = new DataSet(trainingFeaturesAndLabels[0],trainingFeaturesAndLabels[1]);
        DataSet testDataSet = new DataSet(testFeaturesAndLabels[0], testFeaturesAndLabels[1]);

        System.out.println("Train dataSet features:");
        System.out.println(trainDataSet.getFeatures());
        System.out.println("Train dataSet labels:");
        System.out.println(trainDataSet.getLabels());

        System.out.println("Test dataSet features:");
        //System.out.println(testDataSet.getFeatures());

        NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler();
        normalizer.fit(trainDataSet);           //Collect the statistics (mean/stdev) from the training data. This does not modify the input data
        normalizer.transform(trainDataSet);     //Apply normalization to the training data
        normalizer.transform(testDataSet);      //Apply normalization to the test data. This is using statistics calculated from the *training* set

        System.out.println("Test dataSet features normalized:");
        //System.out.println(testDataSet.getFeatures());

        System.out.println("Train dataSet features normalized:");
        //System.out.println(trainDataSet.getFeatures());

        */










        //UIServer uiServer = UIServer.getInstance();

        //UIServer uiServer = UIServer.getInstance();
        //StatsStorage statsStorage = new InMemoryStatsStorage();
        StatsStorage statsStorage = new FileStatsStorage(new File(System.getProperty("java.io.tmpdir"), "ui-stats.dl4j.33"));

        //LSTMModel model = new LSTMModel();
        MultiLayerNetwork network = LSTMModel.buildModel(numFeatures, 128, 64, 0, statsStorage);
        //uiServer.attach(statsStorage);
        for (int epoch = 0; epoch < 100; epoch++) {
            if(epoch%10 == 0 )
                System.out.println("epoch: " + epoch);
            network.fit(trainDataSet);
        }

        INDArray testPredicted = network.output(testDataSet.getFeatures());
        RegressionEvaluation regEval = new RegressionEvaluation(1);
        // Evaluar
        regEval.eval(testDataSet.getLabels(), testPredicted);
        System.out.println("$$$$$$$$$$$$$$$$$$$ PREDICCIONES $$$$$$$$$$$$$$$$$$$");
        long[] shape = testPredicted.shape();
        for(long shapel : shape){
            System.out.println(shapel);
        }
        System.out.println(testPredicted);







        /*
        double xMin = -15;
        double xMax = 15;
        double yMin = -15;
        double yMax = 15;
//Let's evaluate the predictions at every point in the x/y input space, and
//plot this in the background
        int nPointsPerAxis = 40;
        double[][] evalPoints = new double[nPointsPerAxis*nPointsPerAxis][2];
        int count = 0;
        for( int i=0; i<nPointsPerAxis; i++ ){
            for( int j=0; j<nPointsPerAxis; j++ ){
                double x = i * (xMax-xMin)/(nPointsPerAxis-1) + xMin;
                double y = j * (yMax-yMin)/(nPointsPerAxis-1) + yMin;
                evalPoints[count][0] = x;
                evalPoints[count][1] = y;
                count++;
            }
        }


        INDArray allXYPoints = Nd4j.create(evalPoints);
        //INDArray predictionsAtXYPoints = network.output(allXYPoints);

// Mostrar estadísticas
        System.out.println(regEval.stats());
        PlotUtil.plotTestData(testDataSet.getFeatures(), testDataSet.getLabels(), testPredicted, allXYPoints, testPredicted, nPointsPerAxis);
*/
        System.out.println("\n\nDONE");

    }

    private static void generateFeaturesAndLabelfile(String path, List<String[]> data) {
        //Esto no se si valdrá para algo
    }

    // El número de features se saca del numero de elemento del String[]
    // La longitud total se saca del size de la Lista
    // Hay que indicar qué columna tiene los label
    private static DataSet getFeaturesAndLabels3(List<String[]> data, int columnOfLabels, int sequenceLength){
        System.out.println("Numero de strings en array data (features): " + data.get(0).length);
        System.out.println("Número de elementos (sequence length): " + data.size());
        int dataSize = data.size();
        int numFeatures = data.get(0).length;
        int numSamples = dataSize - sequenceLength;
        // Paso 1: Separar features y labels
        double[][] features = new double[dataSize][numFeatures];
        double[] labels = new double[dataSize];
        for (int i = 0; i < dataSize; i++) {
            for (int j = 0; j < numFeatures; j++) {
                features[i][j] = Double.parseDouble(data.get(i)[j]);
            }
            labels[i] = Double.parseDouble(data.get(i)[columnOfLabels]);
        }
        INDArray indArrayFeatures = Nd4j.create(features);
        INDArray indArrayLabels = Nd4j.create(labels);

        // Matrices para almacenar todas las secuencias y etiquetas
        //INDArray allInputs = Nd4j.create(numSamples, numFeatures, sequenceLength); // Forma: (numSamples, sequenceLength, numFeatures)
        //INDArray allOutputs = Nd4j.create(1, 1); // Forma: (numSamples, 1)
        double[] labelsNew = new double[numSamples];
        INDArray[] transSubMatrix = new INDArray[numSamples];
        for(int i = 0; i < numSamples; i++){
            INDArray subMatrix = indArrayFeatures.get(NDArrayIndex.interval(i, sequenceLength + i), NDArrayIndex.all());
            transSubMatrix[i] = subMatrix.transpose();
            System.out.println(transSubMatrix[i]);
            System.out.println("-----------------------------------------------------");
            labelsNew[i] = labels[sequenceLength + i];
            System.out.println(labelsNew[i]);
            System.out.println("-----------------------------------------------------");

        }
        INDArray allInputs = Nd4j.stack(0, transSubMatrix);

        INDArray labels2 = Nd4j.create(labelsNew, new int[]{numSamples, 1, 1});


        // Paso 1: Concatenar los INDArray de rank 1 en un solo INDArray de rank 2

        // Paso 2: Redimensionar a la forma (lengthOfArray, 1, 1)

        // Imprimir el resultado
        System.out.println("Resultado:");
       // allOutputs = Nd4j.stack(0, transLabelMatrix);

/*
        for (int i = sequenceLength; i < dataSize; i++) {
            // Crear secuencia de features
            INDArray input = Nd4j.create(sequenceLength, numFeatures);
            for (int j = 0; j < sequenceLength; j++) {
                input.putRow(j, Nd4j.create(features[i - sequenceLength + j]));
            }

            // Crear label (el siguiente valor del label)
            INDArray output = Nd4j.create(new double[]{labels[i]});
            // Agregar la secuencia y la etiqueta a las matrices grandes
            allInputs.putSlice(i - sequenceLength, input); // Agregar la secuencia
            allOutputs.putScalar(i - sequenceLength, output.getDouble(0)); // Agregar la etiqueta
            System.out.println(input);
            System.out.println("---------------------------------");
            System.out.println(output);
            System.out.println("=================================");
        }
*/
        DataSet dataSet = new DataSet(allInputs, labels2);
        // Ahora tienes un solo DataSet que puedes usar para entrenar tu red LSTM
        System.out.println("DataSet Input shape: " + Arrays.toString(dataSet.getFeatures().shape()));
        System.out.println("DataSet Output shape: " + Arrays.toString(dataSet.getLabels().shape()));

        //System.out.println(allInputs);
        return dataSet;

    }

    // El número de features se saca del numero de elemento del String[]
    // La longitud total se saca del size de la Lista
    // Hay que indicar qué columna tiene los label
    private static DataSet getFeaturesAndLabels4(List<String[]> data, int columnOfLabels, int sequenceLength){
        System.out.println("Numero de strings en array data (features): " + data.get(0).length);
        System.out.println("Número de elementos (sequence length): " + data.size());
        int dataSize = data.size();
        int numFeatures = data.get(0).length;
        // Se quita un dato, ya que una de las filas solo sirve para obtener un label
        // Normalmente numSamples = dataSize - sequenceLength + 1
        int numSamples = dataSize - sequenceLength;
        // Paso 1: Separar features y labels
        double[][] features = new double[dataSize][numFeatures];
        double[] labels = new double[dataSize];
        for (int i = 0; i < dataSize; i++) {
            for (int j = 0; j < numFeatures; j++) {
                features[i][j] = Double.parseDouble(data.get(i)[j]);
            }
            labels[i] = Double.parseDouble(data.get(i)[columnOfLabels]);
        }
        INDArray indArrayFeatures = Nd4j.create(features);
        INDArray indArrayLabels = Nd4j.create(labels);

        // Matrices para almacenar todas las secuencias y etiquetas
        //INDArray allInputs = Nd4j.create(numSamples, numFeatures, sequenceLength); // Forma: (numSamples, sequenceLength, numFeatures)
        //INDArray allOutputs = Nd4j.create(1, 1); // Forma: (numSamples, 1)
        double[] labelsNew = new double[numSamples];
        INDArray[] transSubMatrix = new INDArray[numSamples];
        for(int i = 0; i < numSamples; i++){
            INDArray subMatrix = indArrayFeatures.get(NDArrayIndex.interval(i, sequenceLength + i), NDArrayIndex.all());
            transSubMatrix[i] = subMatrix;
            System.out.println(transSubMatrix[i]);
            System.out.println("-----------------------------------------------------");
            labelsNew[i] = labels[sequenceLength + i];
            System.out.println(labelsNew[i]);
            System.out.println("-----------------------------------------------------");

        }
        INDArray allInputs = Nd4j.stack(2, transSubMatrix);
        System.out.println("All inputs...rank: " + allInputs.rank());
        System.out.println(allInputs);

        INDArray labels2 = Nd4j.create(labelsNew, new int[]{numSamples, 1});


        // Paso 1: Concatenar los INDArray de rank 1 en un solo INDArray de rank 2

        // Paso 2: Redimensionar a la forma (lengthOfArray, 1, 1)

        // Imprimir el resultado
        System.out.println("Resultado:");
        // allOutputs = Nd4j.stack(0, transLabelMatrix);

/*
        for (int i = sequenceLength; i < dataSize; i++) {
            // Crear secuencia de features
            INDArray input = Nd4j.create(sequenceLength, numFeatures);
            for (int j = 0; j < sequenceLength; j++) {
                input.putRow(j, Nd4j.create(features[i - sequenceLength + j]));
            }

            // Crear label (el siguiente valor del label)
            INDArray output = Nd4j.create(new double[]{labels[i]});
            // Agregar la secuencia y la etiqueta a las matrices grandes
            allInputs.putSlice(i - sequenceLength, input); // Agregar la secuencia
            allOutputs.putScalar(i - sequenceLength, output.getDouble(0)); // Agregar la etiqueta
            System.out.println(input);
            System.out.println("---------------------------------");
            System.out.println(output);
            System.out.println("=================================");
        }
*/
        DataSet dataSet = new DataSet(allInputs, labels2);
        // Ahora tienes un solo DataSet que puedes usar para entrenar tu red LSTM
        System.out.println("DataSet Input shape: " + Arrays.toString(dataSet.getFeatures().shape()));
        System.out.println("DataSet Output shape: " + Arrays.toString(dataSet.getLabels().shape()));

        //System.out.println(allInputs);
        return dataSet;

    }

    private static INDArray[] getFeaturesAndLabels(List<String[]> data, int sequenceLength, int numFeatures) {
        // TRAIN DATA
        // Inicializar arrays para features y labels
        int numLabels = 1;
        System.out.println("Num features: " + numFeatures);
        System.out.println("Numero de strings en array data: " + data.get(0).length);
        int numSequences = data.size() - sequenceLength; // 100 - 5 + 1 = 96
        double[][][] features   = new double[numSequences][numFeatures][sequenceLength];
        double[][][] labels     = new double[numSequences][numLabels][sequenceLength];

        // Procesar los datos
        for (int i = 0; i < numSequences; i++) {
            for (int j = 0; j < sequenceLength; j++) {
                for(int k = 0; k < numFeatures; k++){
                    features[i][k][j] = Double.parseDouble(data.get(i + j)[k]);
                }
            }
            // Leer el target (primera columna, que es el target)
            // labels[i][0] = Double.parseDouble(data.get(i + sequenceLength)[0]);
        }
        // Procesar los datos
        for (int i = 0; i < numSequences; i++) {
            for (int j = 0; j < sequenceLength; j++) {
                //int numLabels = 1;
                for(int k = 0; k < numLabels; k++){
                    //features[i][k][j] = Double.parseDouble(data.get(i + j)[k]);
                    labels[i][k][j] = Double.parseDouble(data.get(i + j)[k]);
                }
            }
            // Leer el target (primera columna, que es el target)
            //labels[i][0] = Double.parseDouble(data.get(i + sequenceLength)[0]);
        }



        INDArray featureArray = Nd4j.create(features);
        INDArray labelArray = Nd4j.create(labels);
        //System.out.println("@@@@@@ INDArray features @@@@@@@@@: \n" + featureArray); // [numSamples - sequenceLength, sequenceLength, numFeatures]
        //System.out.println("Forma de labels: " + Arrays.toString(labelArray.shape()));
        //System.out.println("@@@@@@ INDArray labels @@@@@@@@@@@: \n" + labelArray);
        return new INDArray[]{featureArray, labelArray};
    }


    private static INDArray[] getFeaturesAndLabels2(List<String[]> data, int sequenceLength, int numFeatures) {
        // TRAIN DATA
        // Inicializar arrays para features y labels
        int numLabels = 1;
        int sequenceLengthLabels = 1;
        System.out.println("Num features: " + numFeatures);
        System.out.println("Numero de strings en array data: " + data.get(0).length);
        int numSequences = data.size() - sequenceLength; // 100 - 5 + 1 = 96
        double[][][] features   = new double[numSequences][numFeatures][sequenceLength];
        double[][][] labels     = new double[numSequences][numLabels][sequenceLengthLabels];

        // Procesar los datos
        for (int i = 0; i < numSequences; i++) {
            for (int j = 0; j < sequenceLength; j++) {
                for(int k = 0; k < numFeatures; k++){
                    features[i][k][j] = Double.parseDouble(data.get(i + j)[k]);
                }
            }
            // Leer el target (primera columna, que es el target)
            // labels[i][0] = Double.parseDouble(data.get(i + sequenceLength)[0]);
        }
        // Procesar los datos
        for (int i = 0; i < numSequences; i++) {
            for (int j = 0; j < sequenceLengthLabels; j++) {
                //int numLabels = 1;
                for(int k = 0; k < numLabels; k++){
                    //features[i][k][j] = Double.parseDouble(data.get(i + j)[k]);
                    labels[i][k][j] = Double.parseDouble(data.get(i + j)[k]);
                }
            }
            // Leer el target (primera columna, que es el target)
            //labels[i][0] = Double.parseDouble(data.get(i + sequenceLength)[0]);
        }



        INDArray featureArray = Nd4j.create(features);
        INDArray labelArray = Nd4j.create(labels);
        //System.out.println("@@@@@@ INDArray features @@@@@@@@@: \n" + featureArray); // [numSamples - sequenceLength, sequenceLength, numFeatures]
        //System.out.println("Forma de labels: " + Arrays.toString(labelArray.shape()));
        //System.out.println("@@@@@@ INDArray labels @@@@@@@@@@@: \n" + labelArray);
        return new INDArray[]{featureArray, labelArray};
    }
}
