package model.cards;

import lombok.Data;
import model.Game;

@Data
public class MoveCard extends Card {
    private Integer field;   // move to a specific field
    private Integer spaces;  // move by some amount

    @Override
    public void apply(Game game, String playerId) {
        int oldPos = game.getPlayerById(playerId).get().getPosition();

        if (spaces != null) { // Move by some amount
            game.updatePlayerPosition(spaces, playerId);

        } else if (field != null) { // Move to a specific field
            if (field == 30) { // If you land in Jail -> not allowed to collect
                game.getPlayerById(playerId).get().setPosition(30);
            } else {
                if (field < oldPos) {
                    game.updatePlayerMoney(playerId, 200);
                }
                game.getPlayerById(playerId).get().setPosition(field);
            }
        }
    }
}
