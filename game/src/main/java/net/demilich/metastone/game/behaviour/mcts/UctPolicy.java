package net.demilich.metastone.game.behaviour.mcts;

import java.util.Random;

class UctPolicy implements ITreePolicy {

	private final double EPSILON = 1e-7;
	private final Random random = new Random();

	private final double C = 0.75;

	@Override
	public ChanceNode select(ActionNode parent) {
		ChanceNode selected = null;
		double bestValue = Double.NEGATIVE_INFINITY;
		for (ChanceNode child : parent.getChildren()) {
			if (child.getVisits() == 0) {
				boolean x = false;

			}
			//assert child.getVisits() != 0;

			double uctValue = child.getScore() / (double) child.getVisits() + C * Math.sqrt(Math.log(parent.getVisits()) / child.getVisits())
							  + random.nextDouble() * EPSILON;

			// small random number to break ties randomly in unexpanded nodes
			if (uctValue > bestValue) {
				selected = child;
				bestValue = uctValue;
			}
		}

		if (selected == null) {
			boolean x;
		}
		//assert selected != null;
		return selected;
	}

}
