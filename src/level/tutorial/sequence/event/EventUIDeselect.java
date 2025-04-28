package level.tutorial.sequence.event;

import level.Level;

public class EventUIDeselect extends TutorialEvent {
    public final UIElement element;

    public EventUIDeselect(Level l, UIElement element) {
        super(l);
        this.element = element;
    }
}
