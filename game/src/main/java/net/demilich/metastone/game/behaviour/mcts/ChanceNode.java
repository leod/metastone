package net.demilich.metastone.game.behaviour.mcts;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class ChanceNode extends SearchNode {
    private final GameAction action;
    private int actionIndex;
    private final List<ActionNode> outcomeNodes = new ArrayList<>();

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

        for (ActionNode child : outcomeNodes) {
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
        GameContext nextGameContext = getGameContext().clone();
        //assert(nextGameContext.equals(getGameContext()));
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
                System.out.println("asdf " + action);
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
