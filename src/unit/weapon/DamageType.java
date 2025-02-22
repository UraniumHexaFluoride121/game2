package unit.weapon;

import foundation.NamedEnum;

public enum DamageType implements NamedEnum {
    CAPITAL_SHIP("Capital Ships"), CRUISER("Cruisers"), CORVETTE("Corvettes"), FIGHTER("Fighters"), SHIELD("Shields");

    private final String displayName;

    DamageType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getName() {
        return displayName;
    }
}
