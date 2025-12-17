package unit.stats.modifiers.types;

import unit.ShipClass;
import unit.Unit;
import unit.action.Action;
import unit.type.UnitType;

@FunctionalInterface
public interface ActionDependent {
    boolean test(ModifierCategory cat, Unit unit, Action a);

    static ActionDependent unitClass(ShipClass shipClass, Action action) {
        return ((cat, unit, a) -> action == a && unit.data.type.shipClass == shipClass);
    }

    static ActionDependent unitType(UnitType type, Action action) {
        return ((cat, unit, a) -> action == a && unit.data.type == type);
    }
}
