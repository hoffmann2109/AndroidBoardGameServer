package model;

import java.security.SecureRandom;

public record Dice(int sides) {
    public Dice {
        if (sides < 1){
            throw new IllegalArgumentException("A dice must have at least one side!");
        }
    }
    public int roll(){
        SecureRandom random = new SecureRandom();
        return random.nextInt(sides) + 1;
    }
}
