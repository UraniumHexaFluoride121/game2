package level.tutorial.sequence.event;

import level.Level;
import unit.action.Action;

public class EventActionDeselect extends TutorialEvent {
    public final Action action;

    public EventActionDeselect(Level l, Action action) {
        super(l);
        this.action = action;
    }
}
