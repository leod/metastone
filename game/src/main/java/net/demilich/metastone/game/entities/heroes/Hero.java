package net.demilich.metastone.game.entities.heroes;

import java.util.EnumMap;
import java.util.Map;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.heroes.powers.HeroPower;

public class Hero extends Actor {

	private HeroClass heroClass;
	private HeroPower heroPower;
	private Weapon weapon;

	public Hero(HeroCard heroCard, HeroPower heroPower) {
		super(heroCard);
		setName(heroCard.getName());
		this.setHeroClass(heroCard.getHeroClass());
		this.setHeroPower(heroPower);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		Hero hero = (Hero) o;

		if (heroClass != hero.heroClass) return false;
		if (heroPower != null ? !heroPower.equals(hero.heroPower) : hero.heroPower != null) return false;
		return weapon != null ? weapon.equals(hero.weapon) : hero.weapon == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (heroClass != null ? heroClass.hashCode() : 0);
		result = 31 * result + (heroPower != null ? heroPower.hashCode() : 0);
		result = 31 * result + (weapon != null ? weapon.hashCode() : 0);
		return result;
	}

	public void activateWeapon(boolean active) {
		if (weapon != null) {
			weapon.setActive(active);
		}
	}

	@Override
	public Hero clone() {
		Hero clone = (Hero) super.clone();
		if (weapon != null) {
			clone.setWeapon(getWeapon().clone());
		}
		
		clone.setHeroPower((HeroPower) getHeroPower().clone());
		return clone;
	}

	public int getArmor() {
		return getAttributeValue(Attribute.ARMOR);
	}

	@Override
	public int getAttack() {
		int attack = super.getAttack();
		if (weapon != null && weapon.isActive()) {
			attack += weapon.getWeaponDamage();
		}
		return attack;
	}

	public Map<Attribute, Object> getAttributesCopy() {
		Map<Attribute, Object> copy = new EnumMap<>(Attribute.class);
		for (Attribute attribute : attributes.keySet()) {
			if (attribute != Attribute.COMBO) {
				continue;
			}
			copy.put(attribute, attributes.get(attribute));
		}
		return copy;
	}

	public int getEffectiveHp() {
		return getHp() + getArmor();
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.HERO;
	}

	public HeroClass getHeroClass() {
		return heroClass;
	}

	public HeroPower getHeroPower() {
		return heroPower;
	}

	public Weapon getWeapon() {
		return weapon;
	}

	public void modifyArmor(int armor) {
		// armor cannot fall below zero
		int newArmor = Math.max(getArmor() + armor, 0);
		setAttribute(Attribute.ARMOR, newArmor);
	}
	
	public void setHeroClass(HeroClass heroClass) {
		this.heroClass = heroClass;
	}

	public void setHeroPower(HeroPower heroPower) {
		this.heroPower = heroPower;
		heroPower.setOwner(getOwner());
	}

	@Override
	public void setOwner(int ownerIndex) {
		super.setOwner(ownerIndex);
		heroPower.setOwner(ownerIndex);
	}

	public void setWeapon(Weapon weapon) {
		this.weapon = weapon;
		if (weapon != null) {
			weapon.setOwner(getOwner());
		}
	}

}
