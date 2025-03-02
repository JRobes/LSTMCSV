package dual_lstm_csv_manipulation;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class LSTMModel {

    public static MultiLayerNetwork buildModel(int numFeatures, int numHiddenUnits1, int numHiddenUnits2, int numOutputs) {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.001))
                .list()
                .layer(0, new LSTM.Builder()
                        .nIn(numFeatures)
                        .nOut(numHiddenUnits1)
                        .activation(Activation.TANH)
                        .build())
                .layer(1, new LSTM.Builder()
                        .nIn(numHiddenUnits1)
                        .nOut(numHiddenUnits2)
                        .activation(Activation.TANH)
                        .build())

                .layer(2, new DenseLayer.Builder()
                        .nIn(numHiddenUnits2)
                        .nOut(25)
                        .activation(Activation.RELU)
                        .build())
                .layer(3, new DenseLayer.Builder()
                        .nIn(25)
                        .nOut(1)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(4, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(1)
                        .nOut(1)
                        .activation(Activation.IDENTITY)
                        .build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));

        return model;
    }
}