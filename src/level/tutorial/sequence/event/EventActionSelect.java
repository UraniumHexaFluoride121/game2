package level.tutorial.sequence.event;

import level.Level;
import unit.action.Action;

import java.awt.*;

public class EventActionSelect extends TutorialEvent {
    public final Action action;

    public EventActionSelect(Level l, Action action) {
        super(l);
        this.action = action;
    }
}
