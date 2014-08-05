package net.pferdimanzug.hearthstone.analyzer.game.behaviour.heuristic;

import net.pferdimanzug.hearthstone.analyzer.game.GameContext;
import net.pferdimanzug.hearthstone.analyzer.game.GameTag;
import net.pferdimanzug.hearthstone.analyzer.game.Player;
import net.pferdimanzug.hearthstone.analyzer.game.entities.minions.Minion;

public class WeightedHeuristic implements IGameStateHeuristic {

	private int calculateMinionScore(Minion minion) {
		float minionScore = minion.getAttack() + minion.getHp();
		if (minion.hasTag(GameTag.TAUNT)) {
			minionScore += 2;
		}
		if (minion.hasTag(GameTag.WINDFURY)) {
			minionScore += minion.getAttack() * 0.5f;
		}
		if (minion.hasTag(GameTag.DIVINE_SHIELD)) {
			minionScore *= 1.5f;
		}
		if (minion.hasTag(GameTag.SPELL_POWER)) {
			minionScore += minion.getTagValue(GameTag.SPELL_POWER);
		}
		if (minion.hasTag(GameTag.ENRAGED)) {
			minionScore += 1;
		}
		if (minion.hasTag(GameTag.STEALTHED)) {
			minionScore += 1;
		}
		if (minion.hasTag(GameTag.DEATHRATTLES)) {
			minionScore += 1;
		}
		return (int) minionScore;
	}
	
	@Override
	public int getScore(GameContext context, int playerId) {
		int score = 0;
		Player player = context.getPlayer(playerId);
		Player opponent = context.getOpponent(player);
		if (player.getHero().isDead()) {
			return Integer.MIN_VALUE;
		} 
		if (opponent.getHero().isDead()) {
			return Integer.MAX_VALUE;
		} 
		int ownHp = player.getHero().getHp() + player.getHero().getArmor();
		int opponentHp = opponent.getHero().getHp() + opponent.getHero().getArmor();
		score += ownHp - opponentHp;
		
		score += player.getHand().getCount();
		score -= opponent.getHand().getCount();
		score += player.getMinions().size();
		score -= player.getMinions().size();
		for (Minion minion : player.getMinions()) {
			score += calculateMinionScore(minion);
		}
		for (Minion minion : opponent.getMinions()) {
			score -= calculateMinionScore(minion);
		}
		
		score += player.getHero().getAttack();
		score -= opponent.getHero().getAttack();
		
		return score;
	}

}