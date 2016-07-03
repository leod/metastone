package net.demilich.metastone.game.behaviour.heuristic;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.minions.Minion;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NeuralNetworkHeuristic implements IGameStateHeuristic {
    private MultiLayerNetwork model;

    public NeuralNetworkHeuristic(String modelfile) throws IOException {
        model = ModelSerializer.restoreMultiLayerNetwork("model2");
    }

    private void minionToFeatures(Minion m, List<Float> x) {
        x.add((float) m.getAttack());
        x.add((float) m.getHp());
    }

    private void nullMinionToFeatures(List<Float> x) {
        x.add(0.0f);
        x.add(0.0f);
    }

    private void playerToFeatures(Player player, List<Float> x) {
        x.add((float) player.getHero().getHp());
        x.add((float) player.getHero().getArmor());
        x.add((float) player.getMana());
        x.add((float) player.getHand().getCount());

        for (Minion minion : player.getMinions())
            minionToFeatures(minion, x);
        for (int i = 0; i < 7 - player.getMinions().size(); i++)
            nullMinionToFeatures(x);
    }

    private void gameStateToFeature(GameContext context, List<Float> x) {
        x.add((float) context.getTurn());

        Player[] players = context.getPlayers();
        int activePlayerId = context.getActivePlayerId();

        playerToFeatures(players[activePlayerId], x);
        playerToFeatures(players[1 - activePlayerId], x);
    }

    public double getScore(GameContext context, int playerId) {
        List<Float> x = new ArrayList<Float>();
        gameStateToFeature(context, x);

        float[] result = new float[x.size()];
        for (int i = 0; i < x.size(); i++)
            result[i] = x.get(i);
        System.out.println(x.toString());

        INDArray out = model.output(Nd4j.create(result));

        int idx = playerId == context.getActivePlayerId() ? 0 : 1;
        System.out.println(idx + " -> " + out.getFloat(idx));
        System.out.println(out);
        return out.getFloat(idx);
    }

    public void onActionSelected(GameContext context, int playerId) {
    }
}
