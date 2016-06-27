package net.demilich.metastone.game.behaviour.mcts;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;

import java.util.List;

public class TreeBehaviour extends Behaviour {
    private int count = 0;

    private ChanceNode node;
    private List<SearchNode> visited;

    private ActionNode outcomeNode = null;
    private ChanceNode selection = null;

    TreeBehaviour(ChanceNode node, List<SearchNode> visited) {
        this.node = node;
        this.visited = visited;
    }

    ActionNode getOutcomeNode() {
        return outcomeNode;
    }

    ChanceNode getSelection() {
        return selection;
    }

    @Override
    public String getName() {
        return "tree behaviour";
    }

    @Override
    public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
        assert false;
        return null;
    }

    @Override
    public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
        assert count == 0;
        count++;

        return validActions.get(0);

        /*outcomeNode = node.getOutcomeNode(context.clone(), validActions);
        selection = (ChanceNode) outcomeNode.select(visited);

        return validActions.get(selection.getActionIndex());*/
    }
}
