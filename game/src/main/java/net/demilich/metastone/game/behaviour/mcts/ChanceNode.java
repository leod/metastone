package net.demilich.metastone.game.behaviour.mcts;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

class ChanceNode extends SearchNode {
    private static final int MAX_OUTCOMES = 26;

    private final GameAction action;
    private int actionIndex;
    final List<ActionNode> outcomeNodes = new ArrayList<>();

    ChanceNode(SearchContext searchContext, SearchState searchState, GameAction action, int actionIndex) {
        super(searchContext, searchState);
        this.action = action;
        this.actionIndex = actionIndex;
    }

    GameAction getAction() {
        return action;
    }

    int getActionIndex() {
        return actionIndex;
    }

    ActionNode getOutcomeNode(GameContext nextGameContext, List<GameAction> actions) {
        SearchState nextSearchState = new SearchState(nextGameContext);

        boolean isDiscover = (!actions.isEmpty() && actions.get(0).getActionType() == ActionType.DISCOVER);

        if (isDiscover) {
            for (GameAction action : actions)
                assert action.getActionType() == ActionType.DISCOVER;
        }

        for (ActionNode child : outcomeNodes) {
            // For discover, we also have to filter by the given choices
            if (isDiscover && !actions.equals(child.getActions()))
                continue;

            if (nextSearchState.equals(child.getSearchState()))
                return child;
        }

        ActionNode child = getSearchContext().addActionNode(nextSearchState, actions);
        outcomeNodes.add(child);

        return child;
    }

    @Override
    boolean getEndSelection() {
        return false;
    }

    @Override
    SearchNode select(ITreePolicy policy, List<SearchNode> visited) {
        if (outcomeNodes.size() >= MAX_OUTCOMES)
            return outcomeNodes.get(ThreadLocalRandom.current().nextInt(outcomeNodes.size()));

        GameContext nextGameContext = getGameContext().clone();
        /*if (!nextGameContext.equals(getGameContext())) {
            boolean x = nextGameContext.equals(getGameContext());
        }*/

        TreeBehaviour behaviour = new TreeBehaviour(policy, this, visited);

        try {
            nextGameContext.getActivePlayer().setBehaviour(behaviour);

            nextGameContext.getLogic().performGameAction(nextGameContext.getActivePlayerId(), action);
            if (action.getActionType() == ActionType.END_TURN)
                nextGameContext.startTurn(nextGameContext.getActivePlayerId());

            /*if (action.getActionType() == ActionType.SUMMON) {
                GameContext nextGameContext2 = getGameContext().clone();
                nextGameContext2.getLogic().performGameAction(nextGameContext2.getActivePlayerId(), action);

                if (!nextGameContext.equals(nextGameContext2)) {
                    boolean x;
                }
            }*/

            if (nextGameContext.getValidActions().isEmpty()) {
                //System.out.println("asdf " + action);
                nextGameContext.endTurn();
                nextGameContext.startTurn(nextGameContext.getActivePlayerId());
            }
        } catch (Exception e) {
            System.err.println("Exception on action: " + action);
            e.printStackTrace();
            throw e;
        }

        if (behaviour.getSelection() != null)
            return behaviour.getSelection();
        else
            return getOutcomeNode(nextGameContext, nextGameContext.getValidActions());
    }

    @Override
    void dump(int level) {
        super.dump(level);
        System.out.println(action + " ==> " + outcomeNodes.size());
        for (ActionNode outcome : outcomeNodes)
            outcome.dump(level + 1);
    }
}
