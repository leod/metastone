package net.demilich.metastone.game.cards.desc;

import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;

public class Desc<T> {

	protected final Map<T, Object> arguments;

	public Desc(Map<T, Object> arguments) {
		this.arguments = arguments;
	}

	public boolean contains(T arg) {
		return arguments.containsKey(arg);
	}

	public Object get(T arg) {
		return arguments.get(arg);
	}

	public boolean getBool(T arg) {
		return arguments.containsKey(arg) ? (boolean) get(arg) : false;
	}

	public int getInt(T arg) {
		return arguments.containsKey(arg) ? (int) get(arg) : 0;
	}

	public String getString(T arg) {
		return arguments.containsKey(arg) ? (String) get(arg) : "";
	}
	
	public int getValue(T arg, GameContext context, Player player, Entity target, Entity host, int defaultValue) {
		Object storedValue = arguments.get(arg);
		if (storedValue == null) {
			return defaultValue;
		}
		if (storedValue instanceof ValueProvider) {
			ValueProvider valueProvider = (ValueProvider) storedValue;
			return valueProvider.getValue(context, player, target, host);
		}
		return (int)storedValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Desc<?> desc = (Desc<?>) o;

		return arguments != null ? arguments.equals(desc.arguments) : desc.arguments == null;

	}

	@Override
	public int hashCode() {
		return arguments != null ? arguments.hashCode() : 0;
	}
}
