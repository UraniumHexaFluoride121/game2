package unit.stats.modifiers.types;

import unit.Unit;

import java.util.function.BiPredicate;
import java.util.function.Function;

public class SingleUnitModifier extends SingleCardModifier implements UnitDependent {
    private final BiPredicate<ModifierCategory, Unit> predicate;

    public SingleUnitModifier(float effect, String name, String description, Function<Float, String> effectToString, ModifierCategory category, String effectCondition, BiPredicate<ModifierCategory, Unit> predicate) {
        super(effect, name, description, effectToString, category, effectCondition);
        this.predicate = predicate;
    }

    @Override
    public SingleUnitModifier appendEffectValue(String s) {
        super.appendEffectValue(s);
        return this;
    }

    @Override
    public boolean test(ModifierCategory category, Unit unit) {
        return predicate.test(category, unit);
    }
}
