package model.properties;

import lombok.Getter;
import lombok.Setter;

public class HouseableProperty extends BaseProperty {
    @Getter
    @Setter
    private int baseRent;
    @Getter
    @Setter
    private int rent1House;
    @Getter
    @Setter
    private int rent2Houses;
    @Getter
    @Setter
    private int rent3Houses;
    @Getter
    @Setter
    private int rent4Houses;
    @Getter
    @Setter
    private int rentHotel;
    @Getter
    @Setter
    private int housePrice;
    @Getter
    @Setter
    private int hotelPrice;

    public HouseableProperty() {
        super();
    }

    public HouseableProperty(int id, Integer ownerId, String name, int purchasePrice,
                             int baseRent, int rent1House, int rent2Houses, int rent3Houses,
                             int rent4Houses, int rentHotel, int housePrice, int hotelPrice,
                             int mortgageValue, boolean isMortgaged) {
        super(id, ownerId, name, purchasePrice, mortgageValue, isMortgaged);
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
