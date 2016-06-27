package net.demilich.metastone.game.logic;

import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.decks.Deck;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Determinization {
    private List<Integer> cardDrawIndices = new ArrayList<>();
    private int numDrawnCards = 0;

    public int drawCard(int deckSize) {
        if (numDrawnCards == cardDrawIndices.size()) {
            // Determine a new card draw
            int index = ThreadLocalRandom.current().nextInt(deckSize);
            cardDrawIndices.add(index);
            return index;
        } else {
            // Reuse previously determined card draw
            int index = cardDrawIndices.get(numDrawnCards) % deckSize;
            numDrawnCards++;
            return index;
        }
    }

    public int getNumDrawnCards() {
        return numDrawnCards;
    }

    @Override
    public Determinization clone() {
        Determinization d = new Determinization();

        // The list is shared between all clones of a determinization
        d.cardDrawIndices = cardDrawIndices;

        d.numDrawnCards = numDrawnCards;

        return d;
    }
}
