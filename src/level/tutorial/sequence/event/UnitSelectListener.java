package level.tutorial.sequence.event;

import unit.Unit;
import unit.UnitTeam;
import unit.type.UnitType;

import java.util.function.Predicate;

public class UnitSelectListener implements TutorialEventListener {
    private Predicate<Unit> unitPredicate;

    public static UnitSelectListener ofTeam(UnitTeam team) {
        return new UnitSelectListener(u -> u.data.team == team);
    }

    public static UnitSelectListener ofType(UnitType type) {
        return new UnitSelectListener(u -> u.data.type == type);
    }

    public static UnitSelectListener any() {
        return new UnitSelectListener(_ -> true);
    }

    private UnitSelectListener(Predicate<Unit> unitPredicate) {
        this.unitPredicate = unitPredicate;
    }

    @Override
    public boolean test(TutorialEvent tutorialEvent) {
        if (tutorialEvent instanceof EventTileSelect e) {
            Unit u = e.l.getUnit(e.tile);
            if (u != null)
                return unitPredicate.test(u);
        }
        return false;
    }

    @Override
    public void delete() {
        TutorialEventListener.super.delete();
        unitPredicate = null;
    }
}
