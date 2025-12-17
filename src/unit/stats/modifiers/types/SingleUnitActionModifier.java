package unit.stats.modifiers.types;

import unit.Unit;
import unit.action.Action;

import java.util.function.BiPredicate;
import java.util.function.Function;

public class SingleUnitActionModifier extends SingleCardModifier implements ActionDependent {
    private final ActionDependent predicate;

    public SingleUnitActionModifier(float effect, String name, String description, Function<Float, String> effectToString, ModifierCategory category, String effectCondition, ActionDependent predicate) {
        super(effect, name, description, effectToString, category, effectCondition);
        this.predicate = predicate;
    }

    @Override
    public SingleUnitActionModifier appendEffectValue(String s) {
        super.appendEffectValue(s);
        return this;
    }

    @Override
    public boolean test(ModifierCategory cat, Unit unit, Action a) {
        return predicate.test(cat, unit, a);
    }
}
