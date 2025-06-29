package unit;

import foundation.NamedEnum;
import render.types.text.TextRenderable;
import unit.weapon.DamageType;

public enum ShipClass implements NamedEnum {
    FIGHTER("Fighter", TextRenderable.FIGHTER_ICON),
    CORVETTE("Corvette", TextRenderable.CORVETTE_ICON),
    CRUISER("Cruiser", TextRenderable.CRUISER_ICON),
    CAPITAL_SHIP("Capital Ship", TextRenderable.CAPITAL_SHIP_ICON);

    private final String name;
    public final TextRenderable icon;

    ShipClass(String name, TextRenderable icon) {
        this.name = name;
        this.icon = icon;
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
