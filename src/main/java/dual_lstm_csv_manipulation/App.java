package dual_lstm_csv_manipulation;

import dual_lstm_csv_manipulation.investing.InvestingTransformData;

import dual_lstm_csv_manipulation.paths.GetSoucePaths;
import dual_lstm_csv_manipulation.paths.IAbsPaths;
import org.datavec.api.writable.Writable;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.ui.api.UIServer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final Logger log = LoggerFactory.getLogger(App.class);
    //static String fileName = "ACX-short.csv";
    static String fileName = "ACX-2015-2025.csv";
    static String fileNameIBEX = "IBEX35-short.csv";
    //static String path = "C:\\Users\\jrobes\\IdeaProjects\\LSTMCSV\\ACX-short.csv";
    //static String path = "C:\\Users\\COTERENA\\IdeaProjects\\LSTMCSV\\ACX-short.csv";
    public static void main( String[] args ) throws IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {

        System.out.println( "Hello World!" );
        String[] sourcePaths = new String[]{fileName, fileNameIBEX};

        IAbsPaths absPaths = new GetSoucePaths(sourcePaths);
        String[] absolutePaths = absPaths.getAbsPaths();

        IDataPreparation itd = new InvestingTransformData(1, absolutePaths);
        List<List<Writable>> joinedData = itd.getDataAsWritable();

        System.out.println();
        System.out.println();
        System.out.println("Datos Unidos...\nNúmero de filas: " + joinedData.size());
        List<String[]> data = new ArrayList<>();
        for (List<Writable> record : joinedData) {
            Object[] ee = record.toArray();
            String[] stringArray = Arrays.stream(ee)
                    .map(Object::toString) // Convierte cada elemento a String
                    .toArray(String[]::new);
            data.add(stringArray);
        }
        //REVERTIR LOS DATOS, YA QUE PUEDEN ESTAR
        Collections.reverse(data);
        System.out.println("DATA NUM DE FEATURES EN ARCHIVO: " + data.get(0).length);
        int numFeatures = data.get(0).length;
        int numLabels = 1;
        int foreseenDays = 1;

        int sequenceLength = 3;

        int numSamples = data.size();
        double percentOfTraining = 0.7;
        int numberOfTrainingItems = (int)Math.round(percentOfTraining * joinedData.size());
        List<String[]> trainingData = data.subList(0, numberOfTrainingItems);
        List<String[]> testData = data.subList(numberOfTrainingItems -  sequenceLength + 1, data.size());

        System.out.println("Numero de Items de entrenamiento: " + numberOfTrainingItems);
        System.out.println("-----------------------------------------------------");
        System.out.println("Numero de Items de test: " + (joinedData.size() -numberOfTrainingItems));
        System.out.println("-----------------------------------------------------");
        System.out.println("Numero de training Items: " + trainingData.size());
        System.out.println("-----------------------------------------------------");
        System.out.println("Numero de test Items: " + testData.size());
        System.out.println("-----------------------------------------------------");

       // System.out.println("@@@@@@@@@@@@@@@@ Data entero @@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
       // for(String[] e : data){
       //     System.out.println(Arrays.toString(e));
       // }

        System.out.println("@@@@@@@@@@@@@@@@ Training data @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        for(String[] e : trainingData){
            System.out.println(Arrays.toString(e));
        }
        System.out.println("@@@@@@@@@@@@@@@@ Test data @@@@@@@@@@@@@@@@@@@@@@@@@@@");
        for(String[] e : testData){
            System.out.println(Arrays.toString(e));
        }

        INDArray[] trainingFeaturesAndLabels = new INDArray[2];
        trainingFeaturesAndLabels = getFeaturesAndLabels2(trainingData, sequenceLength, numFeatures);

        INDArray[] testFeaturesAndLabels = new INDArray[2];
        testFeaturesAndLabels = getFeaturesAndLabels2(testData, sequenceLength, numFeatures);


        System.out.println("-----------------------------------------------------");


        DataSet trainDataSet = new DataSet(trainingFeaturesAndLabels[0],trainingFeaturesAndLabels[1]);
        DataSet testDataSet = new DataSet(testFeaturesAndLabels[0], testFeaturesAndLabels[1]);

        System.out.println("Test dataSet features:");
        //System.out.println(testDataSet.getFeatures());

        NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler();
        normalizer.fit(trainDataSet);           //Collect the statistics (mean/stdev) from the training data. This does not modify the input data
        normalizer.transform(trainDataSet);     //Apply normalization to the training data
        normalizer.transform(testDataSet);      //Apply normalization to the test data. This is using statistics calculated from the *training* set

        System.out.println("Test dataSet features normalized:");
        //System.out.println(testDataSet.getFeatures());

        System.out.println("Train dataSet features normalized:");
        System.out.println(trainDataSet.getFeatures());

        //UIServer uiServer = UIServer.getInstance();

        LSTMModel model = new LSTMModel();
        MultiLayerNetwork network = model.buildModel(numFeatures, 128, 64, 0);
        for (int epoch = 0; epoch < 1; epoch++) {
            network.fit(trainDataSet);
        }


        System.out.println("\n\nDONE");

    }

    private static INDArray[] getFeaturesAndLabels(List<String[]> data, int sequenceLength, int numFeatures) {
        // TRAIN DATA
        // Inicializar arrays para features y labels
        double[][][] features = new double[data.size() - sequenceLength -1][sequenceLength][numFeatures];
        double[] labels     = new double[data.size() - sequenceLength -1];

        // Procesar los datos
        for (int i = 0; i < data.size() - sequenceLength -1; i++) {
            for (int j = 0; j < sequenceLength; j++) {
                for(int k = 0; k < numFeatures; k++){
                    features[i][j][k] = Double.parseDouble(data.get(i + j)[k]);
                }
            }
            // Leer el target (primera columna, que es el target)
            labels[i] = Double.parseDouble(data.get(i + sequenceLength)[0]);
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
        System.out.println("Num features: " + numFeatures);
        System.out.println("Numero de strings en array data: " + data.get(0).length);
        int numSequences = data.size() - sequenceLength; // 100 - 5 + 1 = 96
        double[][][] features = new double[numSequences][numFeatures][sequenceLength];
        double[][] labels     = new double[numSequences][1];

        // Procesar los datos
        for (int i = 0; i < numSequences; i++) {
            for (int j = 0; j < sequenceLength; j++) {
                for(int k = 0; k < numFeatures; k++){
                    features[i][k][j] = Double.parseDouble(data.get(i + j)[k]);
                }
            }
            // Leer el target (primera columna, que es el target)
            labels[i][0] = Double.parseDouble(data.get(i + sequenceLength)[0]);
        }

        INDArray featureArray = Nd4j.create(features);
        INDArray labelArray = Nd4j.create(labels);
        //System.out.println("@@@@@@ INDArray features @@@@@@@@@: \n" + featureArray); // [numSamples - sequenceLength, sequenceLength, numFeatures]
        //System.out.println("Forma de labels: " + Arrays.toString(labelArray.shape()));
        //System.out.println("@@@@@@ INDArray labels @@@@@@@@@@@: \n" + labelArray);
        return new INDArray[]{featureArray, labelArray};
    }
}
