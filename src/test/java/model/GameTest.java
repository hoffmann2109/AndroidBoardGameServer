package model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Test
    void testAddPlayer() {
        game.addPlayer("1", "Player 1");
        game.addPlayer("2", "Player 2");

        assertThat(game.getPlayers()).hasSize(2);
        assertThat(game.getPlayers().get(0).getName()).isEqualTo("Player 1");
        assertThat(game.getPlayers().get(1).getName()).isEqualTo("Player 2");
    }

    @Test
    void testPlayerMoney() {
        game.addPlayer("1", "Player 1");
        Player player = game.getPlayers().get(0);

        assertThat(player.getMoney()).isEqualTo(1500); // Starting money

        player.addMoney(500);
        assertThat(player.getMoney()).isEqualTo(2000);

        player.subtractMoney(300);
        assertThat(player.getMoney()).isEqualTo(1700);
    }

    @Test
    void testPlayerTurnOrder() {
        game.addPlayer("1", "Player 1");
        game.addPlayer("2", "Player 2");
        game.addPlayer("3", "Player 3");

        assertThat(game.getCurrentPlayer().getName()).isEqualTo("Player 1");

        game.nextPlayer();
        assertThat(game.getCurrentPlayer().getName()).isEqualTo("Player 2");

        game.nextPlayer();
        assertThat(game.getCurrentPlayer().getName()).isEqualTo("Player 3");

        game.nextPlayer();
        assertThat(game.getCurrentPlayer().getName()).isEqualTo("Player 1"); // Should wrap around
    }

    @Test
    void testGetPlayerInfo() {
        game.addPlayer("1", "Player 1");
        game.addPlayer("2", "Player 2");

        var playerInfo = game.getPlayerInfo();

        assertThat(playerInfo).hasSize(2);
        assertThat(playerInfo.get(0).getName()).isEqualTo("Player 1");
        assertThat(playerInfo.get(1).getName()).isEqualTo("Player 2");
        assertThat(playerInfo.get(0).getMoney()).isEqualTo(1500);
        assertThat(playerInfo.get(1).getMoney()).isEqualTo(1500);
    }

    @Test
    void testUpdatePlayerMoney() {
        game.addPlayer("1", "Player 1");
        game.addPlayer("2", "Player 2");

        // Test adding money
        game.updatePlayerMoney("1", 500);
        assertThat(game.getPlayers().get(0).getMoney()).isEqualTo(2000); // 1500 + 500

        // Test subtracting money
        game.updatePlayerMoney("1", -300);
        assertThat(game.getPlayers().get(0).getMoney()).isEqualTo(1700); // 2000 - 300

        // Test zero amount (should not change money)
        game.updatePlayerMoney("1", 0);
        assertThat(game.getPlayers().get(0).getMoney()).isEqualTo(1700);

        // Test non-existent player ID (should not throw exception)
        game.updatePlayerMoney("999", 100);
        assertThat(game.getPlayers()).hasSize(2); // Should still have 2 players
    }

    @Test
    void testRemovePlayerWorks(){
        game.addPlayer("1", "Player 1");
        game.addPlayer("2", "Player 2");
        game.removePlayer("1");
        assertThat(game.getPlayers()).hasSize(1);
        assertThat(game.getPlayers().get(0).getName()).isEqualTo("Player 2");
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12})
    void testUpdatePlayerPositionUpdatesCorrectPlayer(int turn) {
        game.addPlayer("1", "Player 1");
        game.addPlayer("2", "Player 2");

        game.updatePlayerPosition(turn, "1");
        assertEquals(turn, game.getPlayerById("1").get().getPosition());
        assertEquals(0, game.getPlayerById("2").get().getPosition());
    }

    @Test
    void testUpdatePlayerPositionReturnsCorrectBoolean(){
        game.addPlayer("1", "Player 1");
        boolean returnValue = game.updatePlayerPosition(25, "1");
        assertFalse(returnValue);
        returnValue = game.updatePlayerPosition(14, "1");
        assertFalse(returnValue);
        returnValue = game.updatePlayerPosition(1, "1");
        assertTrue(returnValue);
    }

    @ParameterizedTest
    @ValueSource(ints = {39, 40, 41, 60, 79, 80, 81, 500})
    void testUpdatePlayerPositionValueCannotBeGreaterThanFourty(int roll){
        game.addPlayer("1", "Player 1");
        game.updatePlayerPosition(roll, "1");
        assertThat(game.getPlayerById("1").get().getPosition()).isLessThan(40);
    }

    @Test
    void testUpdatePlayerPositionHandlesOverflowCorrectly(){
        game.addPlayer("1", "Player 1");
        game.updatePlayerPosition(39, "1");
        assertEquals(39, game.getPlayerById("1").get().getPosition());
        game.updatePlayerPosition(1, "1");
        assertEquals(0, game.getPlayerById("1").get().getPosition());
        game.updatePlayerPosition(50, "1");
        assertEquals(10, game.getPlayerById("1").get().getPosition());
    }
}