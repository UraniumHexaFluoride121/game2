package unit.info;

import foundation.NamedEnum;

public enum UnitCharacteristic implements NamedEnum {
    DEFENCE("Defence"), SPEED("Speed"), FIREPOWER("Firepower"), VIEW_RANGE("View Range"), FIRING_RANGE("Firing Range"), SHIELD("Shield");

    private final String name;

    UnitCharacteristic(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
