package singleplayer.card;

import foundation.NamedEnum;

public enum CardType implements NamedEnum {
    LOADOUT_CARD("Loadout Card"),
    UTILITY_CARD("Utility Card"),
    UPGRADE_CARD("Upgrade Card"),
    FLEET_CARD("Fleet Card");

    private final String displayName;

    CardType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getName() {
        return displayName;
    }
}
