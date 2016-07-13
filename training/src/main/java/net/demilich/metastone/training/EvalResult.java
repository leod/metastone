package net.demilich.metastone.training;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class EvalResult {
    final String name;
    final float accuracy;
    final float mse;

    EvalResult(String name, float accuracy, float mse) {
        this.name = name;
        this.accuracy = accuracy;
        this.mse = mse;
    }

    float getAccuracy() {
        return accuracy;
    }

    float getMSE() {
        return mse;
    }

    void write(String filename) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(filename));
        pw.write("accuracy: " + accuracy + "\n");
        pw.write("mse: " + mse + "\n");
        pw.flush();
    }
}
