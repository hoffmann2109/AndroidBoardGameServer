package model.cards;

// Can be used later if a card of this type will be added to the game

import lombok.Data;
import model.Game;

@Data
public class VariablePayCard extends Card {

    @Override
    public void apply(Game game, String playerId) {

    }
}