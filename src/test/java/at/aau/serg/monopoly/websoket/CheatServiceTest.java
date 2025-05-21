package at.aau.serg.monopoly.websoket;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CheatServiceTest {
    private CheatService cheatService;
    private CheatService trueCheatService;
    private CheatService falseCheatService;
    private final int FIXED_MONEY_DELTA = 250;
    private final int RANDOM_MONEY_CEILING = 1000;
    private final int RANDOM_MONEY_STEP = 50;
    private final int COINFLIP_AMOUNT = 500;
    private final int STARTING_MONEY = 1000;

    @BeforeEach
    public void setUp(){
        cheatService = new CheatService();
        trueCheatService = mock(CheatService.class);
        falseCheatService = mock(CheatService.class);
        when(trueCheatService.getAmount("yolo", STARTING_MONEY)).thenReturn(STARTING_MONEY);
        when(falseCheatService.getAmount("yolo", STARTING_MONEY)).thenReturn(-STARTING_MONEY/2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Never gonna give you up?", "Never    gonna let you down", "    GAMBLING ADDICTION", "y o l o"})
    public void testNormalizeInput(String input){
        String norm = cheatService.normalizeInput(input);

        // No spaces
        assertFalse(norm.contains(" "),
                () -> "Normalized should have no spaces, but was: `" + norm + "`");

        // No uppercase letters
        assertEquals(norm, norm.toLowerCase(Locale.ROOT),
                () -> "Normalized should be all‐lowercase, but was: `" + norm + "`");
    }

    @Test
    public void testFixedExtraMoney(){
        assertEquals(FIXED_MONEY_DELTA, cheatService.getAmount("nevergonnagiveyouup", 0));
    }

    @RepeatedTest(50)
    public void testRandomMoney(){
        int delta = cheatService.getAmount("nevergonnaletyoudown", 0);

        // Assert multiple of Step
        assertEquals(0, delta % RANDOM_MONEY_STEP,
                "Should always be a multiple of " + RANDOM_MONEY_STEP);

        // Assert in bounds
        assertTrue(delta >= 0 && delta <= RANDOM_MONEY_CEILING,
                "Should be between 0 and " + RANDOM_MONEY_CEILING);
    }

    @RepeatedTest(50)
    public void testCoinflipAlwaysPlusOrMinus500(){
        int delta = cheatService.getAmount("gamblingaddiction", 0);
        assertTrue(
                delta ==  COINFLIP_AMOUNT ||
                        delta == -COINFLIP_AMOUNT,
                () -> "Expected ±" + COINFLIP_AMOUNT + " but got " + delta
        );
    }

    @Test
    public void testCoinflipProducesBothOutcomes(){
        boolean sawWin = false, sawLoss = false;
        for(int i = 0; i < 100; i++){
            int delta = cheatService.getAmount("gamblingaddiction", 0);
            if (delta ==  COINFLIP_AMOUNT) sawWin  = true;
            if (delta == -COINFLIP_AMOUNT) sawLoss = true;
            if (sawWin && sawLoss) break;
        }
        assertTrue(sawWin,  "coinflip() never returned +"  + COINFLIP_AMOUNT);
        assertTrue(sawLoss, "coinflip() never returned -"  + COINFLIP_AMOUNT);
    }

    @Test
    public void testDoubleOfHalfProducesBothIncomes(){
        boolean sawWin = false, sawLoss = false;
        for (int i = 0; i < 100; i++){
            int delta = cheatService.getAmount("yolo", STARTING_MONEY);
            if (delta ==  STARTING_MONEY) sawWin  = true;
            if (delta == -STARTING_MONEY/2) sawLoss = true;
            if (sawWin && sawLoss) break;
        }
        assertTrue(sawWin,  "doubleOrHalf() never returned +"  + STARTING_MONEY);
        assertTrue(sawLoss, "doubleOrHalf() never returned -"  + -STARTING_MONEY/2);
    }

    @Test
    public void testDoubleOrHalf_DoublePart(){
        boolean win = true;

        int delta = trueCheatService.getAmount("yolo", STARTING_MONEY);
        assertEquals(1000, delta);
    }

    @Test
    public void testDoubleOrHalf_HalfPart(){
        int delta = falseCheatService.getAmount("yolo", STARTING_MONEY);
        assertEquals(-500, delta);
    }

    @AfterEach
    public void tearDown(){
        cheatService = null;
        trueCheatService = null;
        falseCheatService = null;
    }
}
