package dual_lstm_csv_manipulation;

import dual_lstm_csv_manipulation.investing.InvestingTransformData;

import dual_lstm_csv_manipulation.paths.GetSoucePaths;
import dual_lstm_csv_manipulation.paths.IAbsPaths;
import org.datavec.api.writable.Writable;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
    static String fileName = "ACX-short.csv";
    static String fileNameIBEX = "IBEX35-short.csv";
    //static String path = "C:\\Users\\jrobes\\IdeaProjects\\LSTMCSV\\ACX-short.csv";
    //static String path = "C:\\Users\\COTERENA\\IdeaProjects\\LSTMCSV\\ACX-short.csv";
    public static void main( String[] args ) throws IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {

        System.out.println( "Hello World!" );
        String[] sourcePaths = new String[]{fileName, fileNameIBEX};


        IAbsPaths absPaths = new GetSoucePaths(sourcePaths);
        String[] absolutePaths = absPaths.getAbsPaths();

        IDataPreparation itd = new InvestingTransformData(0, absolutePaths);
        List<List<Writable>> joinedData = itd.getDataAsWritable();


        System.out.println();
        System.out.println();
        System.out.println("Datos Unidos...\nNúmero de filas: " + joinedData.size());
        for (List<Writable> record : joinedData) {
            System.out.println(record);
        }
        System.out.println("-----------------------------------------------------");

        int numFeatures = 5;
        int numLabels = 1;
        int foreseenDays = 1;

        int sequenceLength = 2;

        double percentOfTraining = 0.7;
        int numberOfTrainingItems = (int)Math.round(percentOfTraining * joinedData.size());

        System.out.println("Numero de Items de entrenamiento: " + numberOfTrainingItems);
        System.out.println("-----------------------------------------------------");
        System.out.println("Numero de Items de test: " + (joinedData.size() -numberOfTrainingItems));
        System.out.println("-----------------------------------------------------");


        double[][] featureMatrixTraining = new double[joinedData.size()][numFeatures];
        double[][] labelMatrixTraining = new double[joinedData.size()][numLabels];





        double[][] featureMatrixTest = new double[joinedData.size()][numFeatures];
        double[][] labelMatrixTest = new double[joinedData.size()][numLabels];

        for (int rowIndex = 0; rowIndex < joinedData.size(); rowIndex++) {
            List<Writable> row = joinedData.get(rowIndex);

            // Extraer características
            for (int colIndex = 0; colIndex < numFeatures; colIndex++) {
                featureMatrixTest[rowIndex][colIndex] = row.get(colIndex + 1).toDouble();
            }
            // Extraer etiquetas
            for (int colIndex = 0; colIndex < numLabels; colIndex++) {
                List<Writable> row2 = joinedData.get(rowIndex);
                labelMatrixTest[rowIndex][colIndex] = row2.get(1).toDouble();
            }
        }



        /*
        for(int r = 0; r < featureMatrixTest.length; r++){
            for(int m = 0; m < numFeatures; m++){
                System.out.print(featureMatrixTest[r][m] + "\t");
            }
            System.out.println();
        }
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        for(int r = 0; r < labelMatrixTest.length; r++){
            for(int m = 0; m < numLabels; m++){
                System.out.print(labelMatrixTest[r][m] + "\t");
            }
            System.out.println();
        }

*/


    /*


        INDArray featureArray = Nd4j.create(featureMatrixTest);
        INDArray labelArray = Nd4j.create(labelMatrixTest);
        System.out.println("#####################################################");
        System.out.println("INDArray feature: \n" + featureArray);
        System.out.println("Num. rows    " + featureArray.rows());


        int rows = featureArray.rows();
        double num = rows*percentOfTraining;
        int ff = (int)((long)num);
        System.out.println("Valor entero para dividir el dataset: " + ff);
        //We can select arbitrary subsets, using INDArray indexing:
        //All columns, first 3 rows (note that internal here is columns 0 inclusive to 3 exclusive)
        INDArray featureTrain = featureArray.get(NDArrayIndex.interval(rows-ff,rows), NDArrayIndex.all()).dup();
        INDArray featureTest =featureArray.get(NDArrayIndex.interval(0,rows-ff), NDArrayIndex.all()).dup();
        System.out.println("##############    INDArray featureTrain    #########################");
        System.out.println(featureTrain);
        //System.out.println("valorrrr  : " + featureTrain.getDouble(4,2));
        System.out.println("##############    INDArray featureTest    #########################");
        System.out.println(featureTest);

        INDArray labelTrain = labelArray.get(NDArrayIndex.interval(rows-ff,rows), NDArrayIndex.all()).dup();
        INDArray labelTest =labelArray.get(NDArrayIndex.interval(0,rows-ff), NDArrayIndex.all()).dup();

        DataSet trainData = new DataSet(featureTrain,labelTrain);
        DataSet testData = new DataSet(featureTest, labelTest);

        System.out.println("Test dataSet features:");
        System.out.println(testData.getFeatures());

        NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler();
        normalizer.fit(trainData);           //Collect the statistics (mean/stdev) from the training data. This does not modify the input data
        normalizer.transform(trainData);     //Apply normalization to the training data
        normalizer.transform(testData);      //Apply normalization to the test data. This is using statistics calculated from the *training* set

        System.out.println("Test dataSet features normalized:");
        System.out.println(testData.getFeatures());

        System.out.println("Train dataSet features normalized:");
        System.out.println(trainData.getFeatures());

 */






/*
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(0.1))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(4).nOut(3).activation(Activation.TANH).build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).activation(Activation.SOFTMAX).nIn(3).nOut(3).build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        for (int epoch = 0; epoch < 1; epoch++) {
            net.fit(trainData);
        }
*/
        System.out.println("\n\nDONE");

    }


}
