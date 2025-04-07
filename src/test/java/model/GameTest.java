package model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}