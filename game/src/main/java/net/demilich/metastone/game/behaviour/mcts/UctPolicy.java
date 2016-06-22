package net.demilich.metastone.game.behaviour.mcts;

import net.demilich.metastone.game.actions.GameAction;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Random;

class UctPolicy implements ITreePolicy {

	private static final double EPSILON = 1e-5;
	private static final Random random = new Random();

	private static final double C = 1 / Math.sqrt(2);

	@Override
	public ChanceNode select(ActionNode parent) {
		ChanceNode selected = null;
		double bestValue = Double.NEGATIVE_INFINITY;
		for (ChanceNode child : parent.getChildren()) {
			double uctValue = child.getVisits() == 0 ? 1000000
					: child.getScore() / (double) child.getVisits() + C * Math.sqrt(Math.log(parent.getVisits()) / child.getVisits())
							+ random.nextDouble() * EPSILON;

			// small random number to break ties randomly in unexpanded nodes
			if (uctValue > bestValue) {
				selected = child;
				bestValue = uctValue;
			}
		}
		assert selected != null;
		return selected;
	}

}
