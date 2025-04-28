package level.tutorial.sequence.event;

import level.Level;

public class EventUISelect extends TutorialEvent {
    public final UIElement element;

    public EventUISelect(Level l, UIElement element) {
        super(l);
        this.element = element;
    }
}
