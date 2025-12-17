package singleplayer.card;

import foundation.NamedEnum;

public enum CardType implements NamedEnum {
    LOADOUT_CARD("Loadout Card");

    private final String displayName;

    CardType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getName() {
        return displayName;
    }
}
