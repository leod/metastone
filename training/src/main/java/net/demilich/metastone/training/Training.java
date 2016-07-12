package net.demilich.metastone.training;

import com.xeiam.xchart.*;
import javafx.scene.chart.ChartBuilder;
import javafx.scene.chart.LineChartBuilder;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChartBuilder;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.eval.RegressionEvaluation;
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
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.awt.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Training {
    // Settings
    ModelConfig modelConfig = new ModelConfig();

    // Data
    List<DataSet> trainData, testData;

    // Model configuration
    MultiLayerConfiguration multiLayerConf;

    // Model
    MultiLayerNetwork model;

    public static void main(String[] args) throws Exception {
        Training training = new Training();

        int numCharts = 4;
        List<Chart> charts = new ArrayList<Chart>();

        training.loadData("zoo_vs_zoo_new/", "zoo_vs_zoo/train/");
        training.createModel();
        training.train();

        EvalResult trainResult = training.evaluate("train", training.trainData);
        EvalResult testResult = training.evaluate("test", training.testData);

        String modelName = training.modelConfig.getName();
        System.out.println("Model name: " + modelName);

        String modelPath = "zoo_vs_zoo_models/" + modelName + "/";
        new File(modelPath).mkdir();

        File[] directories = new File(modelPath).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        int nExp = directories.length;

        modelPath += nExp + 1;
        modelPath += "_train_acc" + String.format("%.2f", trainResult.getAccuracy() * 100.0f);
        modelPath += "_mse" + String.format("%.2f", trainResult.getMSE());
        modelPath += "_test_acc" + String.format("%.2f", testResult.getAccuracy() * 100.0f);
        modelPath += "_mse" + String.format("%.2f", testResult.getMSE());
        modelPath += "/";

        System.out.println("Model path: " + modelPath);
        new File(modelPath).mkdir();

        training.save(modelPath + "model");
        new File(modelPath + "charts/").mkdir();
        training.doCharts(modelPath + "charts/");
    }

    private void loadData(String trainDataDir, String testDataDir) throws IOException {
        System.out.println("Loading train data from " + trainDataDir);
        trainData = TrainingData.load(trainDataDir);
        Collections.shuffle(trainData);
        System.out.println("Num examples: " + trainData.size());

        System.out.println("Loading test data from " + testDataDir);
        testData = TrainingData.load(testDataDir);
        System.out.println("Num examples: " + testData.size());
    }

    private void createModel() {
        System.out.println("Creating model");

        multiLayerConf = modelConfig.createConfiguration(trainData.get(0).numInputs());
        model = new MultiLayerNetwork(multiLayerConf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));
        //model.setListeners(new HistogramIterationListener(1));
    }

    private void train() {
        System.out.println("Training");
        DataSetIterator trainIter = new ListDataSetIterator(trainData, modelConfig.batchSize);

        for (int n = 0; n < modelConfig.nEpochs; n++) {
            System.out.println("Epoch " + n);
            model.fit(trainIter);
            trainIter.reset(); // TODO: Neccessary?
        }
    }

    private void save(String filename) throws IOException {
        System.out.println("Saving to " + filename);
        ModelSerializer.writeModel(model, filename, false);
    }

    private EvalResult evaluate(String name, List<DataSet> list) {
        System.out.println("Evaluating " + name);
        DataSetIterator iter = new ListDataSetIterator(list, 1);

        int samples = 0;
        int correct = 0;
        int[][] confusion = new int[2][2];
        RegressionEvaluation eval = new RegressionEvaluation(1);

        while (iter.hasNext()) {
            DataSet t = iter.next();
            INDArray features = t.getFeatureMatrix();
            INDArray labels = t.getLabels();
            INDArray output = model.output(features, false);

            int p1 = labels.getFloat(0) > 0.0 ? 0 : 1;
            int p2 = output.getFloat(0) > 0.0 ? 0 : 1;

            samples++;
            correct += p1 == p2 ? 1 : 0;
            confusion[p1][p2]++;
            eval.eval(labels, output);
        }

        float accuracy = correct / (float) samples;
        float mse = (float) eval.meanSquaredError(0);

        System.out.println(confusion[0][0] + " " + confusion[0][1]);
        System.out.println(confusion[1][0] + " " + confusion[1][1]);
        System.out.println("accuracy: " + accuracy);
        System.out.println("mse: " + mse);

        return new EvalResult(accuracy, mse);
    }

    private void doCharts(String chartDir) throws IOException {
        // HP at 30
        {
            EvalChart chart = new EvalChart("turn10_hand5_hp30_vs_hp", "hp");
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 1; i <= 30; i++)
                    features.add(TrainingData.getTestFeatures(10, 0, i, 30, 5, 5));
                chart.add("p0 hp w/ p0 active", SeriesMarker.CIRCLE, Color.BLUE, features, model);
            }
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 1; i <= 30; i++)
                    features.add(TrainingData.getTestFeatures(10, 1, i, 30, 5, 5));
                chart.add("p0 hp w/ p1 active", SeriesMarker.SQUARE, Color.BLUE, features, model);
            }
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 1; i <= 30; i++)
                    features.add(TrainingData.getTestFeatures(10, 0, 30, i, 5, 5));
                chart.add("p1 hp w/ p0 active", SeriesMarker.CIRCLE, Color.ORANGE, features, model);
            }
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 1; i <= 30; i++)
                    features.add(TrainingData.getTestFeatures(10, 1, 30, i, 5, 5));
                chart.add("p1 hp w/ p1 active ", SeriesMarker.SQUARE, Color.ORANGE, features, model);
            }
            chart.save(chartDir);
        }
        // HP at 15
        {
            EvalChart chart = new EvalChart("turn10_hand5_hp15_vs_hp", "hp");
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 1; i <= 30; i++)
                    features.add(TrainingData.getTestFeatures(10, 0, i, 15, 5, 5));
                chart.add("p0 hp w/ p0 active", SeriesMarker.CIRCLE, Color.BLUE, features, model);
            }
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 1; i <= 30; i++)
                    features.add(TrainingData.getTestFeatures(10, 1, i, 15, 5, 5));
                chart.add("p0 hp w/ p1 active", SeriesMarker.SQUARE, Color.BLUE, features, model);
            }
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 1; i <= 30; i++)
                    features.add(TrainingData.getTestFeatures(10, 0, 15, i, 5, 5));
                chart.add("p1 hp w/ p0 active", SeriesMarker.CIRCLE, Color.ORANGE, features, model);
            }
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 1; i <= 30; i++)
                    features.add(TrainingData.getTestFeatures(10, 1, 15, i, 5, 5));
                chart.add("p1 hp w/ p1 active ", SeriesMarker.SQUARE, Color.ORANGE, features, model);
            }
            chart.save(chartDir);
        }
        // Hand size at 30HP
        {
            EvalChart chart = new EvalChart("turn10_hand5_hp30_vs_hand", "hand");
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 0; i <= 10; i++)
                    features.add(TrainingData.getTestFeatures(10, 0, 30, 30, i, 5));
                chart.add("p0 hand w/ p0 active", SeriesMarker.CIRCLE, Color.BLUE, features, model);
            }
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 0; i <= 10; i++)
                    features.add(TrainingData.getTestFeatures(10, 1, 30, 30, i, 5));
                chart.add("p0 hand w/ p1 active", SeriesMarker.SQUARE, Color.BLUE, features, model);
            }
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 0; i <= 10; i++)
                    features.add(TrainingData.getTestFeatures(10, 0, 30, 30, 5, i));
                chart.add("p1 hand w/ p0 active", SeriesMarker.CIRCLE, Color.ORANGE, features, model);
            }
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 0; i <= 10; i++)
                    features.add(TrainingData.getTestFeatures(10, 1, 30, 30, 5, i));
                chart.add("p1 hand w/ p1 active", SeriesMarker.SQUARE, Color.ORANGE, features, model);
            }
            chart.save(chartDir);
        }
        // Hand size at 15HP
        {
            EvalChart chart = new EvalChart("turn10_hand5_hp15_vs_hand", "hand");
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 0; i <= 10; i++)
                    features.add(TrainingData.getTestFeatures(10, 0, 15, 15, i, 5));
                chart.add("p0 hand w/ p0 active", SeriesMarker.CIRCLE, Color.BLUE, features, model);
            }
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 0; i <= 10; i++)
                    features.add(TrainingData.getTestFeatures(10, 1, 15, 15, i, 5));
                chart.add("p0 hand w/ p1 active", SeriesMarker.SQUARE, Color.BLUE, features, model);
            }
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 0; i <= 10; i++)
                    features.add(TrainingData.getTestFeatures(10, 0, 15, 15, 5, i));
                chart.add("p1 hand w/ p0 active", SeriesMarker.CIRCLE, Color.ORANGE, features, model);
            }
            {
                List<float[]> features = new ArrayList<float[]>();
                for (int i = 0; i <= 10; i++)
                    features.add(TrainingData.getTestFeatures(10, 1, 15, 15, 5, i));
                chart.add("p1 hand w/ p1 active", SeriesMarker.SQUARE, Color.ORANGE, features, model);
            }
            chart.save(chartDir);
        }
    }
}
