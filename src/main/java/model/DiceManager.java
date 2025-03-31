package model;

import java.util.ArrayList;
import java.util.List;

public class DiceManager implements DiceManagerInterface {
    private Dice firstDice;
    private Dice secondDice;
    private static List<Integer> rollHistory;

    public DiceManager() {
        this.firstDice = new Dice(6);
        this.secondDice = new Dice(6);
        rollHistory = new ArrayList<>();
    }

    @Override
    public int rollDices() {
        int firstDiceRoll = firstDice.roll();
        int secondDiceRoll = secondDice.roll();
        rollHistory.add(firstDiceRoll);
        rollHistory.add(secondDiceRoll);
        return firstDiceRoll + secondDiceRoll;
    }

    @Override
    public List<Integer> getRollHistory() {
        return rollHistory;
    }

}
