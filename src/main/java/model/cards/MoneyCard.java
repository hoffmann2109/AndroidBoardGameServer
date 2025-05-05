package model.cards;

import model.Game;
import model.Player;

import java.util.List;

public class MoneyCard extends Card {
    private int amount;
    private boolean othersPay;   // you get money from all other players if true
    private boolean othersGet;   // you gift money to all other players if true

    @Override
    public void apply(Game game, Player player) {
        String me = player.getId();
        List<Player> all = game.getPlayers();

        // Each player pays some amount to me
        if (othersPay) {
            for (Player p : all) {
                if (!p.getId().equals(me)) {
                    game.updatePlayerMoney(p.getId(), -amount);
                    game.updatePlayerMoney(me, amount);
                }
            }

        } else if (othersGet) { // I have to pay some amount to all other players
            for (Player p : all) {
                if (!p.getId().equals(me)) {
                    game.updatePlayerMoney(p.getId(), amount);
                    game.updatePlayerMoney(me, -amount);
                }
            }

        } else if (getAction() == ActionType.GET_MONEY) { // Simple bank payout
            game.updatePlayerMoney(me, amount);

        } else { // Simple bank payment
            game.updatePlayerMoney(me, -amount);
        }
    }
}
