package unit.bot;

import foundation.NamedEnum;

public enum BotDifficulty implements NamedEnum {
    EASY("Easy", 0.8f), MEDIUM("Medium", 0.4f), HARD("Hard", 0);

    public final String name;
    public final float difficulty;

    BotDifficulty(String name, float difficulty) {
        this.name = name;
        this.difficulty = difficulty;
    }

    @Override
    public String getName() {
        return name;
    }
}
