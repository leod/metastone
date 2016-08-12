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

    // Model directories
    String method;
    String modelName;

    String rootDir;
    String dataDir;
    String modelsDir;
    String modelDir;

    // Model configuration
    int numInputs;
    MultiLayerConfiguration multiLayerConf;

    // Model
    MultiLayerNetwork model;

    // Stats about training
    double[] testAccuracy;
    double[] testMSE;

    public static void main(String[] args) throws Exception {
        Training training = new Training("zoo_vs_zoo", "train_gsv");

        List<DataSet> gsvTrainData = training.loadData(training.dataDir + "gsv/train/", 0);
        List<DataSet> gsvTestData = training.loadData(training.dataDir + "gsv/test/", 0);
        //List<DataSet> randomTrainData = training.loadData(training.dataDir + "random/", 800000);

        training.numInputs = gsvTrainData.get(0).numInputs();

        training.createFolders();

        training.createModel();

        int N = 1;
        training.testAccuracy = new double[N * training.modelConfig.nEpochs];
        training.testMSE = new double[N * training.modelConfig.nEpochs];

        training.train(0, gsvTrainData, gsvTestData);
        //training.train(training.modelConfig.nEpochs, gsvTrainData, gsvTestData);
        //training.train(2*training.modelConfig.nEpochs, gsvTrainData, gsvTestData);

        EvalResult trainResult = training.evaluate("gsv_train", gsvTrainData);
        EvalResult testResult = training.evaluate("gsv_test", gsvTestData);

        trainResult.write(training.modelDir + "eval_" + trainResult.name + ".txt");
        testResult.write(training.modelDir + "eval_" + testResult.name + ".txt");

        System.out.println("Model name: " + training.modelName);
        System.out.println("Model dir: " + training.modelDir);

        training.save(training.modelDir + "model");

        training.doCharts(training.modelDir + "charts/");
    }

    Training(String root, String method) {
        rootDir = root + "/";
        this.method = method;
        dataDir = rootDir + "data/";
        modelsDir = rootDir + "models/" + method + "/";
    }

    private void createFolders() throws IOException {
        new File(modelsDir).mkdir();

        modelName = modelConfig.getName();
        String configPath = modelsDir + modelName + "/";

        new File(configPath).mkdir();

        File[] directories = new File(configPath).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        int nExp = directories.length;

        modelDir = configPath + (nExp+1) + "/";
        new File(modelDir).mkdir();

        new File(modelDir + "charts/").mkdir();
        new File(modelDir + "train/").mkdir();
    }

    private List<DataSet> loadData(String dataDir, int max) throws IOException {
        System.out.println("Loading data from " + dataDir);
        List<DataSet> data = TrainingData.load(dataDir, max);
        System.out.println("Shuffling");
        Collections.shuffle(data);
        System.out.println("Num examples: " + data.size());
        return data;
    }

    private void createModel() {
        System.out.println("Creating model");

        multiLayerConf = modelConfig.createConfiguration(numInputs);
        model = new MultiLayerNetwork(multiLayerConf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));
        //model.setListeners(new HistogramIterationListener(1));
    }

    private void train(int startN, List<DataSet> trainData, List<DataSet> testData) throws IOException {
        System.out.println("Training");
        DataSetIterator trainIter = new ListDataSetIterator(trainData, modelConfig.batchSize);

        for (int n = 0; n < modelConfig.nEpochs; n++) {
            System.out.println("Epoch " + n);
            model.fit(trainIter);
            trainIter.reset(); // TODO: Neccessary?

            EvalResult testResult = evaluate("test", testData);
            testResult.write(modelDir + "train/eval_test_" + (startN+n+1) + ".txt");

            testAccuracy[startN + n] = testResult.getAccuracy();
            testMSE[startN + n] = testResult.getMSE();
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

        return new EvalResult(name, accuracy, mse);
    }

    private void doCharts(String chartDir) throws IOException {
        // Training charts
        {
            double[] xData = new double[testAccuracy.length];
            for (int i = 0; i < xData.length; i++) xData[i] = (double) i;

            {
                Chart chart = new Chart(600, 400);
                chart.setTitle("test_accuracy_vs_epoch");
                chart.setXAxisTitle("epoch");
                chart.setYAxisTitle("test accuracy");
                chart.addSeries("accuracy", xData, testAccuracy);
                BitmapEncoder.savePNG(chart, chartDir + "test_accuracy_vs_epoch.png");
            }
            {
                Chart chart = new Chart(600, 400);
                chart.setTitle("test_mse_vs_epoch");
                chart.setXAxisTitle("epoch");
                chart.setYAxisTitle("test mse");
                chart.addSeries("mse", xData, testMSE);
                BitmapEncoder.savePNG(chart, chartDir + "test_mse_vs_epoch.png");
            }
        }

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
                chart.add("p1 hp w/ p1 active", SeriesMarker.SQUARE, Color.ORANGE, features, model);
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
                chart.add("p1 hp w/ p1 active", SeriesMarker.SQUARE, Color.ORANGE, features, model);
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
