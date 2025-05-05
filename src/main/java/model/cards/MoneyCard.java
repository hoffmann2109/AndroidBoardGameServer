package model.cards;

import model.Game;
import model.Player;

public class MoneyCard extends Card {
    private int amount;
    private boolean othersPay;   // you get money from all other players if true
    private boolean othersGet;   // you gift money to all other players if true

    @Override
    public void apply(Game game, Player player) {

    }
}
