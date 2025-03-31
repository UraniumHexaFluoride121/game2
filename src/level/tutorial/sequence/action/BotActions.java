package level.tutorial.sequence.action;

import level.tutorial.TutorialManager;
import unit.bot.BotActionData;

import java.util.List;
import java.util.function.Supplier;

public abstract class BotActions {
    @SafeVarargs
    public static TutorialAction addActions(Supplier<BotActionData>... actions) {
        return new TutorialAction(() -> {
            TutorialManager.forcedBotActions.addAll(List.of(actions));
        });
    }
}
