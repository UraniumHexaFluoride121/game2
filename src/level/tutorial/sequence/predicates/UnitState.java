package level.tutorial.sequence.predicates;

import unit.Unit;
import unit.UnitTeam;
import unit.type.UnitType;

import java.util.Objects;
import java.util.function.Predicate;

public abstract class UnitState {
    public static Predicate<Unit> ofTeam(UnitTeam team) {
        return u -> u.data.team == team;
    }

    public static Predicate<Unit> ofType(UnitType type) {
        return u -> u.data.type == type;
    }

    public static Predicate<Unit> exists() {
        return Objects::nonNull;
    }
}
