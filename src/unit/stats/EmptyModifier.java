package unit.stats;

import render.UIColourTheme;

import java.util.function.BiConsumer;

public class EmptyModifier implements Modifier {
    private final String name, description, effectDescription, effectDescriptionValue;
    private final UIColourTheme colour, listColour;

    public EmptyModifier(String name, String description, String effectDescription, String effectDescriptionValue, UIColourTheme colour, UIColourTheme listColour) {
        this.name = name;
        this.description = description;
        this.effectDescription = effectDescription;
        this.effectDescriptionValue = effectDescriptionValue;
        this.colour = Modifier.createBackgroundTheme(colour);
        this.listColour = listColour;
    }

    @Override
    public float effect(ModifierCategory category) {
        return 0;
    }

    @Override
    public boolean hasCategory(ModifierCategory category) {
        return false;
    }

    @Override
    public void forEachCategory(BiConsumer<ModifierCategory, Float> action) {

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
