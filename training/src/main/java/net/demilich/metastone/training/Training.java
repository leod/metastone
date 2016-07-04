package net.demilich.metastone.training;

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.weights.HistogramIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.SamplingDataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.Collections;
import java.util.List;

public class Training {
    public static void main(String[] args) throws Exception {
        int seed = 1337;
        double learningRate = 0.001;
        int numHidden1 = 200;
        int numHidden2 = 400;
        int numHidden3 = 200;
        int batchSize = 100;
        int nEpochs = 10;

        System.out.println("Loading train data");
        List<DataSet> trainData = new TrainingData().load("zoo_vs_zoo/train/");
        Collections.shuffle(trainData);
        DataSetIterator trainIter = new ListDataSetIterator(trainData, batchSize);
        //DataSetIterator trainIter = new SamplingDataSetIterator(trainData, batchSize, trainData.numExamples());
        System.out.println("Num examples:" + trainData.size());
        System.out.println("Num inputs:" + trainData.get(0).numInputs());
        System.out.println("Num outcomes:" + trainData.get(0).numOutcomes());
        System.out.println("Done");

        System.out.println("Loading test data");
        List<DataSet> testData = new TrainingData().load("zoo_vs_zoo/test/");
        DataSetIterator testIter = new ListDataSetIterator(testData, 1);
        //DataSetIterator testIter = new SamplingDataSetIterator(testData, 1, testData.numExamples());
        System.out.println("Num examples:" + testData.size());
        System.out.println("Num inputs:" + testData.get(0).numInputs());
        System.out.println("Num outcomes:" + testData.get(0).numOutcomes());
        System.out.println("Done");

        System.out.println("Creating model");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(1)
                .weightInit(WeightInit.XAVIER)
                .learningRate(learningRate)
                .updater(Updater.NESTEROVS).momentum(0.98)
                .regularization(true).l2(1e-4)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(trainData.get(0).numInputs())
                        .nOut(numHidden1)
                        .activation("relu")
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(numHidden1)
                        .nOut(numHidden2)
                        .activation("tanh")
                        .build())
                .layer(2, new DenseLayer.Builder()
                        .nIn(numHidden2)
                        .nOut(numHidden3)
                        .activation("sigmoid")
                        .build())
                .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(numHidden3)
                        .nOut(2)
                        .activation("softmax")
                        .build())
                .pretrain(false)
                .backprop(true)
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));
        //model.setListeners(new HistogramIterationListener(1));

        System.out.println("Training");
        for (int n = 0; n < nEpochs; n++) {
            System.out.println("Epoch " + n);
            model.fit(trainIter);
            trainIter.reset();
        }

        trainIter.reset();

        /*System.out.println("Evaluating train");
        Evaluation eval = new Evaluation(2);
        int i = 0;
        while (trainIter.hasNext()) {
            DataSet t = trainIter.next();
            INDArray features = t.getFeatureMatrix();
            INDArray labels = t.getLabels();
            INDArray predicted = model.output(features, false);

            eval.eval(labels, predicted);

            if (i++ % 100 == 0) {
                System.out.println(features);
                //System.out.println(labels);
                System.out.println(predicted);
                System.out.println("----------------------");
            }
        }

        System.out.println(eval.stats());*/

        System.out.println("Evaluating test");
        Evaluation eval = new Evaluation(2);
        int i = 0;
        while (testIter.hasNext()) {
            DataSet t = testIter.next();
            INDArray features = t.getFeatureMatrix();
            INDArray labels = t.getLabels();
            INDArray predicted = model.output(features, false);

            if (i++ % 100 == 0) {
                System.out.println(features);
                //System.out.println(labels);
                System.out.println(predicted);
                System.out.println("----------------------");
            }

            eval.eval(labels, predicted);
        }

        System.out.println(eval.stats());

        String modelfile = "model3";
        System.out.println("Saving to " + modelfile);
        ModelSerializer.writeModel(model, modelfile, false);
    }
}
