package level.tutorial.sequence.predicates;

import level.Level;
import unit.Unit;

import java.util.function.Predicate;

public abstract class SelectedUnit {
    public static Predicate<Level> any() {
        return l -> l.selectedUnit != null;
    }

    public static Predicate<Level> is(Predicate<Unit> filter) {
        return l -> l.selectedUnit != null && filter.test(l.selectedUnit);
    }
}
