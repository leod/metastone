package net.demilich.metastone.game.behaviour.mcts;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;

import java.util.List;

public class TreeBehaviour extends Behaviour {
    private int count = 0;

    private ITreePolicy policy;
    private ChanceNode node;
    private List<SearchNode> visited;

    private ChanceNode selection = null;

    TreeBehaviour(ITreePolicy policy, ChanceNode node, List<SearchNode> visited) {
        this.policy = policy;
        this.node = node;
        this.visited = visited;
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

        ActionNode outcomeNode = node.getOutcomeNode(context.clone(), validActions);
        visited.add(outcomeNode);
        selection = (ChanceNode) outcomeNode.select(policy, visited);

        /*if (node.outcomeNodes.size() > 1) {
            ActionNode a = node.outcomeNodes.get(0);
            ActionNode b = node.outcomeNodes.get(1);
            boolean x = false;
            assert false;
        }*/


        return validActions.get(selection.getActionIndex());
    }
}
