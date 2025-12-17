package singleplayer;

import foundation.NamedEnum;
import singleplayer.card.Card;
import singleplayer.card.CardAttribute;
import singleplayer.card.CardType;
import unit.type.CorvetteType;
import unit.type.FighterType;

public enum StartingLoadout implements NamedEnum {
    DEFAULT("Default",
            new UnitLoadout()
                    .addUnit(FighterType.INTERCEPTOR, 2)
                    .addUnit(FighterType.BOMBER, 1)
                    .addUnit(CorvetteType.FRIGATE, 1),
            new Card(CardType.LOADOUT_CARD, CardAttribute.INCOME_INCREASE_II, CardAttribute.FIGHTER_DAMAGE_INCREASE_II)),
    BOMBER(FighterType.BOMBER.getName() + " Assault",
            new UnitLoadout()
                    .addUnit(FighterType.INTERCEPTOR, 2)
                    .addUnit(FighterType.BOMBER, 3),
            new Card(CardType.LOADOUT_CARD, CardAttribute.BOMBER_AMMO_INCREASE, CardAttribute.BOMBER_MOVE_SPEED_III, CardAttribute.FIGHTER_ACTION_COST_FIRE_I)),
    DEFENCE("Defence Force",
            new UnitLoadout()
                    .addUnit(CorvetteType.SUPPLY, 1)
                    .addUnit(CorvetteType.FRIGATE, 1)
                    .addUnit(FighterType.INTERCEPTOR, 1)
                    .addUnit(CorvetteType.DEFENDER, 1),
            new Card(CardType.LOADOUT_CARD, CardAttribute.FIGHTER_HP_INCREASE_II, CardAttribute.SUPPLY_MOVE_SPEED_II, CardAttribute.FRIGATE_DEFENCE_NETWORK));

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
