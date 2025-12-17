package unit.stats.modifiers.types;

import render.UIColourTheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SingleModifier implements Modifier {
    protected final float effect;
    protected String name, description, effectDescription, effectDescriptionValue;
    protected final ModifierCategory category;
    public UIColourTheme colour, listColour;

    public SingleModifier(float effect, String name, String description, Function<Float, String> effectToString, ModifierCategory category) {
        this.effect = effect;
        this.name = name;
        this.description = description;
        this.effectDescription = category.displayEffectName();
        this.effectDescriptionValue = category.displayEffectValue(effect, effectToString);
        this.category = category;
        colour = Modifier.createBackgroundTheme(category.textColour);
        listColour = colour;
    }

    public SingleModifier setColour(UIColourTheme colour) {
        this.colour = Modifier.createBackgroundTheme(colour);
        return this;
    }

    public SingleModifier setListColour(UIColourTheme colour) {
        listColour = Modifier.createBackgroundTheme(colour);
        return this;
    }

    public SingleModifier setListAndMainColour(UIColourTheme colour) {
        return setColour(colour).setListColour(colour);
    }

    public SingleModifier setListAndMainColour(Function<Float, UIColourTheme> colourFromEffect) {
        UIColourTheme colour = colourFromEffect.apply(effect);
        return setColour(colour).setListColour(colour);
    }

    public SingleModifier appendEffectValue(String s) {
        effectDescriptionValue += s;
        return this;
    }

    @Override
    public float effect(ModifierCategory category) {
        if (category == this.category)
            return effect;
        throw new IllegalArgumentException();
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
    public ArrayList<ModifierCategory> categories() {
        return new ArrayList<>(Collections.singleton(category));
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
        return this.category == category ? effectDescription : null;
    }

    @Override
    public String effectDescriptionValue(ModifierCategory category) {
        return this.category == category ? effectDescriptionValue : null;
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
