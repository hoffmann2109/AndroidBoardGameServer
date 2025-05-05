package model.cards;

import model.Game;
import model.Player;

public class MoveCard extends Card {
    private Integer field;   // move to a specific field
    private Integer spaces;  // move by some amount

    @Override
    public void apply(Game game, Player player) {

    }
}
