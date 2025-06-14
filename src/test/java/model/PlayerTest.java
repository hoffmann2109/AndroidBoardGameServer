package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void testPositionIsInitializedToZero() {
        Player player = new Player("12345", "TestPlayer");
        assertEquals(0, player.getPosition());
    }

    @Test
    void testJailStateManagement() {
        Player player = new Player("p1", "Test Player");

        assertFalse(player.isInJail());
        assertEquals(2, player.getJailTurns());

        player.sendToJail();
        assertTrue(player.isInJail());
        assertEquals(2, player.getJailTurns());

        player.reduceJailTurns();
        assertEquals(1, player.getJailTurns());
        assertTrue(player.isInJail());

        player.reduceJailTurns();
        assertEquals(0, player.getJailTurns());
        assertFalse(player.isInJail());
    }

    @Test
    void testJailTurnReductionEdgeCases() {
        Player player = new Player("p1", "Test Player");
        player.sendToJail();

        player.reduceJailTurns();
        player.reduceJailTurns();
        player.reduceJailTurns(); // Extra reduction
        assertEquals(0, player.getJailTurns());
        assertFalse(player.isInJail());

        player.reduceJailTurns();
        assertEquals(0, player.getJailTurns());
        assertFalse(player.isInJail());
    }
}
