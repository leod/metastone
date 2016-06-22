package net.demilich.metastone.game.behaviour.mcts;

import net.demilich.metastone.game.GameContext;

class SearchState {
    private int hashCode;
    private GameContext gameContext;

    SearchState(GameContext gameContext) {
        this.hashCode = gameContext.hashCode();
        this.gameContext = gameContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchState that = (SearchState) o;

        if (hashCode != that.hashCode) return false;
        return gameContext.equals(that.gameContext);

    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    GameContext getGameContext() {
        return gameContext;
    }
}