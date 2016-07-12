package net.demilich.metastone.training;

import com.xeiam.xchart.BitmapEncoder;
import com.xeiam.xchart.Chart;
import com.xeiam.xchart.Series;
import com.xeiam.xchart.SeriesMarker;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.factory.Nd4j;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class EvalChart {
    private String name;
    private Chart chart = new Chart(600, 400);
    private int numSeries = 0;

    EvalChart(String name, String xLabel) {
        this.name = name;
        chart.setTitle(name);
        chart.setXAxisTitle(xLabel);
        chart.setYAxisTitle("p0 value");
    }

    void add(String seriesName, SeriesMarker marker, Color color, List<float[]> features, MultiLayerNetwork model) {
        double[] xData = new double[features.size()];
        double[] yData = new double[features.size()];

        for (int i = 0; i < features.size(); i++) {
            xData[i] = (double) i;
            yData[i] = (double) model.output(Nd4j.create(features.get(i)), false).getFloat(0);
        }

        Series series = chart.addSeries(seriesName, xData, yData);

        series.setMarker(marker);
        series.setMarkerColor(color);
        series.setLineColor(color);

        numSeries += 1;
    }

    void save(String chartDir) throws IOException {
        BitmapEncoder.savePNG(chart, chartDir + name + ".png");
    }
}
