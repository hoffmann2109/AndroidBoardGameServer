package model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;

class DiceTest {
    private Dice firstDice;
    private Dice secondDice;
    private DiceManagerInterface diceManager;
    private List<Dice> diceList;
    private final int SIDES = 6;

    @BeforeEach
    void setUp() {
        this.firstDice = new Dice(SIDES);
        this.secondDice = new Dice(SIDES);
        this.diceList = List.of(firstDice, secondDice);
        this.diceManager = new DiceManager();
        diceManager.addDicesToGame(diceList);
    }

    @Test
    void testConstructorWithCorrectParameterWorks() {
        Dice testDice = new Dice(SIDES);
        Dice testSecondDice = new Dice(50);
        assertEquals(SIDES, testDice.sides());
        assertEquals(50, testSecondDice.sides());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10, -100})
    void testDiceWithInvalidSidesReturnsException(int invalidSides){
        assertThrows(IllegalArgumentException.class, () -> new Dice(invalidSides));
    }

    @RepeatedTest(200)
    void testRollingReturnsMoreThanTwo(){
        int roll = diceManager.rollDices();
        assertTrue(roll > 1 && roll <= 2 * SIDES);
    }

    @Test
    void testOneSidedDiceCanOnlyReturnOne(){
        Dice oneSidedDice = new Dice(1);
        List<Dice> onSidedDiceList = List.of(oneSidedDice);
        DiceManager onesidedDiceManager = new DiceManager();
        onesidedDiceManager.addDicesToGame(onSidedDiceList);
        for (int i = 0; i < 100; i++){
            int rollResult = onesidedDiceManager.rollDices();
            assertEquals(1, rollResult);
        }
    }

    @Test
    void testGetHistoryReturnsCorrectHistory(){
        int firstResult = diceManager.rollDices();
        int secondResult = diceManager.rollDices();
        assertEquals(firstResult, diceManager.getRollHistory().get(0));
        assertEquals(secondResult, diceManager.getRollHistory().get(1));
    }
    @Test
    void testIsPaschWhenSame() {
        DiceManager fixedManager = new DiceManager();
        fixedManager.addDicesToGame(List.of(
                new Dice(1),
                new Dice(1)
        ));
        fixedManager.rollDices();
        assertTrue(fixedManager.isPasch());
    }

    @AfterEach
    void tearDown() {
        firstDice = null;
        secondDice = null;
        diceManager = null;
        diceList = null;
    }
}
