package at.aau.serg.monopoly.websoket;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CheatService {
    private int amount;

    public int getAmount(String cheatCode, int currentMoney){
        String cheatCodeTypeSafe = cheatCode
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "");

        switch (cheatCodeTypeSafe){
            case "fixedExtraMoney": return fixedExtraMoney();
            case "randomMoney": return randomMoney();
            case "coinFlip": return coinflip();
            case "doubleOrHalf": return doubleOrHalf(currentMoney);
            default: return 0;
        }
    }

    // Info: Every method returns a delta and not the new amount!

    private int fixedExtraMoney(){
        return 500;
    }

    private int randomMoney(){
        return 1000;
    }

    private int coinflip(){
        return 1500;
    }

    private int doubleOrHalf(int currentMoney){
        return 2000;
    }
}
