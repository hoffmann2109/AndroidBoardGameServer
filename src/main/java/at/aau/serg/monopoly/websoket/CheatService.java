package at.aau.serg.monopoly.websoket;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Locale;

@Service
public class CheatService {
    private final SecureRandom rnd = new SecureRandom();
    private final static int FIXEDMONEYDELTA = 250;
    private final static int RANDOMMONEYCEILING = 1000;
    private final static int RANDOMMONEYSTEP = 50;
    private final static int COINFLIPAMOUNT = 500;

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
        return FIXEDMONEYDELTA;
    }

    private int randomMoney(){
        int maxValue = RANDOMMONEYCEILING;
        double raw = rnd.nextDouble() * maxValue;   // [0.0, maxValue)
        int step = RANDOMMONEYSTEP;
        return (int) (Math.round(raw / step) * step);
    }

    private int coinflip(){
        boolean won = rnd.nextBoolean();
        return won? COINFLIPAMOUNT : -COINFLIPAMOUNT;
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
