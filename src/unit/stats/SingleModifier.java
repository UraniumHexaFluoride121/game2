package unit.stats;

import render.UIColourTheme;

import java.util.function.BiConsumer;

public class SingleModifier implements Modifier {
    private final float effect;
    private final String name, description, effectDescription, effectDescriptionValue;
    private final ModifierCategory category;
    public final UIColourTheme colour, listColour;

    public SingleModifier(float effect, String name, String description, String effectDescription, String effectDescriptionValue, ModifierCategory category, UIColourTheme colour, UIColourTheme listColour) {
        this.effect = effect;
        this.name = name;
        this.description = description;
        this.effectDescription = effectDescription;
        this.effectDescriptionValue = effectDescriptionValue;
        this.category = category;
        this.colour = Modifier.createBackgroundTheme(colour);
        this.listColour = listColour;
    }

    @Override
    public float effect(ModifierCategory category) {
        return category == this.category ? effect : category.defaultEffect;
    }

    @Override
    public boolean hasCategory(ModifierCategory category) {
        return category == this.category;
    }

    @Override
    public void forEachCategory(BiConsumer<ModifierCategory, Float> action) {
        action.accept(category, effect);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String effectDescription() {
        return effectDescription;
    }

    @Override
    public String effectDescriptionValue() {
        return effectDescriptionValue;
    }

    @Override
    public UIColourTheme colour() {
        return colour;
    }

    @Override
    public UIColourTheme listColour() {
        return listColour;
    }
}
