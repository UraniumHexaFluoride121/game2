package unit;

import foundation.NamedEnum;
import render.UIColourTheme;

public enum UnitTeam implements NamedEnum {
    BLUE("blue", "Blue", 0, UIColourTheme.BLUE, UIColourTheme.BLUE_UNAVAILABLE),
    RED("red", "Red", 1, UIColourTheme.RED, UIColourTheme.RED_UNAVAILABLE),
    GREEN("green", "Green", 2, UIColourTheme.GREEN, UIColourTheme.GREEN_UNAVAILABLE),
    YELLOW("yellow", "Yellow", 3, UIColourTheme.YELLOW, UIColourTheme.YELLOW_UNAVAILABLE);

    public static final UnitTeam[] ORDERED_TEAMS = new UnitTeam[values().length];
    public final String s;
    private final String displayName;
    public final int order;
    public final UIColourTheme uiColour, unavailableColour;

    static {
        for (UnitTeam team : values()) {
            ORDERED_TEAMS[team.order] = team;
        }
    }

    UnitTeam(String s, String displayName, int order, UIColourTheme uiColour, UIColourTheme unavailableColour) {
        this.s = s;
        this.displayName = displayName;
        this.order = order;
        this.uiColour = uiColour;
        this.unavailableColour = unavailableColour;
    }

    @Override
    public String getName() {
        return displayName;
    }
}
