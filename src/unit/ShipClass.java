package unit;

import foundation.NamedEnum;
import unit.weapon.DamageType;

public enum ShipClass implements NamedEnum {
    FIGHTER("Fighter"), CORVETTE("Corvette"), CRUISER("Cruiser"), CAPITAL_SHIP("Capital Ship");

    private final String name;

    ShipClass(String name) {
        this.name = name;
    }

    public DamageType getDamageType() {
        return switch (this) {
            case FIGHTER -> DamageType.FIGHTER;
            case CORVETTE -> DamageType.CORVETTE;
            case CRUISER -> DamageType.CRUISER;
            case CAPITAL_SHIP -> DamageType.CAPITAL_SHIP;
        };
    }

    @Override
    public String getName() {
        return name;
    }
}
