package unit;

import render.ui.UIColourTheme;

public enum UnitTeam {
    GREEN("green", 0, UIColourTheme.GREEN, UIColourTheme.GREEN_UNAVAILABLE),
    RED("red", 1, UIColourTheme.RED, UIColourTheme.RED_UNAVAILABLE);

    public static final UnitTeam[] ORDERED_TEAMS = new UnitTeam[values().length];
    public final String s;
    public final int order;
    public final UIColourTheme uiColour, unavailableColour;

    static {
        for (UnitTeam team : values()) {
            ORDERED_TEAMS[team.order] = team;
        }
    }

    UnitTeam(String s, int order, UIColourTheme uiColour, UIColourTheme unavailableColour) {
        this.s = s;
        this.order = order;
        this.uiColour = uiColour;
        this.unavailableColour = unavailableColour;
    }
}
