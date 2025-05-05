package model.cards;

import model.Game;
import model.Player;

public class MoveCard extends Card {
    private Integer field;   // move to a specific field
    private Integer spaces;  // move by some amount

    @Override
    public void apply(Game game, Player player) {
        String playerId = player.getId();
        int oldPos = player.getPosition();

        if (spaces != null) { // Move by some amount
            game.updatePlayerPosition(spaces, playerId);

        } else if (field != null) { // Move to a specific field
            if (field == 30) { // If you land in Jail -> not allowed to collect
                player.setPosition(30);
            } else {
                if (field < oldPos) {
                    game.updatePlayerMoney(playerId, 200);
                }
                player.setPosition(field);
            }
        }
    }
}
