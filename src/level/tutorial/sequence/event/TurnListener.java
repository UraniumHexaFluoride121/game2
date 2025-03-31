package level.tutorial.sequence.event;

import unit.UnitTeam;

public abstract class TurnListener {
    public static TutorialEventListener start() {
        return e -> e instanceof EventTurnStart;
    }

    public static TutorialEventListener start(UnitTeam team) {
        return e -> e instanceof EventTurnStart a && a.team == team;
    }
}
