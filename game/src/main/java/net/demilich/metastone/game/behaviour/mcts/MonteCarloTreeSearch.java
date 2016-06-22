package net.demilich.metastone.game.behaviour.mcts;

import java.util.ArrayList;
import java.util.List;


import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;

public class MonteCarloTreeSearch extends Behaviour {
	//private final static Logger logger = LoggerFactory.getLogger(MonteCarloTreeSearch.class);

	private static final int ITERATIONS = 20000;

    private int nesting = 0;

	@Override
	public String getName() {
		return "MCTS";
	}

	@Override
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		List<Card> discardedCards = new ArrayList<>();
		for (Card card : cards) {
			if (card.getBaseManaCost() >= 4) {
				discardedCards.add(card);
			}
		}
		return discardedCards;
	}

	@Override
	public GameAction requestAction(GameContext gameContext, Player player, List<GameAction> validActions) {
		assert nesting == 0;

		/*for (GameAction action : validActions) {
			System.out.println("ACTION:" + action.toString());
		}
		System.out.println("ACTIONS DONE");*/

		if (validActions.isEmpty())
			return null;

		if (validActions.size() == 1) {
			// logger.info("MCTS selected best action {}", validActions.get(0));
			return validActions.get(0);
		}

		nesting++;

		SearchContext searchContext = new SearchContext();
		ActionNode root = searchContext.addActionNode(new SearchState(gameContext), validActions);

		UctPolicy treePolicy = new UctPolicy();
		for (int i = 0; i < ITERATIONS; i++) {
			root.process(treePolicy);
		}

		GameAction bestAction = validActions.get(root.getBestActionIndex());
		//root.dump(0);
		System.out.println("MCTS selected best action " + bestAction.toString());

		nesting--;

		return bestAction;
	}

}
