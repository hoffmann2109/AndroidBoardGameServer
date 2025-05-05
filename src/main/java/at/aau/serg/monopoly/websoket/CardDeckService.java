package at.aau.serg.monopoly.websoket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import model.cards.Card;
import model.cards.CardType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.*;

@Component
public class CardDeckService {
    private final ObjectMapper mapper;
    private final Map<CardType, Deque<Card>> decks = new EnumMap<>(CardType.class);
    private final Map<CardType, List<Card>> discards = new EnumMap<>(CardType.class);

    public CardDeckService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    // Construct the card decks, load & shuffle them
    @PostConstruct
    public void init() {
        try {
            // 1. Read the JSON file into a Map<String,List<Card>>
            var resource = new ClassPathResource("ChanceAndChestCards.json");
            TypeReference<Map<String, List<Card>>> typeRef = new TypeReference<>() {};
            Map<String, List<Card>> raw =
                    mapper.readValue(resource.getInputStream(), typeRef);

            // 2. For each entry (e.g. "CHANCE" → [Card,...])
            //    convert key to CardType, shuffle, wrap in Deque
            for (var entry : raw.entrySet()) {
                CardType type = CardType.valueOf(entry.getKey());
                List<Card> list = new ArrayList<>(entry.getValue());
                Collections.shuffle(list);
                decks.put(type, new ArrayDeque<>(list));
                discards.put(type, new ArrayList<>());
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load ChanceAndChestCards.json", e);
        }
    }

    /** Draws the next card from the given deck, reshuffling discards if empty. */
    public synchronized Card drawCard(CardType type) {
        Deque<Card> deck = decks.get(type);
        if (deck.isEmpty()) {
            // move discards back into deck
            List<Card> pile = discards.get(type);
            Collections.shuffle(pile);
            deck.addAll(pile);
            pile.clear();
        }
        Card drawn = deck.pop();
        discards.get(type).add(drawn);
        return drawn;
    }
}

