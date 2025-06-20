package model.cards;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import at.aau.serg.monopoly.websoket.CardDeckService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.util.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CardDeckServiceTest {
    private ObjectMapper realMapper = new ObjectMapper();

    private CardDeckService service;
    private Map<CardType, Deque<Card>> decks;
    private Map<CardType, List<Card>> discards;
    private CardType sampleType;

    @BeforeAll
    void initService() throws Exception {
        service = new CardDeckService(realMapper);
        service.init();

        Field decksF = CardDeckService.class.getDeclaredField("decks");
        Field discF  = CardDeckService.class.getDeclaredField("discards");
        decksF.setAccessible(true);
        discF.setAccessible(true);

        decks    = (Map<CardType, Deque<Card>>) decksF.get(service);
        discards = (Map<CardType, List<Card>>) discF.get(service);

        sampleType = CardType.values()[0];
    }

    @Test
    @Order(1)
    void init_populatesAllDecksAndLeavesDiscardsEmpty() {
        for (CardType type : CardType.values()) {
            Deque<Card> deck = decks.get(type);
            List<Card> discard = discards.get(type);

            assertNotNull(deck,    "deck map missing key " + type);
            assertFalse(deck.isEmpty(), "deck for " + type + " should not be empty");

            assertNotNull(discard, "discard map missing key " + type);
            assertTrue(discard.isEmpty(), "discards for " + type + " should start empty");
        }
    }

    @Test
    @Order(2)
    void drawCard_movesOneCardFromDeckToDiscards() {
        int beforeSize = decks.get(sampleType).size();
        Card drawn = service.drawCard(sampleType);

        assertNotNull(drawn);
        assertEquals(beforeSize - 1, decks.get(sampleType).size(),
                "draw removes exactly one card from deck");
        assertEquals(1, discards.get(sampleType).size(),
                "drawn card should appear in discards");
        assertSame(drawn, discards.get(sampleType).get(0),
                "the same card object should be moved to discards");
    }

    @Test
    @Order(3)
    void drawCard_whenDeckEmpty_reshufflesDiscardsBackIntoDeck() {
        Deque<Card> deck = decks.get(sampleType);
        List<Card> discard = discards.get(sampleType);

        while (!deck.isEmpty()) {
            discard.add(deck.pop());
        }
        assertTrue(deck.isEmpty(), "deck must now be empty");
        assertTrue(discard.size() > 1, "discard must contain multiple cards");

        Card next = service.drawCard(sampleType);
        assertNotNull(next);

        assertEquals(1, discard.size(),
                "after reshuffle and draw, discards should contain only the drawn card");
        assertSame(next, discard.get(0),
                "the newly drawn card should now live in discards");
    }
}
