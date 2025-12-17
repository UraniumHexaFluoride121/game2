package unit.stats.modifiers.types;

import java.util.function.Function;

public class SingleCardModifier extends SingleModifier implements CardModifier {
    private final String effectCondition;

    public SingleCardModifier(float effect, String name, String description, Function<Float, String> effectToString, ModifierCategory category, String effectCondition) {
        super(effect, name, description, effectToString, category);
        this.effectCondition = effectCondition;
    }

    @Override
    public String effectCondition(ModifierCategory cat) {
        if (cat == category)
            return effectCondition;
        throw new IllegalArgumentException();
    }
}
