package net.demilich.metastone.tests;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EqualityTests extends TestBase {
    @Test
    public void testDoomsayerHumility() {

        GameContext context = createContext(HeroClass.DRUID, HeroClass.ROGUE);

        GameContext ca = context.clone();
        GameContext cb = context.clone();
        Assert.assertEquals(ca, cb);

        {
            MinionCard doomsayer = (MinionCard) CardCatalogue.getCardById("minion_doomsayer");
            MinionCard wisp = (MinionCard) CardCatalogue.getCardById("minion_chillwind_yeti");
            SpellCard humility = (SpellCard) CardCatalogue.getCardById("spell_humility");

            Player druid = ca.getPlayer1();
            Player rogue = ca.getPlayer2();

            playMinionCard(ca, druid, wisp);
            playMinionCard(ca, druid, wisp);
            playMinionCard(ca, druid, doomsayer);
            ca.endTurn();
            ca.startTurn(1);

            playCardWithTarget(ca, rogue, humility, druid.getMinions().get(0));
            ca.endTurn();
            ca.startTurn(0);
        }
        //cb = createContext(HeroClass.DRUID, HeroClass.ROGUE);

        {
            MinionCard doomsayer = (MinionCard) CardCatalogue.getCardById("minion_doomsayer");
            MinionCard wisp = (MinionCard) CardCatalogue.getCardById("minion_chillwind_yeti");
            SpellCard humility = (SpellCard) CardCatalogue.getCardById("spell_humility");

            Player druid = cb.getPlayer1();
            Player rogue = cb.getPlayer2();

            playMinionCard(cb, druid, wisp);
            playMinionCard(cb, druid, wisp);
            playMinionCard(cb, druid, doomsayer);
            cb.endTurn();
            cb.startTurn(1);

            playCardWithTarget(cb, rogue, humility, druid.getMinions().get(1));
            cb.endTurn();
            cb.startTurn(0);
        }

        Assert.assertEquals(ca, cb);
    }
}
