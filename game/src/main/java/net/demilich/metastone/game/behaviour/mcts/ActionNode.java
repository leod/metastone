package net.demilich.metastone.game.behaviour.mcts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.logic.GameLogic;

class ActionNode extends SearchNode {
    private final List<GameAction> actions;
    private final List<Integer> actionIndices;
    private final List<ChanceNode> children;

    ActionNode(SearchContext searchContext, SearchState searchState, List<GameAction> actions) {
        super(searchContext, searchState);
        this.actions = actions;
        this.children = new ArrayList<>();

        // TODO: Needed?
        actionIndices = new ArrayList<Integer>();
        for (int i = 0; i < actions.size(); i++)
            actionIndices.add(i);
    }

    List<GameAction> getActions() {
        return actions;
    }

    List<ChanceNode> getChildren() {
        return children;
    }

    @Override
    boolean getEndSelection() {
        return !actionIndices.isEmpty();
    }

    @Override
    SearchNode select(ITreePolicy policy, List<SearchNode> visited) {
        if (actionIndices.isEmpty()) {
            return policy.select(this);
        } else {
            return expand();
        }
    }

    private ChanceNode expand() {
        int indexIndex = ThreadLocalRandom.current().nextInt(actionIndices.size());
        int index = actionIndices.get(indexIndex);
        actionIndices.remove(indexIndex);

        GameAction action = actions.get(index);
        ChanceNode child = new ChanceNode(getSearchContext(), getSearchState(), action, index);
        children.add(child);
        return child;
    }

    int getBestActionIndex() {
        int best = -1;
        int bestScore = Integer.MIN_VALUE;
        for (ChanceNode child : children) {
            if (child.getScore() > bestScore) {
                best = child.getActionIndex();
                bestScore = child.getScore();
            }
        }

        return best;
    }

    void process(ITreePolicy treePolicy) {
        List<SearchNode> visited = new LinkedList<>();
        SearchNode current = this;
        visited.add(this);
        while (!current.isTerminal()) {
            boolean endSelection = current.getEndSelection();
            current = current.select(treePolicy, visited);
            visited.add(current);
            if (endSelection)
                break;
        }

        int winner = rollOut(current);
        for (SearchNode node : visited) {
            node.updateStats(winner);
        }
    }

    private int rollOut(SearchNode node) {
        if (node.getGameContext().gameDecided()) {
            GameContext state = node.getGameContext();
            return state.getWinningPlayerId();
        }

        GameContext simulation = node.getGameContext().clone();
        for (Player player : simulation.getPlayers()) {
            player.setBehaviour(new PlayRandomBehaviour());
        }

        while (!simulation.gameDecided()) {
            simulation.startTurn(simulation.getActivePlayerId());
            while (simulation.playTurn()) {}
            if (simulation.getTurn() > GameLogic.TURN_LIMIT) {
                break;
            }
        }

        return simulation.getWinningPlayerId();
    }

    @Override
    void dump(int level) {
        super.dump(level);
        System.out.println(children.size() + " of " + actions.size() +
                           " :: p" + getGameContext().getActivePlayerId() +
                           ", " + getGameContext().getActivePlayer().getMana() +
                           "mana, " + getGameContext().getActivePlayer().getHero().getHp() +
                           " --> " + children.size() +
                           " (hash: " + getSearchState().hashCode() + ")");
        for (ChanceNode child : children) {
            child.dump(level + 1);
        }
    }
}
