package singleplayer;

import foundation.WeightedSelector;
import unit.type.CorvetteType;
import unit.type.FighterType;
import unit.type.UnitType;

public enum EnemyStartingLoadout {
    DEFAULT(new WeightedSelector<UnitType>()
            .add(10, FighterType.INTERCEPTOR)
            .add(4, FighterType.BOMBER)
            .add(5, CorvetteType.FRIGATE)
    );

    public final WeightedSelector<UnitType> startWeights;

    EnemyStartingLoadout(WeightedSelector<UnitType> startWeights) {
        this.startWeights = startWeights;
    }

    public UnitLoadout getLoadout(int points) {
        UnitLoadout loadout = new UnitLoadout();
        int fails = 0;
        while (true) {
            UnitType type = startWeights.get();
            if (points >= type.value) {
                loadout.addUnit(type, 1);
                points -= type.value;
            } else
                fails++;
            if (fails > 10)
                return loadout;
        }
    }
}
