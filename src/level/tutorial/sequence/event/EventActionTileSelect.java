package level.tutorial.sequence.event;

import level.Level;
import unit.action.Action;

import java.awt.*;

public class EventActionTileSelect extends TutorialEvent {
    public final Action action;
    public final Point pos;

    public EventActionTileSelect(Level l, Action action, Point pos) {
        super(l);
        this.action = action;
        this.pos = pos;
    }
}
