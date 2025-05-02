package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameHistory {
    private String id;
    private String userId;
    private int durationMinutes;
    private int endMoney;
    private int levelGained;
    private int playersCount;
    private Date timestamp;
    private boolean won;
}