package unit.weapon;

import foundation.NamedEnum;
import unit.ShipClass;

public enum DamageClass implements NamedEnum {
    CRUISER(ShipClass.CRUISER.getPluralName()), CORVETTE(ShipClass.CORVETTE.getPluralName()), FIGHTER(ShipClass.FIGHTER.getPluralName()), SHIELD("Shields");

    private final String displayName;

    DamageClass(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getName() {
        return displayName;
    }
}
