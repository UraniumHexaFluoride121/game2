package unit.stats.modifiers.types;

import render.UIColourTheme;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class EmptyModifier implements Modifier {
    private final String name, description, effectDescription, effectDescriptionValue;
    private UIColourTheme colour, listColour;

    public EmptyModifier(String name, String description, String effectDescription, UIColourTheme colour) {
        this.name = name;
        this.description = description;
        this.effectDescription = effectDescription;
        this.colour = Modifier.createBackgroundTheme(colour);
        listColour = Modifier.createBackgroundTheme(colour);
        effectDescriptionValue = null;
    }

    public EmptyModifier setColour(UIColourTheme colour) {
        this.colour = Modifier.createBackgroundTheme(colour);
        return this;
    }

    public EmptyModifier setListColour(UIColourTheme colour) {
        listColour = Modifier.createBackgroundTheme(colour);
        return this;
    }

    @Override
    public float effect(ModifierCategory category) {
        throw new IllegalArgumentException();
    }

    @Override
    public boolean hasCategory(ModifierCategory category) {
        return false;
    }

    @Override
    public void forEachCategory(BiConsumer<ModifierCategory, Float> action) {

    }

    @Override
    public ArrayList<ModifierCategory> categories() {
        return new ArrayList<>();
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
    public String effectDescription(ModifierCategory category) {
        return effectDescription;
    }

    @Override
    public String effectDescriptionValue(ModifierCategory category) {
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
