package model.properites;
import lombok.Getter;
import lombok.Setter;

public class TrainStation extends BaseProperty {
    @Getter
    @Setter
    private int baseRent;
    @Getter
    @Setter
    private int rent2Stations;
    @Getter
    @Setter
    private int rent3Stations;
    @Getter
    @Setter
    private int rent4Stations;

    public TrainStation() {
        super();
    }

    public TrainStation(int id, Integer ownerId, String name, int purchasePrice,
                        int baseRent, int rent2Stations, int rent3Stations, int rent4Stations,
                        int mortgageValue, boolean isMortgaged) {
        super(id, ownerId, name, purchasePrice, mortgageValue, isMortgaged);
        this.baseRent = baseRent;
        this.rent2Stations = rent2Stations;
        this.rent3Stations = rent3Stations;
        this.rent4Stations = rent4Stations;
    }
}
