package net.demilich.metastone.training;

public class EvalResult {
    final float accuracy;
    final float mse;

    EvalResult(float accuracy, float mse) {
        this.accuracy = accuracy;
        this.mse = mse;
    }

    float getAccuracy() {
        return accuracy;
    }

    float getMSE() {
        return mse;
    }
}
