package model.properties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TrainStation extends BaseProperty {
    private int baseRent;
    private int rent2Stations;
    private int rent3Stations;
    private int rent4Stations;

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
