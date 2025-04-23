package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerTest {

    @Test
    public void testPositionIsInitializedToZero() {
        Player player = new Player("12345", "TestPlayer");
        assertEquals(0, player.getPosition());
    }
}
