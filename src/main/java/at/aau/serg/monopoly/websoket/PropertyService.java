package at.aau.serg.monopoly.websoket;

import model.properties.HouseableProperty;
import model.properties.TrainStation;
import model.properties.Utility;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class PropertyService {

    private List<HouseableProperty> houseableProperties;
    private List<TrainStation> trainStations;
    private List<Utility> utilities;

    @PostConstruct
    public void init() throws RuntimeException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("propertyData.json")) {
            if (is == null) {
                throw new RuntimeException("propertyData.json not found in resources folder");
            }
            PropertyDataWrapper wrapper = mapper.readValue(is, PropertyDataWrapper.class);
            this.houseableProperties = wrapper.getProperties();
            this.trainStations = wrapper.getTrainStations();
            this.utilities = wrapper.getUtilities();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize property data", e);
        }
    }

    public List<HouseableProperty> getHouseableProperties() {
        return houseableProperties;
    }

    public List<TrainStation> getTrainStations() {
        return trainStations;
    }

    public List<Utility> getUtilities() {
        return utilities;
    }

    public HouseableProperty getHouseablePropertyById(int id) {
        return houseableProperties.stream()
                .filter(property -> property.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public static class PropertyDataWrapper {
        private List<HouseableProperty> properties;
        private List<TrainStation> trainStations;
        private List<Utility> utilities;

        public List<HouseableProperty> getProperties() {
            return properties;
        }
        public void setProperties(List<HouseableProperty> properties) {
            this.properties = properties;
        }
        public List<TrainStation> getTrainStations() {
            return trainStations;
        }
        public void setTrainStations(List<TrainStation> trainStations) {
            this.trainStations = trainStations;
        }
        public List<Utility> getUtilities() {
            return utilities;
        }
        public void setUtilities(List<Utility> utilities) {
            this.utilities = utilities;
        }
    }
}

