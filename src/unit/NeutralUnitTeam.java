package unit;

import foundation.NamedEnum;
import render.UIColourTheme;

public enum NeutralUnitTeam implements NamedEnum {
    NEUTRAL("neutral", "Neutral", 0, null, UIColourTheme.GRAYED_OUT, UIColourTheme.GRAYED_OUT),
    BLUE("blue", "Blue", 1, UnitTeam.BLUE, UIColourTheme.BLUE, UIColourTheme.BLUE_UNAVAILABLE),
    RED("red", "Red", 2, UnitTeam.RED, UIColourTheme.RED, UIColourTheme.RED_UNAVAILABLE),
    GREEN("green", "Green", 3, UnitTeam.GREEN, UIColourTheme.GREEN, UIColourTheme.GREEN_UNAVAILABLE),
    YELLOW("yellow", "Yellow", 4, UnitTeam.YELLOW, UIColourTheme.YELLOW, UIColourTheme.YELLOW_UNAVAILABLE);

    public static final NeutralUnitTeam[] ORDERED_NEUTRAL_TEAMS = new NeutralUnitTeam[values().length];
    public final String s;
    private final String displayName;
    public final int order;
    public final UnitTeam unitTeam;
    public final UIColourTheme uiColour, unavailableColour;

    static {
        for (NeutralUnitTeam team : values()) {
            ORDERED_NEUTRAL_TEAMS[team.order] = team;
        }
    }

    NeutralUnitTeam(String s, String displayName, int order, UnitTeam unitTeam, UIColourTheme uiColour, UIColourTheme unavailableColour) {
        this.s = s;
        this.displayName = displayName;
        this.order = order;
        this.unitTeam = unitTeam;
        this.uiColour = uiColour;
        this.unavailableColour = unavailableColour;
    }

    public static NeutralUnitTeam[] toNeutralTeamArray(UnitTeam[] teams) {
        NeutralUnitTeam[] neutralTeams = new NeutralUnitTeam[teams.length + 1];
        neutralTeams[0] = NEUTRAL;
        for (int i = 1; i < neutralTeams.length; i++) {
            neutralTeams[i] = fromTeam(teams[i - 1]);
        }
        return neutralTeams;
    }

    public static NeutralUnitTeam fromTeam(UnitTeam team) {
        return switch (team) {
            case null -> NEUTRAL;
            case BLUE -> BLUE;
            case RED -> RED;
            case GREEN -> GREEN;
            case YELLOW -> YELLOW;
        };
    }

    @Override
    public String getName() {
        return displayName;
    }
}
