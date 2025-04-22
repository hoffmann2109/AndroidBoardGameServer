package model.properties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HouseableProperty extends BaseProperty {
    private int baseRent;
    private int rent1House;
    private int rent2Houses;
    private int rent3Houses;
    private int rent4Houses;
    private int rentHotel;
    private int housePrice;
    private int hotelPrice;

    public HouseableProperty(int id, String ownerId, String name, int purchasePrice,
                             int baseRent, int rent1House, int rent2Houses, int rent3Houses,
                             int rent4Houses, int rentHotel, int housePrice, int hotelPrice,
                             int mortgageValue, boolean isMortgaged, String image) {
        super(id, ownerId, name, purchasePrice, mortgageValue, image, isMortgaged);
        this.baseRent = baseRent;
        this.rent1House = rent1House;
        this.rent2Houses = rent2Houses;
        this.rent3Houses = rent3Houses;
        this.rent4Houses = rent4Houses;
        this.rentHotel = rentHotel;
        this.housePrice = housePrice;
        this.hotelPrice = hotelPrice;
    }
}
