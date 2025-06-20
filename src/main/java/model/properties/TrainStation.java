package model.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import model.Player;

@Data
@NoArgsConstructor
public class TrainStation extends BaseProperty {
    private int baseRent;
    private int rent2Stations;
    private int rent3Stations;
    private int rent4Stations;

    public TrainStation(int id, String ownerId, String name, int purchasePrice,
                        int baseRent, int rent2Stations, int rent3Stations, int rent4Stations,
                        int mortgageValue, boolean isMortgaged, String image, int position) {
        super(id, ownerId, name, purchasePrice, mortgageValue, image, position, isMortgaged);
        this.baseRent = baseRent;
        this.rent2Stations = rent2Stations;
        this.rent3Stations = rent3Stations;
        this.rent4Stations = rent4Stations;
    }

    @Override
    public int calculateRent(Player owner, Player renter) {
        // For now, just return base rent
        return baseRent;
    }
}
