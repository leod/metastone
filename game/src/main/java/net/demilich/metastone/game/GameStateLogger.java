package net.demilich.metastone.game;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;

import java.io.*;

public class GameStateLogger {
    private Gson gson = new Gson();
    private JsonWriter writer;
    private JsonArray history = new JsonArray();

    public GameStateLogger(String filename) throws IOException {
        writer = new JsonWriter(new FileWriter(new File(filename)));
        writer.setIndent("  ");
        //writer.beginArray();
        startGameLog();
    }

    private void startGameLog() throws IOException {
        writer.beginObject();
        writer.name("turns");
        writer.beginArray();
    }

    private void endGameLog(GameContext context) throws IOException {
        if (writer == null)
            return;

        writer.endArray();
        writer.name("winner").value(context.getWinningPlayerId());
        writer.endObject();
        writer.close();

        writer = null;
    }

    private int heroClassToInt(HeroClass c) {
        switch (c) {
            case DRUID:
                return 0;
            case HUNTER:
                return 1;
            case MAGE:
                return 2;
            case PALADIN:
                return 3;
            case PRIEST:
                return 4;
            case ROGUE:
                return 5;
            case SHAMAN:
                return 6;
            case WARLOCK:
                return 7;
            case WARRIOR:
                return 8;
        }

        throw new RuntimeException("Invalid hero class for game state logger");
    }

    private void writeMinion(Minion minion) throws IOException {
        writer.beginObject();
        writer.name("card_id").value(minion.getSourceCard().getCardId());
        writer.name("attack").value(minion.getAttack());
        writer.name("hp").value(minion.getHp());
        writer.name("max_hp").value(minion.getMaxHp());
        writer.name("stealth").value((Integer) minion.getAttribute(Attribute.STEALTH));
        writer.name("divine_shield").value((Integer) minion.getAttribute(Attribute.DIVINE_SHIELD));
        writer.name("taunt").value((Integer) minion.getAttribute(Attribute.TAUNT));
        writer.name("windfury").value((Integer) minion.getAttribute(Attribute.WINDFURY));
        writer.name("frozen").value((Integer) minion.getAttribute(Attribute.FROZEN));
        writer.name("silenced").value((Integer) minion.getAttribute(Attribute.SILENCED));
        writer.endObject();
    }

    private void writeWeapon(Weapon weapon) throws IOException {
        writer.beginObject();
        writer.name("card_id").value(weapon.getSourceCard().getCardId());
        writer.name("attack").value(weapon.getAttack());
        writer.name("durability").value(weapon.getDurability());
        writer.endObject();
    }

    private void writePlayer(Player player) throws IOException {
        writer.beginObject();
        writer.name("hp").value(player.getHero().getHp());
        writer.name("armor").value(player.getHero().getArmor());
        writer.name("mana").value(player.getMaxMana());

        writer.name("minions");
        writer.beginArray();
        for (Minion minion : player.getMinions())
            writeMinion(minion);
        writer.endArray();

        writer.name("hand");
        writer.beginArray();
        for (Card card : player.getHand())
            writer.value(card.getCardId());
        writer.endArray();

        writer.name("deck");
        writer.beginArray();
        for (Card card : player.getDeck())
            writer.value(card.getCardId());
        writer.endArray();

        Weapon weapon = null;
        for (Entity entity : player.getSetAsideZone()) {
            if (entity instanceof Weapon) {
                assert weapon == null;
                weapon = (Weapon) entity;
            }
        }

        writer.name("weapon");
        if (weapon != null)
            writeWeapon(weapon);
        else
            writer.nullValue();

        writer.endObject();
    }

    private void writeContext(GameContext context) throws IOException {
        writer.beginObject();
        writer.name("turn").value(context.getTurn());
        writer.name("active_player_id").value(context.getActivePlayerId());
        writer.name("players");

        writer.beginArray();
        for (Player player : context.getPlayers())
            writePlayer(player);
        writer.endArray();

        writer.endObject();
    }

    public void log(GameContext context) {
        try {
            if (context.gameDecided()) {
                endGameLog(context);
            } else
                writeContext(context);
        } catch (IOException exception) {
            System.err.println("Error while writing game log: " + exception.toString());
        }
    }
}
