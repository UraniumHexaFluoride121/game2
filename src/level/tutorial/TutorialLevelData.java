package level.tutorial;

import level.GameplaySettings;
import level.PlayerTeam;
import level.TeamData;
import unit.UnitTeam;
import unit.bot.BotDifficulty;

import java.util.HashMap;

public class TutorialLevelData {
    public final HashMap<UnitTeam, TeamData> teams = new HashMap<>();
    public final GameplaySettings settings;
    public float botDifficulty = BotDifficulty.MEDIUM.difficulty;

    public TutorialLevelData(GameplaySettings settings) {
        this.settings = settings;
    }

    public TutorialLevelData addPlayer(PlayerTeam team, boolean bot) {
        UnitTeam unitTeam = UnitTeam.ORDERED_TEAMS[teams.size()];
        teams.put(unitTeam, new TeamData(bot, team));
        return this;
    }
}
