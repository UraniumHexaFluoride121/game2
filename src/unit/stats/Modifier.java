package unit.stats;

import render.GameRenderer;
import render.HorizontalAlign;
import render.RenderOrder;
import render.UIColourTheme;
import render.types.box.UIDisplayBox;
import render.types.box.UIDisplayBoxRenderElement;
import render.types.text.StyleElement;

import java.awt.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.BiConsumer;

public interface Modifier extends Comparable<Modifier> {
    UIColourTheme RED_BACKGROUND = createBackgroundTheme(UIColourTheme.DEEP_RED);
    UIColourTheme BLUE_BACKGROUND = createBackgroundTheme(UIColourTheme.DEEP_LIGHT_BLUE);
    UIColourTheme SHIELD_BACKGROUND = createBackgroundTheme(StyleElement.MODIFIER_SHIELD_HP);
    UIColourTheme DAMAGE_BACKGROUND = createBackgroundTheme(new UIColourTheme(
            new Color(213, 99, 99), new Color(193, 90, 90)
    ));
    UIColourTheme INCOMING_DAMAGE_BACKGROUND = createBackgroundTheme(StyleElement.MODIFIER_INCOMING_DAMAGE);
    UIColourTheme MOVEMENT_BACKGROUND = createBackgroundTheme(StyleElement.MODIFIER_MOVEMENT_SPEED);
    UIColourTheme RESUPPLY_BACKGROUND = createBackgroundTheme(StyleElement.RESUPPLY);
    UIColourTheme MINING_BACKGROUND = createBackgroundTheme(StyleElement.MODIFIER_MINING);
    UIColourTheme VIEW_RANGE_BACKGROUND = createBackgroundTheme(StyleElement.MODIFIER_VIEW_RANGE);
    UIColourTheme YELLOW_BACKGROUND = createBackgroundTheme(UIColourTheme.DEEP_YELLOW);
    UIColourTheme GREEN_BACKGROUND = createBackgroundTheme(UIColourTheme.DEEP_GREEN);


    UIColourTheme RED_DAMAGE_BOX = new UIColourTheme(
            new Color(213, 99, 99), new Color(60, 25, 25, 239)
    );

    float effect(ModifierCategory category);

    boolean hasCategory(ModifierCategory category);

    void forEachCategory(BiConsumer<ModifierCategory, Float> action);

    String name();

    String description();

    String effectDescription();

    String effectDescriptionValue();

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
        UIDisplayBox effectsBox = new UIDisplayBox(0, 0, 16, -1, box -> box.setColourTheme(UIColourTheme.LIGHT_BLUE_BOX), false);
        effectsBox.addText(0.7f, HorizontalAlign.LEFT, effectDescription());
        effectsBox.addText(0.7f, HorizontalAlign.RIGHT, 1, effectDescriptionValue());
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

    static UIColourTheme createBackgroundTheme(Color colour) {
        return createBackgroundTheme(new UIColourTheme(colour, UIColourTheme.darken(colour, 0.87f)));
    }

    static UIColourTheme createBackgroundTheme(StyleElement styleElement) {
        return createBackgroundTheme(styleElement.colour);
    }

    static UIColourTheme createBackgroundTheme(UIColourTheme theme) {
        return theme.backgroundModifier(c -> UIColourTheme.applyAlpha(c, 0.3f));
    }
}
