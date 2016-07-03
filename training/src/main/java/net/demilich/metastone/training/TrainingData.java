package net.demilich.metastone.training;

import com.google.gson.*;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrainingData {
    public static final int MAX_MINIONS = 7;

    private void minionToFeatures(JsonObject o, List<Float> x) {
        x.add(o.get("attack").getAsFloat());
        x.add(o.get("hp").getAsFloat());

        x.add(o.get("divine_shield").isJsonNull() ? 0.0f : o.get("divine_shield").getAsFloat());
        x.add(o.get("taunt").isJsonNull() ? 0.0f : o.get("taunt").getAsFloat());
    }

    private void nullMinionToFeatures(List<Float> x) {
        x.add(0.0f);
        x.add(0.0f);
        x.add(0.0f);
        x.add(0.0f);
    }

    private void playerToFeatures(JsonObject o, List<Float> x) {
        x.add(o.get("hp").getAsFloat());
        x.add(o.get("armor").getAsFloat());
        x.add(o.get("mana").getAsFloat());
        x.add((float) o.get("hand").getAsJsonArray().size());

        JsonArray minions = o.get("minions").getAsJsonArray();
        for (JsonElement minion : minions)
            minionToFeatures(minion.getAsJsonObject(), x);
        for (int i = 0; i < MAX_MINIONS - minions.size(); i++)
            nullMinionToFeatures(x);
    }

    private void gameStateToFeatures(JsonObject o, List<Float> x) {
        x.add(o.get("turn").getAsFloat());

        JsonArray players = o.get("players").getAsJsonArray();
        int activePlayerId = o.get("active_player_id").getAsInt();

        playerToFeatures(players.get(activePlayerId).getAsJsonObject(), x);
        playerToFeatures(players.get(1 - activePlayerId).getAsJsonObject(), x);
    }

    private float[] getFeatures(JsonObject o) {
        List<Float> x = new ArrayList<Float>();

        gameStateToFeatures(o, x);

        float[] result = new float[x.size()];
        for (int i = 0; i < x.size(); i++)
            result[i] = x.get(i);

        return result;
    }

    List<DataSet> load(String directory) throws IOException {
        File d = new File(directory);

        List<DataSet> result = new ArrayList<DataSet>();

        for (File f : d.listFiles()) {
            JsonObject game = new JsonParser().parse(new FileReader(f)).getAsJsonObject();
            JsonArray turns = game.get("turns").getAsJsonArray();
            int winner = game.get("winner").getAsInt();

            for (JsonElement e : turns) {
                JsonObject o = e.getAsJsonObject();
                int activePlayerId = o.get("active_player_id").getAsInt();

                float[] features = getFeatures(o);
                float[] label = new float[2];
                label[0] = activePlayerId == winner ? 1.0f : 0.0f;
                label[1] = activePlayerId == winner ? 0.0f : 1.0f;

                result.add(new DataSet(Nd4j.create(features), Nd4j.create(label)));
            }
        }

        return result;
    }
}
