package net.demilich.metastone.game.behaviour.mcts;

interface ITreePolicy {
	ChanceNode select(ActionNode parent);
}
