package model;

import at.aau.serg.monopoly.websoket.CheatService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CheatServiceTest {
    private CheatService cheatService;

    @BeforeEach
    public void setUp(){
        cheatService = new CheatService();
    }

    @Test
    public void testCheatService(){
    }

    @AfterEach
    public void tearDown(){
        cheatService = null;
    }

}
