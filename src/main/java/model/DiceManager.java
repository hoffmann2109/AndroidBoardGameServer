package model;

import java.util.ArrayList;
import java.util.List;

public class DiceManager implements DiceManagerInterface {
    private static List<Dice> dices;
    private static List<Integer> rollHistory;

    public DiceManager() {
        dices = new ArrayList<>();
        rollHistory = new ArrayList<>();
    }

    public void addDicesToGame(List<Dice> diceList){
        for (Dice dice : diceList) {
            dices.add(dice);
        }
    }

    @Override
    public int rollDices() {
        int rollResult = 0;
        for (Dice dice : dices) {
            rollResult += dice.roll();
        }
        rollHistory.add(rollResult);
        return rollResult;
    }

    @Override
    public List<Integer> getRollHistory() {
        return rollHistory;
    }

}
