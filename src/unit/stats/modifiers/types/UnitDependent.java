package unit.stats.modifiers.types;

import unit.ShipClass;
import unit.Unit;
import unit.type.UnitType;

import java.util.function.BiPredicate;

@FunctionalInterface
public interface UnitDependent extends BiPredicate<ModifierCategory, Unit> {
    static UnitDependent unitClass(ShipClass shipClass) {
        return (cat, u) -> u.data.type.shipClass == shipClass;
    }

    static UnitDependent unitType(UnitType type) {
        return (cat, u) -> u.data.type == type;
    }
}
