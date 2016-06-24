package net.demilich.metastone.game.behaviour.mcts;

import java.util.List;

import net.demilich.metastone.game.GameContext;

abstract class SearchNode {
    private final SearchContext searchContext;
    private final SearchState searchState;

    private int visits;
    private int score;

    SearchNode(SearchContext searchContext, SearchState searchState) {
        this.searchContext = searchContext;
        this.searchState = searchState;
    }

    void updateStats(int winner) {
        visits++;
        if (getPlayer() == winner)
            score++;
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

    int getScore() {
        return score;
    }

    int getVisits() {
        return visits;
    }

    boolean isTerminal() {
        return getGameContext().gameDecided();
    }

    abstract boolean getEndSelection();
    abstract SearchNode select(ITreePolicy policy, List<SearchNode> visited);

    void dump(int level) {
        for (int i = 0; i < level; i++)
            System.out.print("\t");
        System.out.print("[" + this.score + "/" + this.visits + "] ");
        System.out.print("(" + ((float) this.score / this.visits) + "%) ");
    }
}
