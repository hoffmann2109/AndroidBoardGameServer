package model;

public record Dice(int sides) {
    public Dice {
        if (sides < 1){
            throw new IllegalArgumentException("A dice must have at least one side!");
        }
    }
    public int roll(){
        return (int) (Math.random() * sides) + 1;
    }
}
