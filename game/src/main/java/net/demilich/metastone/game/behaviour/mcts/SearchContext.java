package net.demilich.metastone.game.behaviour.mcts;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.GameAction;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class SearchContext {
    private HashMap<SearchState, ActionNode> actionNodes = new HashMap<SearchState, ActionNode>();
    //private HashMap<SearchState, ChanceNode> chanceNodes;

    public SearchNode getActionNode(SearchState state) {
        SearchNode node = actionNodes.get(state);
        assert node != null;
        return node;
    }

    /*public SearchNode getChanceNode(SearchState state) {
        SearchNode node = chanceNodes.get(state);
        assert node != null;
        return node;
    }*/

    public ActionNode addActionNode(SearchState state, List<GameAction> actions) {
        ActionNode node = new ActionNode(this, state, actions);
        actionNodes.put(state, node);
        return node;
    }

    /*public ChanceNode addChanceNode(SearchState state, GameAction action) {
        ChanceNode node = new ChanceNode(this, state, action);
        chanceNodes.put(state, node);
        return node;
    }*/

}
