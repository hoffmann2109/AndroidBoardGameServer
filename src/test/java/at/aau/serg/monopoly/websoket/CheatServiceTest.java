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
    private final static int FIXEDMONEYDELTA = 250;
    private final static int RANDOMMONEYCEILING = 1000;
    private final static int RANDOMMONEYSTEP = 50;
    private final static int COINFLIPAMOUNT = 500;
    private final static int STARTINGMONEY = 1000;

    @BeforeEach
    void setUp(){
        cheatService = new CheatService();
        trueCheatService = mock(CheatService.class);
        falseCheatService = mock(CheatService.class);
        when(trueCheatService.getAmount("yolo", STARTINGMONEY)).thenReturn(STARTINGMONEY);
        when(falseCheatService.getAmount("yolo", STARTINGMONEY)).thenReturn(-STARTINGMONEY /2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Never gonna give you up?", "Never    gonna let you down", "    GAMBLING ADDICTION", "y o l o"})
    void testNormalizeInput(String input){
        String norm = cheatService.normalizeInput(input);

        // No spaces
        assertFalse(norm.contains(" "),
                () -> "Normalized should have no spaces, but was: `" + norm + "`");

        // No uppercase letters
        assertEquals(norm, norm.toLowerCase(Locale.ROOT),
                () -> "Normalized should be all‐lowercase, but was: `" + norm + "`");
    }

    @Test
    void testFixedExtraMoney(){
        assertEquals(FIXEDMONEYDELTA, cheatService.getAmount("nevergonnagiveyouup", 0));
    }

    @RepeatedTest(50)
    void testRandomMoney(){
        int delta = cheatService.getAmount("nevergonnaletyoudown", 0);

        // Assert multiple of Step
        assertEquals(0, delta % RANDOMMONEYSTEP,
                "Should always be a multiple of " + RANDOMMONEYSTEP);

        // Assert in bounds
        assertTrue(delta >= 0 && delta <= RANDOMMONEYCEILING,
                "Should be between 0 and " + RANDOMMONEYCEILING);
    }

    @RepeatedTest(50)
    void testCoinflipAlwaysPlusOrMinus500(){
        int delta = cheatService.getAmount("gamblingaddiction", 0);
        assertTrue(
                delta == COINFLIPAMOUNT ||
                        delta == -COINFLIPAMOUNT,
                () -> "Expected ±" + COINFLIPAMOUNT + " but got " + delta
        );
    }

    @Test
    void testCoinflipProducesBothOutcomes(){
        boolean sawWin = false, sawLoss = false;
        for(int i = 0; i < 100; i++){
            int delta = cheatService.getAmount("gamblingaddiction", 0);
            if (delta == COINFLIPAMOUNT) sawWin  = true;
            if (delta == -COINFLIPAMOUNT) sawLoss = true;
            if (sawWin && sawLoss) break;
        }
        assertTrue(sawWin,  "coinflip() never returned +"  + COINFLIPAMOUNT);
        assertTrue(sawLoss, "coinflip() never returned -"  + COINFLIPAMOUNT);
    }

    @Test
    void testDoubleOfHalfProducesBothIncomes(){
        boolean sawWin = false, sawLoss = false;
        for (int i = 0; i < 100; i++){
            int delta = cheatService.getAmount("yolo", STARTINGMONEY);
            if (delta == STARTINGMONEY) sawWin  = true;
            if (delta == -STARTINGMONEY /2) sawLoss = true;
            if (sawWin && sawLoss) break;
        }
        assertTrue(sawWin,  "doubleOrHalf() never returned +"  + STARTINGMONEY);
        assertTrue(sawLoss, "doubleOrHalf() never returned -"  + -STARTINGMONEY /2);
    }

    @Test
    void testDoubleOrHalf_DoublePart(){
        int delta = trueCheatService.getAmount("yolo", STARTINGMONEY);
        assertEquals(1000, delta);
    }

    @Test
    void testDoubleOrHalf_HalfPart(){
        int delta = falseCheatService.getAmount("yolo", STARTINGMONEY);
        assertEquals(-500, delta);
    }

    @AfterEach
    void tearDown(){
        cheatService = null;
        trueCheatService = null;
        falseCheatService = null;
    }
}
