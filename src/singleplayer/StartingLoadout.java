package singleplayer;

import foundation.NamedEnum;
import singleplayer.card.Card;
import singleplayer.card.CardAttribute;
import singleplayer.card.CardType;
import unit.ShipClass;
import unit.action.Action;
import unit.stats.attribute.UnitAttribute;
import unit.type.CorvetteType;
import unit.type.FighterType;

public enum StartingLoadout implements NamedEnum {
    DEFAULT("Default",
            new UnitLoadout()
                    .addUnit(FighterType.INTERCEPTOR, 2)
                    .addUnit(FighterType.BOMBER, 1)
                    .addUnit(CorvetteType.FRIGATE, 1),
            new Card(CardType.LOADOUT_CARD,
                    CardAttribute.valueOf(2, "INCOME"),
                    CardAttribute.valueOf(2, ShipClass.FIGHTER, "DAMAGE"))),
    BOMBER(FighterType.BOMBER.getName() + " Assault",
            new UnitLoadout()
                    .addUnit(FighterType.INTERCEPTOR, 2)
                    .addUnit(FighterType.BOMBER, 2),
            new Card(CardType.LOADOUT_CARD,
                    CardAttribute.valueOf(1, FighterType.BOMBER, "AMMO"),
                    CardAttribute.valueOf(1, FighterType.BOMBER, "MOVE_SPEED"),
                    CardAttribute.valueOf(1, ShipClass.FIGHTER, "ACTION_COST", Action.FIRE))),
    DEFENCE("Defence Force",
            new UnitLoadout()
                    .addUnit(CorvetteType.FRIGATE, 1)
                    .addUnit(FighterType.INTERCEPTOR, 1)
                    .addUnit(CorvetteType.DEFENDER, 1),
            new Card(CardType.LOADOUT_CARD,
                    CardAttribute.valueOf(2, ShipClass.FIGHTER, "HP"),
                    CardAttribute.valueOf(2, CorvetteType.SUPPLY, "MOVE_SPEED"),
                    CardAttribute.valueOf(1, CorvetteType.FRIGATE, UnitAttribute.DEFENCE_NETWORK)));

    public final String displayName;
    public final UnitLoadout loadout;
    public final Card card;

    StartingLoadout(String displayName, UnitLoadout loadout, Card card) {
        this.loadout = loadout;
        this.displayName = displayName;
        this.card = card;
    }

    @Override
    public String getName() {
        return displayName;
    }
}
