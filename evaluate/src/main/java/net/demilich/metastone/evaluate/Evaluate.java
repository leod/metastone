package net.demilich.metastone.evaluate;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.behaviour.heuristic.IGameStateHeuristic;
import net.demilich.metastone.game.behaviour.heuristic.NeuralNetworkHeuristic;
import net.demilich.metastone.game.behaviour.mcts.MonteCarloTreeSearch;
import net.demilich.metastone.game.behaviour.threat.FeatureVector;
import net.demilich.metastone.game.behaviour.threat.GameStateValueBehaviour;
import net.demilich.metastone.game.behaviour.threat.ThreatBasedHeuristic;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.heroes.MetaHero;
import net.demilich.metastone.game.gameconfig.GameConfig;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;

public class Evaluate {
    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            System.out.println("usage: behaviour1 behaviour2 n deck1 deck2");
            return;
        }

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        CardCatalogue.loadCards();

        int num = new Integer(args[2]);
        String nb1 = args[0];
        String nb2 = args[1];
        String fdeck1 = args[3];
        String fdeck2 = args[4];

        Behaviour b1 = newBehaviour(nb1);
        Behaviour b2 = newBehaviour(nb2);
        Deck deck1 = loadDeck(fdeck1);
        Deck deck2 = loadDeck(fdeck2);

        PlayerConfig p1 = new PlayerConfig();
        p1.setBehaviour(b1);
        p1.setDeck(deck1);
        p1.setName(b1.getName());
        p1.setHeroCard(MetaHero.getHeroCard(deck1.getHeroClass()));

        PlayerConfig p2 = new PlayerConfig();
        p2.setBehaviour(b2);
        p2.setDeck(deck2);
        p2.setName(b2.getName());
        p2.setHeroCard(MetaHero.getHeroCard(deck2.getHeroClass()));

        // TODO: ???
        DeckFormat deckFormat = new DeckFormat();
        for (CardSet set : CardSet.values()) {
            deckFormat.addSet(set);
        }

        GameConfig gameConfig = new GameConfig();
        gameConfig.setNumberOfGames(num);
        gameConfig.setPlayerConfig1(p1);
        gameConfig.setPlayerConfig2(p2);
        gameConfig.setDeckFormat(deckFormat);

        int p1wins = new SimulateGames().execute(gameConfig);
        int p2wins = num - p1wins;

        System.out.println(b1.getName() + ": " + p1wins);
        System.out.println(b2.getName() + ": " + p2wins);
    }

    static Deck loadDeck(String filename) throws IOException {
        JsonObject o = new JsonParser().parse(new FileReader(filename)).getAsJsonObject();
        HeroClass heroClass = HeroClass.valueOf(o.get("heroClass").getAsString());

        Deck deck = new Deck(heroClass, false);
        for (JsonElement cardId : o.get("cards").getAsJsonArray()) {
            Card card = CardCatalogue.getCardById(cardId.getAsString());
            assert card != null;
            deck.getCards().add(card);
        }

        return deck;
    }

    static Behaviour newBehaviour(String name) throws IOException {
        if (name.startsWith("gsvnn:")) {
            String fmodel = name.substring("gsvnn:".length());
            IGameStateHeuristic heuristic = new NeuralNetworkHeuristic(fmodel);
            return new GameStateValueBehaviour(heuristic, "NN");
        } else if (name.equals("gsv")) {
            //return new GameStateValueBehaviour();
            return new GameStateValueBehaviour(new ThreatBasedHeuristic(FeatureVector.getFittest()), "THREAT");
        } else if (name.equals("mcts")) {
            return new MonteCarloTreeSearch();
        } else if (name.equals("random")) {
            return new PlayRandomBehaviour();
        }

        return null;
    }
}
