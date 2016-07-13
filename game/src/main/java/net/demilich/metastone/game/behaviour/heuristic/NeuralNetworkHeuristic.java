package net.demilich.metastone.game.behaviour.heuristic;

import net.demilich.metastone.game.Attribute;
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
        String m = "zoo_vs_zoo_models/10x10_tanh-tanh-tanh_lr0.001000_batch100_epoch30/1_train_acc75.35_mse0.64_test_acc74.31_mse0.66/model";
        model = ModelSerializer.restoreMultiLayerNetwork(m);
    }

    private void minionToFeatures(Minion m, List<Float> x) {
        x.add((float) m.getAttack() / 3.5f - 1.0f);
        x.add((float) m.getHp() / 3.5f - 1.0f);
        x.add(m.getAttribute(Attribute.DIVINE_SHIELD) == null ? 0.0f : (float) (int) m.getAttribute(Attribute.DIVINE_SHIELD));
        x.add(m.getAttribute(Attribute.TAUNT) == null ? 0.0f : (float) (int) m.getAttribute(Attribute.TAUNT));
    }

    private void nullMinionToFeatures(List<Float> x) {
        x.add(-1.0f);
        x.add(-1.0f);
        x.add(-1.0f);
        x.add(-1.0f);
    }

    private void playerToFeatures(Player player, List<Float> x) {
        x.add((float) player.getHero().getHp() / 15.0f - 1.0f);
        x.add((float) player.getHero().getArmor() / 15.0f - 1.0f);
        x.add((float) player.getMana() / 5.0f - 1.0f);
        x.add((float) player.getHand().getCount() / 5.0f - 1.0f);

        for (Minion minion : player.getMinions())
            minionToFeatures(minion, x);
        for (int i = 0; i < 7 - player.getMinions().size(); i++)
            nullMinionToFeatures(x);
    }

    private void gameStateToFeature(GameContext context, List<Float> x) {
        x.add(((float) context.getTurn() - 1.0f) / 10.0f - 1.0f);

        Player[] players = context.getPlayers();
        int activePlayerId = context.getActivePlayerId();

        x.add(activePlayerId == 0 ? 1.0f : -1.0f);

        playerToFeatures(players[0], x);
        playerToFeatures(players[1], x);
    }

    public double getScore(GameContext context, int playerId) {
        List<Float> x = new ArrayList<Float>();
        gameStateToFeature(context, x);

        float[] result = new float[x.size()];
        for (int i = 0; i < x.size(); i++)
            result[i] = x.get(i);
        //System.out.println(x.toString());

        INDArray out = model.output(Nd4j.create(result));

        float p0value = out.getFloat(0);
        float value = playerId == 0 ? p0value : -p0value;
        return value;
    }

    public void onActionSelected(GameContext context, int playerId) {
    }
}
