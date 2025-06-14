package data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameHistoryRequest {
    private String userId;
    private int durationMinutes;
    private int endMoney;
    private boolean won;
}

