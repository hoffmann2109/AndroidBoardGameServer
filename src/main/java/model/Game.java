package model;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class Game {
    private List<Player> players;
    private boolean isStarted;
    private int currentPlayerIndex;

    public Game() {
        this.players = new ArrayList<>();
        this.isStarted = false;
        this.currentPlayerIndex = 0;
    }

    public void addPlayer(String id, String name) {
        players.add(new Player(id, name));
    }

    public void updatePlayerMoney(String playerId, int amount) {
        for (Player player : players) {
            if (player.getId().equals(playerId)) {
                if (amount > 0) {
                    player.addMoney(amount);
                } else {
                    player.subtractMoney(Math.abs(amount));
                }
                break;
            }
        }
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public List<PlayerInfo> getPlayerInfo() {
        List<PlayerInfo> info = new ArrayList<>();
        for (Player player : players) {
            info.add(new PlayerInfo(player.getId(), player.getName(), player.getMoney()));
        }
        return info;
    }

    /**
     * Finds a player by their unique ID.
     * @param id The ID of the player to find.
     * @return An Optional containing the Player if found, otherwise empty.
     */
    public Optional<Player> getPlayerById(String id) {
        return players.stream()
                      .filter(player -> player.getId().equals(id))
                      .findFirst();
    }
}