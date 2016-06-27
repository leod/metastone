package net.demilich.metastone.game.behaviour.mcts;

import java.text.DecimalFormat;
import java.util.List;

import net.demilich.metastone.game.GameContext;

abstract class SearchNode {
    private final SearchContext searchContext;
    private final SearchState searchState;

    private int visits;
    private double score;

    SearchNode(SearchContext searchContext, SearchState searchState) {
        this.searchContext = searchContext;
        this.searchState = searchState;
    }

    void updateStats(int winner, int turns) {
        double reward = Math.pow(searchContext.getRewardDiscount(), turns);

        visits++;
        if (getPlayer() == winner)
            score += reward;
        else
            score -= reward;
    }

    SearchContext getSearchContext() {
        return searchContext;
    }

    SearchState getSearchState() {
        return searchState;
    }

    GameContext getGameContext() {
        return searchState.getGameContext();
    }

    int getPlayer() {
        return getGameContext().getActivePlayerId();
    }

    double getScore() {
        return score;
    }

    int getVisits() {
        return visits;
    }

    boolean isTerminal() {
        return getGameContext().gameDecided();
    }

    abstract boolean getEndSelection();
    abstract SearchNode select(List<SearchNode> visited);

    void dump(int level) {
        for (int i = 0; i < level; i++)
            System.out.print("\t");
        char c = this instanceof ChanceNode ? 'C' : 'A';
        System.out.print(c);
        System.out.print("[" + new DecimalFormat().format(this.score) + "/" + this.visits + "] ");
        System.out.print("(" + new DecimalFormat().format((this.score / this.visits)) + ") ");
    }
}
