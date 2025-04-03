package model.properites;

import lombok.Getter;
import lombok.Setter;

public abstract class BaseProperty {
    @Setter
    @Getter
    protected int id;
        @Setter
        @Getter
        protected Integer ownerId; // kann null sein
        @Setter
        @Getter
        protected String name;
        @Setter
        @Getter
        protected int purchasePrice;
        @Setter
        @Getter
        protected int mortgageValue;
        protected boolean isMortgaged;

        public BaseProperty() {
        }

        public BaseProperty(int id, Integer ownerId, String name, int purchasePrice, int mortgageValue, boolean isMortgaged) {
            this.id = id;
            this.ownerId = ownerId;
            this.name = name;
            this.purchasePrice = purchasePrice;
            this.mortgageValue = mortgageValue;
            this.isMortgaged = isMortgaged;
        }

    public boolean isMortgaged() { return isMortgaged; }
        public void setMortgaged(boolean mortgaged) { isMortgaged = mortgaged; }
    }
