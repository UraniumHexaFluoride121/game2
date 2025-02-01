package unit;

import render.ui.UIColourTheme;

public enum UnitTeam {
    GREEN("green", 0, UIColourTheme.GREEN),
    RED("red", 1, UIColourTheme.RED);

    public static final UnitTeam[] ORDERED_TEAMS = new UnitTeam[values().length];
    public final String s;
    public final int order;
    public final UIColourTheme uiColour;

    static {
        for (UnitTeam team : values()) {
            ORDERED_TEAMS[team.order] = team;
        }
    }

    UnitTeam(String s, int order, UIColourTheme uiColour) {
        this.s = s;
        this.order = order;
        this.uiColour = uiColour;
    }
}
