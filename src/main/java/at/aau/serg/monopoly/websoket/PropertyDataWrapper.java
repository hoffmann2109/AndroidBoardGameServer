package at.aau.serg.monopoly.websoket;

import lombok.Getter;
import lombok.Setter;
import model.properties.HouseableProperty;
import model.properties.TrainStation;
import model.properties.Utility;

import java.util.List;

@Setter
@Getter
public class PropertyDataWrapper {
    private List<HouseableProperty> properties;
    private List<TrainStation> trainStations;
    private List<Utility> utilities;

}