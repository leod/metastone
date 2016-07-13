package net.demilich.metastone.training;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class ModelConfig {
    int seed = 1337;
    int[] hidden = { 50, 50 };
    String[] activation = { "tanh", "tanh", "tanh" };
    double learningRate = 0.001;
    int batchSize = 100;
    int nEpochs = 5;

    MultiLayerConfiguration createConfiguration(int numInputs) {
        NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
            .seed(seed)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .iterations(1)
            .weightInit(WeightInit.XAVIER)
            .learningRate(learningRate)
            .updater(Updater.NESTEROVS).momentum(0.98)
            .regularization(true).l2(1e-4);

        NeuralNetConfiguration.ListBuilder listBuilder = builder.list();
        for (int i = 0; i < hidden.length; i++) {
            int nIn = i == 0 ? numInputs : hidden[i-1];
            listBuilder.layer(i, new DenseLayer.Builder()
                .nIn(nIn)
                .nOut(hidden[i])
                .activation(activation[i])
                .build());
        }
        listBuilder.layer(hidden.length, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
            .nIn(hidden[hidden.length-1])
            .nOut(1)
            .activation(activation[hidden.length])
            .build());

        return listBuilder
            .pretrain(false)
            .backprop(true)
            .build();
    }

    String getName() {
        String s = new String();
        for (int i = 0; i < hidden.length; i++)
            s += hidden[i] + (i < hidden.length-1 ? "x" : "");
        s += "_";
        for (int i = 0; i < activation.length; i++)
            s += activation[i] + (i < activation.length-1 ? "-" : "");
        s += "_lr" + String.format("%.6f", learningRate);
        s += "_batch" + batchSize;
        s += "_epoch" + nEpochs;
        return s;
    }
}
