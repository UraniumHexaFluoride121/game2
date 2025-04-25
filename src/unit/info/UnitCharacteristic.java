package unit.info;

import foundation.NamedEnum;

public enum UnitCharacteristic implements NamedEnum {
    DEFENCE("Defence"),
    SPEED("Speed"),
    VIEW_RANGE("View Range"),
    FIREPOWER("Firepower"),
    FIRING_RANGE("Firing Range"),
    SHIELD("Shield"),
    SHIELD_REGEN("Shield Regen"),
    REPAIR("Repair");

    private final String name;

    UnitCharacteristic(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
