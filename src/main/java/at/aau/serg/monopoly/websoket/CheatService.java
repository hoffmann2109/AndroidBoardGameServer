package at.aau.serg.monopoly.websoket;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Locale;

@Service
public class CheatService {
    private int amount;
    private final SecureRandom rnd = new SecureRandom();
    private final int FIXED_MONEY_DELTA = 250;
    private final int RANDOM_MONEY_CEILING = 1000;
    private final int RANDOM_MONEY_STEP = 50;
    private final int COINFLIP_AMOUNT = 500;

    public int getAmount(String cheatCode, int currentMoney){
        String cheatCodeTypeSafe = normalizeInput(cheatCode);

        switch (cheatCodeTypeSafe){
            case "nevergonnagiveyouup": return fixedExtraMoney();
            case "nevergonnaletyoudown": return randomMoney();
            case "gamblingaddiction": return coinflip();
            case "yolo": return doubleOrHalf(currentMoney);
            default: return 0;
        }
    }

    // INFO: Every method returns a delta and not the new amount

    // Method should be private but I wanted to test it
    public String normalizeInput(String cheatCode){
        return cheatCode
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "");
    }

    private int fixedExtraMoney(){
        return FIXED_MONEY_DELTA;
    }

    private int randomMoney(){
        int maxValue = RANDOM_MONEY_CEILING;
        double raw = rnd.nextDouble() * maxValue;   // [0.0, maxValue)
        int step = RANDOM_MONEY_STEP;
        return (int) (Math.round(raw / step) * step);
    }

    private int coinflip(){
        boolean won = rnd.nextBoolean();
        return won? COINFLIP_AMOUNT: -COINFLIP_AMOUNT;
    }

    private int doubleOrHalf(int currentMoney) {
        boolean win = rnd.nextBoolean();
        if (win) {
            return currentMoney;
        } else {
            return - (currentMoney / 2);
        }
    }
}
