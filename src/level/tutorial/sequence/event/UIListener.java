package level.tutorial.sequence.event;

public abstract class UIListener {
    public static TutorialEventListener select(UIElement element) {
        return e -> e instanceof EventUISelect && ((EventUISelect) e).element == element;
    }

    public static TutorialEventListener deselect(UIElement element) {
        return e -> e instanceof EventUIDeselect && ((EventUIDeselect) e).element == element;
    }
}
