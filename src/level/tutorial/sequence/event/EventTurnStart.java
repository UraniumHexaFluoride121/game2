package level.tutorial.sequence.event;

import level.Level;
import unit.UnitTeam;
import unit.action.Action;

public class EventTurnStart extends TutorialEvent {
    public final UnitTeam team;

    public EventTurnStart(Level l, UnitTeam team) {
        super(l);
        this.team = team;
    }
}
