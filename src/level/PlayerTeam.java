package level;

import foundation.NamedEnum;

public enum PlayerTeam implements NamedEnum {
    A("A"),
    B("B"),
    C("C"),
    D("D"),
    E("E"),
    F("F"),
    G("G"),
    H("H");

    private final String name;

    PlayerTeam(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
