package unit;

public enum UnitPose {
    UP("up", true),
    UP_RIGHT("up_right", true),
    UP_LEFT("up_left", true),
    DOWN("down", true),
    DOWN_RIGHT("down_right", true),
    DOWN_LEFT("down_left", true),
    FORWARD("forward", true),
    INFO("info", true),
    FIRING_LEFT("firing_left", false),
    FIRING_RIGHT("firing_right", false);

    public final String s;
    public final boolean load;

    UnitPose(String s, boolean load) {
        this.s = s;
        this.load = load;
    }
}
