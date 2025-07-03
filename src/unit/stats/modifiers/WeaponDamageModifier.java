package unit.stats.modifiers;

import render.UIColourTheme;
import render.types.text.StyleElement;
import unit.stats.Modifier;
import unit.stats.SingleModifier;

import java.awt.*;

import static unit.stats.ModifierCategory.*;

public abstract class WeaponDamageModifier {
    public static final SingleModifier
            WEAKNESS_1 = new SingleModifier(0.8f, "Weapon Weakness I",
            "The attacking unit's weapon is ineffective against the enemy unit, resulting in reduced " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true) + ". " +
                    "This modifier ranges between level 1 - 3, with each level increasing the " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true) + " penalty.",
            DAMAGE.displayEffectName(),
            DAMAGE.displayEffectValue(0.8f, Modifier::percentMultiplicative),
            DAMAGE, UIColourTheme.DEEP_RED, Modifier.RED_BACKGROUND),

    WEAKNESS_2 = new SingleModifier(0.6f, "Weapon Weakness II",
            "The attacking unit's weapon is ineffective against the enemy unit, resulting in reduced " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true) + ". " +
                    "This modifier ranges between level 1 - 3, with each level increasing the " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true) + " penalty.",
            DAMAGE.displayEffectName(),
            DAMAGE.displayEffectValue(0.6f, Modifier::percentMultiplicative),
            DAMAGE, UIColourTheme.DEEP_RED, Modifier.RED_BACKGROUND),

    WEAKNESS_3 = new SingleModifier(0.4f, "Weapon Weakness III",
            "The attacking unit's weapon is ineffective against the enemy unit, resulting in reduced " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true) + ". " +
                    "This modifier ranges between level 1 - 3, with each level increasing the " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true) + " penalty.",
            DAMAGE.displayEffectName(),
            DAMAGE.displayEffectValue(0.4f, Modifier::percentMultiplicative),
            DAMAGE, UIColourTheme.DEEP_RED, Modifier.RED_BACKGROUND),

    NORMAL_STRENGTH = new SingleModifier(1f, "Normal Weapon Strength",
            "The attacking unit's weapon performs normally against the enemy unit, resulting in no extra " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true) + ".",
            DAMAGE.displayEffectName(),
            DAMAGE.displayEffectValue(1f, Modifier::percentMultiplicative),
            DAMAGE, UIColourTheme.DEEP_LIGHT_BLUE, Modifier.BLUE_BACKGROUND),

    STRENGTH_1 = new SingleModifier(1.2f, "Weapon Strength I",
            "The attacking unit's weapon is effective against the enemy unit, resulting in increased " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true) + ". " +
                    "This modifier ranges between level 1 - 3, with each level increasing the " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true) + " bonus.",
            DAMAGE.displayEffectName(),
            DAMAGE.displayEffectValue(1.2f, Modifier::percentMultiplicative),
            DAMAGE, UIColourTheme.DEEP_GREEN, Modifier.GREEN_BACKGROUND),

    STRENGTH_2 = new SingleModifier(1.4f, "Weapon Strength II",
            "The attacking unit's weapon is effective against the enemy unit, resulting in increased " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true) + ". " +
                    "This modifier ranges between level 1 - 3, with each level increasing the " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true) + " bonus.",
            DAMAGE.displayEffectName(),
            DAMAGE.displayEffectValue(1.4f, Modifier::percentMultiplicative),
            DAMAGE, UIColourTheme.DEEP_GREEN, Modifier.GREEN_BACKGROUND),

    STRENGTH_3 = new SingleModifier(1.6f, "Weapon Strength III",
            "The attacking unit's weapon is effective against the enemy unit, resulting in increased " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true) + ". " +
                    "This modifier ranges between level 1 - 3, with each level increasing the " + DAMAGE.colouredName(StyleElement.NO_COLOUR, true) + " bonus.",
            DAMAGE.displayEffectName(),
            DAMAGE.displayEffectValue(1.6f, Modifier::percentMultiplicative),
            DAMAGE, UIColourTheme.DEEP_GREEN, Modifier.GREEN_BACKGROUND);

    public static Color getDamageModifierColour(Modifier... modifiers) {
        if (modifiers != null)
            for (Modifier m : modifiers) {
                if (m == NORMAL_STRENGTH) return new Color(92, 187, 228);
                if (m == STRENGTH_1 || m == STRENGTH_2 || m == STRENGTH_3) return new Color(130, 200, 77);
                if (m == WEAKNESS_1 || m == WEAKNESS_2 || m == WEAKNESS_3) return new Color(197, 74, 74);
            }
        return new Color(135, 135, 135);
    }
}
