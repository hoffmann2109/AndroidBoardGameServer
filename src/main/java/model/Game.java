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

    public void removePlayer(String id) {
        players.removeIf(player -> player.getId().equals(id));
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
            info.add(new PlayerInfo(player.getId(), player.getName(), player.getMoney(), player.getPosition()));
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

    /**
     * Checks if it's the specified player's turn
     * @param playerId The ID of the player to check
     * @return true if it's the player's turn, false otherwise
     */
    public boolean isPlayerTurn(String playerId) {
        if (players.isEmpty() || currentPlayerIndex >= players.size()) {
            return false;
        }
        return players.get(currentPlayerIndex).getId().equals(playerId);
    }

    /**
     *
     * @param roll The result of roll dice.
     * @param id The ID of the player to find.
     * @return If the player passes the Start field the method returns true, otherwise false.
     */
    public boolean updatePlayerPosition(int roll, String id){
        for (Player player : players) {
            if (player.getId().equals(id)) {
                int oldPos = player.getPosition();
                int newPos = (oldPos + roll) % 40;   // ensures 0–39
                player.setPosition(newPos);

                if (oldPos + roll >= 40) {
                    return true;
                }
            }
        }
        return false;
    }

}