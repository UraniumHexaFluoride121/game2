package level.tutorial.sequence.event;

import level.Level;
import unit.action.Action;

public class EventActionPerform extends TutorialEvent {
    public final Action action;

    public EventActionPerform(Level l, Action action) {
        super(l);
        this.action = action;
    }
}
