package level.tutorial.sequence.event;

public abstract class AnimStateListener {
    public static TutorialEventListener running() {
        return e -> e instanceof EventAnim && ((EventAnim) e).running;
    }

    public static TutorialEventListener ended() {
        return e -> e instanceof EventAnim && !((EventAnim) e).running;
    }
}
