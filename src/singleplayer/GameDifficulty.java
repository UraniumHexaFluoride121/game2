package singleplayer;

import foundation.NamedEnum;
import unit.bot.BotDifficulty;

public enum GameDifficulty implements NamedEnum {
    EASY("Easy", BotDifficulty.EASY, 250),
    MEDIUM("Normal", BotDifficulty.MEDIUM, 300),
    HARD("Hard", BotDifficulty.HARD, 350);

    public final String displayName;
    public final BotDifficulty botDifficulty;
    public final int enemyStartingPoints;

    GameDifficulty(String displayName, BotDifficulty botDifficulty, int enemyStartingPoints) {
        this.displayName = displayName;
        this.botDifficulty = botDifficulty;
        this.enemyStartingPoints = enemyStartingPoints;
    }

    @Override
    public String getName() {
        return displayName;
    }
}
