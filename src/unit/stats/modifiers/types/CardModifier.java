package unit.stats.modifiers.types;

public interface CardModifier extends Modifier {
    String effectCondition(ModifierCategory cat);
}
