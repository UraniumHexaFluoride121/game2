package level.tutorial.sequence.event;

import level.Level;
import unit.action.Action;

public class EventAnim extends TutorialEvent {
    public final boolean running;

    public EventAnim(Level l, boolean running) {
        super(l);
        this.running = running;
    }
}
