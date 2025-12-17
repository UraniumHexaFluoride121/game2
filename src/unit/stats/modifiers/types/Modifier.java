package unit.stats.modifiers.types;

import foundation.math.MathUtil;
import render.GameRenderer;
import render.HorizontalAlign;
import render.RenderOrder;
import render.UIColourTheme;
import render.types.box.UIDisplayBox;
import render.types.box.UIDisplayBoxRenderElement;
import render.types.text.StyleElement;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface Modifier extends Comparable<Modifier> {
    UIColourTheme RED = UIColourTheme.DEEP_RED;
    UIColourTheme BLUE = UIColourTheme.DEEP_LIGHT_BLUE;
    UIColourTheme YELLOW = UIColourTheme.DEEP_YELLOW;
    UIColourTheme GREEN = UIColourTheme.DEEP_GREEN;

    float effect(ModifierCategory category);

    boolean hasCategory(ModifierCategory category);

    void forEachCategory(BiConsumer<ModifierCategory, Float> action);

    ArrayList<ModifierCategory> categories();

    String name();

    String description();

    String effectDescription(ModifierCategory category);

    String effectDescriptionValue(ModifierCategory category);

    UIColourTheme colour();

    UIColourTheme listColour();

    default UIDisplayBoxRenderElement renderBox(GameRenderer r, RenderOrder order, float x, float y, float width) {
        UIDisplayBoxRenderElement b = new UIDisplayBoxRenderElement(r, order, x, y, width, -1, box -> {
            box.setColourTheme(colour());
        }, false);
        b.box.addSpace(0.15f, 0);
        b.box.addText(1f, HorizontalAlign.LEFT, name());
        b.box.addSpace(0.5f, 0);
        b.box.addText(0.6f, HorizontalAlign.LEFT, description());
        b.box.addSpace(0.5f, 0);
        b.box.addText(0.8f, HorizontalAlign.CENTER, "Modifier Effects:");
        b.box.addSpace(0.3f, 0);
        UIDisplayBox effectsBox = new UIDisplayBox(0, 0, 16, -1, box -> box.setColourTheme(UIColourTheme.LIGHT_BLUE_BOX_DARK), false);
        AtomicBoolean first = new AtomicBoolean(true);
        forEachCategory((cat, effect) -> {
            if (!first.get()) {
                effectsBox.addSpace(0.2f, 0);
                effectsBox.addSpace(0.2f, 1);
                first.set(false);
            }
            effectsBox.addText(0.7f, HorizontalAlign.LEFT, "\n" + effectDescription(cat));
            if (effectDescriptionValue(cat) != null)
                effectsBox.addText(0.7f, HorizontalAlign.RIGHT, 1, "\n" + effectDescriptionValue(cat));
        });
        b.box.addBox(effectsBox, HorizontalAlign.CENTER, 0, false);
        return b;
    }

    @Override
    default int compareTo(Modifier o) {
        return Comparator.<String>naturalOrder().compare(name(), o.name());
    }

    static float multiplicativeEffect(ModifierCategory category, Collection<Modifier> modifiers) {
        return modifiers.stream().filter(m -> m.hasCategory(category)).map(m -> m.effect(category)).reduce(category.identity, (a, b) -> a * b);
    }

    static float additiveEffect(ModifierCategory category, Collection<Modifier> modifiers) {
        return modifiers.stream().filter(m -> m.hasCategory(category)).map(m -> m.effect(category) - 1).reduce(category.identity, Float::sum);
    }

    static String percentMultiplicative(float effect) {
        return (effect >= 1 ? "+" : "") + Math.round((effect - 1) * 100) + "%";
    }

    static String percentAdditive(float effect) {
        return (effect >= 0 ? "+" : "") + Math.round(effect * 100) + "%";
    }

    static String signedAdditive(float effect) {
        return (effect >= 1 ? "+" : "") + MathUtil.floatToString(effect);
    }

    static Function<Float, String> string(String s) {
        return effect -> s;
    }

    static UIColourTheme createBackgroundTheme(StyleElement styleElement) {
        return createBackgroundTheme(styleElement.colour);
    }

    static UIColourTheme createBackgroundTheme(Color colour) {
        return createBackgroundTheme(new UIColourTheme(colour, UIColourTheme.darken(colour, 0.87f)));
    }

    static UIColourTheme createBackgroundTheme(UIColourTheme theme) {
        return theme.backgroundModifier(c -> UIColourTheme.setAlpha(c, 0.18f));
    }

    static UIColourTheme listColourFromEffectPercentMultiplicative(float effect, boolean positive) {
        if (MathUtil.equal(effect, 1, 0.01f))
            return BLUE;
        return (effect < 1) == positive ? RED : GREEN;
    }
}
