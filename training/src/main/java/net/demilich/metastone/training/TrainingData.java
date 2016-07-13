package net.demilich.metastone.training;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrainingData {
    public static final int MAX_MINIONS = 7;

    private static void minionToFeatures(JsonObject o, List<Float> x) {
        x.add(o.get("attack").getAsFloat() / 3.5f - 1.0f);
        x.add(o.get("hp").getAsFloat() / 3.5f - 1.0f);

        x.add(o.get("divine_shield").isJsonNull() ? 0.0f : o.get("divine_shield").getAsFloat());
        x.add(o.get("taunt").isJsonNull() ? 0.0f : o.get("taunt").getAsFloat());
    }

    private static void nullMinionToFeatures(List<Float> x) {
        x.add(-1.0f);
        x.add(-1.0f);
        x.add(-1.0f);
        x.add(-1.0f);
    }

    private static void playerToFeatures(JsonObject o, List<Float> x) {
        x.add(o.get("hp").getAsFloat() / 15.0f - 1.0f);
        x.add(o.get("armor").getAsFloat() / 15.0f - 1.0f);
        x.add(o.get("mana").getAsFloat() / 5.0f - 1.0f);
        x.add(((float) o.get("hand").getAsJsonArray().size()) / 5.0f - 1.0f);

        JsonArray minions = o.get("minions").getAsJsonArray();
        /*int attack = 0;
        int hp = 0;
        for (JsonElement minion : minions) {
            attack += minion.getAsJsonObject().get("attack").getAsInt();
            hp += minion.getAsJsonObject().get("hp").getAsInt();
        }
        x.add((float) attack);
        x.add((float) hp);*/
        //x.add((float) minions.size());
        for (JsonElement minion : minions)
            minionToFeatures(minion.getAsJsonObject(), x);
        for (int i = 0; i < MAX_MINIONS - minions.size(); i++)
            nullMinionToFeatures(x);
    }

    private static void gameStateToFeatures(JsonObject o, List<Float> x) {
        x.add((o.get("turn").getAsFloat() - 1.0f) / 10.0f - 1.0f);

        JsonArray players = o.get("players").getAsJsonArray();
        int activePlayerId = 0; //o.get("active_player_id").getAsInt();

        x.add(o.get("active_player_id").getAsInt() == 0 ? 1.0f : -1.0f);

        playerToFeatures(players.get(activePlayerId).getAsJsonObject(), x);
        playerToFeatures(players.get(1 - activePlayerId).getAsJsonObject(), x);
    }

    private static float[] getFeatures(JsonObject o) {
        List<Float> x = new ArrayList<Float>();

        gameStateToFeatures(o, x);

        float[] result = new float[x.size()];
        for (int i = 0; i < x.size(); i++)
            result[i] = x.get(i);

        return result;
    }

    static List<DataSet> load(String directory, int max) throws IOException {
        File d = new File(directory);

        List<DataSet> result = new ArrayList<DataSet>();

        int i = 0;
        for (File f : d.listFiles()) {
            JsonObject game;
            try {
                JsonElement x = new JsonParser().parse(new FileReader(f));
                if (x == null) {
                    System.out.println(f.getAbsolutePath());
                    continue;
                }
                if (x.isJsonNull()) {
                    System.out.println(f.getAbsolutePath());
                    continue;
                }
                game = x.getAsJsonObject();
            }  catch (JsonSyntaxException oops) {
                System.out.println(f.getAbsolutePath());
                continue;
            }
            JsonArray turns = game.get("turns").getAsJsonArray();
            int winner = game.get("winner").getAsInt();

            for (JsonElement e : turns) {
                JsonObject o = e.getAsJsonObject();
                float[] features = getFeatures(o);
                float[] label = new float[1];

                /*int delta = turns.size() - o.get("turn").getAsInt();
                float discount = (float) Math.pow(0.8, (float) delta);*/
                float discount = 1.0f;

                label[0] = winner == 0 ? 1.0f * discount : -1.0f * discount;

                result.add(new DataSet(Nd4j.create(features), Nd4j.create(label)));

                if (++i % 1000 == 0) {
                    System.out.print('.');
                    System.out.flush();
                }
                if (i == max)
                    break;
            }

            if (i == max)
                break;
        }

        return result;
    }

    static float[] getTestFeatures(int turn, int active, int hp1, int hp2, int cards1, int cards2) {
        JsonArray hand1 = new JsonArray();
        for (int i = 0; i < cards1; i++)
            hand1.add(new JsonPrimitive("minion_sea_giant"));

        JsonArray hand2 = new JsonArray();
        for (int i = 0; i < cards2; i++)
            hand2.add(new JsonPrimitive("minion_sea_giant"));

        JsonObject p1 = new JsonObject();
        p1.addProperty("hp", hp1);
        p1.addProperty("armor", 0);
        p1.addProperty("mana", turn);
        p1.add("minions", new JsonArray());
        p1.add("hand", hand1);
        p1.add("deck", new JsonArray());

        JsonObject p2 = new JsonObject();
        p2.addProperty("hp", hp2);
        p2.addProperty("armor", 0);
        p2.addProperty("mana", turn);
        p2.add("minions", new JsonArray());
        p2.add("hand", hand2);
        p2.add("deck", new JsonArray());

        JsonArray ps = new JsonArray();
        ps.add(p1);
        ps.add(p2);

        JsonObject o = new JsonObject();
        o.addProperty("turn", turn);
        o.addProperty("active_player_id", active);
        o.add("players", ps);

        return getFeatures(o);
    }
}
