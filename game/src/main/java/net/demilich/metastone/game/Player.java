package net.demilich.metastone.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.demilich.metastone.game.behaviour.IBehaviour;
import net.demilich.metastone.game.behaviour.human.HumanBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.statistics.GameStatistics;
import net.demilich.metastone.game.gameconfig.PlayerConfig;

public class Player extends CustomCloneable {

	private final String name;
	private Hero hero;
	private final String deckName;

	private final CardCollection deck;
	private final CardCollection hand = new CardCollection();
	private final List<Entity> setAsideZone = new ArrayList<>();
	private final List<Entity> graveyard = new ArrayList<>();
	private final List<Minion> minions = new ArrayList<>();
	private final HashSet<String> secrets = new HashSet<>();

	private final GameStatistics statistics = new GameStatistics();

	private int id = -1;

	private int mana;
	private int maxMana;
	private int lockedMana;

	private boolean hideCards;

	private IBehaviour behaviour;

	private Player(Player otherPlayer) {
		this.name = otherPlayer.name;
		this.deckName = otherPlayer.getDeckName();
		this.setHero(otherPlayer.getHero().clone());
		this.deck = otherPlayer.getDeck().clone();
		for (Minion minion : otherPlayer.getMinions()) {
			minions.add(minion.clone());
		}
		for (Card card : otherPlayer.hand) {
			this.hand.add(card.clone());
		}
		for (Entity entity : otherPlayer.graveyard) {
			this.graveyard.add((Entity) entity.clone());
		}
		for (Entity entity : otherPlayer.setAsideZone) {
			this.setAsideZone.add((Entity) entity.clone());
		}
		/*this.hand.addAll(otherPlayer.hand);
		this.graveyard.addAll(otherPlayer.graveyard);
		this.setAsideZone.addAll(otherPlayer.setAsideZone);*/
		this.secrets.addAll(otherPlayer.secrets);
		this.id = otherPlayer.id;
		this.mana = otherPlayer.mana;
		this.maxMana = otherPlayer.maxMana;
		this.lockedMana = otherPlayer.lockedMana;
		this.behaviour = otherPlayer.behaviour;
		this.getStatistics().merge(otherPlayer.getStatistics());
	}

	public Player(PlayerConfig config) {
		config.build();
		Deck selectedDeck = config.getDeckForPlay();
		this.name = config.getName();
		this.deck = selectedDeck.getCardsCopy();
		this.setHero(config.getHeroForPlay().createHero());
		this.deckName = selectedDeck.getName();
		setBehaviour(config.getBehaviour().clone());
		setHideCards(config.hideCards());
	}

	@Override
	public Player clone() {
		return new Player(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Player player = (Player) o;

		if (id != player.id) return false;
		if (mana != player.mana) return false;
		if (maxMana != player.maxMana) return false;
		if (lockedMana != player.lockedMana) return false;
		if (name != null ? !name.equals(player.name) : player.name != null) return false;
		if (hero != null ? !hero.equals(player.hero) : player.hero != null) return false;
		if (deckName != null ? !deckName.equals(player.deckName) : player.deckName != null) return false;
		if (deck != null ? !deck.equals(player.deck) : player.deck != null) return false;
		if (hand != null ? !hand.equals(player.hand) : player.hand != null) return false;
		if (setAsideZone != null ? !setAsideZone.equals(player.setAsideZone) : player.setAsideZone != null)
			return false;
		// TODO: Needs to be compared independently of order
		//if (graveyard != null ? !graveyard.equals(player.graveyard) : player.graveyard != null) return false;
		if (minions != null ? !minions.equals(player.minions) : player.minions != null) return false;
		return secrets != null ? secrets.equals(player.secrets) : player.secrets == null;
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (hero != null ? hero.hashCode() : 0);
		result = 31 * result + (deckName != null ? deckName.hashCode() : 0);
		result = 31 * result + (deck != null ? deck.hashCode() : 0);
		result = 31 * result + (hand != null ? hand.hashCode() : 0);
		result = 31 * result + (setAsideZone != null ? setAsideZone.hashCode() : 0);
		// TODO: Needs to be compared independently of order
		//result = 31 * result + (graveyard != null ? graveyard.hashCode() : 0);
		result = 31 * result + (minions != null ? minions.hashCode() : 0);
		result = 31 * result + (secrets != null ? secrets.hashCode() : 0);
		result = 31 * result + id;
		result = 31 * result + mana;
		result = 31 * result + maxMana;
		result = 31 * result + lockedMana;
		return result;
	}

	public IBehaviour getBehaviour() {
		return behaviour;
	}

	public List<Actor> getCharacters() {
		List<Actor> characters = new ArrayList<Actor>();
		characters.add(getHero());
		characters.addAll(getMinions());
		return characters;
	}

	public CardCollection getDeck() {
		return deck;
	}

	public String getDeckName() {
		return deckName;
	}

	public List<Entity> getGraveyard() {
		return graveyard;
	}

	public CardCollection getHand() {
		return hand;
	}

	public Hero getHero() {
		return hero;
	}

	public int getId() {
		return id;
	}

	public int getLockedMana() {
		return lockedMana;
	}

	public int getMana() {
		return mana;
	}

	public int getMaxMana() {
		return maxMana;
	}

	public List<Minion> getMinions() {
		return minions;
	}

	public String getName() {
		return "'" + name + "' (" + getHero().getName() + ")";
	}

	public HashSet<String> getSecrets() {
		return secrets;
	}
	
	public List<Entity> getSetAsideZone() {
		return setAsideZone;
	}

	public GameStatistics getStatistics() {
		return statistics;
	}

	public boolean hideCards() {
		return hideCards && !(behaviour instanceof HumanBehaviour);
	}

	public void setBehaviour(IBehaviour behaviour) {
		this.behaviour = behaviour;
	}

	public void setHero(Hero hero) {
		this.hero = hero;
	}

	public void setHideCards(boolean hideCards) {
		this.hideCards = hideCards;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setLockedMana(int lockedMana) {
		this.lockedMana = lockedMana;
	}

	public void setMana(int mana) {
		this.mana = mana;
	}

	public void setMaxMana(int maxMana) {
		this.maxMana = maxMana;
	}

	@Override
	public String toString() {
		return "[PLAYER " + "id: " + getId() + ", name: " + getName() + ", hero: " + getHero() + "]";
	}

}
