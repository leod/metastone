package net.demilich.metastone.game.behaviour.mcts;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.GameAction;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class SearchContext {
    private ITreePolicy policy;
    private double rewardDiscount;

    private HashMap<SearchState, ActionNode> actionNodes = new HashMap<>();

    SearchContext(ITreePolicy policy, double rewardDiscount) {
        this.policy = policy;
        this.rewardDiscount = rewardDiscount;
    }

    ITreePolicy getTreePolicy() {
        return policy;
    }

    double getRewardDiscount() {
        return rewardDiscount;
    }

    ActionNode getActionNode(SearchState state, List<GameAction> actions) {
        /*ActionNode node = actionNodes.get(state);
        if (node == null) {
            node = new ActionNode(this, state, actions);
            actionNodes.put(state, node);
        }*/

        ActionNode node = new ActionNode(this, state, actions);

        node.useCount++;

        return node;
    }
}
