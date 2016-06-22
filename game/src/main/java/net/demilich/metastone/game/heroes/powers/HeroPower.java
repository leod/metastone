package net.demilich.metastone.game.heroes.powers;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.HeroPowerAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.cards.desc.HeroPowerCardDesc;
import net.demilich.metastone.game.targeting.CardLocation;

public class HeroPower extends SpellCard {

	private int used;

	public HeroPower(HeroPowerCardDesc desc) {
		super(desc);
		setLocation(CardLocation.HERO_POWER);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		HeroPower heroPower = (HeroPower) o;

		return used == heroPower.used;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + used;
		return result;
	}

	public int hasBeenUsed() {
		return used;
	}

	public void markUsed() {
		this.used++;
	}

	public void onWillUse(GameContext context, Player player) {

	}

	@Override
	public PlayCardAction play() {
		return new HeroPowerAction(getSpell(), this, getTargetRequirement());
	}
	
	public void setUsed(int used) {
		this.used = used;
	}

}
