package net.demilich.metastone.game.behaviour.mcts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.GreedyOptimizeMove;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.behaviour.heuristic.WeightedHeuristic;
import net.demilich.metastone.game.behaviour.threat.GameStateValueBehaviour;
import net.demilich.metastone.game.logic.GameLogic;

class ActionNode extends SearchNode {
    private final List<GameAction> actions;
    private final List<Integer> actionIndices;
    private final List<ChanceNode> children;

    int useCount = 0;

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
    SearchNode select(List<SearchNode> visited) {
        if (actionIndices.isEmpty()) {
            return getSearchContext().getTreePolicy().select(this);
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
        double bestX = Float.NEGATIVE_INFINITY;
        for (ChanceNode child : children) {
            double x = child.getScore() / child.getVisits();
            if (x  > bestX) {
                best = child.getActionIndex();
                bestX = x;
            }
        }

        return best;
    }

    void process() {
        List<SearchNode> visited = new LinkedList<>();
        SearchNode current = this;
        visited.add(this);
        while (!current.isTerminal()) {
            boolean endSelection = current.getEndSelection();
            current = current.select(visited);
            visited.add(current);
            if (endSelection) {
                break;
            }

            if (visited.size() > 100) {
                boolean x = false;
                assert false;
            }
        }

        rollOut(current, visited);
    }

    int rollOut(SearchNode node, List<SearchNode> visited) {
        if (node.getGameContext().gameDecided()) {
            GameContext state = node.getGameContext();
            return state.getWinningPlayerId();
        }

        GameContext simulation = node.getGameContext().clone();
        for (Player player : simulation.getPlayers()) {
            //player.setBehaviour(new PlayRandomBehaviour());
            player.setBehaviour(new GreedyOptimizeMove(new WeightedHeuristic()));
        }

        while (!simulation.gameDecided()) {
            simulation.startTurn(simulation.getActivePlayerId());
            while (simulation.playTurn()) {}
            if (simulation.getTurn() > GameLogic.TURN_LIMIT) {
                break;
            }
        }

        int winner = simulation.getWinningPlayerId();
        for (SearchNode v : visited) {
            v.updateStats(winner, simulation.getTurn());
        }

        return simulation.getWinningPlayerId();
    }

    @Override
    void dump(int level) {
        super.dump(level);
        System.out.println(children.size() + " of " + actions.size() +
                           " :: p" + getGameContext().getActivePlayerId() +
                           " --> " + children.size() +
                           " (hash: " + getSearchState().hashCode() + ", use count: " + useCount + ")");
        for (ChanceNode child : children) {
            child.dump(level + 1);
        }
    }
}
