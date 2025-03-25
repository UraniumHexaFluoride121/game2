package unit.bot;

import foundation.NamedEnum;

public enum BotDifficulty implements NamedEnum {
    VERY_EASY("Cadet", 1.1f), EASY("Lieutenant", 0.85f), MEDIUM("Captain", 0.5f), HARD("Admiral", 0);

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
