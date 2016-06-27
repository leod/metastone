package net.demilich.metastone.game.behaviour.mcts;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.logic.GameLogic;

public class MonteCarloTreeSearch extends Behaviour {
	//private final static Logger logger = LoggerFactory.getLogger(MonteCarloTreeSearch.class);

	private static final int ITERATIONS = 500;

	private ExecutorService executor;

    private int nesting = 0;

	public MonteCarloTreeSearch() {
		executor = Executors.newFixedThreadPool(4);
	}

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

        System.out.println("ACTIONS:" + validActions.toString());

		if (validActions.isEmpty())
			return null;

		if (validActions.size() == 1) {
			// logger.info("MCTS selected best action {}", validActions.get(0));
			return validActions.get(0);
		}

		nesting++;

		int nEnsembles = 8;

		ActionNode[] roots = new ActionNode[nEnsembles];

		List<Future<Void>> futures = new ArrayList<Future<Void>>();

		GameContext[] context = new GameContext[nEnsembles];

		for (int i = 0; i < nEnsembles; i++) {
			UctPolicy treePolicy = new UctPolicy();
			SearchContext searchContext = new SearchContext(treePolicy, 0.9);
			GameContext detGameContext = gameContext.determinize();
			context[i] = detGameContext;
			SearchState searchState = new SearchState(detGameContext);
			roots[i] = searchContext.getActionNode(searchState, validActions);

			futures.add(executor.submit(new ProcessTask(roots[i])));
		}

		boolean completed = false;
		while (!completed) {
			completed = true;
			for (Future<Void> future : futures) {
				if (!future.isDone()) {
					completed = false;
					continue;
				}
				try {
					future.get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
			futures.removeIf(future -> future.isDone());
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("");

		/*for (int i = 0; i < nEnsembles; i++) {
			System.out.println("* TREE #" + i);
			roots[i].dump(1);
		}*/

		double[] actionScores = new double[validActions.size()];
		double[] actionVisits = new double[validActions.size()];

        for (int i = 0; i < nEnsembles; i++) {
			for (ChanceNode child : roots[i].getChildren()) {
				if (child.getActionIndex() >= validActions.size()) {
					System.out.println("IT HAPPENED: " + child.getActionIndex() + " vs " + validActions.size());
					System.out.println(roots[i].getActions());
					System.out.println(child.getAction());
				}

				actionScores[child.getActionIndex()] += child.getScore();
				actionVisits[child.getActionIndex()] += child.getVisits();
			}
		}

		int[] votes = new int[validActions.size()];

		for (int i = 0; i < nEnsembles; i++) {
			double bestReward = Float.NEGATIVE_INFINITY;
			int bestActionIndex = -1;

            for (ChanceNode child : roots[i].getChildren()) {
				double reward = child.getScore() / child.getVisits();
				if (reward > bestReward) {
					bestActionIndex = child.getActionIndex();
					bestReward = reward;
				}
			}

			votes[bestActionIndex]++;
		}

		int maxVotes = -1;
		int maxVisits = -1;
		int maxReward = -1;
		for (int i = 0; i < validActions.size(); i++) {
            if (maxVotes == -1
					|| votes[maxVotes] < votes[i]
					|| (votes[maxVotes] == votes[i]
						&& actionScores[maxVotes] / actionVisits[maxVotes] < actionScores[i] / actionVisits[i]))
                maxVotes = i;
			if (maxVisits == -1 || actionVisits[maxVisits] < actionVisits[i])
				maxVisits = i;
			if (maxReward == -1 || (actionScores[maxReward] / actionVisits[maxReward] < actionScores[i] / actionVisits[i]))
				maxReward = i;
		}

		System.out.print("VOTES: ");
		for (int i = 0; i < validActions.size(); i++) {
			if (i == maxVotes) System.out.print("[");
			System.out.print(votes[i]);
			if (i == maxVotes) System.out.print("]");
			System.out.print(" ");
		}
		System.out.println("");

		System.out.print("VISITS: ");
		for (int i = 0; i < validActions.size(); i++) {
			if (i == maxVisits) System.out.print("[");
			System.out.print(actionVisits[i]);
			if (i == maxVisits) System.out.print("]");
			System.out.print(" ");
		}
		System.out.println("");

		System.out.print("REWARDS: ");
		for (int i = 0; i < validActions.size(); i++) {
			if (i == maxReward) System.out.print("[");
			System.out.print(new DecimalFormat().format(actionScores[i] / actionVisits[i]));
			if (i == maxReward) System.out.print("]");
			System.out.print(" ");
		}
		System.out.println("");

		int choice = maxReward;

		System.out.println("RESULT: MCTS selected best action " + validActions.get(choice).toString());

		nesting--;

		return validActions.get(choice);
	}

	private class ProcessTask implements Callable<Void> {
		ActionNode root;

		public ProcessTask(ActionNode root) {
			this.root = root;
		}

		@Override
		public Void call() throws Exception {
			for (int k = 0; k < ITERATIONS; k++) {
                root.process();

				if (k % 1000 == 0)
					System.out.print(".");
			}

			return null;
		}
	}
}
