package unit;

import foundation.NamedEnum;

public enum ShipClass implements NamedEnum {
    FIGHTER("Fighter"), CORVETTE("Corvette"), CRUISER("Cruiser"), CAPITAL_SHIP("Capital Ship");

    private final String name;

    ShipClass(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
